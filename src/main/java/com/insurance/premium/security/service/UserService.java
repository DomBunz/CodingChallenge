package com.insurance.premium.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.security.domain.Role;
import com.insurance.premium.security.domain.User;
import com.insurance.premium.security.dto.UserDto;
import com.insurance.premium.security.repository.RoleRepository;
import com.insurance.premium.security.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for User management operations.
 */
@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, 
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Find a user by ID
     * 
     * @param id The user ID
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Find a user by username
     * 
     * @param username The username
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    /**
     * Create a new user
     * 
     * @param userDto The user data
     * @param roleName The role to assign
     * @return The created user
     */
    @Transactional
    public User createUser(UserDto userDto, String roleName) {
        logger.debug("Creating new user: {}", userDto.username());
        
        if (userRepository.existsByUsername(userDto.username())) {
            throw new IllegalArgumentException("Username already exists: " + userDto.username());
        }
        
        if (userRepository.existsByEmail(userDto.email())) {
            throw new IllegalArgumentException("Email already exists: " + userDto.email());
        }
        
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        user.setEmail(userDto.email());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Assign role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        user.addRole(role);
        
        return userRepository.save(user);
    }
    
    /**
     * Update a user
     * 
     * @param id The user ID
     * @param userDto The updated user data
     * @return The updated user
     */
    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        logger.debug("Updating user: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
        
        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(userDto.username()) && 
                userRepository.existsByUsername(userDto.username())) {
            throw new IllegalArgumentException("Username already exists: " + userDto.username());
        }
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(userDto.email()) && 
                userRepository.existsByEmail(userDto.email())) {
            throw new IllegalArgumentException("Email already exists: " + userDto.email());
        }
        
        user.setUsername(userDto.username());
        user.setEmail(userDto.email());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Update password if provided
        if (userDto.password() != null && !userDto.password().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.password()));
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Delete a user
     * 
     * @param id The user ID
     */
    @Transactional
    public void deleteUser(Long id) {
        logger.debug("Deleting user: {}", id);
        
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        
        userRepository.deleteById(id);
    }
    
    /**
     * Convert User entity to UserDto
     * 
     * @param user The user entity
     * @return UserDto
     */
    public UserDto toDto(User user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            null, // Don't include password in DTO
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.isEnabled(),
            user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet())
        );
    }
}
