package com.ipl.auction.service;

import com.ipl.auction.dto.request.RtmExerciseRequest;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.dto.response.TeamRtmSummary;
import com.ipl.auction.entity.*;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.entity.enums.TransactionType;
import com.ipl.auction.exception.*;
import com.ipl.auction.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RtmService {

    private static final int MAX_RTM_CARDS_PER_FRANCHISE = 2;

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final AuctionRepository auctionRepository;
    private final TeamSquadRepository teamSquadRepository;
    private final WalletAuditLogRepository walletAuditLogRepository;

    private final Map<Long, Integer> rtmUsageMap = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public TeamRtmSummary getTeamRtmSummary(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with ID: " + teamId));

        int used = rtmUsageMap.getOrDefault(teamId, 0);
        int remaining = Math.max(0, MAX_RTM_CARDS_PER_FRANCHISE - used);

        return TeamRtmSummary.builder()
                .teamId(team.getId())
                .teamName(team.getTeamName())
                .shortCode(team.getShortCode())
                .rtmCardsTotal(MAX_RTM_CARDS_PER_FRANCHISE)
                .rtmCardsUsed(used)
                .rtmCardsRemaining(remaining)
                .canExerciseRtm(remaining > 0)
                .build();
    }

    public void clearRtmUsage() {
        rtmUsageMap.clear();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerAuctionSummary exerciseRtm(RtmExerciseRequest request) {
        Long teamId = request.getTeamId();
        Long playerId = request.getPlayerId();
        Long auctionId = request.getAuctionId();

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction session not found with ID: " + auctionId));

        Team rtmTeam = teamRepository.findByIdWithPessimisticLock(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Franchise not found with ID: " + teamId));

        Player player = playerRepository.findByIdWithPessimisticLock(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        int usedCards = rtmUsageMap.getOrDefault(teamId, 0);
        if (usedCards >= MAX_RTM_CARDS_PER_FRANCHISE) {
            throw new RtmNotAvailableException("Franchise " + rtmTeam.getShortCode() +
                    " has already exhausted all " + MAX_RTM_CARDS_PER_FRANCHISE + " Right to Match (RTM) cards.");
        }

        if (player.getCurrentWinningTeam() == null || player.getCurrentBidPrice() == null || player.getCurrentBidPrice() <= 0L) {
            throw new InvalidBidException("Cannot exercise RTM: No valid winning bid has been placed for player " + player.getFullName());
        }

        if (player.getCurrentWinningTeam().getId().equals(teamId)) {
            throw new RtmNotAvailableException("Franchise " + rtmTeam.getShortCode() +
                    " already holds the winning bid. RTM is not required.");
        }

        Long matchPrice = player.getCurrentBidPrice();

        // Validate squad capacity
        if (rtmTeam.getCurrentSquadCount() >= rtmTeam.getMaxSquadSize()) {
            throw new SquadLimitExceededException("Franchise " + rtmTeam.getShortCode() +
                    " has reached maximum squad capacity of " + rtmTeam.getMaxSquadSize());
        }

        // Validate foreign player capacity
        if (Boolean.TRUE.equals(player.getIsOverseas()) && rtmTeam.getCurrentForeignCount() >= rtmTeam.getMaxForeignPlayers()) {
            throw new SquadLimitExceededException("Franchise " + rtmTeam.getShortCode() +
                    " has reached maximum foreign player quota of " + rtmTeam.getMaxForeignPlayers());
        }

        // Validate purse with minimum reserve
        int slotsNeeded = Math.max(0, rtmTeam.getMinSquadSize() - (rtmTeam.getCurrentSquadCount() + 1));
        long minPurseReserve = slotsNeeded * 2000000L; // ₹20 Lakhs per player
        if (rtmTeam.getRemainingPurse() < matchPrice + minPurseReserve) {
            throw new InsufficientPurseException("Franchise " + rtmTeam.getShortCode() +
                    " cannot match bid of ₹" + (matchPrice / 10000000.0) + " Cr. Required purse reserve: ₹" +
                    (minPurseReserve / 100000.0) + " Lakhs.");
        }

        // 1. Revert previous winning team acquisition if already assigned in squad
        Optional<TeamSquad> existingSquadOpt = teamSquadRepository.findByPlayerId(player.getId());
        if (existingSquadOpt.isPresent()) {
            TeamSquad prevSquad = existingSquadOpt.get();
            Team prevTeam = prevSquad.getTeam();
            long refundPrice = prevSquad.getSoldPrice();
            long prevPurseBefore = prevTeam.getRemainingPurse();

            prevTeam.setRemainingPurse(prevPurseBefore + refundPrice);
            prevTeam.setCurrentSquadCount(Math.max(0, prevTeam.getCurrentSquadCount() - 1));
            if (Boolean.TRUE.equals(player.getIsOverseas())) {
                prevTeam.setCurrentForeignCount(Math.max(0, prevTeam.getCurrentForeignCount() - 1));
            }
            teamRepository.save(prevTeam);
            teamSquadRepository.delete(prevSquad);

            WalletAuditLog refundAudit = WalletAuditLog.builder()
                    .team(prevTeam)
                    .transactionType(TransactionType.PURSE_CREDIT)
                    .amount(refundPrice)
                    .balanceBefore(prevPurseBefore)
                    .balanceAfter(prevTeam.getRemainingPurse())
                    .referencePlayer(player)
                    .description("Purse refunded: Player " + player.getFullName() + " claimed by " + rtmTeam.getShortCode() + " via RTM")
                    .build();
            walletAuditLogRepository.save(refundAudit);
        }

        // 2. Deduct RTM Team Purse & update roster counts
        long rtmPurseBefore = rtmTeam.getRemainingPurse();
        rtmTeam.deductPurse(matchPrice);
        rtmTeam.setCurrentSquadCount(rtmTeam.getCurrentSquadCount() + 1);
        if (Boolean.TRUE.equals(player.getIsOverseas())) {
            rtmTeam.setCurrentForeignCount(rtmTeam.getCurrentForeignCount() + 1);
        }
        teamRepository.save(rtmTeam);

        // 3. Increment RTM card usage
        rtmUsageMap.put(teamId, usedCards + 1);

        // 4. Update Player state
        player.setStatus(PlayerStatus.SOLD);
        player.setCurrentWinningTeam(rtmTeam);
        playerRepository.save(player);

        // 5. Add to RTM team squad
        TeamSquad newSquad = TeamSquad.builder()
                .team(rtmTeam)
                .player(player)
                .soldPrice(matchPrice)
                .auction(auction)
                .build();
        teamSquadRepository.save(newSquad);

        // 6. Record financial audit log
        WalletAuditLog rtmAudit = WalletAuditLog.builder()
                .team(rtmTeam)
                .transactionType(TransactionType.PURSE_FINAL_DEDUCTION)
                .amount(matchPrice)
                .balanceBefore(rtmPurseBefore)
                .balanceAfter(rtmTeam.getRemainingPurse())
                .referencePlayer(player)
                .description("Player " + player.getFullName() + " acquired via Right to Match (RTM)")
                .build();
        walletAuditLogRepository.save(rtmAudit);

        log.info("RTM exercised successfully: Team {} acquired {} for INR {}",
                rtmTeam.getShortCode(), player.getFullName(), matchPrice);

        return mapToSummary(player);
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
