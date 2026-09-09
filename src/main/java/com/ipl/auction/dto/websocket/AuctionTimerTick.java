package com.ipl.auction.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionTimerTick {

    public enum ClockState {
        IDLE,
        COUNTING_DOWN,
        GOING_ONCE,
        GOING_TWICE,
        HAMMER_STRIKE,
        PAUSED
    }

    private Long auctionId;
    private Long activePlayerId;
    private String activePlayerName;
    private Long currentBidPrice;
    private String formattedBidPrice;
    private String leadingTeamCode;
    private int secondsRemaining;
    private ClockState state;
    private String alertText;
}
