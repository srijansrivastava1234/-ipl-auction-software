package com.ipl.auction.service;

import com.ipl.auction.dto.request.AcceleratedNominationRequest;
import com.ipl.auction.dto.request.RtmExerciseRequest;
import com.ipl.auction.dto.response.*;
import com.ipl.auction.entity.Auction;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.entity.enums.AuctionStatus;
import com.ipl.auction.entity.enums.PlayerRole;
import com.ipl.auction.entity.enums.PlayerStatus;
import com.ipl.auction.exception.InvalidAuctionStateException;
import com.ipl.auction.exception.RtmNotAvailableException;
import com.ipl.auction.repository.AuctionRepository;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class Week10ServicesTest {

    @Autowired
    private RtmService rtmService;

    @Autowired
    private AcceleratedAuctionService acceleratedAuctionService;

    @Autowired
    private ComplianceAuditService complianceAuditService;

    @Autowired
    private AuctionReportService auctionReportService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    private Team csk;
    private Team mi;
    private Auction auction;
    private Player livePlayer;

    @BeforeEach
    void setUp() {
        rtmService.clearRtmUsage();
        acceleratedAuctionService.clearNominations();

        csk = teamRepository.findByShortCode("CSK")
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Chennai Super Kings")
                        .shortCode("CSK")
                        .totalPurse(1000000000L)
                        .remainingPurse(1000000000L)
                        .maxSquadSize(25)
                        .minSquadSize(18)
                        .maxForeignPlayers(8)
                        .build()));

        mi = teamRepository.findByShortCode("MI")
                .orElseGet(() -> teamRepository.save(Team.builder()
                        .teamName("Mumbai Indians")
                        .shortCode("MI")
                        .totalPurse(1000000000L)
                        .remainingPurse(1000000000L)
                        .maxSquadSize(25)
                        .minSquadSize(18)
                        .maxForeignPlayers(8)
                        .build()));

        auction = auctionRepository.findAll().stream().findFirst()
                .orElseGet(() -> auctionRepository.save(Auction.builder()
                        .title("IPL 2025 Mega Auction")
                        .year(2025)
                        .status(AuctionStatus.LIVE)
                        .build()));

        livePlayer = playerRepository.save(Player.builder()
                .fullName("Ruturaj Gaikwad")
                .role(PlayerRole.BATSMAN)
                .country("India")
                .isOverseas(false)
                .age(27)
                .basePrice(20000000L) // ₹2 Crore
                .currentBidPrice(50000000L) // Winning bid: ₹5 Crore
                .currentWinningTeam(mi)
                .status(PlayerStatus.IN_AUCTION)
                .auctionSetCategory("BATSMAN_SET_1")
                .build());
    }

    @Test
    @DisplayName("Week 10: RTM Card summary returns 2 initial cards for franchise")
    void testGetTeamRtmSummary() {
        TeamRtmSummary summary = rtmService.getTeamRtmSummary(csk.getId());
        assertNotNull(summary);
        assertEquals(csk.getId(), summary.getTeamId());
        assertEquals("CSK", summary.getShortCode());
        assertEquals(2, summary.getRtmCardsTotal());
        assertEquals(0, summary.getRtmCardsUsed());
        assertEquals(2, summary.getRtmCardsRemaining());
        assertTrue(summary.isCanExerciseRtm());
    }

    @Test
    @DisplayName("Week 10: Successful Right to Match exercise reallocates cricketer to matching team")
    void testExerciseRtm_Success() {
        RtmExerciseRequest request = RtmExerciseRequest.builder()
                .teamId(csk.getId())
                .playerId(livePlayer.getId())
                .auctionId(auction.getId())
                .build();

        PlayerAuctionSummary result = rtmService.exerciseRtm(request);

        assertNotNull(result);
        assertEquals("Ruturaj Gaikwad", result.getFullName());
        assertEquals(PlayerStatus.SOLD, result.getStatus());
        assertEquals("CSK", result.getSoldToTeamCode());
        assertEquals(50000000L, result.getFinalSoldPrice());

        // Verify RTM card usage
        TeamRtmSummary rtmSummary = rtmService.getTeamRtmSummary(csk.getId());
        assertEquals(1, rtmSummary.getRtmCardsUsed());
        assertEquals(1, rtmSummary.getRtmCardsRemaining());

        // Verify purse deduction
        Team updatedCsk = teamRepository.findById(csk.getId()).orElseThrow();
        assertEquals(950000000L, updatedCsk.getRemainingPurse());
        assertEquals(1, updatedCsk.getCurrentSquadCount());
    }

    @Test
    @DisplayName("Week 10: RTM exercise fails when franchise attempts to RTM own winning bid")
    void testExerciseRtm_SelfOutbiddingRejection() {
        // MI is currently winning team, attempting RTM
        RtmExerciseRequest request = RtmExerciseRequest.builder()
                .teamId(mi.getId())
                .playerId(livePlayer.getId())
                .auctionId(auction.getId())
                .build();

        assertThrows(RtmNotAvailableException.class, () -> rtmService.exerciseRtm(request));
    }

    @Test
    @DisplayName("Week 10: Accelerated round player nomination and pool retrieval")
    void testAcceleratedPoolAndNomination() {
        Player unsoldPlayer = playerRepository.save(Player.builder()
                .fullName("Unsold Pacer")
                .role(PlayerRole.BOWLER)
                .country("India")
                .isOverseas(false)
                .basePrice(5000000L)
                .status(PlayerStatus.UNSOLD)
                .build());

        AcceleratedNominationRequest nomination = AcceleratedNominationRequest.builder()
                .teamId(csk.getId())
                .playerIds(List.of(unsoldPlayer.getId()))
                .roundName("Accelerated Set 1")
                .build();

        AcceleratedPoolResponse poolResponse = acceleratedAuctionService.nominatePlayers(nomination);
        assertNotNull(poolResponse);
        assertTrue(poolResponse.getTotalNominatedPlayers() >= 1);
        assertTrue(poolResponse.getNominatedPlayers().stream()
                .anyMatch(p -> p.getPlayerId().equals(unsoldPlayer.getId())));
    }

    @Test
    @DisplayName("Week 10: Staging nominated player sets status to IN_AUCTION")
    void testStageAcceleratedPlayer() {
        Player unsoldPlayer = playerRepository.save(Player.builder()
                .fullName("Accelerated Spinner")
                .role(PlayerRole.BOWLER)
                .country("India")
                .isOverseas(false)
                .basePrice(5000000L)
                .status(PlayerStatus.UNSOLD)
                .build());

        acceleratedAuctionService.nominatePlayers(AcceleratedNominationRequest.builder()
                .teamId(csk.getId())
                .playerIds(List.of(unsoldPlayer.getId()))
                .build());

        PlayerAuctionSummary staged = acceleratedAuctionService.stageAcceleratedPlayer(auction.getId(), unsoldPlayer.getId());
        assertNotNull(staged);
        assertEquals(PlayerStatus.IN_AUCTION, staged.getStatus());
        assertEquals("Accelerated Spinner", staged.getFullName());
    }

    @Test
    @DisplayName("Week 10: Staging already SOLD player in accelerated round throws exception")
    void testStageAcceleratedPlayer_AlreadySold() {
        Player soldPlayer = playerRepository.save(Player.builder()
                .fullName("Sold Star")
                .role(PlayerRole.BATSMAN)
                .country("India")
                .isOverseas(false)
                .basePrice(20000000L)
                .status(PlayerStatus.SOLD)
                .build());

        assertThrows(InvalidAuctionStateException.class, () ->
                acceleratedAuctionService.stageAcceleratedPlayer(auction.getId(), soldPlayer.getId()));
    }

    @Test
    @DisplayName("Week 10: BCCI compliance audit detects empty squad violations")
    void testComplianceAudit_EmptySquad() {
        TeamComplianceReport report = complianceAuditService.auditTeamCompliance(csk.getId());
        assertNotNull(report);
        assertEquals(csk.getId(), report.getTeamId());
        assertEquals("CSK", report.getShortCode());
        assertFalse(report.isSquadSizeCompliant()); // 0 < 18
        assertFalse(report.isPurseSpendCompliant()); // 0% < 75%
        assertFalse(report.isOverallCompliant());
        assertFalse(report.getViolations().isEmpty());
    }

    @Test
    @DisplayName("Week 10: Macro Auction Summary report aggregates platform spend and record buys")
    void testGenerateAuctionSummary() {
        AuctionSummaryReport report = auctionReportService.generateAuctionSummary(auction.getId());
        assertNotNull(report);
        assertEquals(auction.getId(), report.getAuctionId());
        assertEquals(2025, report.getYear());
        assertTrue(report.getTotalPlayers() > 0);
        assertNotNull(report.getCategorySpend());
    }

    @Test
    @DisplayName("Week 10: Franchise squad roster export contains squad metrics and slots")
    void testExportTeamRoster() {
        TeamRosterExport export = auctionReportService.exportTeamRoster(csk.getId());
        assertNotNull(export);
        assertEquals(csk.getId(), export.getTeamId());
        assertEquals(25, export.getAvailableSquadSlots());
        assertEquals(8, export.getAvailableForeignSlots());
        assertNotNull(export.getPlayers());
    }
}
