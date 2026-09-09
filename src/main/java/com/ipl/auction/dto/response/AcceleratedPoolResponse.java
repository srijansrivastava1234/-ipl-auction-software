package com.ipl.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceleratedPoolResponse {

    private int totalUnsoldPlayers;
    private int totalNominatedPlayers;
    private List<PlayerAuctionSummary> nominatedPlayers;
    private List<PlayerAuctionSummary> unsoldPool;
}
