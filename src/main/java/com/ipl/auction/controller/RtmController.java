package com.ipl.auction.controller;

import com.ipl.auction.dto.request.RtmExerciseRequest;
import com.ipl.auction.dto.response.ApiResponse;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.dto.response.TeamRtmSummary;
import com.ipl.auction.service.RtmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Right To Match (RTM) Operations", description = "Endpoints for BCCI RTM card balance audits and live matching bids")
public class RtmController {

    private final RtmService rtmService;

    @GetMapping("/api/v1/teams/{teamId}/rtm")
    @Operation(summary = "Get Franchise RTM Card Balance", description = "Returns total, used, and remaining RTM cards for a franchise.")
    public ResponseEntity<ApiResponse<TeamRtmSummary>> getTeamRtmSummary(@PathVariable Long teamId) {
        TeamRtmSummary summary = rtmService.getTeamRtmSummary(teamId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Franchise RTM balance fetched successfully"));
    }

    @PostMapping("/api/v1/auction/rtm/exercise")
    @Operation(summary = "Exercise Right to Match (RTM)", description = "Matches the current winning bid price and transfers the player acquisition atomically.")
    public ResponseEntity<ApiResponse<PlayerAuctionSummary>> exerciseRtm(@Valid @RequestBody RtmExerciseRequest request) {
        PlayerAuctionSummary summary = rtmService.exerciseRtm(request);
        return ResponseEntity.ok(ApiResponse.success(summary, "Right To Match successfully exercised! Player " +
                summary.getFullName() + " acquired by " + summary.getSoldToTeamName()));
    }
}
