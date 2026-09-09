package com.ipl.auction.service;

import com.ipl.auction.dto.response.AuctionSummaryReport;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.dto.response.TeamRosterExport;
import com.ipl.auction.entity.Auction;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.entity.TeamSquad;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.AuctionRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.repository.TeamSquadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionReportService {

    private final AuctionRepository auctionRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final TeamSquadRepository teamSquadRepository;

    @Transactional(readOnly = true)
    public AuctionSummaryReport generateAuctionSummary(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction session not found with ID: " + auctionId));

        List<Player> allPlayers = playerRepository.findAll();
        List<Team> allTeams = teamRepository.findAll();

        int totalPlayers = allPlayers.size();
        List<Player> soldPlayers = allPlayers.stream()
                .filter(p -> p.getStatus() == PlayerStatus.SOLD)
                .collect(Collectors.toList());
        int totalSold = soldPlayers.size();
        int totalUnsold = (int) allPlayers.stream().filter(p -> p.getStatus() == PlayerStatus.UNSOLD).count();
        int totalAvailable = (int) allPlayers.stream().filter(p -> p.getStatus() == PlayerStatus.AVAILABLE).count();

        long totalPurseSpentAllTeams = allTeams.stream()
                .mapToLong(t -> t.getTotalPurse() - t.getRemainingPurse())
                .sum();

        Player recordBuyPlayer = soldPlayers.stream()
                .max(Comparator.comparing(p -> p.getCurrentBidPrice() != null ? p.getCurrentBidPrice() : 0L))
                .orElse(null);

        PlayerAuctionSummary recordBuySummary = recordBuyPlayer != null ? mapToSummary(recordBuyPlayer) : null;

        Map<String, Long> categorySpend = new LinkedHashMap<>();
        for (Player p : soldPlayers) {
            String cat = p.getAuctionSetCategory() != null ? p.getAuctionSetCategory() : p.getRole().name();
            long price = p.getCurrentBidPrice() != null ? p.getCurrentBidPrice() : 0L;
            categorySpend.put(cat, categorySpend.getOrDefault(cat, 0L) + price);
        }

        int indianSold = (int) soldPlayers.stream().filter(p -> !Boolean.TRUE.equals(p.getIsOverseas())).count();
        int overseasSold = (int) soldPlayers.stream().filter(p -> Boolean.TRUE.equals(p.getIsOverseas())).count();

        return AuctionSummaryReport.builder()
                .auctionId(auction.getId())
                .auctionTitle(auction.getTitle())
                .year(auction.getYear())
                .totalPlayers(totalPlayers)
                .totalSold(totalSold)
                .totalUnsold(totalUnsold)
                .totalAvailable(totalAvailable)
                .totalPurseSpentAllTeams(totalPurseSpentAllTeams)
                .formattedTotalPurseSpent(formatPrice(totalPurseSpentAllTeams))
                .highestSoldPlayer(recordBuySummary)
                .categorySpend(categorySpend)
                .totalIndianSold(indianSold)
                .totalOverseasSold(overseasSold)
                .build();
    }

    @Transactional(readOnly = true)
    public TeamRosterExport exportTeamRoster(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with ID: " + teamId));

        List<TeamSquad> squadEntries = teamSquadRepository.findByTeamId(teamId);

        List<TeamRosterExport.RosterPlayerDetail> rosterList = squadEntries.stream()
                .map(s -> {
                    Player p = s.getPlayer();
                    return TeamRosterExport.RosterPlayerDetail.builder()
                            .playerId(p.getId())
                            .fullName(p.getFullName())
                            .role(p.getRole())
                            .country(p.getCountry())
                            .overseas(Boolean.TRUE.equals(p.getIsOverseas()))
                            .boughtPrice(s.getSoldPrice())
                            .formattedBoughtPrice(formatPrice(s.getSoldPrice()))
                            .acquiredAt(s.getAcquiredAt())
                            .build();
                })
                .sorted(Comparator.comparing(TeamRosterExport.RosterPlayerDetail::getBoughtPrice).reversed())
                .collect(Collectors.toList());

        long spentPurse = team.getTotalPurse() - team.getRemainingPurse();
        int availableSquadSlots = Math.max(0, team.getMaxSquadSize() - team.getCurrentSquadCount());
        int availableForeignSlots = Math.max(0, team.getMaxForeignPlayers() - team.getCurrentForeignCount());

        return TeamRosterExport.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .shortCode(team.getShortCode())
                .totalPurse(team.getTotalPurse())
                .remainingPurse(team.getRemainingPurse())
                .spentPurse(spentPurse)
                .totalSquadCount(team.getCurrentSquadCount())
                .foreignCount(team.getCurrentForeignCount())
                .availableSquadSlots(availableSquadSlots)
                .availableForeignSlots(availableForeignSlots)
                .players(rosterList)
                .build();
    }

    private PlayerAuctionSummary mapToSummary(Player player) {
        return PlayerAuctionSummary.builder()
                .playerId(player.getId())
                .fullName(player.getFullName())
                .role(player.getRole())
                .country(player.getCountry())
                .overseas(Boolean.TRUE.equals(player.getIsOverseas()))
                .basePrice(player.getBasePrice())
                .formattedBasePrice(formatPrice(player.getBasePrice()))
                .finalSoldPrice(player.getCurrentBidPrice())
                .formattedFinalPrice(formatPrice(player.getCurrentBidPrice()))
                .status(player.getStatus())
                .soldToTeamName(player.getCurrentWinningTeam() != null ? player.getCurrentWinningTeam().getTeamName() : null)
                .soldToTeamCode(player.getCurrentWinningTeam() != null ? player.getCurrentWinningTeam().getShortCode() : null)
                .build();
    }

    private String formatPrice(Long amount) {
        if (amount == null || amount == 0) return "₹0";
        if (amount >= 10000000L) {
            double cr = amount / 10000000.0;
            return String.format("₹ %.2f Crore", cr);
        } else {
            double lk = amount / 100000.0;
            return String.format("₹ %.2f Lakh", lk);
        }
    }
}
