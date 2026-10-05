package com.ipl.auction.controller;

import com.ipl.auction.config.TokenUtil;
import com.ipl.auction.dto.PlaceBidRequest;
import com.ipl.auction.model.Bid;
import com.ipl.auction.service.BidService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
public class BidController {

    private final BidService bidService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TokenUtil tokenUtil;

    public BidController(BidService bidService, SimpMessagingTemplate messagingTemplate, TokenUtil tokenUtil) {
        this.bidService = bidService;
        this.messagingTemplate = messagingTemplate;
        this.tokenUtil = tokenUtil;
    }

    @PostMapping
    public ResponseEntity<?> placeBid(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody PlaceBidRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid authorization token"));
        }

        String token = authHeader.substring(7);
        TokenUtil.UserTokenState tokenState = tokenUtil.validateToken(token);
        if (tokenState == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        // Rule 1: Only TEAM_OWNER or ADMIN can place bids
        if (!"TEAM_OWNER".equals(tokenState.getRole()) && !"ADMIN".equals(tokenState.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only team owners or auctioneers can place bids"));
        }

        // Rule 2: TEAM_OWNER cannot place bids on behalf of another franchise
        if ("TEAM_OWNER".equals(tokenState.getRole()) && !tokenState.getTeamId().equals(request.getTeamId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You cannot place a bid on behalf of another team!"));
        }

        Bid bid = bidService.placeBid(request.getPlayerId(), request.getTeamId(), request.getAmount());

        // Broadcast real-time update to all connected team devices
        messagingTemplate.convertAndSend("/topic/bids", bid);

        return ResponseEntity.ok(bid);
    }

    @GetMapping("/player/{playerId}/highest")
    public ResponseEntity<?> getHighestBid(@PathVariable Long playerId) {
        return bidService.getHighestBidForPlayer(playerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}