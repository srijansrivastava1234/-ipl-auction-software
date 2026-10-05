import React from 'react';

function Leaderboard({ teams, activeTeamId }) {
  const INITIAL_PURSE = 1000000000; // 100 Cr

  const getUtilizedPercent = (budget) => {
    const remaining = Number(budget) || 0;
    const utilized = INITIAL_PURSE - remaining;
    const pct = (utilized / INITIAL_PURSE) * 100;
    return Math.max(0, Math.min(100, pct));
  };

  // Sort teams by highest remaining purse
  const sortedTeams = [...teams].sort((a, b) => b.budget - a.budget);

  return (
    <div className="leaderboard-card-glass">
      <div className="leaderboard-header-row">
        <div className="leaderboard-title-group">
          <span className="leaderboard-icon cyan-text">📊</span>
          <h3 className="leaderboard-title">FRANCHISE PURSE CONTAINMENT LEADERBOARD</h3>
        </div>
        <span className="leaderboard-status-tag font-mono">10 ACTIVE SECTORS</span>
      </div>
      
      <div className="table-responsive">
        <table className="leaderboard-table">
          <thead>
            <tr>
              <th className="th-rank font-mono">#</th>
              <th>FRANCHISE</th>
              <th className="th-num font-mono">REMAINING PURSE</th>
              <th>BUDGET ALLOCATION &amp; UTILIZATION</th>
              <th className="th-status">TELEMETRY</th>
            </tr>
          </thead>
          <tbody>
            {sortedTeams.map((team, idx) => {
              const utilPct = getUtilizedPercent(team.budget);
              const isCurrentTeam = activeTeamId === team.id;
              const alias = team.name.split(' ').map(w => w[0]).join('');

              return (
                <tr key={team.id} className={isCurrentTeam ? 'highlight-active-team' : ''}>
                  <td className="team-rank-col font-mono">{idx + 1}</td>
                  <td className="team-name-col">
                    <span className="team-tag font-mono">{alias}</span>
                    <span className="full-name">{team.name}</span>
                    {isCurrentTeam && <span className="your-team-badge">YOU</span>}
                  </td>
                  <td className="team-purse-col font-mono">
                    ₹{(team.budget / 10000000).toFixed(2)} Cr
                  </td>
                  <td className="team-progress-col">
                    <div className="progress-bar-container">
                      <div
                        className="progress-bar-fill"
                        style={{ width: `${utilPct}%` }}
                      ></div>
                      <span className="progress-text font-mono">{utilPct.toFixed(1)}% COMMITTED</span>
                    </div>
                  </td>
                  <td className="team-telemetry-col font-mono">
                    <span className="status-ping">●</span> ACTIVE
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Leaderboard;
