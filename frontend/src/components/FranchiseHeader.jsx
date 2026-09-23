import React from 'react';

function FranchiseHeader({ 
  user, 
  activeTeam, 
  teamTheme, 
  totalPlayers, 
  currentIndex, 
  currentValuation, 
  timerValue,
  onOpenAddPlayer,
  onLogout 
}) {
  // Format budget or valuation into Cr (Crores)
  const formatToCr = (amount) => {
    if (!amount) return '₹0.00 Cr';
    return `₹${(amount / 10000000).toFixed(2)} Cr`;
  };

  const theme = teamTheme || {
    name: 'IPL ARENA',
    short: 'IPL',
    badge: '🏟️',
    tag: 'War Room Active',
    slogan: 'Official Bidding Hub',
    color: '#06b6d4'
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <header className="app-header team-themed-header">
      <div className="header-brand">
        <div className="brand-badge-circle">
          <span className="brand-icon">{theme.badge || '🏟️'}</span>
        </div>
        <div>
          <div className="brand-title-wrap">
            <span className="brand-team-tag font-mono">{theme.short}</span>
            <h1 className="brand-title">[ {theme.name} • WAR ROOM ]</h1>
          </div>
          <p className="brand-subtitle font-mono">
            <span className="telemetry-dot">●</span> {theme.slogan.toUpperCase()} | LOT: {totalPlayers > 0 ? currentIndex + 1 : 0} / {totalPlayers}
          </p>
        </div>
      </div>

      <div className="header-timer">
        <span className="timer-label font-mono">AUCTION TIMER</span>
        <span className="timer-value font-mono">{timerValue || '00:00.00'}</span>
      </div>

      <div className="user-profile-widget">
        <div className="valuation-badge">
          <span className="valuation-label font-mono">CURRENT VALUATION</span>
          <span className="valuation-value font-mono">{formatToCr(currentValuation)}</span>
        </div>

        {isAdmin && onOpenAddPlayer && (
          <button 
            onClick={onOpenAddPlayer} 
            className="header-add-player-btn font-mono"
            title="Draft New Player into IPL Auction Pool"
          >
            ➕ ADD PLAYER
          </button>
        )}

        <div className="user-details">
          <span className="username-display">{user.username}</span>
          <span className="user-role-badge font-mono">
            {isAdmin ? 'AUCTIONEER' : `${theme.short} CHIEF`}
          </span>
        </div>

        <button onClick={onLogout} className="logout-button font-mono">
          EXIT
        </button>
      </div>
    </header>
  );
}

export default FranchiseHeader;
