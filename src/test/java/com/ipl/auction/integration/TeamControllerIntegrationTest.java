package com.ipl.auction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.TeamRequest;
import com.ipl.auction.entity.Team;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Team Controller — Member 5 Deliverable.
 * <p>
 * Full Spring Boot context with MockMvc. Tests the entire HTTP request
 * lifecycle: Controller → Service → Repository → H2 Database.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("TeamController — Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    private TeamRequest validTeamRequest;

    @BeforeEach
    void setUp() {
        teamRepository.deleteAll();

        validTeamRequest = new TeamRequest();
        validTeamRequest.setName("Mumbai Indians");
        validTeamRequest.setCity("Mumbai");
        validTeamRequest.setOwner("Nita Ambani");
        validTeamRequest.setLogoUrl("https://example.com/mi-logo.png");
        validTeamRequest.setMaxPurse(new BigDecimal("1000000000"));
    }

    // ═══════════════════════════════════════════
    // POST /api/teams — CREATE TEAM
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ POST /api/teams — 201 Created with valid request")
    void createTeam_ValidRequest_Returns201() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Mumbai Indians"))
                .andExpect(jsonPath("$.city").value("Mumbai"))
                .andExpect(jsonPath("$.owner").value("Nita Ambani"))
                .andExpect(jsonPath("$.purseRemaining").isNumber());
    }

    @Test
    @Order(2)
    @DisplayName("❌ POST /api/teams — 400 Bad Request with blank name")
    void createTeam_BlankName_Returns400() throws Exception {
        validTeamRequest.setName("");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @Order(3)
    @DisplayName("❌ POST /api/teams — 400 Bad Request with null body")
    void createTeam_NullBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("❌ POST /api/teams — 409 Conflict with duplicate team name")
    void createTeam_DuplicateName_Returns409() throws Exception {
        // First creation — succeeds
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andExpect(status().isCreated());

        // Second creation — conflicts
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Mumbai Indians")));
    }

    // ═══════════════════════════════════════════
    // GET /api/teams — LIST ALL TEAMS
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ GET /api/teams — 200 OK with list of teams")
    void getAllTeams_TeamsExist_Returns200WithList() throws Exception {
        // Seed data
        createTeamInDB("Mumbai Indians", "Mumbai");
        createTeamInDB("Chennai Super Kings", "Chennai");

        mockMvc.perform(get("/api/teams"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name",
                        containsInAnyOrder("Mumbai Indians", "Chennai Super Kings")));
    }

    @Test
    @Order(6)
    @DisplayName("✅ GET /api/teams — 200 OK with empty list when no teams")
    void getAllTeams_NoTeams_Returns200WithEmptyList() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ═══════════════════════════════════════════
    // GET /api/teams/{id} — GET TEAM BY ID
    // ═══════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("✅ GET /api/teams/{id} — 200 OK with valid ID")
    void getTeamById_ValidId_Returns200() throws Exception {
        Team team = createTeamInDB("Mumbai Indians", "Mumbai");

        mockMvc.perform(get("/api/teams/{id}", team.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(team.getId()))
                .andExpect(jsonPath("$.name").value("Mumbai Indians"));
    }

    @Test
    @Order(8)
    @DisplayName("❌ GET /api/teams/{id} — 404 Not Found with invalid ID")
    void getTeamById_InvalidId_Returns404() throws Exception {
        mockMvc.perform(get("/api/teams/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    // ═══════════════════════════════════════════
    // PUT /api/teams/{id} — UPDATE TEAM
    // ═══════════════════════════════════════════

    @Test
    @Order(9)
    @DisplayName("✅ PUT /api/teams/{id} — 200 OK with valid update")
    void updateTeam_ValidRequest_Returns200() throws Exception {
        Team team = createTeamInDB("Mumbai Indians", "Mumbai");
        validTeamRequest.setCity("Navi Mumbai");

        mockMvc.perform(put("/api/teams/{id}", team.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Navi Mumbai"));
    }

    @Test
    @Order(10)
    @DisplayName("❌ PUT /api/teams/{id} — 404 Not Found with invalid ID")
    void updateTeam_InvalidId_Returns404() throws Exception {
        mockMvc.perform(put("/api/teams/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // DELETE /api/teams/{id} — DELETE TEAM
    // ═══════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("✅ DELETE /api/teams/{id} — 204 No Content with valid ID")
    void deleteTeam_ValidId_Returns204() throws Exception {
        Team team = createTeamInDB("Mumbai Indians", "Mumbai");

        mockMvc.perform(delete("/api/teams/{id}", team.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/teams/{id}", team.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12)
    @DisplayName("❌ DELETE /api/teams/{id} — 404 Not Found with invalid ID")
    void deleteTeam_InvalidId_Returns404() throws Exception {
        mockMvc.perform(delete("/api/teams/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // CONTENT TYPE VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("❌ POST /api/teams — 415 Unsupported with XML content type")
    void createTeam_XmlContentType_Returns415() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_XML)
                        .content("<team><name>Test</name></team>"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @Order(14)
    @DisplayName("❌ POST /api/teams — 400 Bad Request with malformed JSON")
    void createTeam_MalformedJson_Returns400() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // RESPONSE STRUCTURE VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("✅ POST /api/teams — Response contains all expected fields")
    void createTeam_ValidRequest_ResponseHasAllFields() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTeamRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").isString())
                .andExpect(jsonPath("$.city").isString())
                .andExpect(jsonPath("$.owner").isString())
                .andExpect(jsonPath("$.purseRemaining").isNumber())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    // ═══════════════════════════════════════════
    // HELPER METHOD
    // ═══════════════════════════════════════════

    private Team createTeamInDB(String name, String city) {
        Team team = new Team();
        team.setName(name);
        team.setCity(city);
        team.setOwner("Test Owner");
        team.setPurseRemaining(new BigDecimal("1000000000"));
        team.setMaxPurse(new BigDecimal("1000000000"));
        return teamRepository.save(team);
    }
}
