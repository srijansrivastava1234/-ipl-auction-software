package com.ipl.auction.edge;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge-Case Tests: Illegal Bid Scenarios — Member 5 Deliverable.
 * <p>
 * Tests various illegal bidding situations:
 * - Bidding on already-sold players
 * - Bidding below base price
 * - Bidding when auction is not active
 * - Bidding with invalid team/player IDs
 * - Bidding with malformed request bodies
 * - SQL injection attempts via bid fields
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Edge Case — Illegal Bid Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IllegalBidTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AuctionSessionRepository auctionSessionRepository;

    @Autowired
    private BidRepository bidRepository;

    private Team team;
    private Player unsoldPlayer;
    private Player soldPlayer;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        auctionSessionRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        // Team
        team = new Team();
        team.setName("Mumbai Indians");
        team.setCity("Mumbai");
        team.setOwner("Nita Ambani");
        team.setPurseRemaining(new BigDecimal("1000000000"));
        team.setMaxPurse(new BigDecimal("1000000000"));
        team = teamRepository.save(team);

        // Unsold player
        unsoldPlayer = new Player();
        unsoldPlayer.setName("Virat Kohli");
        unsoldPlayer.setAge(35);
        unsoldPlayer.setRole(PlayerRole.BATSMAN);
        unsoldPlayer.setCountry("India");
        unsoldPlayer.setBasePrice(new BigDecimal("200000000"));
        unsoldPlayer.setStatus(PlayerStatus.UNSOLD);
        unsoldPlayer = playerRepository.save(unsoldPlayer);

        // Already sold player
        soldPlayer = new Player();
        soldPlayer.setName("MS Dhoni");
        soldPlayer.setAge(42);
        soldPlayer.setRole(PlayerRole.WICKETKEEPER);
        soldPlayer.setCountry("India");
        soldPlayer.setBasePrice(new BigDecimal("150000000"));
        soldPlayer.setStatus(PlayerStatus.SOLD);
        soldPlayer.setSoldPrice(new BigDecimal("160000000"));
        soldPlayer = playerRepository.save(soldPlayer);

        // Active auction session
        AuctionSession session = new AuctionSession();
        session.setStatus(AuctionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        session.setRound(1);
        auctionSessionRepository.save(session);
    }

    // ═══════════════════════════════════════════
    // BID ON SOLD PLAYER
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("❌ Bid on already SOLD player — 409 Conflict")
    void bid_OnSoldPlayer_Returns409() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(soldPlayer.getId());
        bid.setAmount(new BigDecimal("200000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("sold")));
    }

    // ═══════════════════════════════════════════
    // BID BELOW BASE PRICE
    // ═══════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("❌ Bid at ₹1 below base price — 400 Bad Request")
    void bid_OneRupeeBelowBase_Returns400() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("199999999")); // ₹20 Cr - ₹1

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("base price")));
    }

    @Test
    @Order(3)
    @DisplayName("❌ Bid at ₹1 (far below base) — 400 Bad Request")
    void bid_OneRupee_Returns400() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("1"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // BID WHEN AUCTION NOT ACTIVE
    // ═══════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("❌ Bid when auction is COMPLETED — 400 Bad Request")
    void bid_AuctionCompleted_Returns400() throws Exception {
        // Stop the auction
        auctionSessionRepository.deleteAll();
        AuctionSession completed = new AuctionSession();
        completed.setStatus(AuctionStatus.COMPLETED);
        completed.setStartTime(LocalDateTime.now().minusHours(2));
        completed.setEndTime(LocalDateTime.now());
        auctionSessionRepository.save(completed);

        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("250000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("❌ Bid when no auction session exists — 400 Bad Request")
    void bid_NoAuctionSession_Returns400() throws Exception {
        auctionSessionRepository.deleteAll();

        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("250000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // INVALID ENTITY REFERENCES
    // ═══════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("❌ Bid with non-existent team ID — 404 Not Found")
    void bid_NonExistentTeam_Returns404() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(99999L);
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("250000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("❌ Bid with non-existent player ID — 404 Not Found")
    void bid_NonExistentPlayer_Returns404() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(99999L);
        bid.setAmount(new BigDecimal("250000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isNotFound());
    }

    // ═══════════════════════════════════════════
    // MALFORMED REQUEST BODIES
    // ═══════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("❌ Bid with null teamId — 400 Bad Request")
    void bid_NullTeamId_Returns400() throws Exception {
        String json = """
                {
                    "teamId": null,
                    "playerId": %d,
                    "amount": 250000000
                }
                """.formatted(unsoldPlayer.getId());

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("❌ Bid with null playerId — 400 Bad Request")
    void bid_NullPlayerId_Returns400() throws Exception {
        String json = """
                {
                    "teamId": %d,
                    "playerId": null,
                    "amount": 250000000
                }
                """.formatted(team.getId());

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("❌ Bid with string amount — 400 Bad Request")
    void bid_StringAmount_Returns400() throws Exception {
        String json = """
                {
                    "teamId": %d,
                    "playerId": %d,
                    "amount": "not-a-number"
                }
                """.formatted(team.getId(), unsoldPlayer.getId());

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    @DisplayName("❌ Bid with empty JSON body — 400 Bad Request")
    void bid_EmptyBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ═══════════════════════════════════════════
    // SQL INJECTION ATTEMPT
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("🔒 SQL injection via bid amount field — should be blocked")
    void bid_SqlInjectionAttempt_IsBlocked() throws Exception {
        String maliciousJson = """
                {
                    "teamId": 1,
                    "playerId": 1,
                    "amount": "250000000; DROP TABLE teams;--"
                }
                """;

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(maliciousJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(13)
    @DisplayName("❌ Bid with missing Content-Type header — 415 Unsupported")
    void bid_NoContentType_Returns415() throws Exception {
        BidRequest bid = new BidRequest();
        bid.setTeamId(team.getId());
        bid.setPlayerId(unsoldPlayer.getId());
        bid.setAmount(new BigDecimal("250000000"));

        mockMvc.perform(post("/api/auction/bid")
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
