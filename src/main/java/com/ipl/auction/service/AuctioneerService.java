package com.ipl.auction.service;

import com.ipl.auction.dto.response.BidResponse;
import com.ipl.auction.dto.response.PlayerAuctionSummary;
import com.ipl.auction.entity.*;
import com.ipl.auction.entity.enums.BidStatus;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.entity.enums.TransactionType;
import com.ipl.auction.exception.InvalidBidException;
import com.ipl.auction.exception.PlayerNotAvailableException;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctioneerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final TeamSquadRepository teamSquadRepository;
    private final WalletAuditLogRepository walletAuditLogRepository;

    /**
     * Brings an available player to the active auction stage.
     */
    @Transactional
    public PlayerAuctionSummary bringPlayerToStage(Long auctionId, Long playerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found with ID: " + auctionId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        if (player.getStatus() != PlayerStatus.AVAILABLE && player.getStatus() != PlayerStatus.UNSOLD) {
            throw new PlayerNotAvailableException("Player " + player.getFullName() +
                    " cannot be brought to stage. Current status: " + player.getStatus());
        }

        player.setStatus(PlayerStatus.IN_AUCTION);
        player.setCurrentBidPrice(0L);
        player.setCurrentWinningTeam(null);
        playerRepository.save(player);

        auction.setCurrentPlayer(player);
        auctionRepository.save(auction);

        log.info("Player {} brought to stage for Auction {}", player.getFullName(), auction.getTitle());

        return mapToSummary(player, null);
    }

    /**
     * Final Hammer Strike: Sold!
     * Atomically deducts purse from winning team, adds player to team squad roster,
     * logs financial audit entry, and sets player status to SOLD.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PlayerAuctionSummary strikeHammerSold(Long auctionId, Long playerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found with ID: " + auctionId));

        Player player = playerRepository.findByIdWithPessimisticLock(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        if (player.getStatus() != PlayerStatus.IN_AUCTION) {
            throw new PlayerNotAvailableException("Cannot sell player. Player " + player.getFullName() +
                    " is currently " + player.getStatus() + ", not active in auction.");
        }

        if (player.getCurrentWinningTeam() == null || player.getCurrentBidPrice() == null || player.getCurrentBidPrice() == 0L) {
            throw new InvalidBidException("Cannot sell player " + player.getFullName() + " because no valid bids have been placed.");
        }

        Team winningTeam = teamRepository.findByIdWithPessimisticLock(player.getCurrentWinningTeam().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Winning team not found"));

        Long finalPrice = player.getCurrentBidPrice();
        Long purseBefore = winningTeam.getRemainingPurse();

        // 1. Deduct Team Purse & Update Squad Counts
        winningTeam.deductPurse(finalPrice);
        winningTeam.setCurrentSquadCount(winningTeam.getCurrentSquadCount() + 1);
        if (Boolean.TRUE.equals(player.getIsOverseas())) {
            winningTeam.setCurrentForeignCount(winningTeam.getCurrentForeignCount() + 1);
        }
        teamRepository.save(winningTeam);

        // 2. Record Financial Audit Trail
        WalletAuditLog auditLog = WalletAuditLog.builder()
                .team(winningTeam)
                .transactionType(TransactionType.PURSE_FINAL_DEDUCTION)
                .amount(finalPrice)
                .balanceBefore(purseBefore)
                .balanceAfter(winningTeam.getRemainingPurse())
                .referencePlayer(player)
                .description("Acquisition of " + player.getFullName() + " in " + auction.getTitle())
                .createdAt(LocalDateTime.now())
                .build();
        walletAuditLogRepository.save(auditLog);

        // 3. Add to Team Squad Roster
        TeamSquad teamSquad = TeamSquad.builder()
                .team(winningTeam)
                .player(player)
                .soldPrice(finalPrice)
                .auction(auction)
                .acquiredAt(LocalDateTime.now())
                .build();
        teamSquadRepository.save(teamSquad);

        // 4. Mark Winning Bid Status
        bidRepository.findTopByPlayerIdOrderByBidAmountDesc(player.getId()).ifPresent(topBid -> {
            topBid.setBidStatus(BidStatus.WINNING_BID);
            bidRepository.save(topBid);
        });

        // 5. Update Player & Auction Stage
        player.setStatus(PlayerStatus.SOLD);
        playerRepository.save(player);

        auction.setCurrentPlayer(null);
        auctionRepository.save(auction);

        log.info("HAMMER STRUCK: SOLD! Player {} acquired by {} for {}",
                player.getFullName(), winningTeam.getTeamName(), BidResponse.formatCurrency(finalPrice));

        return mapToSummary(player, winningTeam);
    }

    /**
     * Passes a player as UNSOLD when no bids are placed.
     */
    @Transactional
    public PlayerAuctionSummary passPlayerUnsold(Long auctionId, Long playerId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction not found with ID: " + auctionId));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + playerId));

        if (player.getStatus() != PlayerStatus.IN_AUCTION) {
            throw new PlayerNotAvailableException("Player " + player.getFullName() +
                    " is not in auction floor. Current status: " + player.getStatus());
        }

        player.setStatus(PlayerStatus.UNSOLD);
        player.setCurrentBidPrice(0L);
        player.setCurrentWinningTeam(null);
        playerRepository.save(player);

        auction.setCurrentPlayer(null);
        auctionRepository.save(auction);

        log.info("Player {} passed as UNSOLD in Auction {}", player.getFullName(), auction.getTitle());

        return mapToSummary(player, null);
    }

    @Transactional
    public PlayerAuctionSummary strikeHammerUnsold(Long auctionId, Long playerId) {
        return passPlayerUnsold(auctionId, playerId);
    }

    private PlayerAuctionSummary mapToSummary(Player player, Team team) {
        return PlayerAuctionSummary.builder()
                .playerId(player.getId())
                .fullName(player.getFullName())
                .role(player.getRole())
                .country(player.getCountry())
                .overseas(Boolean.TRUE.equals(player.getIsOverseas()))
                .basePrice(player.getBasePrice())
                .formattedBasePrice(BidResponse.formatCurrency(player.getBasePrice()))
                .finalSoldPrice(player.getCurrentBidPrice())
                .formattedFinalPrice(BidResponse.formatCurrency(player.getCurrentBidPrice()))
                .status(player.getStatus())
                .soldToTeamName(team != null ? team.getTeamName() : (player.getCurrentWinningTeam() != null ? player.getCurrentWinningTeam().getTeamName() : null))
                .soldToTeamCode(team != null ? team.getShortCode() : (player.getCurrentWinningTeam() != null ? player.getCurrentWinningTeam().getShortCode() : null))
                .build();
    }
}
