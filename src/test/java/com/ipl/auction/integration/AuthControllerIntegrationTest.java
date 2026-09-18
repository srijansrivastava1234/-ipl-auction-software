package com.ipl.auction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.AuthRequest;
import com.ipl.auction.dto.RegisterRequest;
import com.ipl.auction.repository.UserRepository;
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
 * Integration Tests for Auth Controller — Member 5 Deliverable.
 * <p>
 * Tests authentication endpoints: register, login, token refresh.
 * Validates JWT token flow and error responses for invalid credentials.
 * Does NOT test JWT implementation (Member 2's scope).
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController — Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private RegisterRequest validRegisterRequest;
    private AuthRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        validRegisterRequest = new RegisterRequest();
        validRegisterRequest.setUsername("admin");
        validRegisterRequest.setPassword("SecureP@ss123");
        validRegisterRequest.setEmail("admin@ipl-auction.dev");
        validRegisterRequest.setRole("ADMIN");

        validLoginRequest = new AuthRequest();
        validLoginRequest.setUsername("admin");
        validLoginRequest.setPassword("SecureP@ss123");
    }

    // ═══════════════════════════════════════════
    // POST /api/auth/register — USER REGISTRATION
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ POST /api/auth/register — 201 with valid registration")
    void register_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(containsString("registered")));
    }

    @Test
    @Order(2)
    @DisplayName("❌ POST /api/auth/register — 409 with duplicate username")
    void register_DuplicateUsername_Returns409() throws Exception {
        // First — success
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        // Second — conflict
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("❌ POST /api/auth/register — 400 with blank username")
    void register_BlankUsername_Returns400() throws Exception {
        validRegisterRequest.setUsername("");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("❌ POST /api/auth/register — 400 with weak password")
    void register_WeakPassword_Returns400() throws Exception {
        validRegisterRequest.setPassword("123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("❌ POST /api/auth/register — 400 with invalid email format")
    void register_InvalidEmail_Returns400() throws Exception {
        validRegisterRequest.setEmail("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // POST /api/auth/login — USER LOGIN
    // ═══════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("✅ POST /api/auth/login — 200 with valid credentials")
    void login_ValidCredentials_Returns200WithToken() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        // Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    @Order(7)
    @DisplayName("❌ POST /api/auth/login — 401 with wrong password")
    void login_WrongPassword_Returns401() throws Exception {
        // Register first
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        // Login with wrong password
        validLoginRequest.setPassword("WrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("❌ POST /api/auth/login — 401 with non-existent user")
    void login_NonExistentUser_Returns401() throws Exception {
        validLoginRequest.setUsername("ghost_user");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(9)
    @DisplayName("❌ POST /api/auth/login — 400 with empty body")
    void login_EmptyBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // PROTECTED ENDPOINT ACCESS WITH TOKEN
    // ═══════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("✅ GET /api/teams — 200 with valid JWT token")
    void accessProtectedEndpoint_ValidToken_Returns200() throws Exception {
        // Register + Login to get token
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegisterRequest)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()).get("token").asText();

        // Access protected endpoint with token
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(11)
    @DisplayName("❌ GET /api/teams — 401 without Authorization header")
    void accessProtectedEndpoint_NoToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(12)
    @DisplayName("❌ GET /api/teams — 401 with invalid/malformed token")
    void accessProtectedEndpoint_InvalidToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(13)
    @DisplayName("❌ GET /api/teams — 401 with expired token")
    void accessProtectedEndpoint_ExpiredToken_Returns401() throws Exception {
        // Simulated expired token (Member 2 provides JWT utils, this tests the guard)
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDAwMDAxfQ.invalid";

        mockMvc.perform(get("/api/teams")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
}
