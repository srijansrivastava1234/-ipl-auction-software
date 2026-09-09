package com.ipl.auction.dto.response;

import com.ipl.auction.entity.enums.PlayerRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRosterExport {

    private Long teamId;
    private String teamName;
    private String shortCode;
    private Long totalPurse;
    private Long remainingPurse;
    private Long spentPurse;
    private int totalSquadCount;
    private int foreignCount;
    private int availableSquadSlots;
    private int availableForeignSlots;
    private List<RosterPlayerDetail> players;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RosterPlayerDetail {
        private Long playerId;
        private String fullName;
        private PlayerRole role;
        private String country;
        private boolean overseas;
        private Long boughtPrice;
        private String formattedBoughtPrice;
        private LocalDateTime acquiredAt;
    }
}
