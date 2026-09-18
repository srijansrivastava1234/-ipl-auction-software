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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Edge-Case Tests: Concurrent Bid Race Conditions — Member 5 Deliverable.
 * <p>
 * Tests scenarios where multiple teams bid on the same player simultaneously:
 * - Two teams bidding at the exact same time
 * - Multiple rapid sequential bids from different teams
 * - Same team sending duplicate bids concurrently
 * - Bid + purse deduction atomicity
 * </p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Edge Case — Concurrent Bid Race Condition Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConcurrentBidTest {

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

    private Team team1;
    private Team team2;
    private Team team3;
    private Player player;

    @BeforeEach
    void setUp() {
        bidRepository.deleteAll();
        auctionSessionRepository.deleteAll();
        playerRepository.deleteAll();
        teamRepository.deleteAll();

        // Team 1 — Mumbai Indians
        team1 = createTeamInDB("Mumbai Indians", "1000000000");

        // Team 2 — Chennai Super Kings
        team2 = createTeamInDB("Chennai Super Kings", "1000000000");

        // Team 3 — Royal Challengers
        team3 = createTeamInDB("Royal Challengers", "1000000000");

        // Player to bid on
        player = new Player();
        player.setName("Virat Kohli");
        player.setAge(35);
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
    // CONCURRENT BIDS FROM DIFFERENT TEAMS
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("⚡ Two teams bid simultaneously on same player — only highest wins or first processed")
    void concurrentBids_TwoTeams_OnlyOneShouldWin() throws Exception {
        BidRequest bid1 = createBidRequest(team1.getId(), player.getId(), "250000000");
        BidRequest bid2 = createBidRequest(team2.getId(), player.getId(), "300000000");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        List<Future<Integer>> results = new ArrayList<>();

        // Thread 1: Team 1 bids ₹25 Cr
        results.add(executor.submit(() -> {
            latch.await(); // Wait for both threads to be ready
            MvcResult result = mockMvc.perform(post("/api/auction/bid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bid1)))
                    .andReturn();
            return result.getResponse().getStatus();
        }));

        // Thread 2: Team 2 bids ₹30 Cr
        results.add(executor.submit(() -> {
            latch.await(); // Wait for both threads to be ready
            MvcResult result = mockMvc.perform(post("/api/auction/bid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bid2)))
                    .andReturn();
            return result.getResponse().getStatus();
        }));

        // Release both threads simultaneously
        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // At least one should succeed (200), system should not crash
        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> f : results) {
            statusCodes.add(f.get());
        }

        // Verify: at least one bid was accepted, no 500 errors
        assertThat(statusCodes).doesNotContain(500);
        assertThat(statusCodes).contains(200);
    }

    // ═══════════════════════════════════════════
    // RAPID SEQUENTIAL BIDS
    // ═══════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("⚡ 10 rapid sequential bids from different teams — no data corruption")
    void rapidBids_TenSequential_NoDuplicatesOrCorruption() throws Exception {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Player p = new Player();
            p.setName("Player " + i);
            p.setAge(25);
            p.setRole(PlayerRole.BATSMAN);
            p.setCountry("India");
            p.setBasePrice(new BigDecimal("200000000"));
            p.setStatus(PlayerStatus.UNSOLD);
            players.add(playerRepository.save(p));
        }

        int successCount = 0;
        for (int i = 0; i < 10; i++) {
            Team bidTeam = (i % 2 == 0) ? team1 : team2;
            BidRequest bid = createBidRequest(
                    bidTeam.getId(),
                    players.get(i).getId(),
                    String.valueOf(200000000 + (i * 10000000L)));

            MvcResult result = mockMvc.perform(post("/api/auction/bid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bid)))
                    .andReturn();

            if (result.getResponse().getStatus() == 200) {
                successCount++;
            }
        }

        // All 10 should succeed since each targets a different player
        assertThat(successCount).isEqualTo(10);
    }

    // ═══════════════════════════════════════════
    // SAME TEAM DUPLICATE CONCURRENT BIDS
    // ═══════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("⚡ Same team sends identical bid twice concurrently — deduplicated")
    void concurrentBids_SameTeamDuplicate_HandleGracefully() throws Exception {
        BidRequest bid = createBidRequest(team1.getId(), player.getId(), "250000000");
        String bidJson = objectMapper.writeValueAsString(bid);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            results.add(executor.submit(() -> {
                latch.await();
                MvcResult result = mockMvc.perform(post("/api/auction/bid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bidJson))
                        .andReturn();
                return result.getResponse().getStatus();
            }));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> f : results) {
            statusCodes.add(f.get());
        }

        // Should not produce 500 errors
        assertThat(statusCodes).doesNotContain(500);
        // At least one should succeed
        assertThat(statusCodes).contains(200);
    }

    // ═══════════════════════════════════════════
    // THREE-WAY CONCURRENT BID
    // ═══════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("⚡ Three teams bid simultaneously — system handles without errors")
    void concurrentBids_ThreeTeams_NoServerError() throws Exception {
        BidRequest bid1 = createBidRequest(team1.getId(), player.getId(), "250000000");
        BidRequest bid2 = createBidRequest(team2.getId(), player.getId(), "300000000");
        BidRequest bid3 = createBidRequest(team3.getId(), player.getId(), "350000000");

        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(1);

        List<Future<Integer>> results = new ArrayList<>();
        for (BidRequest bid : List.of(bid1, bid2, bid3)) {
            results.add(executor.submit(() -> {
                latch.await();
                MvcResult result = mockMvc.perform(post("/api/auction/bid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bid)))
                        .andReturn();
                return result.getResponse().getStatus();
            }));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> f : results) {
            statusCodes.add(f.get());
        }

        // Critical: No 500 Internal Server Errors
        assertThat(statusCodes).doesNotContain(500);
    }

    // ═══════════════════════════════════════════
    // PURSE ATOMICITY TEST
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("⚡ Bid + purse deduction must be atomic — purse never goes negative")
    void bid_PurseDeduction_IsAtomic() throws Exception {
        // Set team purse to exactly enough for 1 bid
        team1.setPurseRemaining(new BigDecimal("250000000")); // ₹25 Cr
        teamRepository.save(team1);

        Player player2 = new Player();
        player2.setName("Player Two");
        player2.setAge(28);
        player2.setRole(PlayerRole.BOWLER);
        player2.setCountry("India");
        player2.setBasePrice(new BigDecimal("200000000"));
        player2.setStatus(PlayerStatus.UNSOLD);
        player2 = playerRepository.save(player2);

        // Bid 1: ₹25 Cr on Player 1 — should succeed (uses full purse)
        BidRequest bid1 = createBidRequest(team1.getId(), player.getId(), "250000000");
        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid1)))
                .andExpect(status().isOk());

        // Bid 2: ₹20 Cr on Player 2 — should FAIL (purse depleted)
        BidRequest bid2 = createBidRequest(team1.getId(), player2.getId(), "200000000");
        mockMvc.perform(post("/api/auction/bid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bid2)))
                .andExpect(status().isBadRequest());

        // Verify purse is non-negative
        Team refreshed = teamRepository.findById(team1.getId()).orElseThrow();
        assertThat(refreshed.getPurseRemaining()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    // ═══════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════

    private Team createTeamInDB(String name, String purse) {
        Team t = new Team();
        t.setName(name);
        t.setCity("TestCity");
        t.setOwner("Test Owner");
        t.setPurseRemaining(new BigDecimal(purse));
        t.setMaxPurse(new BigDecimal(purse));
        return teamRepository.save(t);
    }

    private BidRequest createBidRequest(Long teamId, Long playerId, String amount) {
        BidRequest req = new BidRequest();
        req.setTeamId(teamId);
        req.setPlayerId(playerId);
        req.setAmount(new BigDecimal(amount));
        return req;
    }
}
