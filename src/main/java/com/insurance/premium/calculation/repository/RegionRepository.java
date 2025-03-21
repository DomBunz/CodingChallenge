package com.insurance.premium.calculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.calculation.domain.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    // count() method is inherited from JpaRepository
}
