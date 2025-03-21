package com.insurance.premium.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Main configuration class for the application.
 * Contains beans and configuration settings that are common across modules.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {

    /**
     * Property placeholder configurer to resolve ${...} placeholders
     * within Spring bean definitions.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
