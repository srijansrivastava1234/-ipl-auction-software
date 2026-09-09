package com.ipl.auction.controller;

import com.ipl.auction.dto.response.ApiResponse;
import com.ipl.auction.dto.response.AuctionSummaryReport;
import com.ipl.auction.dto.response.TeamRosterExport;
import com.ipl.auction.service.AuctionReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Auction Analytics & Reports", description = "Endpoints for macro auction statistics, expenditure categories, and franchise squad roster exports")
public class ReportController {

    private final AuctionReportService auctionReportService;

    @GetMapping("/auction-summary")
    @Operation(summary = "Get Macro Auction Analytics Summary", description = "Generates comprehensive analytics on total spend, category expenditure breakdown, sold/unsold counts, and highest bids.")
    public ResponseEntity<ApiResponse<AuctionSummaryReport>> getAuctionSummary(@RequestParam(defaultValue = "1") Long auctionId) {
        AuctionSummaryReport report = auctionReportService.generateAuctionSummary(auctionId);
        return ResponseEntity.ok(ApiResponse.success(report, "Auction summary report generated successfully"));
    }

    @GetMapping("/teams/{teamId}/roster-export")
    @Operation(summary = "Export Franchise Squad Roster", description = "Exports full squad composition, player details, acquisition prices, and purse audit metrics for a franchise.")
    public ResponseEntity<ApiResponse<TeamRosterExport>> exportTeamRoster(@PathVariable Long teamId) {
        TeamRosterExport export = auctionReportService.exportTeamRoster(teamId);
        return ResponseEntity.ok(ApiResponse.success(export, "Team roster exported successfully"));
    }
}
