/**
 * Squad & Purse Analytics Controller
 * Member 4 - Frontend & API Integration Lead (Week 6.5 Deliverable)
 */

const SquadController = {
    // 10 Full Franchise Squad & Purse Datasets
    teamsData: {
        "Royal Challengers Bengaluru": {
            total: 120.0,
            spent: 77.5,
            primaryColor: "#d32f2f",
            logo: "🦅",
            squad: [
                { name: "Virat Kohli", role: "Batter", country: "India", isOverseas: false, price: "₹ 21.00 Cr", retention: true },
                { name: "Rajat Patidar", role: "Batter", country: "India", isOverseas: false, price: "₹ 11.00 Cr", retention: true },
                { name: "Yash Dayal", role: "Bowler", country: "India", isOverseas: false, price: "₹ 5.00 Cr", retention: true },
                { name: "Phil Salt", role: "Wicket-Keeper", country: "England", isOverseas: true, price: "₹ 11.50 Cr", retention: false },
                { name: "Liam Livingstone", role: "All-Rounder", country: "England", isOverseas: true, price: "₹ 8.75 Cr", retention: false },
                { name: "Josh Hazlewood", role: "Bowler", country: "Australia", isOverseas: true, price: "₹ 12.50 Cr", retention: false },
                { name: "Krunal Pandya", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 5.75 Cr", retention: false },
                { name: "Bhuvneshwar Kumar", role: "Bowler", country: "India", isOverseas: false, price: "₹ 10.75 Cr", retention: false }
            ]
        },
        "Chennai Super Kings": {
            total: 120.0,
            spent: 65.0,
            primaryColor: "#f9cd05",
            logo: "🦁",
            squad: [
                { name: "Ruturaj Gaikwad", role: "Batter", country: "India", isOverseas: false, price: "₹ 18.00 Cr", retention: true },
                { name: "Ravindra Jadeja", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 18.00 Cr", retention: true },
                { name: "Matheesha Pathirana", role: "Bowler", country: "Sri Lanka", isOverseas: true, price: "₹ 13.00 Cr", retention: true },
                { name: "Shivam Dube", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 12.00 Cr", retention: true },
                { name: "MS Dhoni", role: "Wicket-Keeper", country: "India", isOverseas: false, price: "₹ 4.00 Cr", retention: true }
            ]
        },
        "Mumbai Indians": {
            total: 120.0,
            spent: 82.0,
            primaryColor: "#004ba0",
            logo: "⚡",
            squad: [
                { name: "Jasprit Bumrah", role: "Bowler", country: "India", isOverseas: false, price: "₹ 18.00 Cr", retention: true },
                { name: "Suryakumar Yadav", role: "Batter", country: "India", isOverseas: false, price: "₹ 16.35 Cr", retention: true },
                { name: "Hardik Pandya", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 16.35 Cr", retention: true },
                { name: "Rohit Sharma", role: "Batter", country: "India", isOverseas: false, price: "₹ 16.30 Cr", retention: true },
                { name: "Tilak Varma", role: "Batter", country: "India", isOverseas: false, price: "₹ 8.00 Cr", retention: true },
                { name: "Trent Boult", role: "Bowler", country: "New Zealand", isOverseas: true, price: "₹ 12.50 Cr", retention: false }
            ]
        },
        "Kolkata Knight Riders": {
            total: 120.0,
            spent: 55.0,
            primaryColor: "#3a225d",
            logo: "⚔️",
            squad: [
                { name: "Rinku Singh", role: "Batter", country: "India", isOverseas: false, price: "₹ 13.00 Cr", retention: true },
                { name: "Varun Chakaravarthy", role: "Bowler", country: "India", isOverseas: false, price: "₹ 12.00 Cr", retention: true },
                { name: "Sunil Narine", role: "All-Rounder", country: "West Indies", isOverseas: true, price: "₹ 12.00 Cr", retention: true },
                { name: "Andre Russell", role: "All-Rounder", country: "West Indies", isOverseas: true, price: "₹ 12.00 Cr", retention: true },
                { name: "Harshit Rana", role: "Bowler", country: "India", isOverseas: false, price: "₹ 4.00 Cr", retention: true }
            ]
        },
        "Sunrisers Hyderabad": {
            total: 120.0,
            spent: 70.0,
            primaryColor: "#f26522",
            logo: "🦅",
            squad: [
                { name: "Heinrich Klaasen", role: "Wicket-Keeper", country: "South Africa", isOverseas: true, price: "₹ 23.00 Cr", retention: true },
                { name: "Pat Cummins", role: "All-Rounder", country: "Australia", isOverseas: true, price: "₹ 18.00 Cr", retention: true },
                { name: "Abhishek Sharma", role: "Batter", country: "India", isOverseas: false, price: "₹ 14.00 Cr", retention: true },
                { name: "Travis Head", role: "Batter", country: "Australia", isOverseas: true, price: "₹ 14.00 Cr", retention: true }
            ]
        },
        "Rajasthan Royals": {
            total: 120.0,
            spent: 68.0,
            primaryColor: "#ea1a85",
            logo: "👑",
            squad: [
                { name: "Sanju Samson", role: "Wicket-Keeper", country: "India", isOverseas: false, price: "₹ 18.00 Cr", retention: true },
                { name: "Yashasvi Jaiswal", role: "Batter", country: "India", isOverseas: false, price: "₹ 18.00 Cr", retention: true },
                { name: "Riyan Parag", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 14.00 Cr", retention: true },
                { name: "Shimron Hetmyer", role: "Batter", country: "West Indies", isOverseas: true, price: "₹ 11.00 Cr", retention: true }
            ]
        },
        "Gujarat Titans": {
            total: 120.0,
            spent: 62.0,
            primaryColor: "#1b2133",
            logo: "⚡",
            squad: [
                { name: "Rashid Khan", role: "Bowler", country: "Afghanistan", isOverseas: true, price: "₹ 18.00 Cr", retention: true },
                { name: "Shubman Gill", role: "Batter", country: "India", isOverseas: false, price: "₹ 16.50 Cr", retention: true },
                { name: "Sai Sudharsan", role: "Batter", country: "India", isOverseas: false, price: "₹ 8.50 Cr", retention: true },
                { name: "Rahul Tewatia", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 4.00 Cr", retention: true }
            ]
        },
        "Lucknow Super Giants": {
            total: 120.0,
            spent: 58.0,
            primaryColor: "#a72056",
            logo: "🏏",
            squad: [
                { name: "Nicholas Pooran", role: "Wicket-Keeper", country: "West Indies", isOverseas: true, price: "₹ 21.00 Cr", retention: true },
                { name: "Ravi Bishnoi", role: "Bowler", country: "India", isOverseas: false, price: "₹ 11.00 Cr", retention: true },
                { name: "Mayank Yadav", role: "Bowler", country: "India", isOverseas: false, price: "₹ 11.00 Cr", retention: true }
            ]
        },
        "Delhi Capitals": {
            total: 120.0,
            spent: 60.0,
            primaryColor: "#0078bc",
            logo: "🐯",
            squad: [
                { name: "Axar Patel", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 16.50 Cr", retention: true },
                { name: "Kuldeep Yadav", role: "Bowler", country: "India", isOverseas: false, price: "₹ 13.25 Cr", retention: true },
                { name: "Tristan Stubbs", role: "Batter", country: "South Africa", isOverseas: true, price: "₹ 10.00 Cr", retention: true }
            ]
        },
        "Punjab Kings": {
            total: 120.0,
            spent: 45.0,
            primaryColor: "#ed1b24",
            logo: "🦁",
            squad: [
                { name: "Shashank Singh", role: "All-Rounder", country: "India", isOverseas: false, price: "₹ 5.50 Cr", retention: true },
                { name: "Prabhsimran Singh", role: "Wicket-Keeper", country: "India", isOverseas: false, price: "₹ 4.00 Cr", retention: true }
            ]
        }
    },

    selectedTeam: "Royal Challengers Bengaluru",

    init() {
        if (typeof document === 'undefined') return;
        this.populateTeamDropdown();
        this.renderMetrics();
        this.renderRosterTable();

        const teamSelect = document.getElementById('squad-team-select');
        if (teamSelect) {
            teamSelect.addEventListener('change', (e) => {
                this.selectedTeam = e.target.value;
                this.renderMetrics();
                this.renderRosterTable();
            });
        }
    },

    populateTeamDropdown() {
        const teamSelect = document.getElementById('squad-team-select');
        if (!teamSelect) return;

        teamSelect.innerHTML = Object.keys(this.teamsData).map(tName => `
            <option value="${tName}" ${tName === this.selectedTeam ? 'selected' : ''}>
                ${this.teamsData[tName].logo} ${tName}
            </option>
        `).join('');
    },

    /**
     * Render Financial Purse & Squad count cards
     */
    renderMetrics() {
        const teamData = this.teamsData[this.selectedTeam];
        if (!teamData) return;

        const squad = teamData.squad || [];
        const totalPlayers = squad.length;
        const overseasPlayers = squad.filter(p => p.isOverseas || (p.country && p.country !== 'India')).length;
        const remainingPurse = (teamData.total - teamData.spent).toFixed(2);
        const spentPercent = Math.min(100, Math.round((teamData.spent / teamData.total) * 100));

        const purseEl = document.getElementById('squad-remaining-purse');
        const spentEl = document.getElementById('squad-spent-purse');
        const squadCountEl = document.getElementById('squad-player-count');
        const overseasCountEl = document.getElementById('squad-overseas-count');
        const teamHeaderEl = document.getElementById('squad-team-header');
        const purseBarEl = document.getElementById('squad-purse-bar');

        if (purseEl) purseEl.textContent = `₹ ${remainingPurse} Cr`;
        if (spentEl) spentEl.textContent = `Total Spent: ₹ ${teamData.spent.toFixed(2)} Cr / ${teamData.total.toFixed(2)} Cr (${spentPercent}%)`;
        if (squadCountEl) squadCountEl.textContent = `${totalPlayers} / 25`;
        if (overseasCountEl) overseasCountEl.textContent = `${overseasPlayers} / 8`;
        if (teamHeaderEl) teamHeaderEl.innerHTML = `${teamData.logo} ${this.selectedTeam} — Financial & Squad Analytics`;

        if (purseBarEl) {
            purseBarEl.style.width = `${spentPercent}%`;
            if (remainingPurse < 10) {
                purseBarEl.classList.add('danger');
            } else {
                purseBarEl.classList.remove('danger');
            }
        }

        // Render Role Breakdown Counts
        this.renderRoleBreakdown(squad);
    },

    /**
     * Render Role composition count pills
     */
    renderRoleBreakdown(squad) {
        const breakdownContainer = document.getElementById('squad-role-breakdown');
        if (!breakdownContainer) return;

        const counts = {
            Batter: squad.filter(p => p.role === 'Batter').length,
            Bowler: squad.filter(p => p.role === 'Bowler').length,
            "All-Rounder": squad.filter(p => p.role === 'All-Rounder').length,
            "Wicket-Keeper": squad.filter(p => p.role === 'Wicket-Keeper').length
        };

        breakdownContainer.innerHTML = `
            <div class="role-pill">🏏 Batters: <strong>${counts.Batter}</strong></div>
            <div class="role-pill">🎯 Bowlers: <strong>${counts.Bowler}</strong></div>
            <div class="role-pill">⚡ All-Rounders: <strong>${counts['All-Rounder']}</strong></div>
            <div class="role-pill">🧤 Wicket-Keepers: <strong>${counts['Wicket-Keeper']}</strong></div>
        `;
    },

    /**
     * Render Full Team Squad Roster Table
     */
    renderRosterTable() {
        const tbody = document.getElementById('squad-roster-tbody');
        if (!tbody) return;

        const teamData = this.teamsData[this.selectedTeam];
        const squad = (teamData && teamData.squad) ? teamData.squad : [];

        if (squad.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 20px; color: var(--text-muted);">No players acquired yet. Participate in live auction!</td></tr>`;
            return;
        }

        tbody.innerHTML = squad.map((p, idx) => `
            <tr>
                <td>#${idx + 1}</td>
                <td>
                    <div style="font-weight: 600; display: flex; align-items: center; gap: 6px;">
                        ${p.name}
                        ${p.isOverseas ? '<span title="Overseas Player">✈️</span>' : ''}
                    </div>
                </td>
                <td><span class="card-tag" style="background: rgba(58, 134, 255, 0.15); color: var(--ipl-electric-blue);">${p.role}</span></td>
                <td>${p.country}</td>
                <td style="font-weight: 700; color: var(--ipl-gold);">${p.price}</td>
            </tr>
        `).join('');
    },

    /**
     * Check if team can afford a bid and satisfies squad constraints
     */
    canAffordBid(teamName, proposedBidVal, isOverseas) {
        const teamData = this.teamsData[teamName];
        if (!teamData) return { allowed: true };

        const squad = teamData.squad || [];
        const remainingPurse = teamData.total - teamData.spent;

        if (proposedBidVal > remainingPurse) {
            return { allowed: false, reason: `Purse exceeded! Remaining budget is only ₹ ${remainingPurse.toFixed(2)} Cr` };
        }

        if (squad.length >= 25) {
            return { allowed: false, reason: `Squad limit reached! Maximum 25 players allowed per franchise.` };
        }

        const overseasCount = squad.filter(p => p.isOverseas || (p.country && p.country !== 'India')).length;
        if (isOverseas && overseasCount >= 8) {
            return { allowed: false, reason: `Overseas quota full! Maximum 8 overseas players allowed.` };
        }

        return { allowed: true };
    },

    /**
     * Record purchase from Live Auction controller
     */
    recordPurchase(teamName, player, priceInCr) {
        if (!this.teamsData[teamName]) {
            this.teamsData[teamName] = { total: 120.0, spent: 0, squad: [] };
        }

        const team = this.teamsData[teamName];
        team.spent += priceInCr;
        team.squad.push({
            name: player.name,
            role: player.role,
            country: player.country,
            isOverseas: player.isOverseas,
            price: `₹ ${priceInCr.toFixed(2)} Cr`,
            retention: false
        });

        // Update AdminController team cards
        if (window.AdminController && window.AdminController.teams) {
            const adminTeam = window.AdminController.teams.find(t => t.name === teamName);
            if (adminTeam) {
                adminTeam.spent = team.spent;
                adminTeam.players = team.squad.length;
                if (player.isOverseas) adminTeam.overseas += 1;
                window.AdminController.renderTeamCards();
            }
        }

        this.renderMetrics();
        this.renderRosterTable();
    },

    /**
     * Update Team Purse Limit
     */
    updateTeamPurse(teamName, newPurse) {
        if (this.teamsData[teamName]) {
            this.teamsData[teamName].total = newPurse;
            this.renderMetrics();
        }
    },

    /**
     * Export Squad Report to JSON / CSV
     */
    exportSquadReport() {
        const teamData = this.teamsData[this.selectedTeam];
        if (!teamData) return;

        const dataStr = "data:text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(teamData, null, 2));
        const downloadAnchor = document.createElement('a');
        downloadAnchor.setAttribute("href", dataStr);
        downloadAnchor.setAttribute("download", `${this.selectedTeam.replace(/\s+/g, '_')}_Squad_Report.json`);
        document.body.appendChild(downloadAnchor);
        downloadAnchor.click();
        downloadAnchor.remove();

        if (window.Toast) window.Toast.show(`Exported squad report for ${this.selectedTeam}`, 'success');
    }
};

if (typeof window !== 'undefined') {
    window.SquadController = SquadController;
    document.addEventListener('DOMContentLoaded', () => SquadController.init());
}

if (typeof module !== 'undefined') {
    module.exports = SquadController;
}
