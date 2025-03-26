package com.insurance.premium.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    
    private final UserDetailsService userDetailsService;
    private final SecurityAuditLogFilter securityAuditLogFilter;
    
    public SecurityConfig(UserDetailsService userDetailsService, SecurityAuditLogFilter securityAuditLogFilter) {
        this.userDetailsService = userDetailsService;
        this.securityAuditLogFilter = securityAuditLogFilter;
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/public/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // API endpoints require authentication
                .requestMatchers("/api/applications/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT, ROLE_API_CLIENT)
                .requestMatchers("/api/calculations/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT, ROLE_API_CLIENT)
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasAuthority(ROLE_ADMIN)
                // Agent endpoints
                .requestMatchers("/api/agent/**").hasAnyAuthority(ROLE_ADMIN, ROLE_AGENT)
                // Customer endpoints
                .requestMatchers("/api/customer/**").hasAnyAuthority(ROLE_ADMIN, ROLE_CUSTOMER)
                // Default security for any other endpoint
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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