package com.ipl.auction.unit;

import com.ipl.auction.dto.TeamRequest;
import com.ipl.auction.dto.TeamResponse;
import com.ipl.auction.entity.Team;
import com.ipl.auction.exception.DuplicateResourceException;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.service.TeamService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TeamService — Member 5 Deliverable.
 * <p>
 * Tests the team CRUD service layer using Mockito to mock the repository.
 * Does NOT test the actual business logic implementation (Member 1's scope).
 * Tests validate the contract/behavior of the service interface.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService — Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamServiceUnitTest {

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private TeamService teamService;

    private Team sampleTeam;
    private TeamRequest sampleRequest;

    @BeforeEach
    void setUp() {
        // ── Arrange: Sample team entity ──
        sampleTeam = new Team();
        sampleTeam.setId(1L);
        sampleTeam.setName("Mumbai Indians");
        sampleTeam.setCity("Mumbai");
        sampleTeam.setOwner("Nita Ambani");
        sampleTeam.setLogoUrl("https://example.com/mi-logo.png");
        sampleTeam.setPurseRemaining(new BigDecimal("1000000000")); // ₹100 Cr
        sampleTeam.setMaxPurse(new BigDecimal("1000000000"));

        // ── Arrange: Sample request DTO ──
        sampleRequest = new TeamRequest();
        sampleRequest.setName("Mumbai Indians");
        sampleRequest.setCity("Mumbai");
        sampleRequest.setOwner("Nita Ambani");
        sampleRequest.setLogoUrl("https://example.com/mi-logo.png");
        sampleRequest.setMaxPurse(new BigDecimal("1000000000"));
    }

    // ═══════════════════════════════════════════
    // CREATE TEAM TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ Should create a new team successfully")
    void createTeam_ValidRequest_ReturnsTeamResponse() {
        // Arrange
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(sampleTeam);

        // Act
        TeamResponse response = teamService.createTeam(sampleRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Mumbai Indians");
        assertThat(response.getCity()).isEqualTo("Mumbai");
        assertThat(response.getPurseRemaining()).isEqualByComparingTo(new BigDecimal("1000000000"));
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @Order(2)
    @DisplayName("❌ Should throw DuplicateResourceException when team name already exists")
    void createTeam_DuplicateName_ThrowsException() {
        // Arrange
        when(teamRepository.existsByName("Mumbai Indians")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> teamService.createTeam(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Mumbai Indians");

        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    @Order(3)
    @DisplayName("❌ Should throw exception when team name is null")
    void createTeam_NullName_ThrowsException() {
        // Arrange
        sampleRequest.setName(null);

        // Act & Assert
        assertThatThrownBy(() -> teamService.createTeam(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(4)
    @DisplayName("❌ Should throw exception when team name is blank")
    void createTeam_BlankName_ThrowsException() {
        // Arrange
        sampleRequest.setName("   ");

        // Act & Assert
        assertThatThrownBy(() -> teamService.createTeam(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ═══════════════════════════════════════════
    // GET TEAM BY ID TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("✅ Should return team when valid ID is provided")
    void getTeamById_ValidId_ReturnsTeam() {
        // Arrange
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));

        // Act
        TeamResponse response = teamService.getTeamById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Mumbai Indians");
        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    @Order(6)
    @DisplayName("❌ Should throw ResourceNotFoundException for non-existent team ID")
    void getTeamById_InvalidId_ThrowsException() {
        // Arrange
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> teamService.getTeamById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @Order(7)
    @DisplayName("❌ Should throw exception for null team ID")
    void getTeamById_NullId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> teamService.getTeamById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ═══════════════════════════════════════════
    // GET ALL TEAMS TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(8)
    @DisplayName("✅ Should return all teams")
    void getAllTeams_TeamsExist_ReturnsList() {
        // Arrange
        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("Chennai Super Kings");
        team2.setCity("Chennai");
        team2.setOwner("N. Srinivasan");
        team2.setPurseRemaining(new BigDecimal("1000000000"));

        when(teamRepository.findAll()).thenReturn(Arrays.asList(sampleTeam, team2));

        // Act
        List<TeamResponse> teams = teamService.getAllTeams();

        // Assert
        assertThat(teams).hasSize(2);
        assertThat(teams).extracting(TeamResponse::getName)
                .containsExactlyInAnyOrder("Mumbai Indians", "Chennai Super Kings");
    }

    @Test
    @Order(9)
    @DisplayName("✅ Should return empty list when no teams exist")
    void getAllTeams_NoTeams_ReturnsEmptyList() {
        // Arrange
        when(teamRepository.findAll()).thenReturn(List.of());

        // Act
        List<TeamResponse> teams = teamService.getAllTeams();

        // Assert
        assertThat(teams).isEmpty();
    }

    // ═══════════════════════════════════════════
    // UPDATE TEAM TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("✅ Should update team successfully")
    void updateTeam_ValidRequest_ReturnsUpdatedTeam() {
        // Arrange
        sampleRequest.setCity("Navi Mumbai");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(teamRepository.save(any(Team.class))).thenReturn(sampleTeam);

        // Act
        TeamResponse response = teamService.updateTeam(1L, sampleRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @Order(11)
    @DisplayName("❌ Should throw ResourceNotFoundException when updating non-existent team")
    void updateTeam_InvalidId_ThrowsException() {
        // Arrange
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> teamService.updateTeam(999L, sampleRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════
    // DELETE TEAM TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("✅ Should delete team successfully")
    void deleteTeam_ValidId_DeletesTeam() {
        // Arrange
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        doNothing().when(teamRepository).delete(any(Team.class));

        // Act
        teamService.deleteTeam(1L);

        // Assert
        verify(teamRepository, times(1)).delete(sampleTeam);
    }

    @Test
    @Order(13)
    @DisplayName("❌ Should throw ResourceNotFoundException when deleting non-existent team")
    void deleteTeam_InvalidId_ThrowsException() {
        // Arrange
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> teamService.deleteTeam(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════
    // PURSE MANAGEMENT TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(14)
    @DisplayName("✅ Should return correct remaining purse balance")
    void getPurseBalance_ValidTeam_ReturnsBalance() {
        // Arrange
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));

        // Act
        BigDecimal purse = teamService.getPurseBalance(1L);

        // Assert
        assertThat(purse).isEqualByComparingTo(new BigDecimal("1000000000"));
    }

    @Test
    @Order(15)
    @DisplayName("✅ Should deduct purse amount after bid")
    void deductPurse_ValidAmount_UpdatesPurse() {
        // Arrange
        BigDecimal bidAmount = new BigDecimal("150000000"); // ₹15 Cr
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));
        when(teamRepository.save(any(Team.class))).thenReturn(sampleTeam);

        // Act
        teamService.deductPurse(1L, bidAmount);

        // Assert
        verify(teamRepository, times(1)).save(any(Team.class));
    }

    @Test
    @Order(16)
    @DisplayName("❌ Should throw exception when deducting more than purse balance")
    void deductPurse_ExceedsPurse_ThrowsException() {
        // Arrange
        BigDecimal excessiveAmount = new BigDecimal("2000000000"); // ₹200 Cr — more than ₹100 Cr
        when(teamRepository.findById(1L)).thenReturn(Optional.of(sampleTeam));

        // Act & Assert
        assertThatThrownBy(() -> teamService.deductPurse(1L, excessiveAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("purse");
    }

    @Test
    @Order(17)
    @DisplayName("❌ Should throw exception when deducting negative amount")
    void deductPurse_NegativeAmount_ThrowsException() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-50000000");

        // Act & Assert
        assertThatThrownBy(() -> teamService.deductPurse(1L, negativeAmount))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
