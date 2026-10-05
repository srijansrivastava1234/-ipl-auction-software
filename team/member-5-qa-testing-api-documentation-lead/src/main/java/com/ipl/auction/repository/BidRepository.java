package com.ipl.auction.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ipl.auction.model.Bid;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByPlayerIdOrderByAmountDesc(Long playerId);
}