import React, { useEffect, useRef, useMemo, useState } from 'react';
import { TEAM_THEMES, getCustomThemes } from '../teamThemes';
import { getPlayerAnalytics } from '../playerAnalytics';

const getTeamMeta = (teamName) => {
  if (!teamName) return { short: 'IPL', badge: '🏏', color: '#00f2fe', accent: '#0284c7' };
  
  const customThemes = getCustomThemes();
  for (const [key, val] of Object.entries(customThemes)) {
    if (val.name && (teamName.toUpperCase() === val.name.toUpperCase() || teamName.includes(val.short))) {
      return {
        short: val.short || key,
        badge: val.badge || '🏏',
        color: val.color || '#10b981',
        accent: val.accent || val.color || '#10b981'
      };
    }
  }

  const nameUpper = teamName.toUpperCase();
  if (nameUpper.includes('CHENNAI') || nameUpper.includes('CSK')) return { ...TEAM_THEMES.CSK, short: 'CSK' };
  if (nameUpper.includes('MUMBAI') || nameUpper.includes('MI')) return { ...TEAM_THEMES.MI, short: 'MI' };
  if (nameUpper.includes('ROYAL CHALLENGERS') || nameUpper.includes('BENGALURU') || nameUpper.includes('RCB')) return { ...TEAM_THEMES.RCB, short: 'RCB' };
  if (nameUpper.includes('KOLKATA') || nameUpper.includes('KNIGHT') || nameUpper.includes('KKR')) return { ...TEAM_THEMES.KKR, short: 'KKR' };
  if (nameUpper.includes('RAJASTHAN') || nameUpper.includes('ROYALS') || nameUpper.includes('RR')) return { ...TEAM_THEMES.RR, short: 'RR' };
  if (nameUpper.includes('SUNRISERS') || nameUpper.includes('HYDERABAD') || nameUpper.includes('SRH')) return { ...TEAM_THEMES.SRH, short: 'SRH' };
  if (nameUpper.includes('DELHI') || nameUpper.includes('CAPITALS') || nameUpper.includes('DC')) return { ...TEAM_THEMES.DC, short: 'DC' };
  if (nameUpper.includes('GUJARAT') || nameUpper.includes('TITANS') || nameUpper.includes('GT')) return { ...TEAM_THEMES.GT, short: 'GT' };
  if (nameUpper.includes('LUCKNOW') || nameUpper.includes('SUPER GIANTS') || nameUpper.includes('LSG')) return { ...TEAM_THEMES.LSG, short: 'LSG' };
  if (nameUpper.includes('PUNJAB') || nameUpper.includes('KINGS') || nameUpper.includes('PBKS')) return { ...TEAM_THEMES.PBKS, short: 'PBKS' };
  
  return {
    short: teamName.substring(0, 4).toUpperCase(),
    badge: '🏏',
    color: '#38bdf8',
    accent: '#0284c7'
  };
};

