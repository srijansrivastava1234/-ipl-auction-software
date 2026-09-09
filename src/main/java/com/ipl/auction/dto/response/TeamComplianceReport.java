package com.ipl.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamComplianceReport {

    private Long teamId;
    private String teamName;
    private String shortCode;
    private int squadSize;
    private boolean squadSizeCompliant;
    private int foreignPlayerCount;
    private boolean foreignPlayerCompliant;
    private Long totalPurse;
    private Long remainingPurse;
    private Long spentPurse;
    private double purseSpentPercentage;
    private boolean purseSpendCompliant;
    private Map<String, Long> roleCounts;
    private boolean roleCompositionCompliant;
    private boolean overallCompliant;
    private List<String> violations;
}
