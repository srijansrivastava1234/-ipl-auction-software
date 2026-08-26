import { useState } from 'react';

function BiddingConsole({
  user,
  player,
  onPlaceBid,
  onSellPlayer,
  onUnsoldPlayer,
  onPrevPlayer,
  onNextPlayer,
  currentIndex,
  totalPlayers
}) {
  const [customBidStr, setCustomBidStr] = useState('');

  if (!player) return null;

  const isSold = player.status === 'SOLD';
  const isAdmin = user.role === 'ADMIN';
  const isOwner = user.role === 'TEAM_OWNER';

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
          <h3 className="panel-title">🕹️ TACTICAL CONTROL</h3>
          
          <div className="action-buttons-row">
            <button
              onClick={onSellPlayer}
              disabled={isSold}
              className={`action-button hammer-button ${isSold ? 'disabled' : ''}`}
            >
              🔨 HAMMER DOWN
            </button>
            <button
              onClick={onUnsoldPlayer}
              disabled={player.status === 'UNSOLD'}
              className={`action-button unsold-button ${player.status === 'UNSOLD' ? 'disabled' : ''}`}
            >
              ♦ UNSOLD
            </button>
          </div>

          <div className="stage-navigation">
            <button
              onClick={onPrevPlayer}
              disabled={currentIndex === 0}
              className="nav-button prev-button"
            >
              ◀ PREV
            </button>
            <span className="stage-indicator">
              {currentIndex + 1} / {totalPlayers}
            </span>
            <button
              onClick={onNextPlayer}
              disabled={currentIndex === totalPlayers - 1}
              className="nav-button next-button"
            >
              NEXT ▶
            </button>
          </div>
        </div>
      )}

      {/* Franchise Owner Console */}
      {isOwner && (
        <div className="console-panel owner-panel">
          <h3 className="panel-title">🏏 FRANCHISE BIDDING DESK</h3>
          
          {!isSold ? (
            <div className="bidding-actions">
              <div className="increment-row">
                <button
                  onClick={() => onPlaceBid(2000000, true)} // true indicates incremental
                  className="bid-increment-btn l20"
                >
                  +₹20L
                </button>
                <button
                  onClick={() => onPlaceBid(5000000, true)}
                  className="bid-increment-btn l50"
                >
                  +₹50L
                </button>
                <button
                  onClick={() => onPlaceBid(10000000, true)}
                  className="bid-increment-btn c1"
                >
                  +₹1Cr
                </button>
              </div>

              <form onSubmit={handleCustomSubmit} className="custom-bid-form">
                <input
                  type="number"
                  placeholder="Custom bid (e.g. 25000000)"
                  value={customBidStr}
                  onChange={(e) => setCustomBidStr(e.target.value)}
                  className="custom-bid-input"
                />
                <button type="submit" className="custom-bid-submit">
                  SUBMIT
                </button>
              </form>
            </div>
          ) : (
            <div className="bidding-msg finished">
              🔒 CLOSED • ACQUIRED BY {player.team?.name?.toUpperCase()}
            </div>
          )}

          <div className="stage-navigation">
            <button
              onClick={onPrevPlayer}
              disabled={currentIndex === 0}
              className="nav-button prev-button"
            >
              ◀ PREV
            </button>
            <span className="stage-indicator">
              {currentIndex + 1} / {totalPlayers}
            </span>
            <button
              onClick={onNextPlayer}
              disabled={currentIndex === totalPlayers - 1}
              className="nav-button next-button"
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
