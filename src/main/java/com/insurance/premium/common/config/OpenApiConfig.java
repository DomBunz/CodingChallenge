package com.insurance.premium.common.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI insurancePremiumOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Insurance Premium Calculator API")
                        .description("API for calculating insurance premiums based on vehicle type, region, and annual mileage")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Dominik Bunz")
                                .email("dombunz@gmail.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                ));
    }
    
    @Bean
    public GroupedOpenApi premiumApi() {
        return GroupedOpenApi.builder()
                .group("premium-calculation")
                .pathsToMatch("/api/premium/**")
                .build();
    }
    
    @Bean
    public GroupedOpenApi applicationApi() {
        return GroupedOpenApi.builder()
                .group("application-management")
                .pathsToMatch("/api/applications/**")
                .build();
    }

    @Bean
    public GroupedOpenApi factorApi() {
        return GroupedOpenApi.builder()
                .group("factor-administration")
                .pathsToMatch("/api/admin/premium/management/**")
                .build();
    }

    @Bean
    public GroupedOpenApi configurationApi() {
        return GroupedOpenApi.builder()
                .group("configuration-administration")
                .pathsToMatch("/api/admin/configurations/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("user-management")
                .pathsToMatch("/api/admin/users/**")
                .build();
    }
}
