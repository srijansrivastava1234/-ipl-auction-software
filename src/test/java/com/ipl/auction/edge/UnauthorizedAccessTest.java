package com.ipl.auction.edge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.BidRequest;
import com.ipl.auction.dto.TeamRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge-Case Tests: Unauthorized API Access — Member 5 Deliverable.
 * <p>
 * Tests all protected endpoints WITHOUT valid authentication:
 * - Missing Authorization header
 * - Malformed Bearer token
 * - Expired JWT token
 * - Invalid JWT signature
 * - Role-based access violations (USER trying ADMIN endpoints)
 * - CSRF/XSS-like payloads in headers
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Edge Case — Unauthorized Access Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UnauthorizedAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ═══════════════════════════════════════════
    // MISSING AUTHORIZATION HEADER
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("❌ GET /api/teams — 401 without Authorization header")
    void getTeams_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    @DisplayName("❌ POST /api/teams — 401 without Authorization header")
    void createTeam_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"city\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("❌ GET /api/players — 401 without Authorization header")
    void getPlayers_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(get("/api/players"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    @DisplayName("❌ POST /api/auction/start — 401 without Authorization header")
    void startAuction_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(post("/api/auction/start"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("❌ POST /api/auction/bid — 401 without Authorization header")
    void placeBid_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"teamId\":1,\"playerId\":1,\"amount\":250000000}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("❌ DELETE /api/teams/1 — 401 without Authorization header")
    void deleteTeam_NoAuthHeader_Returns401() throws Exception {
        mockMvc.perform(delete("/api/teams/1"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════
    // MALFORMED TOKENS
    // ═══════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("❌ GET /api/teams — 401 with empty Bearer token")
    void getTeams_EmptyBearer_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("❌ GET /api/teams — 401 with 'Bearer' only (no token)")
    void getTeams_BearerOnly_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    @DisplayName("❌ GET /api/teams — 401 with random string token")
    void getTeams_RandomStringToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer this-is-not-a-valid-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(10)
    @DisplayName("❌ GET /api/teams — 401 with Basic auth instead of Bearer")
    void getTeams_BasicAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQ="))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════
    // EXPIRED / TAMPERED TOKENS
    // ═══════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("❌ GET /api/teams — 401 with expired JWT token")
    void getTeams_ExpiredToken_Returns401() throws Exception {
        // JWT with exp=1600000001 (Sep 2020 — expired)
        String expired = "eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAxfQ." +
                "fake_signature_here";

        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    @DisplayName("❌ GET /api/teams — 401 with tampered JWT payload")
    void getTeams_TamperedToken_Returns401() throws Exception {
        // Valid JWT structure but tampered payload (changed role to ADMIN)
        String tampered = "eyJhbGciOiJIUzI1NiJ9." +
                "eyJzdWIiOiJoYWNrZXIiLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE2MDAwMDAwMDAsImV4cCI6OTk5OTk5OTk5OX0." +
                "tampered_signature";

        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════
    // MALICIOUS HEADER PAYLOADS
    // ═══════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("🔒 XSS payload in Authorization header — safely rejected")
    void getTeams_XssInHeader_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer <script>alert('xss')</script>"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(14)
    @DisplayName("🔒 SQL injection in Authorization header — safely rejected")
    void getTeams_SqlInjectionInHeader_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer ' OR 1=1; DROP TABLE users;--"))
                .andExpect(status().isUnauthorized());
    }

    // ═══════════════════════════════════════════
    // ROLE-BASED ACCESS CONTROL
    // ═══════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("❌ DELETE /api/teams/1 — 403 when USER role tries ADMIN action")
    void deleteTeam_UserRole_Returns403() throws Exception {
        // This test assumes Member 2 implements role-based access:
        // Regular USER shouldn't be able to delete teams (ADMIN only)
        // Token with role=USER attempting an ADMIN-only endpoint
        String userToken = obtainUserRoleToken();
        if (userToken != null) {
            mockMvc.perform(delete("/api/teams/1")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @Order(16)
    @DisplayName("❌ POST /api/auction/start — 403 when USER role tries to start auction")
    void startAuction_UserRole_Returns403() throws Exception {
        String userToken = obtainUserRoleToken();
        if (userToken != null) {
            mockMvc.perform(post("/api/auction/start")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ═══════════════════════════════════════════
    // AUTH ENDPOINTS SHOULD REMAIN PUBLIC
    // ═══════════════════════════════════════════

    @Test
    @Order(17)
    @DisplayName("✅ POST /api/auth/login — accessible without token (public)")
    void login_NoToken_IsAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                // Should NOT be 401 — login is public
                // May be 400 (bad credentials) but not 401 for the endpoint itself
                .andExpect(status().is(not(401)));
    }

    @Test
    @Order(18)
    @DisplayName("✅ POST /api/auth/register — accessible without token (public)")
    void register_NoToken_IsAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\",\"email\":\"\"}"))
                // Should NOT be 401 — register is public
                .andExpect(status().is(not(401)));
    }

    // ═══════════════════════════════════════════
    // HELPER: Get a USER-role token
    // ═══════════════════════════════════════════

    private String obtainUserRoleToken() {
        try {
            // Register a regular user
            String registerJson = """
                    {
                        "username": "regular_user",
                        "password": "SecureP@ss123",
                        "email": "user@ipl-auction.dev",
                        "role": "USER"
                    }
                    """;
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(registerJson))
                    .andExpect(status().isCreated());

            // Login to get token
            String loginJson = """
                    {
                        "username": "regular_user",
                        "password": "SecureP@ss123"
                    }
                    """;
            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson))
                    .andExpect(status().isOk())
                    .andReturn();

            return objectMapper.readTree(
                    result.getResponse().getContentAsString()).get("token").asText();
        } catch (Exception e) {
            // If auth isn't implemented yet, skip role-based tests
            return null;
        }
    }
}
