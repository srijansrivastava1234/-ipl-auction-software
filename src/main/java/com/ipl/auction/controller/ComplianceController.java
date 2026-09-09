package com.ipl.auction.controller;

import com.ipl.auction.dto.response.ApiResponse;
import com.ipl.auction.dto.response.TeamComplianceReport;
import com.ipl.auction.service.ComplianceAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Franchise Roster Compliance", description = "BCCI regulatory audit endpoints for squad composition, purse limits, and role balance")
public class ComplianceController {

    private final ComplianceAuditService complianceAuditService;

    @GetMapping("/{teamId}/compliance")
    @Operation(summary = "Audit Franchise Compliance", description = "Performs an exhaustive BCCI regulatory check on squad size, foreign quota, purse spend, and role balance.")
    public ResponseEntity<ApiResponse<TeamComplianceReport>> getTeamCompliance(@PathVariable Long teamId) {
        TeamComplianceReport report = complianceAuditService.auditTeamCompliance(teamId);
        return ResponseEntity.ok(ApiResponse.success(report, "Franchise compliance audit completed"));
    }

    @GetMapping("/compliance-overview")
    @Operation(summary = "Audit All Franchises Compliance", description = "Audits all participating franchises against BCCI auction and roster regulations.")
    public ResponseEntity<ApiResponse<List<TeamComplianceReport>>> getAllTeamsCompliance() {
        List<TeamComplianceReport> reports = complianceAuditService.auditAllTeams();
        return ResponseEntity.ok(ApiResponse.success(reports, "All franchise compliance audits completed"));
    }
}
