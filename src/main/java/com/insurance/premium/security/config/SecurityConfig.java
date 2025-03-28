package com.insurance.premium.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.insurance.premium.security.filter.SecurityAuditLogFilter;

/**
 * Spring Security configuration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final SecurityAuditLogFilter securityAuditLogFilter;
    
    public SecurityConfig(SecurityAuditLogFilter securityAuditLogFilter) {
        this.securityAuditLogFilter = securityAuditLogFilter;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        final String ROLE_ADMIN = "ROLE_ADMIN";
        final String ROLE_AGENT = "ROLE_AGENT";
        final String ROLE_API_CLIENT = "ROLE_API_CLIENT";
        final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
        
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/submit-application", "/calculate-premium")
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/public/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error").permitAll()
                
                // Common web endpoints
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/application-form", "/calculate-premium", "/submit-application").authenticated()
                .requestMatchers("/user/change-password").authenticated()
                
                // Customer only endpoints
                .requestMatchers("/my-applications").hasAuthority(ROLE_CUSTOMER)
                
                // Admin & Agent only endpoints
                .requestMatchers("/applications", "/applications/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT)
                
                // Admin only endpoints
                .requestMatchers("/admin/**", "/users/**").hasAuthority(ROLE_ADMIN)
                .requestMatchers("/api/admin/**").hasAuthority(ROLE_ADMIN)
                
                // API endpoints with role-based access
                .requestMatchers("/api/applications/*/status/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT)
                .requestMatchers("/api/applications").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT, ROLE_API_CLIENT)
                .requestMatchers("/api/applications/my").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT, ROLE_CUSTOMER)
                
                // Premium calculation endpoints
                .requestMatchers("/api/premium/**").authenticated()
                
                // Configuration endpoints - admin only
                .requestMatchers("/api/config/**").hasAuthority(ROLE_ADMIN)
                
                // Agent endpoints
                .requestMatchers("/api/agent/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT)
                
                // Customer endpoints
                .requestMatchers("/api/customer/**").hasAnyAuthority(ROLE_ADMIN, ROLE_CUSTOMER)
                
                // Default security for any other endpoint
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .httpBasic(httpBasic -> {})
            .headers(headers -> headers
                .frameOptions(FrameOptionsConfig::sameOrigin)
            )
            .addFilterAfter(securityAuditLogFilter, BasicAuthenticationFilter.class);
        
        return http.build();
    }
}