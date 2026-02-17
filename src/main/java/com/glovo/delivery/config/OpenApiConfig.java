package com.glovo.delivery.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryDispatchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Dispatch Core API")
                        .description("High-load core engine for real-time order dispatch system. "
                                + "Matches orders with the most suitable couriers using a scoring algorithm.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Glovo Dev Team")));
    }
}
