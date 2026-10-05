package com.ipl.auction.service;

import com.ipl.auction.model.Bid;
import com.ipl.auction.model.Player;
import com.ipl.auction.model.Team;
import com.ipl.auction.repository.BidRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidServiceTest {

    @Mock
    private BidRepository bidRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private BidService bidService;

    private Player player;
    private Team teamcsk;
    private Team teammi;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId(1L);
        player.setName("Virat Kohli");
        player.setBasePrice(new BigDecimal("20000000")); // 2 Cr
        player.setStatus(Player.PlayerStatus.UNSOLD);

        teamcsk = new Team();
        teamcsk.setId(10L);
        teamcsk.setName("Chennai Super Kings");
        teamcsk.setBudget(new BigDecimal("1000000000")); // 100 Cr

        teammi = new Team();
        teammi.setId(20L);
        teammi.setName("Mumbai Indians");
        teammi.setBudget(new BigDecimal("1000000000")); // 100 Cr
    }

    @Test
    void testPlaceBid_Success() {
        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(teamcsk));
        when(bidRepository.findByPlayerIdOrderByAmountDesc(1L)).thenReturn(Collections.emptyList());
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal bidAmount = new BigDecimal("22000000"); // 2.2 Cr
        Bid result = bidService.placeBid(1L, 10L, bidAmount);

        assertNotNull(result);
        assertEquals(bidAmount, result.getAmount());
        assertEquals(teamcsk, result.getTeam());
        assertEquals(player, result.getPlayer());
        assertEquals(bidAmount, player.getBasePrice()); // Player base price updated to leading bid

        verify(playerRepository, times(1)).save(player);
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_InsufficientBudget() {
        teamcsk.setBudget(new BigDecimal("10000000")); // Only 1 Cr

        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(teamcsk));

        BigDecimal bidAmount = new BigDecimal("22000000"); // 2.2 Cr

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bidService.placeBid(1L, 10L, bidAmount);
        });

        assertTrue(exception.getMessage().contains("does not have enough budget"));
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_ConsecutiveBiddingFromSameTeam() {
        Bid existingBid = new Bid();
        existingBid.setId(100L);
        existingBid.setPlayer(player);
        existingBid.setTeam(teamcsk);
        existingBid.setAmount(new BigDecimal("21000000"));

        List<Bid> bids = new ArrayList<>();
        bids.add(existingBid);

        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(teamcsk));
        when(bidRepository.findByPlayerIdOrderByAmountDesc(1L)).thenReturn(bids);

        BigDecimal bidAmount = new BigDecimal("22000000");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bidService.placeBid(1L, 10L, bidAmount);
        });

        assertTrue(exception.getMessage().contains("already holds the highest bid"));
        verify(bidRepository, never()).save(any(Bid.class));
    }

    @Test
    void testPlaceBid_BelowCurrentHighestBid() {
        Bid existingBid = new Bid();
        existingBid.setId(100L);
        existingBid.setPlayer(player);
        existingBid.setTeam(teamcsk);
        existingBid.setAmount(new BigDecimal("25000000")); // 2.5 Cr

        List<Bid> bids = new ArrayList<>();
        bids.add(existingBid);

        when(playerRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(player));
        when(teamRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(teammi)); // MI trying to bid
        when(bidRepository.findByPlayerIdOrderByAmountDesc(1L)).thenReturn(bids);

        BigDecimal bidAmount = new BigDecimal("24000000"); // 2.4 Cr (Lower than 2.5 Cr)

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bidService.placeBid(1L, 20L, bidAmount);
        });

        assertTrue(exception.getMessage().contains("must be higher than the current highest bid"));
        verify(bidRepository, never()).save(any(Bid.class));
    }
}
