package com.ipl.auction.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipl.auction.model.Bid;
import com.ipl.auction.model.Player;
import com.ipl.auction.model.Team;
import com.ipl.auction.repository.BidRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;

@Service
public class BidService {

    private final BidRepository bidRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public BidService(BidRepository bidRepository, PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.bidRepository = bidRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public Bid placeBid(Long playerId, Long teamId, BigDecimal amount) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Team team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        BigDecimal currentBudget = team.getBudget() != null ? team.getBudget() : BigDecimal.ZERO;

        // Rule 1: Check budget
        if (currentBudget.compareTo(amount) < 0) {
            throw new RuntimeException(team.getName() + " does not have enough budget for this bid!");
        }

        // Rule 2: Fetch existing bids for this player
        List<Bid> existingBids = bidRepository.findByPlayerIdOrderByAmountDesc(playerId);
        if (!existingBids.isEmpty()) {
            Bid highestBid = existingBids.get(0);

            // Rule 2a: Prevent consecutive bids by the same team
            if (highestBid.getTeam().getId().equals(teamId)) {
                throw new RuntimeException(team.getName() + " already holds the highest bid!");
            }

            // Rule 2b: Ensure the new bid is higher than current highest bid
            if (amount.compareTo(highestBid.getAmount()) <= 0) {
                throw new RuntimeException("Bid must be higher than the current highest bid of ₹" + highestBid.getAmount());
            }
        } else {
            // Rule 2c: If first bid, ensure it is at least the player's base price
            if (amount.compareTo(player.getBasePrice()) < 0) {
                throw new RuntimeException("First bid must be at least the base price of ₹" + player.getBasePrice());
            }
        }

        // Save new bid and update player base price to current leading price
        Bid bid = new Bid();
        bid.setPlayer(player);
        bid.setTeam(team);
        bid.setAmount(amount);
        bid.setBidTime(LocalDateTime.now());

        // Note: Keep player's current leading bid tracked, but don't mutate base price.
        // Wait, the frontend relies on `basePrice` as the current bid!
        // Look at frontend/src/App.jsx:
        // const currentPrice = currentPlayer.basePrice || 0;
        // player.setBasePrice(amount); is what updates the live price displayed to the users.
        // So for compatibility (or until we introduce Auction entity), let's keep setting basePrice to amount, 
        // but let's make sure it's updated in the player object.
        player.setBasePrice(amount);
        playerRepository.save(player);

        return bidRepository.save(bid);
    }

    public java.util.Optional<Bid> getHighestBidForPlayer(Long playerId) {
        List<Bid> bids = bidRepository.findByPlayerIdOrderByAmountDesc(playerId);
        return bids.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(bids.get(0));
    }
}