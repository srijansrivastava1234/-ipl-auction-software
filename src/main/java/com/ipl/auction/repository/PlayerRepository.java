package com.ipl.auction.repository;

import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.enums.PlayerStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /**
     * Pessimistic Write Lock ensures exclusive row access during active bidding
     * so simultaneous bids on the same player block and execute sequentially.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Player p WHERE p.id = :id")
    Optional<Player> findByIdWithPessimisticLock(@Param("id") Long id);

    List<Player> findByStatus(PlayerStatus status);

    List<Player> findByAuctionSetCategory(String category);

    List<Player> findByStatusOrderByBasePriceDesc(PlayerStatus status);

    List<Player> findByStatusIn(List<PlayerStatus> statuses);

    @Query("SELECT DISTINCT p.auctionSetCategory FROM Player p WHERE p.auctionSetCategory IS NOT NULL ORDER BY p.auctionSetCategory ASC")
    List<String> findDistinctAuctionSetCategories();
}
