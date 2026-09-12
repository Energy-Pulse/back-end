package com.energypulse.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI energyPulseOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("EnergyPulse API")
                        .description("REST API for the EnergyPulse Machine Learning Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EnergyPulse Team")));
    }
}