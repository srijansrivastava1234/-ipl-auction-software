package com.ipl.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class PlaceBidRequest {

    @NotNull(message = "Player ID cannot be null")
    private Long playerId;

    @NotNull(message = "Team ID cannot be null")
    private Long teamId;

    @NotNull(message = "Bid amount cannot be null")
    @Positive(message = "Bid amount must be positive")
    private BigDecimal amount;

    public PlaceBidRequest() {}

    public PlaceBidRequest(Long playerId, Long teamId, BigDecimal amount) {
        this.playerId = playerId;
        this.teamId = teamId;
        this.amount = amount;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
