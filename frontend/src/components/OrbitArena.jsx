import React from 'react';

const getTeamAlias = (teamName) => {
  if (!teamName) return '';
  if (teamName.includes('Chennai')) return 'CSK';
  if (teamName.includes('Mumbai')) return 'MI';
  if (teamName.includes('Royal Challengers') || teamName.includes('Bengaluru')) return 'RCB';
  if (teamName.includes('Kolkata')) return 'KKR';
  if (teamName.includes('Rajasthan')) return 'RR';
  if (teamName.includes('Sunrisers') || teamName.includes('Hyderabad')) return 'SRH';
  if (teamName.includes('Delhi')) return 'DC';
  if (teamName.includes('Gujarat')) return 'GT';
  if (teamName.includes('Lucknow')) return 'LSG';
  if (teamName.includes('Punjab')) return 'PBKS';
  return teamName.substring(0, 3).toUpperCase();
};

const teamPositions = {
  'PBKS': { x: 0, y: -160 },
  'SRH': { x: -140, y: -100 },
  'RCB': { x: -70, y: -50 },
  'LSG': { x: -220, y: 80 },
  'RR': { x: -130, y: 80 },
  'MI': { x: -60, y: 70 },
  'GT': { x: 130, y: -100 },
  'CSK': { x: 110, y: 0 },
  'KKR': { x: 130, y: 80 },
  'DC': { x: 230, y: 80 },
};

function OrbitArena({ player, teams, activeHighestBid, onTeamClick }) {
  const activeBidTeamAlias = activeHighestBid && activeHighestBid.team 
    ? getTeamAlias(activeHighestBid.team.name) 
    : (player && player.status === 'SOLD' && player.team ? getTeamAlias(player.team.name) : null);

  return (
    <div className="orbit-arena-container">
      {/* Background Orbits */}
      <svg className="orbit-svg-bg" viewBox="0 0 800 500">
        {/* Concentric dotted orbit ellipses */}
        <ellipse cx="400" cy="220" rx="160" ry="100" className="orbit-ellipse orbit-inner" />
        <ellipse cx="400" cy="220" rx="280" ry="170" className="orbit-ellipse orbit-outer" />
        
        {/* Left indicators curved lines */}
        <path d="M 120 180 Q 180 220 120 260" fill="none" className="hud-curve left-curve" />
        
        {/* Right indicators curved lines */}
        <path d="M 680 180 Q 620 220 680 260" fill="none" className="hud-curve right-curve" />

        {/* Glow Line connecting center to active bid team */}
        {activeBidTeamAlias && teamPositions[activeBidTeamAlias] && (
          <line 
            x1="400" 
            y1="220" 
            x2={400 + teamPositions[activeBidTeamAlias].x} 
            y2={220 + teamPositions[activeBidTeamAlias].y} 
            className="active-bid-beam"
          />
        )}
      </svg>

      {/* HUD Telemetry text: Left & Right Columns */}
      <div className="hud-column hud-left">
        <div className="hud-label orange">POWERPLAY</div>
        <div className="hud-label green">MIDDLE OVERS</div>
        <div className="hud-label blue">DEATH OVERS</div>
      </div>

      <div className="hud-column hud-right">
        <div className="hud-label orange reverse">POWERPLAY</div>
        <div className="hud-label green reverse">MIDDLE OVERS</div>
        <div className="hud-label blue reverse">DEATH OVERS</div>
      </div>

      {/* Central Orbit Area */}
      <div className="orbit-center">
        {/* Cricket Bat & Particles */}
        <div className="bat-halo-wrapper">
          <div className="halo-ring ring-1"></div>
          <div className="halo-ring ring-2"></div>
          
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="glowing-bat">
            <path d="M18 3a2.1 2.1 0 0 0-3 0L3 15v6h6L21 9a2.1 2.1 0 0 0 0-3z" />
            <path d="M9 15l6-6" />
            <path d="M17 3l4 4" />
          </svg>
          
          {/* Neon sparks */}
          <div className="telemetry-spark spark-1">✦</div>
          <div className="telemetry-spark spark-2">✦</div>
          <div className="telemetry-spark spark-3">✦</div>
          <div className="telemetry-spark spark-4">✦</div>
        </div>

        {/* Floating Team Nodes */}
        {teams.map((team) => {
          const alias = getTeamAlias(team.name);
          const pos = teamPositions[alias] || { x: 0, y: 0 };
          const isActive = activeBidTeamAlias === alias;
          
          return (
            <div 
              key={team.id}
              className={`team-orbit-node ${alias.toLowerCase()} ${isActive ? 'active' : ''}`}
              style={{
                transform: `translate(${pos.x}px, ${pos.y}px)`,
                cursor: 'pointer'
              }}
              onClick={() => onTeamClick && onTeamClick(team)}
            >
              <span className="node-text">{isActive ? `🔥 ${alias}` : alias}</span>
              {isActive && <span className="active-glow-ring"></span>}
            </div>
          );
        })}

        {/* Player Telemetry Card positioned in the middle, below the bat */}
        {player && (
          <div className="center-player-telemetry-card">
            <h2 className="telemetry-player-name">{player.name}</h2>
            <div className="telemetry-player-meta">
              {player.role.toUpperCase()} | BASE: ₹{(player.originalBasePrice / 10000000).toFixed(2)} CR
            </div>
            <div className={`telemetry-status-badge ${player.status.toLowerCase()}`}>
              {player.status === 'SOLD' ? '🏆 SOLD' : '🔥 LIVE'}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OrbitArena;
