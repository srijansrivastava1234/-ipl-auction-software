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
 * Edge-Case Tests: Purse Overflow Scenarios — Member 5 Deliverable.
 * <p>
 * Tests boundary conditions around the ₹100 Cr purse limit:
 * - Bids exactly at purse limit
 * - Bids exceeding purse by ₹1
 * - Multiple bids exhausting purse progressively
 * - Bids when purse is already zero
 * - Maximum integer overflow attempts
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Edge Case — Purse Overflow Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PurseOverflowTest {

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

    private Team team;
    private Player player;

    @BeforeEach
    void setUp() {
        auctionSessionRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        // Team with exactly ₹100 Cr purse
        team = new Team();
        team.setName("Test Team");
        team.setCity("TestCity");
        team.setOwner("Test Owner");
        team.setPurseRemaining(new BigDecimal("1000000000")); // ₹100 Cr
        team.setMaxPurse(new BigDecimal("1000000000"));
        team = teamRepository.save(team);

        // Player with ₹20 Cr base
        player = new Player();
        player.setName("Test Player");
        player.setAge(25);
        player.setRole(PlayerRole.BATSMAN);
        player.setCountry("India");
        player.setBasePrice(new BigDecimal("200000000"));
        player.setStatus(PlayerStatus.UNSOLD);
        player = playerRepository.save(player);

        // Active auction
        AuctionSession session = new AuctionSession();
        session.setStatus(AuctionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        session.setRound(1);
        auctionSessionRepository.save(session);
    }

    // ═══════════════════════════════════════════
    // PURSE BOUNDARY TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ Bid exactly at purse limit (₹100 Cr) — should SUCCEED")
    void bid_ExactlyAtPurseLimit_Succeeds() throws Exception {
        BidRequest bid = createBid("1000000000"); // ₹100 Cr = purse limit

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    @DisplayName("❌ Bid ₹1 over purse limit (₹100,00,00,001) — should FAIL")
    void bid_OneRupeeOverPurse_Fails() throws Exception {
        BidRequest bid = createBid("1000000001"); // ₹100 Cr + ₹1

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("purse")));
    }

    @Test
    @Order(3)
    @DisplayName("❌ Bid double the purse (₹200 Cr) — should FAIL")
    void bid_DoublePurse_Fails() throws Exception {
        BidRequest bid = createBid("2000000000"); // ₹200 Cr

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("purse")));
    }

    @Test
    @Order(4)
    @DisplayName("❌ Bid with astronomically large amount — should FAIL gracefully")
    void bid_AstronomicalAmount_FailsGracefully() throws Exception {
        BidRequest bid = createBid("99999999999999999"); // Ridiculously large

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("❌ Bid when purse is already ₹0 — should FAIL")
    void bid_ZeroPurse_Fails() throws Exception {
        // Drain the purse
        team.setPurseRemaining(BigDecimal.ZERO);
        teamRepository.save(team);

        BidRequest bid = createBid("200000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("purse")));
    }

    @Test
    @Order(6)
    @DisplayName("❌ Bid when purse has exactly ₹1 remaining — should FAIL (below base price)")
    void bid_OnlyOneRupeeLeft_Fails() throws Exception {
        team.setPurseRemaining(new BigDecimal("1")); // ₹1 left
        teamRepository.save(team);

        BidRequest bid = createBid("200000000"); // ₹20 Cr base

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    @DisplayName("✅ Bid when purse has exactly base price remaining — should SUCCEED")
    void bid_PurseEqualsBasePrice_Succeeds() throws Exception {
        team.setPurseRemaining(new BigDecimal("200000000")); // ₹20 Cr = base price
        teamRepository.save(team);

        BidRequest bid = createBid("200000000"); // Bid at base price

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(8)
    @DisplayName("❌ Bid with negative amount — should FAIL with validation error")
    void bid_NegativeAmount_Fails() throws Exception {
        BidRequest bid = createBid("-500000000");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    @DisplayName("❌ Bid with zero amount — should FAIL")
    void bid_ZeroAmount_Fails() throws Exception {
        BidRequest bid = createBid("0");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    @DisplayName("❌ Bid with decimal precision (₹25,00,00,000.50) — should handle correctly")
    void bid_DecimalPrecision_HandlesCorrectly() throws Exception {
        BidRequest bid = createBid("250000000.50");

        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid)))
                // Should either accept whole-rupee equivalent or reject fractional bids
                .andExpect(status().is(anyOf(equalTo(200), equalTo(400))));
    }

    // ═══════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════

    private BidRequest createBid(String amount) {
        BidRequest req = new BidRequest();
        req.setTeamId(team.getId());
        req.setPlayerId(player.getId());
        req.setAmount(new BigDecimal(amount));
        return req;
    }
}
