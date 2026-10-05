import { useState } from 'react';

function BiddingConsole({
  user,
  player,
  teamTheme,
  onPlaceBid,
  onSellPlayer,
  onUnsoldPlayer,
  onPrevPlayer,
  onNextPlayer,
  onOpenAddPlayer,
  currentIndex,
  totalPlayers
}) {
  const [customBidStr, setCustomBidStr] = useState('');

  if (!player) return null;

  const isSold = player.status === 'SOLD';
  const isAdmin = user.role === 'ADMIN';
  const isOwner = user.role === 'TEAM_OWNER';

  const theme = teamTheme || {
    name: 'FRANCHISE',
    short: 'TEAM',
    badge: '🏏',
    tag: 'War Room Active',
    slogan: 'Bidding Desk',
    color: '#06b6d4'
  };

  const handleCustomSubmit = (e) => {
    e.preventDefault();
    const val = Number(customBidStr);
    if (!val || isNaN(val)) return;
    onPlaceBid(val, false); // false indicates absolute amount
    setCustomBidStr('');
  };

  return (
    <div className="console-container">
      {/* Auctioneer Stage Controls */}
      {isAdmin && (
        <div className="console-panel auctioneer-panel">
          <div className="console-panel-header">
            <div className="console-header-left-wrap">
              <span className="console-badge-icon">⚖️</span>
              <div>
                <h3 className="panel-title font-display">GLOBAL AUCTIONEER DESK</h3>
                <span className="console-sub-label font-mono">BCCI STAGE CONTROLLER • LOT #{currentIndex + 1}</span>
              </div>
            </div>
            {onOpenAddPlayer && (
              <button 
                onClick={onOpenAddPlayer}
                className="admin-quick-add-btn font-mono"
                title="Draft New Player into Auction Pool"
              >
                ➕ ADD NEW PLAYER
              </button>
            )}
          </div>
          
          <div className="action-buttons-row">
            <button
              onClick={onSellPlayer}
              disabled={isSold}
              className={`action-button hammer-button font-mono ${isSold ? 'disabled' : ''}`}
            >
              🔨 HAMMER DOWN
            </button>
            <button
              onClick={onUnsoldPlayer}
              disabled={player.status === 'UNSOLD'}
              className={`action-button unsold-button font-mono ${player.status === 'UNSOLD' ? 'disabled' : ''}`}
            >
              ♦ UNSOLD
            </button>
          </div>

          <div className="stage-navigation">
            <button
              onClick={onPrevPlayer}
              disabled={currentIndex === 0}
              className="nav-button prev-button font-mono"
            >
              ◀ PREV
            </button>
            <span className="stage-indicator font-mono">
              {currentIndex + 1} / {totalPlayers}
            </span>
            <button
              onClick={onNextPlayer}
              disabled={currentIndex === totalPlayers - 1}
              className="nav-button next-button font-mono"
            >
              NEXT ▶
            </button>
          </div>
        </div>
      )}

      {/* Franchise Owner Console */}
      {isOwner && (
        <div className="console-panel owner-panel">
          <div className="console-panel-header">
            <div className="console-team-icon-pill">
              <span className="console-badge-icon">{theme.badge || '🏏'}</span>
            </div>
            <div>
              <div className="console-title-row">
                <span className="console-team-tag font-mono">{theme.short}</span>
                <h3 className="panel-title font-display">{theme.name} BIDDING DESK</h3>
              </div>
              <span className="console-sub-label font-mono">
                {theme.slogan.toUpperCase()} • 1-CLICK TACTICAL BID
              </span>
            </div>
          </div>
          
          {!isSold ? (
            <div className="bidding-actions">
              <div className="increment-row">
                <button
                  onClick={() => onPlaceBid(2000000, true)} // true indicates incremental
                  className="bid-increment-btn l20 font-mono"
                >
                  <span className="btn-plus">+</span>₹20L
                </button>
                <button
                  onClick={() => onPlaceBid(5000000, true)}
                  className="bid-increment-btn l50 font-mono"
                >
                  <span className="btn-plus">+</span>₹50L
                </button>
                <button
                  onClick={() => onPlaceBid(10000000, true)}
                  className="bid-increment-btn c1 font-mono"
                >
                  <span className="btn-plus">+</span>₹1Cr
                </button>
              </div>

              <form onSubmit={handleCustomSubmit} className="custom-bid-form">
                <input
                  type="number"
                  placeholder="Custom bid amount (₹)"
                  value={customBidStr}
                  onChange={(e) => setCustomBidStr(e.target.value)}
                  className="custom-bid-input font-mono"
                />
                <button type="submit" className="custom-bid-submit font-mono">
                  BID NOW
                </button>
              </form>
            </div>
          ) : (
            <div className="bidding-msg finished font-mono">
              🔒 AUCTION CLOSED • ACQUIRED BY {player.team?.name?.toUpperCase()}
            </div>
          )}

          <div className="stage-navigation">
            <button
              onClick={onPrevPlayer}
              disabled={currentIndex === 0}
              className="nav-button prev-button font-mono"
            >
              ◀ PREV
            </button>
            <span className="stage-indicator font-mono">
              LOT {currentIndex + 1} / {totalPlayers}
            </span>
            <button
              onClick={onNextPlayer}
              disabled={currentIndex === totalPlayers - 1}
              className="nav-button next-button font-mono"
            >
              NEXT ▶
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default BiddingConsole;
