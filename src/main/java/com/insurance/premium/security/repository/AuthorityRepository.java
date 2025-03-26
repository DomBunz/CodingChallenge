package com.insurance.premium.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.security.domain.Authority;

import java.util.Optional;

/**
 * Repository for Authority entity operations.
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    
    /**
     * Find an authority by name
     * 
     * @param name The authority name to search for
     * @return Optional containing the authority if found
     */
    Optional<Authority> findByName(String name);
}
