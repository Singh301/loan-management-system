package com.sudhanshu.loanmanagement.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearerAuth";

    private static final String API_VERSION = "1.0.0";

    @Bean
    public OpenAPI loanManagementOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()

                                .title("Loan Management System API")

                                .version(API_VERSION)

                                .description("""
Enterprise Loan Management System REST API

Key Features:
• JWT Authentication & Authorization
• Role-Based Access Control (Admin, Manager, Customer)
• Customer Management
• Loan Application & Approval Workflow
• EMI Schedule Generation
• Loan Repayment Management
• Loan Analytics & Dashboard
• Search, Filtering & Pagination
• Document Upload & Download
• Audit Logging
• Global Exception Handling
• OpenAPI 3 Documentation
""")

                                .contact(
                                        new Contact()
                                                .name("Sudhanshu Kumar Singh")
                                                .email("singh.kr1sudhanshu@gmail.com")
                                                .url("https://github.com/Singh301")
                                )

                                .license(
                                        new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT"))
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME))

                .schemaRequirement(
                        SECURITY_SCHEME,

                        new SecurityScheme()

                                .name("Authorization")

                                .description("Enter JWT Bearer token")

                                .type(SecurityScheme.Type.HTTP)

                                .scheme("bearer")

                                .bearerFormat("JWT"))
                .addServersItem(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server")
                )

                .externalDocs(

                        new ExternalDocumentation()

                                .description("Project Documentation")

                                .url("https://github.com/Singh301/loan-management-system"));
    }
}




