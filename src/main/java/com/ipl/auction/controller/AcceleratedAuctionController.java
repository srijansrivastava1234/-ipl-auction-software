package com.ipl.auction.controller;

import com.ipl.auction.dto.request.AcceleratedNominationRequest;
import com.ipl.auction.dto.response.AcceleratedPoolResponse;
import com.ipl.auction.dto.response.ApiResponse;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.service.AcceleratedAuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auction/accelerated")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Accelerated Auction Round", description = "Endpoints for unsold player pool, franchise shortlist nominations, and accelerated staging")
public class AcceleratedAuctionController {

    private final AcceleratedAuctionService acceleratedAuctionService;

    @GetMapping("/pool")
    @Operation(summary = "Get Accelerated Pool & Nominations", description = "Returns all unsold players and currently nominated cricketers for the accelerated round.")
    public ResponseEntity<ApiResponse<AcceleratedPoolResponse>> getAcceleratedPool() {
        AcceleratedPoolResponse pool = acceleratedAuctionService.getAcceleratedPool();
        return ResponseEntity.ok(ApiResponse.success(pool, "Accelerated auction pool retrieved successfully"));
    }

    @PostMapping("/nominate")
    @Operation(summary = "Nominate Players for Accelerated Round", description = "Franchises submit shortlisted unsold player IDs to be brought back to the auction floor.")
    public ResponseEntity<ApiResponse<AcceleratedPoolResponse>> nominatePlayers(@Valid @RequestBody AcceleratedNominationRequest request) {
        AcceleratedPoolResponse response = acceleratedAuctionService.nominatePlayers(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cricketers nominated successfully for accelerated bidding"));
    }

    @PostMapping("/stage/{playerId}")
    @Operation(summary = "Stage Nominated Player to Podium", description = "Brings a nominated player directly to the auctioneer podium for accelerated bidding.")
    public ResponseEntity<ApiResponse<PlayerAuctionSummary>> stageAcceleratedPlayer(
            @RequestParam(defaultValue = "1") Long auctionId,
            @PathVariable Long playerId) {
        PlayerAuctionSummary summary = acceleratedAuctionService.stageAcceleratedPlayer(auctionId, playerId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Player " + summary.getFullName() + " brought to stage for Accelerated Round"));
    }
}
