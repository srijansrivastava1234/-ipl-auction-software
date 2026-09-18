package com.ipl.auction.unit;

import com.ipl.auction.dto.BidRequest;
import com.ipl.auction.entity.Player;
import com.ipl.auction.entity.Team;
import com.ipl.auction.enums.PlayerStatus;
import com.ipl.auction.validator.BidValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for BidValidator — Member 5 Deliverable.
 * <p>
 * Tests the bid validation rules in isolation:
 * - Minimum bid increments
 * - Purse sufficiency checks
 * - Base price validation
 * - Player eligibility checks
 * - Team squad size limits
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BidValidator — Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BidValidationUnitTest {

    private BidValidator bidValidator;
    private Team sampleTeam;
    private Player samplePlayer;

    @BeforeEach
    void setUp() {
        bidValidator = new BidValidator();

        sampleTeam = new Team();
        sampleTeam.setId(1L);
        sampleTeam.setName("Mumbai Indians");
        sampleTeam.setPurseRemaining(new BigDecimal("1000000000")); // ₹100 Cr
        sampleTeam.setMaxPurse(new BigDecimal("1000000000"));
        sampleTeam.setSquadSize(15); // Current squad size

        samplePlayer = new Player();
        samplePlayer.setId(1L);
        samplePlayer.setName("Virat Kohli");
        samplePlayer.setBasePrice(new BigDecimal("200000000")); // ₹20 Cr
        samplePlayer.setStatus(PlayerStatus.UNSOLD);
    }

    // ═══════════════════════════════════════════
    // BID AMOUNT VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("✅ Valid bid — above base price, within purse")
    void validateBid_ValidAmount_NoExceptionThrown() {
        BigDecimal bidAmount = new BigDecimal("250000000"); // ₹25 Cr

        assertThatCode(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(2)
    @DisplayName("✅ Valid bid — exactly at base price")
    void validateBid_ExactBasePrice_NoExceptionThrown() {
        BigDecimal bidAmount = new BigDecimal("200000000"); // ₹20 Cr = base

        assertThatCode(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(3)
    @DisplayName("❌ Bid below base price should fail")
    void validateBid_BelowBasePrice_ThrowsException() {
        BigDecimal bidAmount = new BigDecimal("100000000"); // ₹10 Cr < ₹20 Cr base

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base price");
    }

    @Test
    @Order(4)
    @DisplayName("❌ Zero bid amount should fail")
    void validateBid_ZeroAmount_ThrowsException() {
        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(5)
    @DisplayName("❌ Negative bid amount should fail")
    void validateBid_NegativeAmount_ThrowsException() {
        BigDecimal negative = new BigDecimal("-50000000");

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, negative))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(6)
    @DisplayName("❌ Null bid amount should fail")
    void validateBid_NullAmount_ThrowsException() {
        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ═══════════════════════════════════════════
    // PURSE VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("❌ Bid exceeding team purse should fail")
    void validateBid_ExceedsPurse_ThrowsException() {
        BigDecimal excessiveBid = new BigDecimal("1500000000"); // ₹150 Cr > ₹100 Cr purse

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, excessiveBid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("purse");
    }

    @Test
    @Order(8)
    @DisplayName("✅ Bid exactly at purse limit should pass")
    void validateBid_ExactPurseLimit_NoExceptionThrown() {
        BigDecimal exactPurse = new BigDecimal("1000000000"); // ₹100 Cr = purse

        assertThatCode(() -> bidValidator.validateBid(sampleTeam, samplePlayer, exactPurse))
                .doesNotThrowAnyException();
    }

    @Test
    @Order(9)
    @DisplayName("❌ Bid when purse is zero should fail")
    void validateBid_ZeroPurse_ThrowsException() {
        sampleTeam.setPurseRemaining(BigDecimal.ZERO);
        BigDecimal bidAmount = new BigDecimal("200000000");

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .isInstanceOf(IllegalStateException.class);
    }

    // ═══════════════════════════════════════════
    // PLAYER ELIGIBILITY VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(10)
    @DisplayName("❌ Bid on SOLD player should fail")
    void validateBid_SoldPlayer_ThrowsException() {
        samplePlayer.setStatus(PlayerStatus.SOLD);
        BigDecimal bidAmount = new BigDecimal("250000000");

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sold");
    }

    @Test
    @Order(11)
    @DisplayName("❌ Bid on WITHDRAWN player should fail")
    void validateBid_WithdrawnPlayer_ThrowsException() {
        samplePlayer.setStatus(PlayerStatus.WITHDRAWN);
        BigDecimal bidAmount = new BigDecimal("250000000");

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .isInstanceOf(IllegalStateException.class);
    }

    // ═══════════════════════════════════════════
    // SQUAD SIZE VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(12)
    @DisplayName("❌ Bid when squad is at max capacity (25) should fail")
    void validateBid_SquadFull_ThrowsException() {
        sampleTeam.setSquadSize(25); // IPL max squad size
        BigDecimal bidAmount = new BigDecimal("250000000");

        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("squad");
    }

    @Test
    @Order(13)
    @DisplayName("✅ Bid when squad has room (< 25) should pass")
    void validateBid_SquadHasRoom_NoExceptionThrown() {
        sampleTeam.setSquadSize(20);
        BigDecimal bidAmount = new BigDecimal("250000000");

        assertThatCode(() -> bidValidator.validateBid(sampleTeam, samplePlayer, bidAmount))
                .doesNotThrowAnyException();
    }

    // ═══════════════════════════════════════════
    // MINIMUM BID INCREMENT VALIDATION
    // ═══════════════════════════════════════════

    @ParameterizedTest
    @CsvSource({
            "200000000, 210000000, true",   // ₹20 Cr base, ₹21 Cr bid — valid increment
            "200000000, 205000000, true",   // ₹20 Cr base, ₹20.5 Cr bid — valid
            "200000000, 200100000, false",  // ₹20 Cr base, ₹20.01 Cr bid — too small increment
            "500000000, 510000000, true",   // ₹50 Cr base, ₹51 Cr bid — valid
    })
    @Order(14)
    @DisplayName("⚡ Parameterized: Minimum bid increment validation")
    void validateBidIncrement_VariousAmounts(String currentBid, String newBid, boolean shouldPass) {
        BigDecimal current = new BigDecimal(currentBid);
        BigDecimal proposed = new BigDecimal(newBid);

        if (shouldPass) {
            assertThatCode(() -> bidValidator.validateBidIncrement(current, proposed))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> bidValidator.validateBidIncrement(current, proposed))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("increment");
        }
    }

    // ═══════════════════════════════════════════
    // NULL ENTITY VALIDATION
    // ═══════════════════════════════════════════

    @Test
    @Order(15)
    @DisplayName("❌ Null team should fail validation")
    void validateBid_NullTeam_ThrowsException() {
        assertThatThrownBy(() -> bidValidator.validateBid(null, samplePlayer, new BigDecimal("250000000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Order(16)
    @DisplayName("❌ Null player should fail validation")
    void validateBid_NullPlayer_ThrowsException() {
        assertThatThrownBy(() -> bidValidator.validateBid(sampleTeam, null, new BigDecimal("250000000")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
