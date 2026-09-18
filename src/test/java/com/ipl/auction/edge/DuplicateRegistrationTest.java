package com.ipl.auction.edge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.PlayerRequest;
import com.ipl.auction.dto.TeamRequest;
import com.ipl.auction.enums.PlayerRole;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge-Case Tests: Duplicate Registration — Member 5 Deliverable.
 * <p>
 * Tests scenarios where clients attempt to register duplicate entities:
 * - Duplicate team names
 * - Duplicate player names
 * - Duplicate user registration
 * - Case-insensitive duplicate detection
 * - Whitespace-padded duplicate names
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Edge Case — Duplicate Registration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DuplicateRegistrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();
        teamRepository.deleteAll();
    }

    // ═══════════════════════════════════════════
    // DUPLICATE TEAM NAMES
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("❌ Exact duplicate team name — 409 Conflict")
    void createTeam_ExactDuplicate_Returns409() throws Exception {
        TeamRequest request = createTeamRequest("Mumbai Indians", "Mumbai");

        // First — success
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second — conflict
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Mumbai Indians")));
    }

    @Test
    @Order(2)
    @DisplayName("❌ Case-insensitive duplicate team — should detect 'MUMBAI INDIANS' = 'Mumbai Indians'")
    void createTeam_CaseInsensitiveDuplicate_Returns409() throws Exception {
        TeamRequest first = createTeamRequest("Mumbai Indians", "Mumbai");
        TeamRequest second = createTeamRequest("MUMBAI INDIANS", "Mumbai");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("❌ Whitespace-padded duplicate team — '  Mumbai Indians  ' = 'Mumbai Indians'")
    void createTeam_WhitespacePaddedDuplicate_Returns409() throws Exception {
        TeamRequest first = createTeamRequest("Mumbai Indians", "Mumbai");
        TeamRequest second = createTeamRequest("  Mumbai Indians  ", "Mumbai");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("✅ Different team name — should succeed (not a duplicate)")
    void createTeam_DifferentName_Returns201() throws Exception {
        TeamRequest first = createTeamRequest("Mumbai Indians", "Mumbai");
        TeamRequest second = createTeamRequest("Chennai Super Kings", "Chennai");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isCreated());
    }

    // ═══════════════════════════════════════════
    // DUPLICATE PLAYER NAMES
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("❌ Exact duplicate player name — 409 Conflict")
    void createPlayer_ExactDuplicate_Returns409() throws Exception {
        PlayerRequest request = createPlayerRequest("Virat Kohli", PlayerRole.BATSMAN);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    @DisplayName("❌ Case-insensitive duplicate player — 'VIRAT KOHLI' = 'Virat Kohli'")
    void createPlayer_CaseInsensitiveDuplicate_Returns409() throws Exception {
        PlayerRequest first = createPlayerRequest("Virat Kohli", PlayerRole.BATSMAN);
        PlayerRequest second = createPlayerRequest("VIRAT KOHLI", PlayerRole.BATSMAN);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(7)
    @DisplayName("✅ Same name different role — behavior depends on business rule")
    void createPlayer_SameNameDifferentRole_DependsOnRules() throws Exception {
        PlayerRequest first = createPlayerRequest("Test Player", PlayerRole.BATSMAN);
        PlayerRequest second = createPlayerRequest("Test Player", PlayerRole.BOWLER);

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(first)))
                .andExpect(status().isCreated());

        // Should be 409 since player name is unique, regardless of role
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(second)))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════
    // DUPLICATE USER REGISTRATION
    // ═══════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("❌ Duplicate username registration — 409 Conflict")
    void registerUser_DuplicateUsername_Returns409() throws Exception {
        String registerJson = """
                {
                    "username": "admin",
                    "password": "SecureP@ss123",
                    "email": "admin@ipl-auction.dev",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    @DisplayName("❌ Duplicate email registration — 409 Conflict")
    void registerUser_DuplicateEmail_Returns409() throws Exception {
        String first = """
                {
                    "username": "user1",
                    "password": "SecureP@ss123",
                    "email": "same@ipl-auction.dev",
                    "role": "USER"
                }
                """;
        String second = """
                {
                    "username": "user2",
                    "password": "SecureP@ss123",
                    "email": "same@ipl-auction.dev",
                    "role": "USER"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(first))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(second))
                .andExpect(status().isConflict());
    }

    // ═══════════════════════════════════════════
    // RAPID-FIRE DUPLICATE PREVENTION
    // ═══════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("❌ Rapid-fire 5x duplicate team creation — only first succeeds")
    void createTeam_RapidFireDuplicates_OnlyFirstSucceeds() throws Exception {
        TeamRequest request = createTeamRequest("Test Team", "TestCity");
        String json = objectMapper.writeValueAsString(request);

        // First — should succeed
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // Subsequent 4 attempts — should all fail
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/teams")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isConflict());
        }
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private TeamRequest createTeamRequest(String name, String city) {
        TeamRequest req = new TeamRequest();
        req.setName(name);
        req.setCity(city);
        req.setOwner("Test Owner");
        req.setMaxPurse(new BigDecimal("1000000000"));
        return req;
    }

    private PlayerRequest createPlayerRequest(String name, PlayerRole role) {
        PlayerRequest req = new PlayerRequest();
        req.setName(name);
        req.setAge(30);
        req.setRole(role);
        req.setCountry("India");
        req.setBasePrice(new BigDecimal("200000000"));
        return req;
    }
}
