package com.ipl.auction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRtmSummary {

    private Long teamId;
    private String teamName;
    private String shortCode;
    private int rtmCardsTotal;
    private int rtmCardsUsed;
    private int rtmCardsRemaining;
    private boolean canExerciseRtm;
}
