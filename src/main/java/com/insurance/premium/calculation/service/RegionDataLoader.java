package com.insurance.premium.calculation.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.repository.RegionFactorRepository;
import com.insurance.premium.calculation.repository.RegionRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Service for loading region data from CSV file.
 */
@Service
public class RegionDataLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(RegionDataLoader.class);
    private static final String CSV_FILE_PATH = "postcodes.csv";
    
    private final RegionRepository regionRepository;
    private final RegionFactorRepository regionFactorRepository;
    private final DataSource dataSource;
    
    @Autowired
    public RegionDataLoader(RegionRepository regionRepository,
                            RegionFactorRepository regionFactorRepository,
                            DataSource dataSource) {
        this.regionRepository = regionRepository;
        this.regionFactorRepository = regionFactorRepository;
        this.dataSource = dataSource;
    }
    
    /**
     * Load region data from CSV file when the application is ready.
     * This method is called automatically by Spring Boot when the application is fully started.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void loadRegionData() {
        // Skip if data is already loaded
        if (regionRepository.count() > 0) {
            logger.info("Region data already loaded, skipping import");
            return;
        }
        
        logger.info("Loading region data from CSV file: {}", CSV_FILE_PATH);
        try {
            List<Region> regions = parseRegionsFromCsv();
            insertRegionsWithJdbc(regions);
            logger.info("Successfully loaded {} regions", regions.size());
        } catch (Exception e) {
            logger.error("Failed to load region data from CSV file", e);
        }
    }
    
    /**
     * Insert regions using JDBC batch inserts for better performance.
     * 
     * @param regions List of Region entities to insert
     * @throws SQLException if a database error occurs
     */
    private void insertRegionsWithJdbc(List<Region> regions) throws SQLException {
        final int batchSize = 500;
        try (Connection conn = dataSource.getConnection()) {
            final String sql = "INSERT INTO " + conn.getSchema() + ".regions "
                             + "(federal_state, country, area, city, postal_code, district, region_factor_id) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            boolean autoCommit = conn.getAutoCommit();
            if (autoCommit) conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {            
                int count = 0;
                for (Region region : regions) {
                    ps.setString(1, region.getFederalState());
                    ps.setString(2, region.getCountry());
                    ps.setString(3, region.getArea());
                    ps.setString(4, region.getCity());
                    ps.setString(5, region.getPostalCode());
                    ps.setString(6, region.getDistrict());
                    ps.setLong(7, region.getRegionFactor().getId());
                    ps.addBatch();
                    
                    if (++count % batchSize == 0) {
                        ps.executeBatch();
                        conn.commit();
                        logger.info("Inserted batch of {} regions ({}/{})", batchSize, count, regions.size());
                    }
                }
                
                // Insert remaining records
                if (count % batchSize != 0) {
                    ps.executeBatch();
                    conn.commit();
                    logger.info("Inserted final batch of {} regions", count % batchSize);
                }
            }            
            if (autoCommit) conn.setAutoCommit(true);
        } catch (Exception e) {
            logger.error("Failed to insert regions", e);
            throw e;
        }
    }
    
    /**
     * Parse regions from CSV file.
     * 
     * @return List of Region entities
     * @throws IOException if an I/O error occurs
     * @throws CsvValidationException if the CSV file is invalid
     */
    private List<Region> parseRegionsFromCsv() throws IOException, CsvValidationException {
        List<Region> regions = new ArrayList<>();
        Resource resource = new ClassPathResource(CSV_FILE_PATH);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {
            
            // Get default region factor
            RegionFactor defaultRegionFactor = regionFactorRepository.findDefaultRegionFactor()
                .orElseThrow(() -> new IOException("Default region factor not found"));
            
            // Fetch all region factors at once and create a map for quick lookup
            List<RegionFactor> allRegionFactors = regionFactorRepository.findAll();
            Map<String, RegionFactor> regionFactorMap = new HashMap<>();
            for (RegionFactor factor : allRegionFactors) {
                if (factor.getFederalState() != null && !factor.getFederalState().isBlank()) {
                    regionFactorMap.put(factor.getFederalState().toLowerCase(), factor);
                }
            }
            
            String[] header = csvReader.readNext();
            if (header == null) {
                throw new IOException("CSV file is empty");
            }
            int idxState = findColumnIndex(header, "REGION1"),
              idxCountry = findColumnIndex(header, "REGION3"),
                 idxArea = findColumnIndex(header, "REGION4"),
                 idxCity = findColumnIndex(header, "ORT"),
             idxPostcode = findColumnIndex(header, "POSTLEITZAHL"),
             idxDistrict = findColumnIndex(header, "AREA1"),
                  maxIdx = Math.max(idxState, Math.max(idxCountry, Math.max(idxArea, Math.max(idxCity, Math.max(idxPostcode, idxDistrict)))));
            
            // Track processed entries to avoid duplicates
            Set<String> processed = new HashSet<>();
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length > maxIdx) {
                    String state = line[idxState].trim().replace("\"", ""),
                         country = line[idxCountry].trim().replace("\"", ""),
                            area = line[idxArea].trim().replace("\"", ""),
                            city = line[idxCity].trim().replace("\"", ""),
                        postcode = line[idxPostcode].trim().replace("\"", ""),
                        district = line[idxDistrict].trim().replace("\"", "");
                    
                    // Skip invalid entries
                    if (state.isBlank() || country.isBlank() || city.isBlank() || postcode.isBlank()) {
                        logger.warn("Skipping because of invalid data: {}", (Object) line);
                        continue;
                    }
                    
                    // Skip duplicates
                    String key = state + "_" + country + "_" + area + "_" + city + "_" + postcode + "_" + district;
                    if (processed.contains(key)) {
                        logger.warn("Skipping duplicate region: {}", (Object) line);
                        continue;
                    }
                    processed.add(key);

                    // Get the region factor from the map instead of querying the database
                    RegionFactor regionFactor = regionFactorMap.get(state.toLowerCase());
                    if (regionFactor == null) {
                        logger.warn("Region factor not found for state: {}", state);
                        regionFactor = defaultRegionFactor;
                    }
                    
                    // Create and add region
                    Region reg = new Region(state, country, area, city, postcode, district, regionFactor);
                    regions.add(reg);
                } else {
                    logger.warn("Skipping invalid line with insufficient columns: {}", (Object) line);
                }
            }
        }
        return regions;
    }
    
    /**
     * Find the index of a column in the header row.
     * 
     * @param header The header row
     * @param columnName The name of the column to find
     * @return The index of the column, or -1 if not found
     */
    private int findColumnIndex(String[] header, String columnName) throws CsvValidationException {
        for (int i = 0; i < header.length; i++) {
            if (columnName.equalsIgnoreCase(header[i].trim().replace("\"", ""))) {
                return i;
            }
        }
        throw new CsvValidationException("Column " + columnName + " not found in header: " + Arrays.toString(header));
    }
}
