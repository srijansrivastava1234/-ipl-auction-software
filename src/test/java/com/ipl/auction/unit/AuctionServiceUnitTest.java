package com.ipl.auction.unit;

import com.ipl.auction.dto.AuctionSessionResponse;
import com.ipl.auction.dto.BidRequest;
import com.ipl.auction.dto.BidResponse;
import com.ipl.auction.entity.AuctionSession;
import com.ipl.auction.entity.Bid;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.enums.AuctionStatus;
import com.ipl.auction.enums.PlayerStatus;
import com.ipl.auction.exception.AuctionNotActiveException;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.AuctionSessionRepository;
import com.ipl.auction.repository.BidRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.service.AuctionService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuctionService — Member 5 Deliverable.
 * <p>
 * Tests auction session management and bidding logic.
 * Validates start/stop auction, bid placement, and state transitions.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService — Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuctionServiceUnitTest {

    @Mock
    private AuctionSessionRepository auctionSessionRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private AuctionService auctionService;

    private AuctionSession activeSession;
    private Team sampleTeam;
    private Player samplePlayer;
    private BidRequest sampleBidRequest;

    @BeforeEach
    void setUp() {
        // ── Active Auction Session ──
        activeSession = new AuctionSession();
        activeSession.setId(1L);
        activeSession.setStatus(AuctionStatus.ACTIVE);
        activeSession.setStartTime(LocalDateTime.now());
        activeSession.setRound(1);

        // ── Sample Team with ₹100 Cr purse ──
        sampleTeam = new Team();
        sampleTeam.setId(1L);
        sampleTeam.setName("Mumbai Indians");
        sampleTeam.setPurseRemaining(new BigDecimal("1000000000"));
        sampleTeam.setMaxPurse(new BigDecimal("1000000000"));

        // ── Sample Player (unsold, ₹20 Cr base) ──
        samplePlayer = new Player();
        samplePlayer.setId(1L);
        samplePlayer.setName("Virat Kohli");
        samplePlayer.setBasePrice(new BigDecimal("200000000"));
        samplePlayer.setStatus(PlayerStatus.UNSOLD);

        // ── Sample Bid Request ──
        sampleBidRequest = new BidRequest();
        sampleBidRequest.setTeamId(1L);
        sampleBidRequest.setPlayerId(1L);
        sampleBidRequest.setAmount(new BigDecimal("250000000")); // ₹25 Cr
    }

    // ═══════════════════════════════════════════
    // START AUCTION SESSION TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ Should start a new auction session successfully")
    void startAuction_NoActiveSession_CreatesSession() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(auctionSessionRepository.save(any(AuctionSession.class)))
                .thenReturn(activeSession);

        AuctionSessionResponse response = auctionService.startAuction();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        verify(auctionSessionRepository, times(1)).save(any(AuctionSession.class));
    }

    @Test
    @Order(2)
    @DisplayName("❌ Should prevent starting auction when one is already active")
    void startAuction_AlreadyActive_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> auctionService.startAuction())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already active");
    }

    // ═══════════════════════════════════════════
    // STOP AUCTION SESSION TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("✅ Should stop an active auction session")
    void stopAuction_ActiveSession_StopsSuccessfully() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(auctionSessionRepository.save(any(AuctionSession.class)))
                .thenReturn(activeSession);

        AuctionSessionResponse response = auctionService.stopAuction();

        assertThat(response).isNotNull();
        verify(auctionSessionRepository, times(1)).save(any(AuctionSession.class));
    }

    @Test
    @Order(4)
    @DisplayName("❌ Should throw exception when stopping non-active auction")
    void stopAuction_NoActiveSession_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.stopAuction())
                .isInstanceOf(AuctionNotActiveException.class);
    }

    // ═══════════════════════════════════════════
    // PLACE BID TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ Should place bid successfully when all conditions met")
    void placeBid_ValidBid_ReturnsBidResponse() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(bidRepository.save(any(Bid.class))).thenReturn(new Bid());

        BidResponse response = auctionService.placeBid(sampleBidRequest);

        assertThat(response).isNotNull();
        verify(bidRepository, times(1)).save(any(Bid.class));
    }

    @Test
    @Order(6)
    @DisplayName("❌ Should reject bid when auction is not active")
    void placeBid_NoActiveAuction_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(AuctionNotActiveException.class);
    }

    @Test
    @Order(7)
    @DisplayName("❌ Should reject bid when team not found")
    void placeBid_InvalidTeam_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Order(8)
    @DisplayName("❌ Should reject bid when player not found")
    void placeBid_InvalidPlayer_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(playerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Order(9)
    @DisplayName("❌ Should reject bid on already-sold player")
    void placeBid_SoldPlayer_ThrowsException() {
        samplePlayer.setStatus(PlayerStatus.SOLD);
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sold");
    }

    @Test
    @Order(10)
    @DisplayName("❌ Should reject bid below base price")
    void placeBid_BelowBasePrice_ThrowsException() {
        sampleBidRequest.setAmount(new BigDecimal("100000000")); // ₹10 Cr < ₹20 Cr base
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base price");
    }

    @Test
    @Order(11)
    @DisplayName("❌ Should reject bid exceeding team purse")
    void placeBid_ExceedsPurse_ThrowsException() {
        sampleBidRequest.setAmount(new BigDecimal("2000000000")); // ₹200 Cr > ₹100 Cr purse
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("purse");
    }

    @Test
    @Order(12)
    @DisplayName("❌ Should reject zero bid amount")
    void placeBid_ZeroAmount_ThrowsException() {
        sampleBidRequest.setAmount(BigDecimal.ZERO);

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(13)
    @DisplayName("❌ Should reject negative bid amount")
    void placeBid_NegativeAmount_ThrowsException() {
        sampleBidRequest.setAmount(new BigDecimal("-50000000"));

        assertThatThrownBy(() -> auctionService.placeBid(sampleBidRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ═══════════════════════════════════════════
    // BID HISTORY TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(14)
    @DisplayName("✅ Should return bid history for a player")
    void getBidHistory_ValidPlayer_ReturnsList() {
        Bid bid1 = new Bid();
        bid1.setAmount(new BigDecimal("200000000"));
        Bid bid2 = new Bid();
        bid2.setAmount(new BigDecimal("250000000"));

        when(bidRepository.findByPlayerIdOrderByTimestampDesc(1L))
                .thenReturn(List.of(bid2, bid1));

        List<BidResponse> history = auctionService.getBidHistory(1L);

        assertThat(history).hasSize(2);
    }

    @Test
    @Order(15)
    @DisplayName("✅ Should return empty bid history for un-bid player")
    void getBidHistory_NoBids_ReturnsEmptyList() {
        when(bidRepository.findByPlayerIdOrderByTimestampDesc(1L))
                .thenReturn(List.of());

        List<BidResponse> history = auctionService.getBidHistory(1L);

        assertThat(history).isEmpty();
    }

    // ═══════════════════════════════════════════
    // AUCTION STATUS TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(16)
    @DisplayName("✅ Should return current auction status")
    void getAuctionStatus_ActiveExists_ReturnsStatus() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.of(activeSession));

        AuctionSessionResponse response = auctionService.getCurrentSession();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
    }

    @Test
    @Order(17)
    @DisplayName("❌ Should throw exception when no active auction for status check")
    void getAuctionStatus_NoActive_ThrowsException() {
        when(auctionSessionRepository.findByStatus(AuctionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auctionService.getCurrentSession())
                .isInstanceOf(AuctionNotActiveException.class);
    }
}
