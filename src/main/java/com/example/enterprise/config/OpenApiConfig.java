package com.example.enterprise.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Enterprise Portfolio API")
                        .version("1.0.0")
                        .description("REST API for product management with Clean Architecture and Java 21")
                        .contact(new Contact()
                                .name("Developer")
                                .email("dev@example.com")
                                .url("https://github.com/yourusername"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Development"),
                        new Server().url("https://staging-api.example.com").description("Staging"),
                        new Server().url("https://api.example.com").description("Production")
                ));
    }
}
