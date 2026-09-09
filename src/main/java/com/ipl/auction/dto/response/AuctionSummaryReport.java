package com.ipl.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionSummaryReport {

    private Long auctionId;
    private String auctionTitle;
    private Integer year;
    private int totalPlayers;
    private int totalSold;
    private int totalUnsold;
    private int totalAvailable;
    private Long totalPurseSpentAllTeams;
    private String formattedTotalPurseSpent;
    private PlayerAuctionSummary highestSoldPlayer;
    private Map<String, Long> categorySpend;
    private int totalIndianSold;
    private int totalOverseasSold;
}