function OrbitArena({ player, nextPlayer, teams, activeHighestBid, onTeamClick, userTeamId, teamTheme }) {
  const activeBidTeamId = activeHighestBid && activeHighestBid.team ? activeHighestBid.team.id : null;
  const activeBidTeamName = activeHighestBid && activeHighestBid.team ? activeHighestBid.team.name : (player && player.status === 'SOLD' && player.team ? player.team.name : null);

  // Analytics for active and next players
  const currentAnalytics = useMemo(() => getPlayerAnalytics(player), [player]);
  const nextAnalytics = useMemo(() => getPlayerAnalytics(nextPlayer), [nextPlayer]);

  // Distribute teams across Inner and Outer orbital tracks
  const { innerOrbitTeams, outerOrbitTeams } = useMemo(() => {
    const inner = [];
    const outer = [];

    teams.forEach((team, idx) => {
      const meta = getTeamMeta(team.name);
      const enriched = { ...team, meta };
      if (idx % 2 === 0) {
        inner.push(enriched);
      } else {
        outer.push(enriched);
      }
    });

    return { innerOrbitTeams: inner, outerOrbitTeams: outer };
  }, [teams]);

  // Orbit Radii
  const INNER_RX = 265;
  const INNER_RY = 145;
  const OUTER_RX = 385;
  const OUTER_RY = 220;

  // Refs for smooth RequestAnimationFrame orbital rotation along true elliptical tracks
  const isPausedRef = useRef(false);
  const innerAngleRef = useRef(0);
  const outerAngleRef = useRef(Math.PI / 4);
  const innerNodeRefs = useRef([]);
  const outerNodeRefs = useRef([]);

  useEffect(() => {
    let animId;
    let lastTime = performance.now();

    const animate = (time) => {
      const delta = (time - lastTime) / 1000;
      lastTime = time;

      if (!isPausedRef.current) {
        // Inner track orbital angular velocity (rad/s)
        innerAngleRef.current += 0.22 * delta;
        // Outer track orbital angular velocity (counter-orbit or slower orbit)
        outerAngleRef.current -= 0.16 * delta;

        // Position inner orbit teams
        innerOrbitTeams.forEach((_, idx) => {
          const el = innerNodeRefs.current[idx];
          if (el) {
            const phase = innerAngleRef.current + (idx / innerOrbitTeams.length) * 2 * Math.PI;
            const x = Math.round(INNER_RX * Math.cos(phase));
            const y = Math.round(INNER_RY * Math.sin(phase));
            el.style.transform = `translate3d(${x}px, ${y}px, 0)`;
          }
        });

        // Position outer orbit teams
        outerOrbitTeams.forEach((_, idx) => {
          const el = outerNodeRefs.current[idx];
          if (el) {
            const phase = outerAngleRef.current + (idx / outerOrbitTeams.length) * 2 * Math.PI;
            const x = Math.round(OUTER_RX * Math.cos(phase));
            const y = Math.round(OUTER_RY * Math.sin(phase));
            el.style.transform = `translate3d(${x}px, ${y}px, 0)`;
          }
        });
      }

      animId = requestAnimationFrame(animate);
    };

    animId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(animId);
  }, [innerOrbitTeams, outerOrbitTeams]);

  return (
    <div 
      className="orbit-arena-container"
      onMouseEnter={() => { isPausedRef.current = true; }}
      onMouseLeave={() => { isPausedRef.current = false; }}
    >
      {/* Background SVG Grid & True Orbit Guide Ellipses */}
      <svg className="orbit-svg-bg" viewBox="0 0 1000 560">
        <ellipse cx="500" cy="280" rx={INNER_RX} ry={INNER_RY} className="orbit-ellipse orbit-inner-guide" />
        <ellipse cx="500" cy="280" rx={OUTER_RX} ry={OUTER_RY} className="orbit-ellipse orbit-outer-guide" />
        
        {/* Curved HUD side brackets */}
        <path d="M 60 210 Q 130 280 60 350" fill="none" className="hud-curve left-curve" />
        <path d="M 940 210 Q 870 280 940 350" fill="none" className="hud-curve right-curve" />
      </svg>

      {/* LEFT SIDE PANEL: Past Performance & 2026 AI Prediction */}
      <div className="hud-telemetry-dock hud-left-telemetry">
        <div className="hud-glass-card performance-hud-card">
          <div className="hud-card-header">
            <span className="hud-card-badge font-mono">📊 PAST PERFORMANCE</span>
            <span className="hud-status-dot green"></span>
          </div>

          {currentAnalytics ? (
            <div className="player-stats-content">
              <div className="stat-chips-grid font-mono">
                <div className="stat-chip">
                  <span className="chip-label">IPL MATCHES</span>
                  <span className="chip-val">{currentAnalytics.matches}</span>
                </div>
                <div className="stat-chip">
                  <span className="chip-label">{currentAnalytics.runs ? 'TOTAL RUNS' : 'TOTAL WKTS'}</span>
                  <span className="chip-val highlight-gold">{currentAnalytics.runs || currentAnalytics.wickets}</span>
                </div>
                <div className="stat-chip">
                  <span className="chip-label">{currentAnalytics.strikeRate ? 'CAREER SR' : 'ECONOMY'}</span>
                  <span className="chip-val">{currentAnalytics.strikeRate || currentAnalytics.economy}</span>
                </div>
                <div className="stat-chip">
                  <span className="chip-label">{currentAnalytics.avg ? 'AVERAGE' : 'DOT %'}</span>
                  <span className="chip-val">{currentAnalytics.avg || currentAnalytics.dotBallPct}</span>
                </div>
              </div>

              {/* 2026 AI Prediction Engine */}
              <div className="ai-forecast-deck">
                <div className="forecast-title-row font-mono">
                  <span className="forecast-tag">⚡ 2026 AI FORECAST</span>
                  <span className="forecast-impact-tier">{currentAnalytics.impactTier}</span>
                </div>

                <div className="forecast-main-metric font-display">
                  {currentAnalytics.forecastRuns}
                </div>
                <div className="forecast-highlight-sub font-mono">
                  {currentAnalytics.forecastHighlight}
                </div>

                <div className="forecast-rating-bar-wrap">
                  <div className="rating-label-row font-mono">
                    <span>IMPACT INDEX</span>
                    <span className="rating-num">{currentAnalytics.impactScore}%</span>
                  </div>
                  <div className="rating-track">
                    <div 
                      className="rating-fill" 
                      style={{ width: `${currentAnalytics.impactScore}%` }}
                    ></div>
                  </div>
                </div>

                <div className="tactical-breakdown-row font-mono">
                  <span>PP: <strong>{currentAnalytics.powerplayRating}</strong></span>
                  <span>•</span>
                  <span>DEATH: <strong>{currentAnalytics.deathRating}</strong></span>
                </div>
              </div>
            </div>
          ) : (
            <div className="empty-hud-note font-mono">STANDBY FOR TELEMETRY...</div>
          )}
        </div>
      </div>

      {/* RIGHT SIDE PANEL: Info About Next Player in the Bid */}
      <div className="hud-telemetry-dock hud-right-telemetry">
        <div className="hud-glass-card next-player-hud-card">
          <div className="hud-card-header">
            <span className="hud-card-badge font-mono">⏭️ UP NEXT IN THE BID</span>
            <span className="on-deck-pulse font-mono">🟡 ON DECK</span>
          </div>

          {nextPlayer ? (
            <div className="next-player-content">
              <div className="next-lot-badge font-mono">
                LOT #{nextPlayer.id} • QUEUED IN TUNNEL
              </div>

              <div className="next-player-name font-display">
                {nextPlayer.name}
              </div>

              <div className="next-player-meta font-mono">
                <span className="next-role-pill">{nextPlayer.role.toUpperCase()}</span>
                <span className="meta-dot">•</span>
                <span className="next-country-pill">{nextPlayer.country || 'INDIA'}</span>
              </div>

              <div className="next-player-price-box font-mono">
                <span className="price-box-label">STARTING VALUATION</span>
                <span className="price-box-value font-mono">
                  ₹{((nextPlayer.originalBasePrice || nextPlayer.basePrice) / 10000000).toFixed(2)} Cr
                </span>
              </div>

              {nextAnalytics && (
                <div className="next-teaser-stats font-mono">
                  <div className="teaser-stat-item">
                    <span className="teaser-label">EXPERIENCE:</span>
                    <span className="teaser-val">{nextAnalytics.matches} IPL MATCHES</span>
                  </div>
                  <div className="teaser-stat-item">
                    <span className="teaser-label">CAREER RECORD:</span>
                    <span className="teaser-val highlight">
                      {nextAnalytics.runs ? `${nextAnalytics.runs} RUNS (${nextAnalytics.strikeRate} SR)` : `${nextAnalytics.wickets} WKTS (${nextAnalytics.economy} ECO)`}
                    </span>
                  </div>
                  <div className="teaser-stat-item">
                    <span className="teaser-label">2026 TARGET:</span>
                    <span className="teaser-val highlight-forecast">{nextAnalytics.forecastRuns}</span>
                  </div>
                </div>
              )}

              <div className="next-standby-footer font-mono">
                <span className="standby-dot"></span> READY TO ENTER ARENA
              </div>
            </div>
          ) : (
            <div className="final-lot-card font-mono">
              <span className="final-icon">🏁</span>
              <span className="final-text">CURRENT LOT IS LAST IN AUCTION POOL</span>
            </div>
          )}
        </div>
      </div>

      {/* Center Anchor for Orbiting Team Nodes */}
      <div className="orbit-center">
        {/* INNER ORBIT TRACK (Rotating teams around the center) */}
        {innerOrbitTeams.map((team, idx) => {
          const isActive = activeBidTeamId ? team.id === activeBidTeamId : (activeBidTeamName && team.name === activeBidTeamName);
          const isUserTeam = userTeamId === team.id;
          const initialAngle = (idx / innerOrbitTeams.length) * 2 * Math.PI;
          const initX = Math.round(INNER_RX * Math.cos(initialAngle));
          const initY = Math.round(INNER_RY * Math.sin(initialAngle));

          return (
            <div
              key={team.id}
              ref={(el) => (innerNodeRefs.current[idx] = el)}
              className="orbit-satellite-anchor"
              style={{ transform: `translate3d(${initX}px, ${initY}px, 0)` }}
            >
              <div
                className={`team-orbit-node ${isActive ? 'active' : ''} ${isUserTeam ? 'user-franchise-node' : ''}`}
                style={{
                  borderColor: team.meta.color,
                  '--node-color': team.meta.color,
                  boxShadow: isActive ? `0 0 25px ${team.meta.color}` : `0 0 12px ${team.meta.color}40`
                }}
                onClick={() => onTeamClick && onTeamClick(team)}
                title={`${team.name} • Available Purse: ₹${(team.budget / 10000000).toFixed(2)} Cr (Click to bid)`}
              >
                <span className="node-crest">{team.meta.badge}</span>
                <span className="node-team-name font-display" style={{ color: team.meta.color }}>
                  {team.meta.short || team.name}
                </span>
                <span className="node-team-purse font-mono">
                  ₹{(team.budget / 10000000).toFixed(0)}Cr
                </span>
                {isActive && <span className="active-flame-beacon">🔥</span>}
                {isUserTeam && !isActive && <span className="user-team-orbit-halo"></span>}
              </div>
            </div>
          );
        })}

        {/* OUTER ORBIT TRACK (Rotating teams around the center) */}
        {outerOrbitTeams.map((team, idx) => {
          const isActive = activeBidTeamId ? team.id === activeBidTeamId : (activeBidTeamName && team.name === activeBidTeamName);
          const isUserTeam = userTeamId === team.id;
          const initialAngle = Math.PI / 4 + (idx / outerOrbitTeams.length) * 2 * Math.PI;
          const initX = Math.round(OUTER_RX * Math.cos(initialAngle));
          const initY = Math.round(OUTER_RY * Math.sin(initialAngle));

          return (
            <div
              key={team.id}
              ref={(el) => (outerNodeRefs.current[idx] = el)}
              className="orbit-satellite-anchor"
              style={{ transform: `translate3d(${initX}px, ${initY}px, 0)` }}
            >
              <div
                className={`team-orbit-node ${isActive ? 'active' : ''} ${isUserTeam ? 'user-franchise-node' : ''}`}
                style={{
                  borderColor: team.meta.color,
                  '--node-color': team.meta.color,
                  boxShadow: isActive ? `0 0 25px ${team.meta.color}` : `0 0 12px ${team.meta.color}40`
                }}
                onClick={() => onTeamClick && onTeamClick(team)}
                title={`${team.name} • Available Purse: ₹${(team.budget / 10000000).toFixed(2)} Cr (Click to bid)`}
              >
                <span className="node-crest">{team.meta.badge}</span>
                <span className="node-team-name font-display" style={{ color: team.meta.color }}>
                  {team.meta.short || team.name}
                </span>
                <span className="node-team-purse font-mono">
                  ₹{(team.budget / 10000000).toFixed(0)}Cr
                </span>
                {isActive && <span className="active-flame-beacon">🔥</span>}
                {isUserTeam && !isActive && <span className="user-team-orbit-halo"></span>}
              </div>
            </div>
          );
        })}

        {/* Central Halo & Holographic Cricket Icon */}
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

        {/* Centerpiece: Player Name Card (The Core Around Which All Team Names Rotate) */}
        {player && (
          <div className="center-player-telemetry-card">
            <div className="card-orbit-core-glow"></div>
            <div className="player-lot-pill font-mono">LOT #{player.id} • LIVE BIDDING</div>
            <h2 className="telemetry-player-name font-display">{player.name}</h2>
            <div className="telemetry-player-meta font-mono">
              <span className="player-role-tag">{player.role.toUpperCase()}</span>
              <span className="meta-separator">•</span>
              <span className="player-country-tag">{player.country || 'INDIA'}</span>
            </div>
            <div className={`telemetry-status-badge font-mono ${player.status.toLowerCase()}`}>
              {player.status === 'SOLD' ? '🏆 HAMMER DOWN: SOLD' : '⚡ LIVE IN ORBIT ARENA'}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default OrbitArena;
