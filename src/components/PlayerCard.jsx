function PlayerCard({ player }) {
  if (!player) {
    return (
      <div className="player-card empty">
        <p>No players currently on the auction block.</p>
      </div>
    );
  }

  const getRoleIcon = (role) => {
    switch (role) {
      case 'Batsman': return '🏏';
      case 'Bowler': return '🥎';
      case 'All-Rounder': return '⚡';
      case 'Wicketkeeper-Batsman': return '🧤';
      default: return '👤';
    }
  };

  const getStatusClass = (status) => {
    return status === 'SOLD' ? 'status-sold' : 'status-unsold';
  };

  return (
    <div className={`player-card-glass ${getStatusClass(player.status)}`}>
      <div className="card-header">
        <span className={`status-badge ${getStatusClass(player.status)}`}>
          {player.status}
        </span>
        <div className="player-role-badge">
          <span className="role-icon">{getRoleIcon(player.role)}</span>
          <span className="role-text">{player.role}</span>
        </div>
      </div>

      <div className="card-body">
        <h2 className="player-name">{player.name}</h2>
        
        <div className="price-container">
          <div className="price-box">
            <span className="price-label">REGISTERED BASE</span>
            <span className="price-value base-price">₹{(player.basePrice / 10000000).toFixed(2)} Cr</span>
          </div>
          
          <div className="price-box active-bid-box">
            <span className="price-label">
              {player.status === 'SOLD' ? 'FINAL ACQUISITION PRICE' : 'CURRENT LEADING BID'}
            </span>
            <span className="price-value current-bid">
              ₹{player.basePrice?.toLocaleString('en-IN')}
            </span>
          </div>
        </div>
      </div>

      {player.status === 'SOLD' && (
        <div className="sold-banner">
          <span className="sold-trophy">🏆</span>
          <span>Acquired by <strong>{player.team?.name}</strong></span>
        </div>
      )}
    </div>
  );
}

export default PlayerCard;
