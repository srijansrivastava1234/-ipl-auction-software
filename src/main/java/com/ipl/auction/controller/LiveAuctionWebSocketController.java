package com.ipl.auction.controller;

import com.ipl.auction.dto.request.BidRequest;
import com.ipl.auction.dto.response.BidResponse;
import com.ipl.auction.dto.websocket.AuctionTimerTick;
import com.ipl.auction.dto.websocket.AuctionWebSocketMessage;
import com.ipl.auction.service.AuctionTimerService;
import com.ipl.auction.service.BiddingEngineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LiveAuctionWebSocketController {

    private final BiddingEngineService biddingEngineService;
    private final AuctionTimerService auctionTimerService;

    @MessageMapping("/bid")
    @SendTo("/topic/bids")
    public AuctionWebSocketMessage<BidResponse> handleLiveBid(BidRequest request) {
        log.info("STOMP Live Bid received: Player={}, Team={}, Amount={}",
                request.getPlayerId(), request.getTeamId(), request.getBidAmount());

        BidResponse bidResponse = biddingEngineService.placeBid(request);

        // Reset the live auction countdown clock
        auctionTimerService.resetTimerOnBid(
                request.getAuctionId(),
                request.getPlayerId(),
                bidResponse.getBidAmount(),
                bidResponse.getTeamCode()
        );

        return AuctionWebSocketMessage.<BidResponse>builder()
                .type(AuctionWebSocketMessage.MessageType.BID_PLACED)
                .payload(bidResponse)
                .message("New leading bid: " + bidResponse.getFormattedBidAmount() + " by " + bidResponse.getTeamName())
                .build();
    }

    @MessageMapping("/timer/status")
    @SendTo("/topic/timer")
    public AuctionTimerTick handleTimerStatusQuery() {
        return auctionTimerService.getCurrentStatus();
    }
}
