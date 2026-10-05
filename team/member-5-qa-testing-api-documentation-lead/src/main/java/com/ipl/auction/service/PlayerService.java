package com.ipl.auction.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipl.auction.model.Player;
import com.ipl.auction.model.Player.PlayerStatus;
import com.ipl.auction.model.Team;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    public PlayerService(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public java.util.Optional<Player> getPlayerById(Long id) {
        return playerRepository.findById(id);
    }

    @Transactional
    public Player createPlayer(Player player) {
        if (player.getStatus() == null) {
            player.setStatus(PlayerStatus.UNSOLD);
        }
        return playerRepository.save(player);
    }

    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        player.setName(playerDetails.getName());
        player.setRole(playerDetails.getRole());
        player.setBasePrice(playerDetails.getBasePrice());
        player.setStatus(playerDetails.getStatus());
        player.setCountry(playerDetails.getCountry());
        player.setOverseas(playerDetails.isOverseas());
        if (playerDetails.getTeam() != null) {
            player.setTeam(playerDetails.getTeam());
        }
        return playerRepository.save(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        playerRepository.delete(player);
    }

    @Transactional
    public Player sellPlayer(Long playerId, Long teamId, BigDecimal finalPrice) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Team team = teamRepository.findByIdForUpdate(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        BigDecimal teamBudget = team.getBudget() != null ? team.getBudget() : BigDecimal.ZERO;

        if (teamBudget.compareTo(finalPrice) < 0) {
            throw new RuntimeException("Team does not have sufficient budget!");
        }

        // Verify squad limit (maximum 25 players allowed per franchise roster)
        long currentSquadSize = playerRepository.countByTeamId(teamId);
        if (currentSquadSize >= 25) {
            throw new RuntimeException("Team has already reached the maximum squad limit of 25 players!");
        }

        // Verify overseas quota (maximum 8 overseas players allowed per franchise roster if the player is overseas)
        if (player.isOverseas()) {
            long overseasCount = playerRepository.countByTeamIdAndOverseasTrue(teamId);
            if (overseasCount >= 8) {
                throw new RuntimeException("Team has already reached the maximum limit of 8 overseas players!");
            }
        }

        // Deduct budget
        team.setBudget(teamBudget.subtract(finalPrice));
        teamRepository.save(team);

        // Assign player to team & mark SOLD using enum
        player.setTeam(team);
        player.setStatus(PlayerStatus.SOLD);
        player.setBasePrice(finalPrice);

        return playerRepository.save(player);
    }

    @Transactional
    public Player markUnsold(Long playerId) {
        Player player = playerRepository.findByIdForUpdate(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        
        // If it was previously sold, refund the team's budget
        if (player.getStatus() == PlayerStatus.SOLD && player.getTeam() != null) {
            Team team = teamRepository.findByIdForUpdate(player.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            team.setBudget(team.getBudget().add(player.getBasePrice()));
            teamRepository.save(team);
        }

        player.setTeam(null);
        player.setStatus(PlayerStatus.UNSOLD);
        player.setBasePrice(player.getOriginalBasePrice());

        return playerRepository.save(player);
    }
}