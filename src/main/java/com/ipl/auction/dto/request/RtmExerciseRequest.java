package com.ipl.auction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtmExerciseRequest {

    @NotNull(message = "Franchise Team ID is mandatory to exercise Right to Match")
    private Long teamId;

    @NotNull(message = "Player ID under auction hammer is mandatory")
    private Long playerId;

    @NotNull(message = "Auction Session ID is mandatory")
    private Long auctionId;
}
