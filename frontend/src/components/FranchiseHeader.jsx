import React from 'react';

function FranchiseHeader({ user, activeTeam, totalPlayers, currentIndex, currentValuation, onLogout }) {
  // Format budget or valuation into Cr (Crores)
  const formatToCr = (amount) => {
    if (!amount) return '₹0.00 Cr';
    return `₹${(amount / 10000000).toFixed(2)} Cr`;
  };

  return (
    <header className="app-header">
      <div className="header-brand">
        <span className="brand-icon">🏟️</span>
        <div>
          <h1 className="brand-title">[ THE ORBITAL HEX-SYNAPSE ]</h1>
          <p className="brand-subtitle">
            <span className="telemetry-dot">●</span> SYS TELEMETRY: ACTIVE | LOT: {totalPlayers > 0 ? currentIndex + 1 : 0} / {totalPlayers}
          </p>
        </div>
      </div>

      <div className="header-timer">
        <span className="timer-label">AUCTION TIMER</span>
        <span className="timer-value">00:00.00</span>
      </div>

      <div className="user-profile-widget">
        <div className="valuation-badge">
          <span className="valuation-label">CURRENT VALUATION</span>
          <span className="valuation-value">{formatToCr(currentValuation)}</span>
        </div>

        <div className="user-details">
          <span className="username-display">{user.username}</span>
          <span className="user-role-badge">
            {user.role === 'ADMIN' ? 'AUCTIONEER' : 'FRANCHISE OWNER'}
          </span>
        </div>

        <button onClick={onLogout} className="logout-button">
          EXIT
        </button>
      </div>
    </header>
  );
}

export default FranchiseHeader;
