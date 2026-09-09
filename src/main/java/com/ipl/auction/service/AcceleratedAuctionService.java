package com.ipl.auction.service;

import com.ipl.auction.dto.request.AcceleratedNominationRequest;
import com.ipl.auction.dto.response.AcceleratedPoolResponse;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.entity.Auction;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.exception.InvalidAuctionStateException;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.AuctionRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcceleratedAuctionService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final AuctionRepository auctionRepository;

    private final Set<Long> nominatedPlayerIds = ConcurrentHashMap.newKeySet();

    @Transactional(readOnly = true)
    public AcceleratedPoolResponse getAcceleratedPool() {
        List<Player> unsoldPlayers = playerRepository.findByStatusIn(
                List.of(PlayerStatus.UNSOLD, PlayerStatus.AVAILABLE));

        List<PlayerAuctionSummary> unsoldSummaries = unsoldPlayers.stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        List<PlayerAuctionSummary> nominatedSummaries = unsoldPlayers.stream()
                .filter(p -> nominatedPlayerIds.contains(p.getId()))
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        return AcceleratedPoolResponse.builder()
                .totalUnsoldPlayers(unsoldSummaries.size())
                .totalNominatedPlayers(nominatedSummaries.size())
                .nominatedPlayers(nominatedSummaries)
                .unsoldPool(unsoldSummaries)
                .build();
    }

    @Transactional
    public AcceleratedPoolResponse nominatePlayers(AcceleratedNominationRequest request) {
        teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with ID: " + request.getTeamId()));

        List<Player> players = playerRepository.findAllById(request.getPlayerIds());
        for (Player p : players) {
            if (p.getStatus() == PlayerStatus.SOLD) {
                throw new InvalidAuctionStateException("Cannot nominate cricketer " + p.getFullName() +
                        " because they have already been SOLD.");
            }
            nominatedPlayerIds.add(p.getId());
        }

        log.info("Franchise {} nominated {} cricketers for the Accelerated Round",
                request.getTeamId(), request.getPlayerIds().size());

        return getAcceleratedPool();
    }

    @Transactional
    public PlayerAuctionSummary stageAcceleratedPlayer(Long auctionId, Long playerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction session not found with ID: " + auctionId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        if (player.getStatus() == PlayerStatus.SOLD) {
            throw new InvalidAuctionStateException("Player " + player.getFullName() + " is already SOLD");
        }

        player.setStatus(PlayerStatus.IN_AUCTION);
        player.setCurrentBidPrice(0L);
        player.setCurrentWinningTeam(null);
        playerRepository.save(player);

        auction.setCurrentPlayer(player);
        auctionRepository.save(auction);

        nominatedPlayerIds.remove(playerId);

        log.info("Accelerated player {} brought to podium for auction {}", player.getFullName(), auction.getTitle());
        return mapToSummary(player);
    }

    public void clearNominations() {
        nominatedPlayerIds.clear();
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
