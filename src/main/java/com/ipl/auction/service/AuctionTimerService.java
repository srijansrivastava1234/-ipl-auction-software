package com.ipl.auction.service;

import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.dto.websocket.AuctionTimerTick;
import com.ipl.auction.dto.websocket.AuctionWebSocketMessage;
import com.ipl.auction.entity.Auction;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.repository.AuctionRepository;
import com.ipl.auction.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionTimerService {

    public static final int DEFAULT_AUCTION_COUNTDOWN_SECONDS = 30;

    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;
    private final AuctionRepository auctionRepository;
    private final AuctioneerService auctioneerService;

    private final AtomicInteger secondsRemaining = new AtomicInteger(0);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private volatile Long currentAuctionId;
    private volatile Long currentActivePlayerId;

    public synchronized void startTimer(Long auctionId, Long playerId) {
        this.currentAuctionId = auctionId;
        this.currentActivePlayerId = playerId;
        this.secondsRemaining.set(DEFAULT_AUCTION_COUNTDOWN_SECONDS);
        this.isRunning.set(true);
        this.isPaused.set(false);

        log.info("Auction countdown started: Auction={}, Player={}, Seconds={}",
                auctionId, playerId, DEFAULT_AUCTION_COUNTDOWN_SECONDS);

        broadcastTick(buildTick(AuctionTimerTick.ClockState.COUNTING_DOWN, "Bidding is LIVE! Clock reset to 30s"));
    }

    public synchronized void resetTimerOnBid(Long auctionId, Long playerId, Long newBid, String teamCode) {
        this.currentAuctionId = auctionId;
        this.currentActivePlayerId = playerId;
        this.secondsRemaining.set(DEFAULT_AUCTION_COUNTDOWN_SECONDS);
        this.isRunning.set(true);
        this.isPaused.set(false);

        log.info("Bid placed! Timer reset: Player={}, NewBid={}, LeadingTeam={}", playerId, newBid, teamCode);

        broadcastTick(buildTick(AuctionTimerTick.ClockState.COUNTING_DOWN,
                "New Bid Placed by " + teamCode + "! Clock reset to 30s"));
    }

    public synchronized void pauseTimer() {
        if (isRunning.get()) {
            isPaused.set(true);
            log.info("Auction countdown paused at {}s", secondsRemaining.get());
            broadcastTick(buildTick(AuctionTimerTick.ClockState.PAUSED, "Auction timer paused by auctioneer"));
        }
    }

    public synchronized void resumeTimer() {
        if (isRunning.get() && isPaused.get()) {
            isPaused.set(false);
            log.info("Auction countdown resumed from {}s", secondsRemaining.get());
            broadcastTick(buildTick(AuctionTimerTick.ClockState.COUNTING_DOWN, "Auction timer resumed"));
        }
    }

    public synchronized void stopTimer() {
        isRunning.set(false);
        isPaused.set(false);
        secondsRemaining.set(0);
        log.info("Auction countdown stopped");
        broadcastTick(buildTick(AuctionTimerTick.ClockState.IDLE, "Timer stopped"));
    }

    @Scheduled(fixedRate = 1000)
    public void onTick() {
        if (!isRunning.get() || isPaused.get()) {
            return;
        }

        int remaining = secondsRemaining.decrementAndGet();

        if (remaining > 10) {
            broadcastTick(buildTick(AuctionTimerTick.ClockState.COUNTING_DOWN, remaining + "s remaining"));
        } else if (remaining > 5) {
            broadcastTick(buildTick(AuctionTimerTick.ClockState.GOING_ONCE, "Going ONCE at " + remaining + "s!"));
        } else if (remaining > 0) {
            broadcastTick(buildTick(AuctionTimerTick.ClockState.GOING_TWICE, "Going TWICE... Final call!"));
        } else {
            // Reached 0: Auto-hammer strike
            isRunning.set(false);
            handleAutoHammerExpiry();
        }
    }

    private void handleAutoHammerExpiry() {
        if (currentAuctionId == null || currentActivePlayerId == null) {
            return;
        }

        log.info("Countdown reached 0! Triggering automated hammer strike for player {}", currentActivePlayerId);

        try {
            Player player = playerRepository.findById(currentActivePlayerId).orElse(null);
            if (player == null || player.getStatus() != PlayerStatus.IN_AUCTION) {
                return;
            }

            if (player.getCurrentWinningTeam() != null && player.getCurrentBidPrice() != null && player.getCurrentBidPrice() > 0) {
                PlayerAuctionSummary summary = auctioneerService.strikeHammerSold(currentAuctionId, currentActivePlayerId);
                broadcastHammer(summary, "SOLD! " + summary.getFullName() + " acquired by " + summary.getSoldToTeamName());
            } else {
                PlayerAuctionSummary summary = auctioneerService.strikeHammerUnsold(currentAuctionId, currentActivePlayerId);
                broadcastHammer(summary, "UNSOLD! " + summary.getFullName() + " moves to unsold pool");
            }
        } catch (Exception ex) {
            log.error("Error during automated hammer strike on timer expiry", ex);
        }
    }

    public AuctionTimerTick getCurrentStatus() {
        AuctionTimerTick.ClockState state = isRunning.get() ?
                (isPaused.get() ? AuctionTimerTick.ClockState.PAUSED : AuctionTimerTick.ClockState.COUNTING_DOWN) :
                AuctionTimerTick.ClockState.IDLE;
        return buildTick(state, "Timer status query");
    }

    private AuctionTimerTick buildTick(AuctionTimerTick.ClockState state, String alertText) {
        String playerName = "None";
        Long bidPrice = 0L;
        String teamCode = "None";

        if (currentActivePlayerId != null) {
            Player p = playerRepository.findById(currentActivePlayerId).orElse(null);
            if (p != null) {
                playerName = p.getFullName();
                bidPrice = p.getCurrentBidPrice() != null ? p.getCurrentBidPrice() : 0L;
                if (p.getCurrentWinningTeam() != null) {
                    teamCode = p.getCurrentWinningTeam().getShortCode();
                }
            }
        }

        return AuctionTimerTick.builder()
                .auctionId(currentAuctionId)
                .activePlayerId(currentActivePlayerId)
                .activePlayerName(playerName)
                .currentBidPrice(bidPrice)
                .formattedBidPrice(formatPrice(bidPrice))
                .leadingTeamCode(teamCode)
                .secondsRemaining(Math.max(0, secondsRemaining.get()))
                .state(state)
                .alertText(alertText)
                .build();
    }

    private void broadcastTick(AuctionTimerTick tick) {
        try {
            messagingTemplate.convertAndSend("/topic/timer", tick);
        } catch (Exception ex) {
            log.trace("WebSocket broadcast skipped: {}", ex.getMessage());
        }
    }

    private void broadcastHammer(PlayerAuctionSummary summary, String msg) {
        try {
            AuctionWebSocketMessage<PlayerAuctionSummary> wsMessage = AuctionWebSocketMessage.<PlayerAuctionSummary>builder()
                    .type(AuctionWebSocketMessage.MessageType.HAMMER_FALL)
                    .payload(summary)
                    .message(msg)
                    .build();
            messagingTemplate.convertAndSend("/topic/auction", wsMessage);
        } catch (Exception ex) {
            log.trace("WebSocket hammer broadcast skipped: {}", ex.getMessage());
        }
    }

    private String formatPrice(Long amount) {
        if (amount == null || amount == 0) return "₹0";
        if (amount >= 10000000L) {
            double cr = amount / 10000000.0;
            return String.format("₹ %.2f Crore", cr);
        } else {
            double lk = amount / 100000.0;
            return String.format("₹ %.2f Lakh", lk);
        }
    }
}
