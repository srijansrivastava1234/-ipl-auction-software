/**
 * Live Auction Room Controller - Real-Time Bidding Arena
 * Member 4 - Frontend & API Integration Lead (Weeks 5 & 6 Deliverables)
 */

const AuctionController = {
    // Current Active Player state
    activePlayer: {
        id: 101,
        name: "Virat Kohli",
        role: "Batter",
        country: "India",
        isOverseas: false,
        basePriceVal: 2.0, // in Crores
        basePriceStr: "₹ 2.00 Cr",
        set: "Set 1 - Marquee Batsman",
        stats: "Matches: 252 | Runs: 8,004 | SR: 131.9"
    },

    currentBidVal: 14.5, // in Crores
    currentBidTeam: "Royal Challengers Bengaluru",
    bidStatus: "ACTIVE", // ACTIVE, SOLD, UNSOLD
    timerSeconds: 10,
    timerInterval: null,

    bidHistory: [
        { team: "Chennai Super Kings", amount: "₹ 14.00 Cr", time: "14:02:10" },
        { team: "Royal Challengers Bengaluru", amount: "₹ 14.50 Cr", time: "14:03:45" }
    ],

    teams: [
        { name: "Royal Challengers Bengaluru", logo: "🦅" },
        { name: "Chennai Super Kings", logo: "🦁" },
        { name: "Mumbai Indians", logo: "⚡" },
        { name: "Kolkata Knight Riders", logo: "⚔️" },
        { name: "Sunrisers Hyderabad", logo: "🦅" },
        { name: "Gujarat Titans", logo: "⚡" },
        { name: "Rajasthan Royals", logo: "👑" },
        { name: "Delhi Capitals", logo: "🐯" },
        { name: "Punjab Kings", logo: "🦁" },
        { name: "Lucknow Super Giants", logo: "🏏" }
    ],

    init() {
        if (typeof document === 'undefined') return;
        this.populateTeamSelect();
        this.renderPodium();
        this.renderBidHistory();
        this.startTimer();
    },

    populateTeamSelect() {
        const select = document.getElementById('auction-bidding-franchise-select');
        if (!select) return;

        select.innerHTML = this.teams.map(t => `<option value="${t.name}">${t.logo} ${t.name}</option>`).join('');
    },

    /**
     * Start/Reset Countdown Timer
     */
    startTimer() {
        this.stopTimer();
        this.timerSeconds = 10;
        this.updateTimerUI();

        if (this.bidStatus !== 'ACTIVE') return;

        this.timerInterval = setInterval(() => {
            if (this.timerSeconds > 0) {
                this.timerSeconds--;
                this.updateTimerUI();
            } else {
                this.stopTimer();
                if (this.currentBidTeam !== "Base Price") {
                    this.markSold();
                } else {
                    this.markUnsold();
                }
            }
        }, 1000);
    },

    stopTimer() {
        if (this.timerInterval) {
            clearInterval(this.timerInterval);
            this.timerInterval = null;
        }
    },

    updateTimerUI() {
        const timerEl = document.getElementById('auction-timer');
        if (timerEl) {
            timerEl.textContent = `${this.timerSeconds}s`;
            if (this.timerSeconds <= 3 && this.bidStatus === 'ACTIVE') {
                timerEl.classList.add('warning');
            } else {
                timerEl.classList.remove('warning');
            }
        }
    },

    /**
     * Render Podium Player details
     */
    renderPodium() {
        const nameEl = document.getElementById('auction-player-name');
        const roleEl = document.getElementById('auction-player-role');
        const basePriceEl = document.getElementById('auction-player-base');
        const currentBidEl = document.getElementById('auction-current-bid');
        const biddingTeamEl = document.getElementById('auction-bidding-team');
        const setBadgeEl = document.getElementById('auction-player-set');
        const statusBadgeEl = document.getElementById('auction-status-badge');
        const statsEl = document.getElementById('auction-player-stats');

        if (nameEl) nameEl.textContent = this.activePlayer.name;
        if (roleEl) roleEl.textContent = `${this.activePlayer.country} ${this.activePlayer.isOverseas ? '✈️' : '🇮🇳'} • ${this.activePlayer.role}`;
        if (basePriceEl) basePriceEl.textContent = this.activePlayer.basePriceStr;
        if (currentBidEl) currentBidEl.textContent = `₹ ${this.currentBidVal.toFixed(2)} Cr`;
        if (biddingTeamEl) biddingTeamEl.textContent = `Held by: ${this.currentBidTeam}`;
        if (setBadgeEl) setBadgeEl.textContent = this.activePlayer.set || "Set 1 - Marquee";
        if (statsEl) statsEl.textContent = this.activePlayer.stats || "Capped International Player";

        if (statusBadgeEl) {
            statusBadgeEl.textContent = this.bidStatus === 'ACTIVE' ? 'LIVE BIDDING' : this.bidStatus;
            statusBadgeEl.className = `status-badge ${this.bidStatus === 'SOLD' ? 'success' : this.bidStatus === 'UNSOLD' ? 'danger' : 'gold'}`;
        }
    },

    /**
     * Render scrolling live bid history feed
     */
    renderBidHistory() {
        const historyContainer = document.getElementById('auction-bid-history');
        if (!historyContainer) return;

        if (this.bidHistory.length === 0) {
            historyContainer.innerHTML = `<div style="font-size: 12px; color: var(--text-muted); padding: 12px;">No bids placed yet for this player.</div>`;
            return;
        }

        historyContainer.innerHTML = this.bidHistory.map((item, idx) => `
            <div class="bid-item ${idx === 0 ? 'latest' : ''}">
                <span class="bid-team-badge">${item.team}</span>
                <span class="bid-amount">${item.amount}</span>
                <span class="bid-time">${item.time}</span>
            </div>
        `).join('');
    },

    /**
     * Raise bid handler
     * @param {number} increment - Amount in Crores (0.2 = 20L, 0.5 = 50L, 1.0 = 1Cr, 2.0 = 2Cr)
     */
    async raiseBid(increment = 0.5) {
        if (this.bidStatus !== 'ACTIVE') {
            if (window.Toast) window.Toast.show(`Bidding is closed for ${this.activePlayer.name} (${this.bidStatus})`, 'warning');
            return;
        }

        const teamSelect = document.getElementById('auction-bidding-franchise-select');
        const selectedTeam = teamSelect ? teamSelect.value : "Royal Challengers Bengaluru";

        // Validate purse budget with SquadController
        if (window.SquadController && window.SquadController.canAffordBid) {
            const nextTotal = this.currentBidVal + increment;
            const canAfford = window.SquadController.canAffordBid(selectedTeam, nextTotal, this.activePlayer.isOverseas);
            if (!canAfford.allowed) {
                if (window.Toast) window.Toast.show(`Bid rejected for ${selectedTeam}: ${canAfford.reason}`, 'danger');
                return;
            }
        }

        this.currentBidVal += increment;
        this.currentBidTeam = selectedTeam;

        const timestamp = new Date().toLocaleTimeString();
        const formattedAmount = `₹ ${this.currentBidVal.toFixed(2)} Cr`;

        const newBid = {
            team: selectedTeam,
            amount: formattedAmount,
            time: timestamp
        };

        this.bidHistory.unshift(newBid);
        this.renderPodium();
        this.renderBidHistory();
        this.startTimer(); // Reset countdown timer on new bid

        // Attempt API endpoint trigger via interceptor
        try {
            const apiClient = typeof window !== 'undefined' ? window.api : null;
            if (apiClient) {
                await apiClient.post(CONFIG.ENDPOINTS.AUCTION.BID, {
                    playerId: this.activePlayer.id,
                    team: selectedTeam,
                    bidAmount: this.currentBidVal
                });
            }
        } catch (err) {
            // Mock mode fallback
        }

        if (window.Toast) window.Toast.show(`🔨 Bid Raised! ${formattedAmount} by ${selectedTeam}`, 'success');
    },

    /**
     * Submit Custom Bid Amount
     */
    submitCustomBid() {
        const customInput = document.getElementById('auction-custom-bid-input');
        if (!customInput) return;

        const customVal = parseFloat(customInput.value);
        if (isNaN(customVal) || customVal <= this.currentBidVal) {
            if (window.Toast) window.Toast.show(`Custom bid must be strictly greater than current highest bid (₹ ${this.currentBidVal.toFixed(2)} Cr)`, 'warning');
            return;
        }

        const diff = customVal - this.currentBidVal;
        this.raiseBid(diff);
        customInput.value = '';
    },

    /**
     * Mark player as SOLD
     */
    markSold() {
        if (this.bidStatus === 'SOLD') return;

        this.stopTimer();
        this.bidStatus = 'SOLD';
        this.renderPodium();

        // Update status in admin pool
        if (window.AdminController && window.AdminController.players) {
            const target = window.AdminController.players.find(p => p.id === this.activePlayer.id);
            if (target) target.status = `Sold (${this.currentBidTeam})`;
            window.AdminController.renderPlayerTable();
        }

        // Show Celebration Overlay
        this.showCelebrationModal();

        // Notify Squad Controller to update purse
        if (window.SquadController && window.SquadController.recordPurchase) {
            window.SquadController.recordPurchase(this.currentBidTeam, this.activePlayer, this.currentBidVal);
        }
    },

    showCelebrationModal() {
        const overlay = document.getElementById('sold-celebration-overlay');
        const titleEl = document.getElementById('sold-player-name-banner');
        const descEl = document.getElementById('sold-player-desc-banner');

        if (overlay && titleEl && descEl) {
            titleEl.textContent = `${this.activePlayer.name}`;
            descEl.innerHTML = `SOLD to <strong style="color: var(--ipl-gold); font-size: 18px;">${this.currentBidTeam}</strong> for <span style="color: var(--success); font-weight: 800; font-size: 20px;">₹ ${this.currentBidVal.toFixed(2)} Cr</span>`;
            overlay.style.display = 'flex';
        }
    },

    closeCelebrationModal() {
        const overlay = document.getElementById('sold-celebration-overlay');
        if (overlay) overlay.style.display = 'none';
    },

    /**
     * Mark player as UNSOLD
     */
    markUnsold() {
        this.stopTimer();
        this.bidStatus = 'UNSOLD';
        this.renderPodium();

        if (window.AdminController && window.AdminController.players) {
            const target = window.AdminController.players.find(p => p.id === this.activePlayer.id);
            if (target) target.status = 'Unsold';
            window.AdminController.renderPlayerTable();
        }

        if (window.Toast) window.Toast.show(`${this.activePlayer.name} is UNSOLD and passed to accelerated round.`, 'info');
    },

    /**
     * Load player directly into the podium
     */
    loadPlayerDirectly(player) {
        this.closeCelebrationModal();
        const baseVal = parseFloat(player.basePrice.replace(/[^0-9.]/g, '')) || 1.0;
        
        this.activePlayer = {
            id: player.id,
            name: player.name,
            role: player.role,
            country: player.country,
            isOverseas: player.isOverseas,
            basePriceVal: baseVal,
            basePriceStr: player.basePrice,
            set: player.set || "Set 1 - Marquee",
            stats: player.matches ? `Matches: ${player.matches} | Runs: ${player.runs || 0} | Wickets: ${player.wickets || 0}` : "Capped International"
        };

        this.currentBidVal = baseVal;
        this.currentBidTeam = "Base Price";
        this.bidStatus = "ACTIVE";
        this.bidHistory = [{ team: "Starting Base", amount: player.basePrice, time: new Date().toLocaleTimeString() }];
        
        this.renderPodium();
        this.renderBidHistory();
        this.startTimer();
    },

    /**
     * Load next player onto podium from pool
     */
    nextPlayer() {
        this.closeCelebrationModal();
        const pool = (window.AdminController && window.AdminController.players) ? window.AdminController.players : [];
        const available = pool.filter(p => p.status === 'Available');

        if (available.length > 0) {
            const next = available[0];
            this.loadPlayerDirectly(next);
            if (window.Toast) window.Toast.show(`Now on Podium: ${next.name} (${next.basePrice})`, 'info');
        } else {
            if (window.Toast) window.Toast.show("All players in the current auction pool have been auctioned!", "warning");
        }
    }
};

if (typeof window !== 'undefined') {
    window.AuctionController = AuctionController;
    document.addEventListener('DOMContentLoaded', () => AuctionController.init());
}

if (typeof module !== 'undefined') {
    module.exports = AuctionController;
}
