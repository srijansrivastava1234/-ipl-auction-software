package com.ipl.auction.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import com.ipl.auction.model.Player;
import com.ipl.auction.model.Player.PlayerStatus;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByStatus(PlayerStatus status);
    List<Player> findByRole(String role);
    long countByTeamId(Long teamId);
    long countByTeamIdAndOverseasTrue(Long teamId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Player p where p.id = :id")
    Optional<Player> findByIdForUpdate(@Param("id") Long id);
}