package com.ipl.auction.unit;

import com.ipl.auction.dto.PlayerRequest;
import com.ipl.auction.dto.PlayerResponse;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.enums.PlayerRole;
import com.ipl.auction.enums.PlayerStatus;
import com.ipl.auction.exception.DuplicateResourceException;
import com.ipl.auction.exception.ResourceNotFoundException;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.service.PlayerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PlayerService — Member 5 Deliverable.
 * <p>
 * Tests the player management service layer with Mockito mocks.
 * Covers CRUD, search/filter, status transitions, and validation.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerService — Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayerServiceUnitTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    private Player samplePlayer;
    private PlayerRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePlayer = new Player();
        samplePlayer.setId(1L);
        samplePlayer.setName("Virat Kohli");
        samplePlayer.setAge(35);
        samplePlayer.setRole(PlayerRole.BATSMAN);
        samplePlayer.setCountry("India");
        samplePlayer.setBasePrice(new BigDecimal("200000000")); // ₹20 Cr
        samplePlayer.setSoldPrice(null);
        samplePlayer.setStatus(PlayerStatus.UNSOLD);
        samplePlayer.setTeam(null);

        sampleRequest = new PlayerRequest();
        sampleRequest.setName("Virat Kohli");
        sampleRequest.setAge(35);
        sampleRequest.setRole(PlayerRole.BATSMAN);
        sampleRequest.setCountry("India");
        sampleRequest.setBasePrice(new BigDecimal("200000000"));
    }

    // ═══════════════════════════════════════════
    // CREATE PLAYER TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ Should register a new player successfully")
    void createPlayer_ValidRequest_ReturnsPlayerResponse() {
        when(playerRepository.existsByName(anyString())).thenReturn(false);
        when(playerRepository.save(any(Player.class))).thenReturn(samplePlayer);

        PlayerResponse response = playerService.createPlayer(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Virat Kohli");
        assertThat(response.getRole()).isEqualTo(PlayerRole.BATSMAN);
        assertThat(response.getStatus()).isEqualTo(PlayerStatus.UNSOLD);
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @Order(2)
    @DisplayName("❌ Should throw exception for duplicate player name")
    void createPlayer_DuplicateName_ThrowsException() {
        when(playerRepository.existsByName("Virat Kohli")).thenReturn(true);

        assertThatThrownBy(() -> playerService.createPlayer(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Virat Kohli");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @Order(3)
    @DisplayName("❌ Should reject player with null/blank name")
    void createPlayer_InvalidName_ThrowsException(String invalidName) {
        sampleRequest.setName(invalidName);

        assertThatThrownBy(() -> playerService.createPlayer(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(4)
    @DisplayName("❌ Should reject player with negative base price")
    void createPlayer_NegativeBasePrice_ThrowsException() {
        sampleRequest.setBasePrice(new BigDecimal("-100"));

        assertThatThrownBy(() -> playerService.createPlayer(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(5)
    @DisplayName("❌ Should reject player with zero base price")
    void createPlayer_ZeroBasePrice_ThrowsException() {
        sampleRequest.setBasePrice(BigDecimal.ZERO);

        assertThatThrownBy(() -> playerService.createPlayer(sampleRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ═══════════════════════════════════════════
    // GET PLAYER TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("✅ Should return player by valid ID")
    void getPlayerById_ValidId_ReturnsPlayer() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        PlayerResponse response = playerService.getPlayerById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Virat Kohli");
    }

    @Test
    @Order(7)
    @DisplayName("❌ Should throw ResourceNotFoundException for invalid player ID")
    void getPlayerById_InvalidId_ThrowsException() {
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @Order(8)
    @DisplayName("✅ Should return all players")
    void getAllPlayers_PlayersExist_ReturnsList() {
        Player player2 = new Player();
        player2.setId(2L);
        player2.setName("MS Dhoni");
        player2.setRole(PlayerRole.WICKETKEEPER);

        when(playerRepository.findAll()).thenReturn(Arrays.asList(samplePlayer, player2));

        List<PlayerResponse> players = playerService.getAllPlayers();

        assertThat(players).hasSize(2);
    }

    // ═══════════════════════════════════════════
    // FILTER / SEARCH TESTS
    // ═══════════════════════════════════════════

    @ParameterizedTest
    @EnumSource(PlayerRole.class)
    @Order(9)
    @DisplayName("✅ Should filter players by each role type")
    void getPlayersByRole_ValidRole_ReturnsFilteredList(PlayerRole role) {
        samplePlayer.setRole(role);
        when(playerRepository.findByRole(role)).thenReturn(List.of(samplePlayer));

        List<PlayerResponse> result = playerService.getPlayersByRole(role);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getRole()).isEqualTo(role);
    }

    @Test
    @Order(10)
    @DisplayName("✅ Should filter unsold players only")
    void getUnsoldPlayers_UnsoldExist_ReturnsList() {
        when(playerRepository.findByStatus(PlayerStatus.UNSOLD))
                .thenReturn(List.of(samplePlayer));

        List<PlayerResponse> result = playerService.getPlayersByStatus(PlayerStatus.UNSOLD);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(PlayerStatus.UNSOLD);
    }

    @Test
    @Order(11)
    @DisplayName("✅ Should return empty list when no players match filter")
    void getPlayersByRole_NoMatch_ReturnsEmptyList() {
        when(playerRepository.findByRole(PlayerRole.ALLROUNDER)).thenReturn(List.of());

        List<PlayerResponse> result = playerService.getPlayersByRole(PlayerRole.ALLROUNDER);

        assertThat(result).isEmpty();
    }

    // ═══════════════════════════════════════════
    // UPDATE PLAYER TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("✅ Should update player details successfully")
    void updatePlayer_ValidRequest_ReturnsUpdatedPlayer() {
        sampleRequest.setAge(36);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(samplePlayer);

        PlayerResponse response = playerService.updatePlayer(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(playerRepository, times(1)).save(any(Player.class));
    }

    // ═══════════════════════════════════════════
    // DELETE PLAYER TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(13)
    @DisplayName("✅ Should delete player successfully")
    void deletePlayer_ValidId_DeletesPlayer() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        doNothing().when(playerRepository).delete(any(Player.class));

        playerService.deletePlayer(1L);

        verify(playerRepository, times(1)).delete(samplePlayer);
    }

    @Test
    @Order(14)
    @DisplayName("❌ Should throw exception when deleting sold player")
    void deletePlayer_SoldPlayer_ThrowsException() {
        samplePlayer.setStatus(PlayerStatus.SOLD);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThatThrownBy(() -> playerService.deletePlayer(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sold");
    }

    // ═══════════════════════════════════════════
    // PLAYER STATUS TRANSITION TESTS
    // ═══════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("✅ Should mark player as SOLD with team assignment")
    void markAsSold_ValidTeam_UpdatesStatus() {
        Team buyingTeam = new Team();
        buyingTeam.setId(1L);
        buyingTeam.setName("Mumbai Indians");

        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(playerRepository.save(any(Player.class))).thenReturn(samplePlayer);

        playerService.markAsSold(1L, buyingTeam, new BigDecimal("250000000"));

        verify(playerRepository, times(1)).save(any(Player.class));
    }

    @Test
    @Order(16)
    @DisplayName("❌ Should throw exception when marking already-sold player as SOLD")
    void markAsSold_AlreadySold_ThrowsException() {
        samplePlayer.setStatus(PlayerStatus.SOLD);
        Team buyingTeam = new Team();
        buyingTeam.setId(1L);

        when(playerRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThatThrownBy(() -> playerService.markAsSold(1L, buyingTeam, new BigDecimal("250000000")))
                .isInstanceOf(IllegalStateException.class);
    }
}
