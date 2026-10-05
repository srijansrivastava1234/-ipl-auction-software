package com.ipl.auction.service;

import com.ipl.auction.model.Player;
import com.ipl.auction.model.Team;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player player;
    private Team team;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setName("Jasprit Bumrah");
        player.setBasePrice(new BigDecimal("20000000"));
        player.setStatus(Player.PlayerStatus.UNSOLD);

        team = new Team();
        team.setId(5L);
        team.setName("Mumbai Indians");
        team.setBudget(new BigDecimal("1000000000")); // 100 Cr
    }

    @Test
    void testSellPlayer_Success() {
        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(team));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal finalPrice = new BigDecimal("80000000"); // 8 Cr
        Player result = playerService.sellPlayer(1L, 5L, finalPrice);

        assertNotNull(result);
        assertEquals(Player.PlayerStatus.SOLD, result.getStatus());
        assertEquals(team, result.getTeam());
        assertEquals(finalPrice, result.getBasePrice());
        
        // Budget deduction check: 100 Cr - 8 Cr = 92 Cr
        BigDecimal expectedBudget = new BigDecimal("920000000");
        assertEquals(expectedBudget, team.getBudget());

        verify(teamRepository, times(1)).save(team);
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void testSellPlayer_InsufficientBudget() {
        team.setBudget(new BigDecimal("50000000")); // 5 Cr

        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(team));

        BigDecimal finalPrice = new BigDecimal("80000000"); // 8 Cr (Exceeds 5 Cr budget)

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.sellPlayer(1L, 5L, finalPrice);
        });

        assertTrue(exception.getMessage().contains("does not have sufficient budget"));
        verify(teamRepository, never()).save(any(Team.class));
        verify(playerRepository, never()).save(any(Player.class));
    }
}
