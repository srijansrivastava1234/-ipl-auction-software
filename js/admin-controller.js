/**
 * Admin Dashboard Controller - Player & Team Management
 * Member 4 - Frontend & API Integration Lead (Weeks 3 & 4 Deliverables)
 */

const AdminController = {
    // Initial standard IPL 2026 Auction Pool
    defaultPlayers: [
        { id: 101, name: "Virat Kohli", role: "Batter", basePrice: "₹ 2.00 Cr", country: "India", isOverseas: false, status: "Active Podium", set: "Set 1 - Marquee", matches: 252, runs: 8004, sr: 131.9 },
        { id: 102, name: "Jasprit Bumrah", role: "Bowler", basePrice: "₹ 2.00 Cr", country: "India", isOverseas: false, status: "Available", set: "Set 1 - Marquee", matches: 133, wickets: 165, econ: 7.3 },
        { id: 103, name: "Travis Head", role: "Batter", basePrice: "₹ 2.00 Cr", country: "Australia", isOverseas: true, status: "Available", set: "Set 1 - Marquee", matches: 28, runs: 891, sr: 174.5 },
        { id: 104, name: "Heinrich Klaasen", role: "Wicket-Keeper", basePrice: "₹ 1.50 Cr", country: "South Africa", isOverseas: true, status: "Available", set: "Set 2 - Overseas WK", matches: 35, runs: 993, sr: 168.3 },
        { id: 105, name: "Rishabh Pant", role: "Wicket-Keeper", basePrice: "₹ 2.00 Cr", country: "India", isOverseas: false, status: "Available", set: "Set 2 - Indian WK", matches: 111, runs: 3284, sr: 148.9 },
        { id: 106, name: "Hardik Pandya", role: "All-Rounder", basePrice: "₹ 2.00 Cr", country: "India", isOverseas: false, status: "Available", set: "Set 3 - All-Rounder", matches: 137, runs: 2525, wickets: 64 },
        { id: 107, name: "Rashid Khan", role: "Bowler", basePrice: "₹ 2.00 Cr", country: "Afghanistan", isOverseas: true, status: "Available", set: "Set 1 - Marquee", matches: 121, wickets: 149, econ: 6.8 },
        { id: 108, name: "Sunil Narine", role: "All-Rounder", basePrice: "₹ 2.00 Cr", country: "West Indies", isOverseas: true, status: "Available", set: "Set 3 - All-Rounder", matches: 177, runs: 1534, wickets: 180 },
        { id: 109, name: "Shubman Gill", role: "Batter", basePrice: "₹ 2.00 Cr", country: "India", isOverseas: false, status: "Available", set: "Set 1 - Marquee", matches: 103, runs: 3216, sr: 135.7 },
        { id: 110, name: "Kagiso Rabada", role: "Bowler", basePrice: "₹ 2.00 Cr", country: "South Africa", isOverseas: true, status: "Available", set: "Set 4 - Fast Bowlers", matches: 80, wickets: 117, econ: 8.4 }
    ],

    players: [],

    // 10 Official IPL Franchises (Week 4 Team Management)
    teams: [
        { id: "CSK", name: "Chennai Super Kings", primaryColor: "#f9cd05", logo: "🦁", purse: 120.0, spent: 65.0, players: 18, overseas: 6, rtm: 1, coach: "Stephen Fleming" },
        { id: "MI", name: "Mumbai Indians", primaryColor: "#004ba0", logo: "⚡", purse: 120.0, spent: 82.0, players: 15, overseas: 4, rtm: 2, coach: "Mahela Jayawardene" },
        { id: "RCB", name: "Royal Challengers Bengaluru", primaryColor: "#d32f2f", logo: "🦅", purse: 120.0, spent: 77.5, players: 16, overseas: 5, rtm: 1, coach: "Andy Flower" },
        { id: "KKR", name: "Kolkata Knight Riders", primaryColor: "#3a225d", logo: "⚔️", purse: 120.0, spent: 55.0, players: 17, overseas: 5, rtm: 2, coach: "Chandrakant Pandit" },
        { id: "SRH", name: "Sunrisers Hyderabad", primaryColor: "#f26522", logo: "🦅", purse: 120.0, spent: 70.0, players: 14, overseas: 6, rtm: 1, coach: "Daniel Vettori" },
        { id: "RR", name: "Rajasthan Royals", primaryColor: "#ea1a85", logo: "👑", purse: 120.0, spent: 68.0, players: 16, overseas: 4, rtm: 1, coach: "Rahul Dravid" },
        { id: "GT", name: "Gujarat Titans", primaryColor: "#1b2133", logo: "⚡", purse: 120.0, spent: 62.0, players: 15, overseas: 4, rtm: 2, coach: "Ashish Nehra" },
        { id: "LSG", name: "Lucknow Super Giants", primaryColor: "#a72056", logo: "🏏", purse: 120.0, spent: 58.0, players: 14, overseas: 5, rtm: 2, coach: "Justin Langer" },
        { id: "DC", name: "Delhi Capitals", primaryColor: "#0078bc", logo: "🐯", purse: 120.0, spent: 60.0, players: 15, overseas: 4, rtm: 2, coach: "Hemang Badani" },
        { id: "PK", name: "Punjab Kings", primaryColor: "#ed1b24", logo: "🦁", purse: 120.0, spent: 45.0, players: 12, overseas: 3, rtm: 3, coach: "Ricky Ponting" }
    ],

    currentFilters: {
        role: "ALL",
        nationality: "ALL",
        status: "ALL",
        search: ""
    },

    init() {
        if (typeof document === 'undefined') return;
        this.players = JSON.parse(JSON.stringify(this.defaultPlayers));
        this.renderPlayerTable();
        this.renderTeamCards();

        const addPlayerForm = document.getElementById('add-player-form');
        if (addPlayerForm) {
            addPlayerForm.addEventListener('submit', (e) => this.handleAddPlayer(e));
        }

        // Search & Filter listeners
        const searchInput = document.getElementById('admin-search-player');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.currentFilters.search = e.target.value.toLowerCase().trim();
                this.renderPlayerTable();
            });
        }

        const roleFilter = document.getElementById('admin-filter-role');
        if (roleFilter) {
            roleFilter.addEventListener('change', (e) => {
                this.currentFilters.role = e.target.value;
                this.renderPlayerTable();
            });
        }

        const nationalityFilter = document.getElementById('admin-filter-nationality');
        if (nationalityFilter) {
            nationalityFilter.addEventListener('change', (e) => {
                this.currentFilters.nationality = e.target.value;
                this.renderPlayerTable();
            });
        }

        const statusFilter = document.getElementById('admin-filter-status');
        if (statusFilter) {
            statusFilter.addEventListener('change', (e) => {
                this.currentFilters.status = e.target.value;
                this.renderPlayerTable();
            });
        }
    },

    /**
     * Render registered player management table with filtering
     */
    renderPlayerTable() {
        const tableBody = document.getElementById('admin-player-tbody');
        if (!tableBody) return;

        const filtered = this.players.filter(p => {
            const matchesSearch = !this.currentFilters.search || p.name.toLowerCase().includes(this.currentFilters.search) || p.country.toLowerCase().includes(this.currentFilters.search);
            const matchesRole = this.currentFilters.role === 'ALL' || p.role.toUpperCase() === this.currentFilters.role.toUpperCase();
            const matchesNat = this.currentFilters.nationality === 'ALL' || (this.currentFilters.nationality === 'OVERSEAS' ? p.isOverseas : !p.isOverseas);
            const matchesStatus = this.currentFilters.status === 'ALL' || p.status.toUpperCase().includes(this.currentFilters.status.toUpperCase());
            return matchesSearch && matchesRole && matchesNat && matchesStatus;
        });

        if (filtered.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 24px; color: var(--text-muted);">No players matched the selected criteria.</td></tr>`;
            return;
        }

        tableBody.innerHTML = filtered.map((p) => `
            <tr>
                <td><strong>#${p.id}</strong></td>
                <td>
                    <div style="font-weight: 600; display: flex; align-items: center; gap: 6px;">
                        ${this.escapeHtml(p.name)}
                        ${p.isOverseas ? '<span title="Overseas Player">✈️</span>' : ''}
                    </div>
                    <div style="font-size: 11px; color: var(--text-muted);">${p.set || 'Capped Pool'}</div>
                </td>
                <td><span class="card-tag" style="background: rgba(58, 134, 255, 0.15); color: var(--ipl-electric-blue);">${p.role}</span></td>
                <td>${p.country}</td>
                <td style="font-weight: 700; color: var(--ipl-gold);">${p.basePrice}</td>
                <td>
                    <span class="status-badge ${p.status === 'Active Podium' ? 'gold' : p.status === 'Sold' ? 'success' : p.status === 'Unsold' ? '' : 'gold'}">
                        ${p.status}
                    </span>
                </td>
                <td>
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-primary btn-sm" onclick="AdminController.sendToPodium(${p.id})">🔨 Podium</button>
                        <button class="btn btn-outline btn-sm" onclick="AdminController.deletePlayer(${p.id})">🗑️</button>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    /**
     * Render all 10 IPL Franchise Cards (Week 4 Team Management)
     */
    renderTeamCards() {
        const container = document.getElementById('admin-teams-grid');
        if (!container) return;

        container.innerHTML = this.teams.map(t => {
            const remaining = (t.purse - t.spent).toFixed(2);
            const spentPercent = Math.min(100, Math.round((t.spent / t.purse) * 100));
            return `
                <div class="team-card" style="border-top-color: ${t.primaryColor};">
                    <div class="team-card-header">
                        <div class="team-avatar" style="border-color: ${t.primaryColor};">${t.logo}</div>
                        <div>
                            <div class="team-card-title">${t.name}</div>
                            <div class="team-card-sub">Coach: ${t.coach} • RTM: ${t.rtm}</div>
                        </div>
                    </div>
                    <div class="team-stat-row">
                        <span class="team-stat-label">Remaining Purse</span>
                        <span class="team-stat-value" style="color: var(--ipl-gold);">₹ ${remaining} Cr</span>
                    </div>
                    <div class="progress-bar-container">
                        <div class="progress-bar-fill ${remaining < 15 ? 'danger' : ''}" style="width: ${spentPercent}%;"></div>
                    </div>
                    <div class="team-stat-row">
                        <span class="team-stat-label">Squad Capacity</span>
                        <span class="team-stat-value">${t.players} / 25 (${t.overseas} Overseas)</span>
                    </div>
                    <div class="team-stat-row" style="margin-top: 12px;">
                        <button class="btn btn-outline btn-sm" style="width: 100%;" onclick="AdminController.editTeamPurse('${t.id}')">⚙️ Configure Purse</button>
                    </div>
                </div>
            `;
        }).join('');
    },

    /**
     * Send specific player to Live Auction Podium
     */
    sendToPodium(playerId) {
        const player = this.players.find(p => p.id === playerId);
        if (!player) return;

        // Reset others to Available if they were on podium
        this.players.forEach(p => {
            if (p.status === 'Active Podium') p.status = 'Available';
        });
        player.status = 'Active Podium';
        this.renderPlayerTable();

        if (window.AuctionController && window.AuctionController.loadPlayerDirectly) {
            window.AuctionController.loadPlayerDirectly(player);
        }

        if (window.Toast) window.Toast.show(`"${player.name}" loaded onto Live Auction Podium!`, 'success');
        if (window.navigateTo) window.navigateTo('auction');
    },

    /**
     * Add player form submit handler
     */
    async handleAddPlayer(e) {
        if (e) e.preventDefault();

        const nameEl = document.getElementById('player-name');
        const roleEl = document.getElementById('player-role');
        const priceEl = document.getElementById('player-base-price');
        const countryEl = document.getElementById('player-country');
        const overseasEl = document.getElementById('player-overseas');

        const name = nameEl ? nameEl.value.trim() : '';
        const role = roleEl ? roleEl.value : 'Batter';
        const basePrice = priceEl ? priceEl.value : '₹ 1.00 Cr';
        const country = countryEl ? countryEl.value.trim() : 'India';
        const isOverseas = overseasEl ? overseasEl.checked : false;

        if (!name) {
            if (window.Toast) window.Toast.show('Please enter player name', 'danger');
            return;
        }

        const newPlayer = {
            id: 100 + this.players.length + 1,
            name: name,
            role: role,
            basePrice: basePrice,
            country: country || (isOverseas ? 'Foreign' : 'India'),
            isOverseas: isOverseas,
            status: 'Available',
            set: isOverseas ? 'Set 2 - Overseas' : 'Set 1 - Indian'
        };

        try {
            const apiClient = typeof window !== 'undefined' ? window.api : null;
            if (apiClient) {
                await apiClient.post(CONFIG.ENDPOINTS.ADMIN.PLAYERS, newPlayer);
            }
        } catch (err) {
            // Fallback for offline mode
        }

        this.players.push(newPlayer);
        this.renderPlayerTable();

        if (nameEl) nameEl.value = '';
        if (countryEl) countryEl.value = '';
        if (overseasEl) overseasEl.checked = false;

        if (window.Toast) window.Toast.show(`Player "${name}" added to auction pool!`, 'success');
    },

    /**
     * Delete player from pool
     */
    deletePlayer(id) {
        const idx = this.players.findIndex(p => p.id === id);
        if (idx !== -1) {
            const removed = this.players.splice(idx, 1)[0];
            this.renderPlayerTable();
            if (window.Toast) window.Toast.show(`Removed "${removed.name}" from pool`, 'warning');
        }
    },

    /**
     * Reset Player Pool to Default
     */
    resetDefaultPool() {
        this.players = JSON.parse(JSON.stringify(this.defaultPlayers));
        this.renderPlayerTable();
        if (window.Toast) window.Toast.show("Player pool restored to official IPL 2026 catalog.", "info");
    },

    /**
     * Edit Team Purse
     */
    editTeamPurse(teamId) {
        const team = this.teams.find(t => t.id === teamId);
        if (!team) return;

        const newPurse = prompt(`Enter total purse cap for ${team.name} (in ₹ Crores):`, team.purse);
        if (newPurse && !isNaN(parseFloat(newPurse))) {
            team.purse = parseFloat(newPurse);
            this.renderTeamCards();
            if (window.SquadController) {
                window.SquadController.updateTeamPurse(team.name, team.purse);
            }
            if (window.Toast) window.Toast.show(`Purse for ${team.name} updated to ₹ ${team.purse} Cr`, 'success');
        }
    },

    escapeHtml(str) {
        return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }
};

if (typeof window !== 'undefined') {
    window.AdminController = AdminController;
    document.addEventListener('DOMContentLoaded', () => AdminController.init());
}

if (typeof module !== 'undefined') {
    module.exports = AdminController;
}
