package com.ipl.auction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "highest_bidder_team_id")
    private Team highestBidder;

    private BigDecimal currentBid;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status = AuctionStatus.LIVE;

    // Concurrency control for live bidding
    @Version
    private Long version;

    public enum AuctionStatus {
        UPCOMING, LIVE, COMPLETED
    }

    public Auction() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public Team getHighestBidder() { return highestBidder; }
    public void setHighestBidder(Team highestBidder) { this.highestBidder = highestBidder; }

    public BigDecimal getCurrentBid() { return currentBid; }
    public void setCurrentBid(BigDecimal currentBid) { this.currentBid = currentBid; }

    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }

    public Long getVersion() { return version; }
}