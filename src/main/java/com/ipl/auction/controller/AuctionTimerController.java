package com.ipl.auction.controller;

import com.ipl.auction.dto.response.ApiResponse;
import com.ipl.auction.dto.websocket.AuctionTimerTick;
import com.ipl.auction.service.AuctionTimerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auction/timer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Live Auction Timer & Clock", description = "Endpoints controlling the real-time 30-second bidding clock and automated hammer countdown")
public class AuctionTimerController {

    private final AuctionTimerService auctionTimerService;

    @PostMapping("/start")
    @Operation(summary = "Start Auction Timer", description = "Starts or restarts the 30-second countdown clock for the staged player.")
    public ResponseEntity<ApiResponse<AuctionTimerTick>> startTimer(
            @RequestParam(defaultValue = "1") Long auctionId,
            @RequestParam Long playerId) {
        auctionTimerService.startTimer(auctionId, playerId);
        return ResponseEntity.ok(ApiResponse.success(auctionTimerService.getCurrentStatus(), "Countdown timer started at 30s"));
    }

    @PostMapping("/pause")
    @Operation(summary = "Pause Auction Timer", description = "Temporarily freezes the countdown clock.")
    public ResponseEntity<ApiResponse<AuctionTimerTick>> pauseTimer() {
        auctionTimerService.pauseTimer();
        return ResponseEntity.ok(ApiResponse.success(auctionTimerService.getCurrentStatus(), "Auction timer paused"));
    }

    @PostMapping("/resume")
    @Operation(summary = "Resume Auction Timer", description = "Resumes the countdown clock from its paused state.")
    public ResponseEntity<ApiResponse<AuctionTimerTick>> resumeTimer() {
        auctionTimerService.resumeTimer();
        return ResponseEntity.ok(ApiResponse.success(auctionTimerService.getCurrentStatus(), "Auction timer resumed"));
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop Auction Timer", description = "Halts the countdown clock and sets state to IDLE.")
    public ResponseEntity<ApiResponse<AuctionTimerTick>> stopTimer() {
        auctionTimerService.stopTimer();
        return ResponseEntity.ok(ApiResponse.success(auctionTimerService.getCurrentStatus(), "Auction timer stopped"));
    }

    @GetMapping("/status")
    @Operation(summary = "Get Timer Status", description = "Returns the current seconds remaining and countdown state.")
    public ResponseEntity<ApiResponse<AuctionTimerTick>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success(auctionTimerService.getCurrentStatus(), "Current timer status retrieved"));
    }
}
