package com.ipl.auction.service;

import com.ipl.auction.dto.response.TeamComplianceReport;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.entity.TeamSquad;
import com.ipl.auction.entity.enums.PlayerRole;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.repository.TeamSquadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceAuditService {

    private final TeamRepository teamRepository;
    private final TeamSquadRepository teamSquadRepository;

    private static final int BCCI_MIN_SQUAD_SIZE = 18;
    private static final int BCCI_MAX_SQUAD_SIZE = 25;
    private static final int BCCI_MAX_FOREIGN_PLAYERS = 8;
    private static final double BCCI_MIN_PURSE_SPEND_PERCENTAGE = 75.0;

    private static final int MIN_BATSMEN = 3;
    private static final int MIN_BOWLERS = 3;
    private static final int MIN_ALL_ROUNDERS = 2;
    private static final int MIN_WICKET_KEEPERS = 1;

    @Transactional(readOnly = true)
    public TeamComplianceReport auditTeamCompliance(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with ID: " + teamId));

        List<TeamSquad> squadMembers = teamSquadRepository.findByTeamId(teamId);
        List<String> violations = new ArrayList<>();

        int squadSize = squadMembers.size();
        boolean squadSizeCompliant = (squadSize >= BCCI_MIN_SQUAD_SIZE && squadSize <= BCCI_MAX_SQUAD_SIZE);
        if (squadSize < BCCI_MIN_SQUAD_SIZE) {
            violations.add("Squad size (" + squadSize + ") below BCCI minimum mandatory threshold of " + BCCI_MIN_SQUAD_SIZE);
        } else if (squadSize > BCCI_MAX_SQUAD_SIZE) {
            violations.add("Squad size (" + squadSize + ") exceeds BCCI maximum cap of " + BCCI_MAX_SQUAD_SIZE);
        }

        int foreignCount = (int) squadMembers.stream()
                .filter(s -> s.getPlayer() != null && Boolean.TRUE.equals(s.getPlayer().getIsOverseas()))
                .count();
        boolean foreignPlayerCompliant = (foreignCount <= BCCI_MAX_FOREIGN_PLAYERS);
        if (!foreignPlayerCompliant) {
            violations.add("Overseas player count (" + foreignCount + ") exceeds BCCI maximum quota of " + BCCI_MAX_FOREIGN_PLAYERS);
        }

        long totalPurse = team.getTotalPurse();
        long remainingPurse = team.getRemainingPurse();
        long spentPurse = totalPurse - remainingPurse;
        double purseSpentPercentage = totalPurse > 0 ? (spentPurse * 100.0 / totalPurse) : 0.0;
        boolean purseSpendCompliant = (purseSpentPercentage >= BCCI_MIN_PURSE_SPEND_PERCENTAGE);
        if (!purseSpendCompliant) {
            violations.add(String.format("Purse spend (%.2f%%) below BCCI mandatory 75%% minimum spend rule", purseSpentPercentage));
        }

        Map<String, Long> roleCounts = new LinkedHashMap<>();
        for (PlayerRole role : PlayerRole.values()) {
            roleCounts.put(role.name(), 0L);
        }
        for (TeamSquad s : squadMembers) {
            Player p = s.getPlayer();
            if (p != null && p.getRole() != null) {
                roleCounts.put(p.getRole().name(), roleCounts.getOrDefault(p.getRole().name(), 0L) + 1);
            }
        }

        long batsmen = roleCounts.getOrDefault(PlayerRole.BATSMAN.name(), 0L);
        long bowlers = roleCounts.getOrDefault(PlayerRole.BOWLER.name(), 0L);
        long allRounders = roleCounts.getOrDefault(PlayerRole.ALL_ROUNDER.name(), 0L);
        long keepers = roleCounts.getOrDefault(PlayerRole.WICKET_KEEPER.name(), 0L);

        boolean roleCompositionCompliant = true;
        if (batsmen < MIN_BATSMEN) {
            violations.add("Insufficient batsmen (" + batsmen + "/" + MIN_BATSMEN + ")");
            roleCompositionCompliant = false;
        }
        if (bowlers < MIN_BOWLERS) {
            violations.add("Insufficient bowlers (" + bowlers + "/" + MIN_BOWLERS + ")");
            roleCompositionCompliant = false;
        }
        if (allRounders < MIN_ALL_ROUNDERS) {
            violations.add("Insufficient all-rounders (" + allRounders + "/" + MIN_ALL_ROUNDERS + ")");
            roleCompositionCompliant = false;
        }
        if (keepers < MIN_WICKET_KEEPERS) {
            violations.add("Insufficient wicket-keepers (" + keepers + "/" + MIN_WICKET_KEEPERS + ")");
            roleCompositionCompliant = false;
        }

        boolean overallCompliant = squadSizeCompliant && foreignPlayerCompliant && purseSpendCompliant && roleCompositionCompliant;

        return TeamComplianceReport.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .shortCode(team.getShortCode())
                .squadSize(squadSize)
                .squadSizeCompliant(squadSizeCompliant)
                .foreignPlayerCount(foreignCount)
                .foreignPlayerCompliant(foreignPlayerCompliant)
                .totalPurse(totalPurse)
                .remainingPurse(remainingPurse)
                .spentPurse(spentPurse)
                .purseSpentPercentage(Math.round(purseSpentPercentage * 100.0) / 100.0)
                .purseSpendCompliant(purseSpendCompliant)
                .roleCounts(roleCounts)
                .roleCompositionCompliant(roleCompositionCompliant)
                .overallCompliant(overallCompliant)
                .violations(violations)
                .build();
    }

    @Transactional(readOnly = true)
    public List<TeamComplianceReport> auditAllTeams() {
        return teamRepository.findAll().stream()
                .map(t -> auditTeamCompliance(t.getId()))
                .collect(Collectors.toList());
    }
}
