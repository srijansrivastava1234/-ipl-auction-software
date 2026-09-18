package com.ipl.auction.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ipl.auction.dto.BidRequest;
import com.ipl.auction.entity.AuctionSession;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.enums.AuctionStatus;
import com.ipl.auction.enums.PlayerRole;
import com.ipl.auction.enums.PlayerStatus;
import com.ipl.auction.repository.AuctionSessionRepository;
import com.ipl.auction.repository.BidRepository;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Auction Controller — Member 5 Deliverable.
 * <p>
 * Tests the full auction lifecycle: start session, place bids, stop session.
 * Validates bid constraints, state transitions, and error responses.
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuctionController — Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuctionSessionRepository auctionSessionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private BidRepository bidRepository;

    private Team sampleTeam;
    private Player samplePlayer;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        auctionSessionRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        // Seed a team
        sampleTeam = new Team();
        sampleTeam.setName("Mumbai Indians");
        sampleTeam.setCity("Mumbai");
        sampleTeam.setOwner("Nita Ambani");
        sampleTeam.setPurseRemaining(new BigDecimal("1000000000"));
        sampleTeam.setMaxPurse(new BigDecimal("1000000000"));
        sampleTeam = teamRepository.save(sampleTeam);

        // Seed a player
        samplePlayer = new Player();
        samplePlayer.setName("Virat Kohli");
        samplePlayer.setAge(35);
        samplePlayer.setRole(PlayerRole.BATSMAN);
        samplePlayer.setCountry("India");
        samplePlayer.setBasePrice(new BigDecimal("200000000"));
        samplePlayer.setStatus(PlayerStatus.UNSOLD);
        samplePlayer = playerRepository.save(samplePlayer);
    }

    // ═══════════════════════════════════════════
    // POST /api/auction/start — START AUCTION
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ POST /api/auction/start — 200 starts auction session")
    void startAuction_NoActiveSession_Returns200() throws Exception {
        mockMvc.perform(post("/api/auction/start"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.startTime").exists());
    }

    @Test
    @Order(2)
    @DisplayName("❌ POST /api/auction/start — 409 when auction already active")
    void startAuction_AlreadyActive_Returns409() throws Exception {
        createActiveSession();

        mockMvc.perform(post("/api/auction/start"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already active")));
    }

    // ═══════════════════════════════════════════
    // POST /api/auction/stop — STOP AUCTION
    // ═══════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("✅ POST /api/auction/stop — 200 stops active auction")
    void stopAuction_ActiveSession_Returns200() throws Exception {
        createActiveSession();

        mockMvc.perform(post("/api/auction/stop"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @Order(4)
    @DisplayName("❌ POST /api/auction/stop — 400 when no active auction")
    void stopAuction_NoActiveSession_Returns400() throws Exception {
        mockMvc.perform(post("/api/auction/stop"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // POST /api/auction/bid — PLACE BID
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ POST /api/auction/bid — 200 with valid bid")
    void placeBid_ValidBid_Returns200() throws Exception {
        createActiveSession();
        BidRequest bidRequest = createBidRequest(
                sampleTeam.getId(), samplePlayer.getId(), "250000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(sampleTeam.getId()))
                .andExpect(jsonPath("$.playerId").value(samplePlayer.getId()))
                .andExpect(jsonPath("$.amount").isNumber());
    }

    @Test
    @Order(6)
    @DisplayName("❌ POST /api/auction/bid — 400 bid below base price")
    void placeBid_BelowBasePrice_Returns400() throws Exception {
        createActiveSession();
        BidRequest bidRequest = createBidRequest(
                sampleTeam.getId(), samplePlayer.getId(), "100000000"); // ₹10 Cr < ₹20 Cr base

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("base price")));
    }

    @Test
    @Order(7)
    @DisplayName("❌ POST /api/auction/bid — 400 bid exceeds team purse")
    void placeBid_ExceedsPurse_Returns400() throws Exception {
        createActiveSession();
        BidRequest bidRequest = createBidRequest(
                sampleTeam.getId(), samplePlayer.getId(), "2000000000"); // ₹200 Cr > ₹100 Cr purse

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("purse")));
    }

    @Test
    @Order(8)
    @DisplayName("❌ POST /api/auction/bid — 400 when auction not active")
    void placeBid_NoActiveAuction_Returns400() throws Exception {
        BidRequest bidRequest = createBidRequest(
                sampleTeam.getId(), samplePlayer.getId(), "250000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("❌ POST /api/auction/bid — 404 with non-existent team")
    void placeBid_InvalidTeamId_Returns404() throws Exception {
        createActiveSession();
        BidRequest bidRequest = createBidRequest(
                99999L, samplePlayer.getId(), "250000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    @DisplayName("❌ POST /api/auction/bid — 404 with non-existent player")
    void placeBid_InvalidPlayerId_Returns404() throws Exception {
        createActiveSession();
        BidRequest bidRequest = createBidRequest(
                sampleTeam.getId(), 99999L, "250000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bidRequest)))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // GET /api/auction/status — AUCTION STATUS
    // ═══════════════════════════════════════════

    @Test
    @Order(11)
    @DisplayName("✅ GET /api/auction/status — 200 returns active session info")
    void getAuctionStatus_ActiveSession_Returns200() throws Exception {
        createActiveSession();

        mockMvc.perform(get("/api/auction/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    // ═══════════════════════════════════════════
    // GET /api/auction/bids/{playerId} — BID HISTORY
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("✅ GET /api/auction/bids/{playerId} — 200 returns bid history")
    void getBidHistory_ValidPlayer_Returns200() throws Exception {
        mockMvc.perform(get("/api/auction/bids/{playerId}", samplePlayer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ═══════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════

    private AuctionSession createActiveSession() {
        AuctionSession session = new AuctionSession();
        session.setStatus(AuctionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        session.setRound(1);
        return auctionSessionRepository.save(session);
    }

    private BidRequest createBidRequest(Long teamId, Long playerId, String amount) {
        BidRequest request = new BidRequest();
        request.setTeamId(teamId);
        request.setPlayerId(playerId);
        request.setAmount(new BigDecimal(amount));
        return request;
    }
}
