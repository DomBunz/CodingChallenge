package com.insurance.premium.calculation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.insurance.premium.calculation.domain.MileageFactor;

@Repository
public interface MileageFactorRepository extends JpaRepository<MileageFactor, Long> {
    
    /**
     * Find the mileage factor for a given annual mileage. There are currently
     * no checks to ensure that mileage ranges do not overlap, so this might
     * return multiple values.
     * 
     * @param mileage The annual mileage
     * @return The mileage factor that applies to the given mileage
     */
    @Query("SELECT mf FROM MileageFactor mf WHERE mf.minMileage <= :mileage AND (mf.maxMileage IS NULL OR mf.maxMileage >= :mileage) ORDER BY mf.minMileage DESC")
    List<MileageFactor> findAllByMileage(@Param("mileage") int mileage);
    
    /**
     * Find the mileage factor for a given annual mileage
     * 
     * @param mileage The annual mileage
     * @return The mileage factor that applies to the given mileage
     */
    default Optional<MileageFactor> findByMileage(int mileage) {
        List<MileageFactor> factors = findAllByMileage(mileage);
        return factors.isEmpty() ? Optional.empty() : Optional.of(factors.get(0));
    }
    
    /**
     * Find all mileage factors ordered by min mileage
     * 
     * @return List of mileage factors ordered by min mileage
     */
    List<MileageFactor> findAllByOrderByMinMileageAsc();
}
