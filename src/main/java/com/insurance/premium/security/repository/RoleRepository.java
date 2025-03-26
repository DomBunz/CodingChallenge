package com.insurance.premium.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.security.domain.Role;

import java.util.Optional;

/**
 * Repository for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find a role by name
     * 
     * @param name The role name to search for
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);
}
