package com.ipl.auction.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceleratedNominationRequest {

    @NotNull(message = "Franchise Team ID is required for accelerated round nomination")
    private Long teamId;

    @NotEmpty(message = "Nominated player IDs list cannot be empty")
    private List<Long> playerIds;

    private String roundName;
}
