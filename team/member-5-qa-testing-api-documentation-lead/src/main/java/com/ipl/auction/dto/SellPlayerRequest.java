package com.ipl.auction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class SellPlayerRequest {

    @NotNull(message = "Team ID cannot be null")
    private Long teamId;

    @NotNull(message = "Final price cannot be null")
    @Positive(message = "Final price must be positive")
    private BigDecimal finalPrice;

    public SellPlayerRequest() {}

    public SellPlayerRequest(Long teamId, BigDecimal finalPrice) {
        this.teamId = teamId;
        this.finalPrice = finalPrice;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(BigDecimal finalPrice) {
        this.finalPrice = finalPrice;
    }
}
