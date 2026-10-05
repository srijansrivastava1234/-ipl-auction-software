package com.ipl.auction.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipl.auction.model.Auction;
import com.ipl.auction.model.Auction.AuctionStatus;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    Optional<Auction> findByStatus(AuctionStatus status);
}