package com.ipl.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidResponse {

    private Long bidId;
    private Long auctionId;
    private Long playerId;
    private String playerName;
    private Long teamId;
    private String teamName;
    private String teamShortCode;
    private Long bidAmount;
    private String formattedBidAmount;
    private Long nextMinimumBid;
    private String formattedNextMinimumBid;
    private String bidStatus;
    private LocalDateTime bidTimestamp;

    public String getTeamCode() {
        return teamShortCode;
    }

    public static String formatCurrency(Long amount) {
        if (amount == null) return "₹ 0";
        if (amount >= 10000000) {
            double crores = amount / 10000000.0;
            return String.format("₹ %.2f Crore", crores);
        } else if (amount >= 100000) {
            double lakhs = amount / 100000.0;
            return String.format("₹ %.2f Lakh", lakhs);
        } else {
            return String.format("₹ %,d", amount);
        }
    }
}
