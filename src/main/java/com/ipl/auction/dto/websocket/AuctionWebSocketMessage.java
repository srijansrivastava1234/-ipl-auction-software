package com.ipl.auction.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionWebSocketMessage<T> {

    public enum MessageType {
        BID_PLACED,
        TIMER_TICK,
        HAMMER_FALL,
        STAGE_UPDATED,
        RTM_NOTIFICATION,
        AUCTION_STATUS
    }

    private MessageType type;
    private T payload;
    private String message;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
