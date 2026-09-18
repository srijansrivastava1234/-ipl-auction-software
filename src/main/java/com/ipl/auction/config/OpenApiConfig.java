package com.ipl.auction.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger 3 Configuration for IPL Auction System.
 * <p>
 * Member 5 Deliverable — Provides interactive API documentation
 * accessible at /swagger-ui.html
 * </p>
 *
 * @author Member 5 — QA, Testing & API Documentation Lead
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI iplAuctionOpenAPI() {
        return new OpenAPI()
                // ── API Metadata ──
                .info(new Info()
                        .title("IPL Auction System API")
                        .version("1.0.0")
                        .description("""
                                **IPL Player Auction Management System** — Complete REST API.

                                ### Features
                                - 🏏 **Team Management** — Register, update, and manage IPL franchise teams
                                - 👤 **Player Management** — Add, search, and manage player profiles
                                - 💰 **Live Auction** — Real-time bidding engine with purse management
                                - 🔐 **Authentication** — JWT-based secure access with role-based authorization

                                ### Authentication
                                All protected endpoints require a valid JWT token.
                                Use the `/api/auth/login` endpoint to obtain a token, then click
                                **Authorize** above and enter: `Bearer <your-token>`
                                """)
                        .contact(new Contact()
                                .name("IPL Auction Development Team")
                                .email("team@ipl-auction.dev")
                                .url("https://github.com/ipl-auction-team/ipl-auction-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // ── Server Environments ──
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("🖥️ Local Development Server"),
                        new Server()
                                .url("https://staging-api.ipl-auction.dev")
                                .description("🧪 Staging Server"),
                        new Server()
                                .url("https://api.ipl-auction.dev")
                                .description("🚀 Production Server")))

                // ── JWT Security Scheme ──
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token obtained from `/api/auth/login`")))

                // ── API Tags (Grouping) ──
                .tags(List.of(
                        new Tag().name("Authentication")
                                .description("🔐 User registration, login, and JWT token management"),
                        new Tag().name("Teams")
                                .description("🏏 IPL franchise team CRUD operations and purse management"),
                        new Tag().name("Players")
                                .description("👤 Player registration, search, and profile management"),
                        new Tag().name("Auction")
                                .description("💰 Live auction session management and bidding operations"),
                        new Tag().name("Analytics")
                                .description("📊 Auction statistics, reports, and analytics")));
    }
}
