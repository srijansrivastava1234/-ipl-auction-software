package com.ipl.auction.service;

import com.ipl.auction.dto.websocket.AuctionTimerTick;
import com.ipl.auction.dto.websocket.AuctionWebSocketMessage;
import com.ipl.auction.entity.Auction;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.entity.enums.AuctionStatus;
import com.ipl.auction.entity.enums.PlayerRole;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.repository.AuctionRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Week11And12IntegrationTest {

    @Autowired
    private AuctionTimerService auctionTimerService;

    @Autowired
    private AuctionMetricsService auctionMetricsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private Team testTeam;
    private Player testPlayer;
    private Auction testAuction;

    @BeforeEach
    void setUp() {
        testTeam = teamRepository.findByShortCode("CSK")
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Chennai Super Kings")
                        .shortCode("CSK")
                        .totalPurse(1000000000L)
                        .remainingPurse(1000000000L)
                        .build()));

        testPlayer = playerRepository.save(Player.builder()
                .fullName("Live Auction Cricketer")
                .role(PlayerRole.ALL_ROUNDER)
                .country("India")
                .isOverseas(false)
                .basePrice(20000000L)
                .currentBidPrice(25000000L)
                .status(PlayerStatus.IN_AUCTION)
                .currentWinningTeam(testTeam)
                .build());

        testAuction = auctionRepository.findAll().stream().findFirst()
                .orElseGet(() -> auctionRepository.save(Auction.builder()
                        .title("IPL 2025 Live Auction")
                        .year(2025)
                        .status(AuctionStatus.LIVE)
                        .build()));
    }

    @Test
    @DisplayName("Week 11: WebSocket SimpMessagingTemplate bean is loaded and operational")
    void testWebSocketMessagingTemplateLoaded() {
        assertNotNull(messagingTemplate);
    }

    @Test
    @DisplayName("Week 11: AuctionTimerService starts countdown at 30 seconds")
    void testAuctionTimerService_StartAndStatus() {
        auctionTimerService.startTimer(testAuction.getId(), testPlayer.getId());

        AuctionTimerTick tick = auctionTimerService.getCurrentStatus();
        assertNotNull(tick);
        assertEquals(30, tick.getSecondsRemaining());
        assertEquals(AuctionTimerTick.ClockState.COUNTING_DOWN, tick.getState());
        assertEquals(testPlayer.getId(), tick.getActivePlayerId());
    }

    @Test
    @DisplayName("Week 11: AuctionTimerService resets clock to 30 seconds on valid incoming bid")
    void testAuctionTimerService_ResetOnBid() {
        auctionTimerService.startTimer(testAuction.getId(), testPlayer.getId());
        auctionTimerService.onTick(); // Decrement by 1s

        auctionTimerService.resetTimerOnBid(testAuction.getId(), testPlayer.getId(), 30000000L, "CSK");

        AuctionTimerTick tick = auctionTimerService.getCurrentStatus();
        assertEquals(30, tick.getSecondsRemaining());
        assertEquals(AuctionTimerTick.ClockState.COUNTING_DOWN, tick.getState());
    }

    @Test
    @DisplayName("Week 11: AuctionTimerService pauses and resumes countdown")
    void testAuctionTimerService_PauseAndResume() {
        auctionTimerService.startTimer(testAuction.getId(), testPlayer.getId());
        auctionTimerService.pauseTimer();

        AuctionTimerTick pausedTick = auctionTimerService.getCurrentStatus();
        assertEquals(AuctionTimerTick.ClockState.PAUSED, pausedTick.getState());

        auctionTimerService.resumeTimer();
        AuctionTimerTick resumedTick = auctionTimerService.getCurrentStatus();
        assertEquals(AuctionTimerTick.ClockState.COUNTING_DOWN, resumedTick.getState());

        auctionTimerService.stopTimer();
        AuctionTimerTick stoppedTick = auctionTimerService.getCurrentStatus();
        assertEquals(AuctionTimerTick.ClockState.IDLE, stoppedTick.getState());
    }

    @Test
    @DisplayName("Week 11: AuctionWebSocketMessage envelope serializes with timestamp and message type")
    void testWebSocketMessageEnvelope() {
        AuctionWebSocketMessage<String> message = AuctionWebSocketMessage.<String>builder()
                .type(AuctionWebSocketMessage.MessageType.BID_PLACED)
                .payload("₹2.50 Crore by CSK")
                .message("Bid Accepted")
                .build();

        assertNotNull(message);
        assertEquals(AuctionWebSocketMessage.MessageType.BID_PLACED, message.getType());
        assertNotNull(message.getTimestamp());
        assertEquals("₹2.50 Crore by CSK", message.getPayload());
    }

    @Test
    @DisplayName("Week 12: AuctionMetricsService increments and tracks Micrometer counters")
    void testAuctionMetricsService() {
        double bidsBefore = auctionMetricsService.getTotalBids();
        double soldBefore = auctionMetricsService.getTotalSold();

        auctionMetricsService.recordBidPlaced();
        auctionMetricsService.recordPlayerSold();

        assertEquals(bidsBefore + 1.0, auctionMetricsService.getTotalBids());
        assertEquals(soldBefore + 1.0, auctionMetricsService.getTotalSold());
    }
}
