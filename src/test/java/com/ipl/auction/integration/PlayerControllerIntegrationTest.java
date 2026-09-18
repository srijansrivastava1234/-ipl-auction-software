package com.ipl.auction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.PlayerRequest;
import com.ipl.auction.entity.Player;
import com.ipl.auction.enums.PlayerRole;
import com.ipl.auction.enums.PlayerStatus;
import com.ipl.auction.repository.PlayerRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Player Controller — Member 5 Deliverable.
 * <p>
 * Tests full request lifecycle for player CRUD and search/filter endpoints.
 * Uses H2 in-memory database with @Transactional for test isolation.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("PlayerController — Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlayerRepository playerRepository;

    private PlayerRequest validPlayerRequest;

    @BeforeEach
    void setUp() {
        playerRepository.deleteAll();

        validPlayerRequest = new PlayerRequest();
        validPlayerRequest.setName("Virat Kohli");
        validPlayerRequest.setAge(35);
        validPlayerRequest.setRole(PlayerRole.BATSMAN);
        validPlayerRequest.setCountry("India");
        validPlayerRequest.setBasePrice(new BigDecimal("200000000"));
    }

    // ═══════════════════════════════════════════
    // POST /api/players — CREATE PLAYER
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ POST /api/players — 201 Created with valid request")
    void createPlayer_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayerRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Virat Kohli"))
                .andExpect(jsonPath("$.role").value("BATSMAN"))
                .andExpect(jsonPath("$.status").value("UNSOLD"))
                .andExpect(jsonPath("$.basePrice").isNumber());
    }

    @Test
    @Order(2)
    @DisplayName("❌ POST /api/players — 400 with missing required fields")
    void createPlayer_MissingFields_Returns400() throws Exception {
        PlayerRequest incomplete = new PlayerRequest();
        incomplete.setName("Test");
        // Missing role, country, basePrice

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incomplete)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @Order(3)
    @DisplayName("❌ POST /api/players — 409 Conflict with duplicate name")
    void createPlayer_DuplicateName_Returns409() throws Exception {
        // First — success
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayerRequest)))
                .andExpect(status().isCreated());

        // Second — conflict
        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(4)
    @DisplayName("❌ POST /api/players — 400 with negative base price")
    void createPlayer_NegativeBasePrice_Returns400() throws Exception {
        validPlayerRequest.setBasePrice(new BigDecimal("-100"));

        mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayerRequest)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // GET /api/players — LIST & SEARCH
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ GET /api/players — 200 with all players")
    void getAllPlayers_Returns200() throws Exception {
        createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);
        createPlayerInDB("Jasprit Bumrah", PlayerRole.BOWLER);

        mockMvc.perform(get("/api/players"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @Order(6)
    @DisplayName("✅ GET /api/players?role=BATSMAN — 200 filtered by role")
    void getPlayersByRole_Returns200Filtered() throws Exception {
        createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);
        createPlayerInDB("Jasprit Bumrah", PlayerRole.BOWLER);

        mockMvc.perform(get("/api/players")
                        .param("role", "BATSMAN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("BATSMAN"));
    }

    @Test
    @Order(7)
    @DisplayName("✅ GET /api/players?status=UNSOLD — 200 filtered by status")
    void getPlayersByStatus_Returns200Filtered() throws Exception {
        Player sold = createPlayerInDB("MS Dhoni", PlayerRole.WICKETKEEPER);
        sold.setStatus(PlayerStatus.SOLD);
        playerRepository.save(sold);

        createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);

        mockMvc.perform(get("/api/players")
                        .param("status", "UNSOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("UNSOLD"));
    }

    @Test
    @Order(8)
    @DisplayName("✅ GET /api/players?country=India — 200 filtered by country")
    void getPlayersByCountry_Returns200() throws Exception {
        createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);

        mockMvc.perform(get("/api/players")
                        .param("country", "India"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("India"));
    }

    // ═══════════════════════════════════════════
    // GET /api/players/{id} — GET BY ID
    // ═══════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("✅ GET /api/players/{id} — 200 with valid ID")
    void getPlayerById_ValidId_Returns200() throws Exception {
        Player player = createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);

        mockMvc.perform(get("/api/players/{id}", player.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Virat Kohli"));
    }

    @Test
    @Order(10)
    @DisplayName("❌ GET /api/players/{id} — 404 with invalid ID")
    void getPlayerById_InvalidId_Returns404() throws Exception {
        mockMvc.perform(get("/api/players/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // PUT /api/players/{id} — UPDATE
    // ═══════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("✅ PUT /api/players/{id} — 200 with valid update")
    void updatePlayer_ValidRequest_Returns200() throws Exception {
        Player player = createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);
        validPlayerRequest.setAge(36);

        mockMvc.perform(put("/api/players/{id}", player.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPlayerRequest)))
                .andExpect(status().isOk());
    }

    // ═══════════════════════════════════════════
    // DELETE /api/players/{id} — DELETE
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("✅ DELETE /api/players/{id} — 204 with valid unsold player")
    void deletePlayer_UnsoldPlayer_Returns204() throws Exception {
        Player player = createPlayerInDB("Virat Kohli", PlayerRole.BATSMAN);

        mockMvc.perform(delete("/api/players/{id}", player.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(13)
    @DisplayName("❌ DELETE /api/players/{id} — 404 with invalid ID")
    void deletePlayer_InvalidId_Returns404() throws Exception {
        mockMvc.perform(delete("/api/players/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // HELPER METHOD
    // ═══════════════════════════════════════════

    private Player createPlayerInDB(String name, PlayerRole role) {
        Player player = new Player();
        player.setName(name);
        player.setAge(30);
        player.setRole(role);
        player.setCountry("India");
        player.setBasePrice(new BigDecimal("200000000"));
        player.setStatus(PlayerStatus.UNSOLD);
        return playerRepository.save(player);
    }
}
