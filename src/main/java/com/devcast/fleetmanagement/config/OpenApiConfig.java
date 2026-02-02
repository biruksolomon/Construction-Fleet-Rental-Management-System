package com.devcast.fleetmanagement.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {


    @Value("${Application.version}")
    private String appVersion;

    @Value("${Application.name}")
    private String appName;

    @Value("${Application.Description}")
    private String appDescription;

    @Value("${Application.base-url}")
    private String baseUrl;

  /*  @Value("${app.ngrok-url:}")
    private String ngrokUrl;*/


    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();


        servers.add(new Server()
                .url(baseUrl )
                .description("Local Development Server"));

        servers.add(new Server()
                .url("https://api.skegmarket.com")
                .description("Production Server"));

        servers.add(new Server()
                .url("https://staging-api.skegmarket.com")
                .description("Staging Server"));

        return new OpenAPI()
                .info(new Info()
                        .title(appName + " API" + " Developed By Biruk Solomon")
                        .version(appVersion)
                        .description(appDescription)
                        .termsOfService("https://ethiofleetmanagment.com/terms")
                        .contact(new Contact()
                                .name(appName + " Development Team")
                                .email("dev@fleet_management.com")
                                .url("https://ethiofleetmanagment.com/contact"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createBearerAuthScheme())
                        .addSecuritySchemes("API Key", createApiKeyScheme()));
    }

    private SecurityScheme createBearerAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .name("Authorization")
                .description("Enter JWT Bearer token in the format: Bearer {token}");
    }

    private SecurityScheme createApiKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("API Key for service-to-service authentication");
    }
}
