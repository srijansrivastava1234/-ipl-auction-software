import { useEffect, useState } from 'react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import Login from './components/Login';
import FranchiseHeader from './components/FranchiseHeader';
import PlayerCard from './components/PlayerCard';
import BiddingConsole from './components/BiddingConsole';
import Leaderboard from './components/Leaderboard';
import OrbitArena from './components/OrbitArena';

const BACKEND_URL = import.meta.env.VITE_API_URL || 'http://localhost:8082';

function App() {
  const [user, setUser] = useState(null);
  const [players, setPlayers] = useState([]);
  const [teams, setTeams] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [activeHighestBid, setActiveHighestBid] = useState(null);
  const [transitioning, setTransitioning] = useState(false);

  // Timer countdown state
  const [timerSeconds, setTimerSeconds] = useState(15.00);
  const [timerActive, setTimerActive] = useState(false);

  // Load user from session storage on startup
  useEffect(() => {
    const savedUser = sessionStorage.getItem('ipl_user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  // Timer Tick Effect
  useEffect(() => {
    const currentPlayer = players[currentIndex];
    if (!timerActive || loading || !user || !currentPlayer || currentPlayer.status === 'SOLD') {
      return;
    }

    const interval = setInterval(() => {
      setTimerSeconds((prev) => {
        if (prev <= 0.01) {
          clearInterval(interval);
          return 0;
        }
        return Number((prev - 0.01).toFixed(2));
      });
    }, 10);

    return () => clearInterval(interval);
  }, [timerActive, loading, user, players, currentIndex]);

  // Fetch initial teams and players once user is logged in
  useEffect(() => {
    if (!user) return;
    fetchInitialData();

    // Establish WebSocket Connection
    const stompClient = new Client({
      webSocketFactory: () => new SockJS(`${BACKEND_URL}/ws-auction`),
      reconnectDelay: 5000,
      connectHeaders: {
        Authorization: `Bearer ${user.token}`
      },
      onConnect: () => {
        // Subscribe to live bids
        stompClient.subscribe('/topic/bids', (msg) => {
          const newBid = JSON.parse(msg.body);
          setMessage(`🔥 Live Bid! ${newBid.team.name} raised bid to ₹${newBid.amount.toLocaleString('en-IN')}!`);
          
          // Update active highest bid state instantly from WebSocket broadcast
          setActiveHighestBid(newBid);

          // Reset countdown timer
          setTimerSeconds(15.00);
          setTimerActive(true);

          fetchInitialData();
        });

        // Subscribe to final sales
        stompClient.subscribe('/topic/players', (msg) => {
          const soldPlayer = JSON.parse(msg.body);
          if (soldPlayer.status === 'SOLD') {
            setMessage(`🎉 HAMMER DOWN! ${soldPlayer.name} SOLD to ${soldPlayer.team.name} for ₹${soldPlayer.basePrice.toLocaleString('en-IN')}!`);
            setTimerActive(false);
          } else {
            setMessage(`🚫 Player ${soldPlayer.name} marked UNSOLD.`);
            setTimerActive(false);
            setTimerSeconds(0);
          }
          
          fetchInitialData();
        });
      },
      onStompError: (frame) => {
        console.warn('STOMP error:', frame);
      }
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [user]);

  // Fetch active player's highest bid whenever the active player changes
  useEffect(() => {
    if (players.length > 0 && players[currentIndex]) {
      fetchHighestBid(players[currentIndex].id);
      
      // Reset timer on player changes
      setTimerSeconds(15.00);
      setTimerActive(true);
    } else {
      setActiveHighestBid(null);
    }
  }, [currentIndex, players]);

  const fetchInitialData = async () => {
    if (!user) return;
    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };
      const [playersRes, teamsRes] = await Promise.all([
        axios.get(`${BACKEND_URL}/api/players`, config),
        axios.get(`${BACKEND_URL}/api/teams`, config)
      ]);
      setPlayers(playersRes.data);
      setTeams(teamsRes.data);
    } catch (error) {
      console.error('Error fetching initial data:', error);
      setMessage('⚠️ Could not connect to Spring Boot server.');
    }
  };

  const fetchHighestBid = async (playerId) => {
    if (!user) return;
    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };
      const res = await axios.get(`${BACKEND_URL}/api/bids/player/${playerId}/highest`, config);
      if (res.status === 200 && res.data) {
        setActiveHighestBid(res.data);
      } else {
        setActiveHighestBid(null);
      }
    } catch (error) {
      console.error('Error fetching highest bid:', error);
      setActiveHighestBid(null);
    }
  };

  const handleLoginSuccess = (loginData) => {
    setTransitioning(true);
    setTimeout(() => {
      sessionStorage.setItem('ipl_user', JSON.stringify(loginData));
      setUser(loginData);
      setTransitioning(false);
    }, 1800);
  };

  const handleLogout = () => {
    sessionStorage.removeItem('ipl_user');
    setUser(null);
    setPlayers([]);
    setTeams([]);
    setActiveHighestBid(null);
    setMessage('');
  };

  const handlePlaceBid = async (amount, isIncremental) => {
    const currentPlayer = players[currentIndex];
    if (!currentPlayer || !user) return;

    let targetBidAmount = 0;
    const currentPrice = activeHighestBid ? activeHighestBid.amount : currentPlayer.basePrice;

    if (isIncremental) {
      // Dynamic Scaling: if current price is less than 10 Lakhs (e.g. 240,000), 
      // scale down the increments (20L -> 20k, 50L -> 50k, 1Cr -> 100k) to match database scaling.
      let incrementAmount = amount;
      if (currentPrice < 1000000) {
        if (amount === 2000000) incrementAmount = 20000;
        else if (amount === 5000000) incrementAmount = 50000;
        else if (amount === 10000000) incrementAmount = 100000;
      }
      targetBidAmount = currentPrice + incrementAmount;
    } else {
      if (amount <= currentPrice) {
        setMessage(`⚠️ Bid must be higher than current bid (₹${currentPrice.toLocaleString('en-IN')})!`);
        return;
      }
      targetBidAmount = amount;
    }

    const previousBid = activeHighestBid;
    const bidderTeam = teams.find(t => t.id === user.teamId);

    // Optimistic UI Update: change state immediately so user sees the change directly
    setActiveHighestBid({
      id: Date.now(),
      amount: targetBidAmount,
      team: bidderTeam || { id: user.teamId, name: 'Bidding Team' }
    });
    setTimerSeconds(15.00);
    setTimerActive(true);

    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };

      await axios.post(`${BACKEND_URL}/api/bids`, {
        playerId: currentPlayer.id,
        teamId: user.teamId,
        amount: targetBidAmount
      }, config);

      setMessage('');
    } catch (err) {
      const errMsg = err.response?.data?.error || err.message;
      setMessage(`❌ Bidding Error: ${errMsg}`);
      // Revert optimistic update on failure
      setActiveHighestBid(previousBid);
    }
  };

  const formatTimer = (seconds) => {
    if (seconds <= 0) return '00:00.00';
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    const hundredths = Math.round((seconds % 1) * 100);
    
    const minsStr = mins.toString().padStart(2, '0');
    const secsStr = secs.toString().padStart(2, '0');
    const hundStr = hundredths.toString().padStart(2, '0');
    
    return `${minsStr}:${secsStr}.${hundStr}`;
  };

  const handleTeamClick = async (clickedTeam) => {
    const currentPlayer = players[currentIndex];
    if (!currentPlayer || !user) return;

    if (currentPlayer.status === 'SOLD') {
      setMessage('⚠️ Bidding is closed. This player is already sold.');
      return;
    }

    // Role-based restrictions
    if (user.role === 'TEAM_OWNER') {
      if (user.teamId !== clickedTeam.id) {
        setMessage('⚠️ You cannot place a bid on behalf of another team!');
        return;
      }
    }

    // Bid Calculation
    const currentPrice = activeHighestBid ? activeHighestBid.amount : currentPlayer.basePrice;
    
    let incrementAmount = 2000000; // default 20L
    if (currentPrice < 1000000) {
      incrementAmount = 20000; // scale down to 20k if current price is in thousands
    }
    const targetBidAmount = activeHighestBid ? currentPrice + incrementAmount : currentPrice; // increment or start with base price

    const previousBid = activeHighestBid;

    // Optimistic UI Update: change state immediately so user sees the change directly
    setActiveHighestBid({
      id: Date.now(),
      amount: targetBidAmount,
      team: clickedTeam
    });
    setTimerSeconds(15.00);
    setTimerActive(true);

    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };

      await axios.post(`${BACKEND_URL}/api/bids`, {
        playerId: currentPlayer.id,
        teamId: clickedTeam.id,
        amount: targetBidAmount
      }, config);

      setMessage('');
    } catch (err) {
      const errMsg = err.response?.data?.error || err.message;
      setMessage(`❌ Bidding Error: ${errMsg}`);
      // Revert optimistic update on failure
      setActiveHighestBid(previousBid);
    }
  };

  const handleSellPlayer = async () => {
    const currentPlayer = players[currentIndex];
    if (!currentPlayer || !user) return;

    if (!activeHighestBid) {
      setMessage('⚠️ No bids have been placed on this player yet. Cannot finalize sale.');
      return;
    }

    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };

      await axios.put(`${BACKEND_URL}/api/players/${currentPlayer.id}/sell`, {
        teamId: activeHighestBid.team.id,
        finalPrice: activeHighestBid.amount
      }, config);

      setMessage('');
    } catch (err) {
      const errMsg = err.response?.data?.error || err.message;
      setMessage(`❌ Failed to finalize sale: ${errMsg}`);
    }
  };

  const handleUnsoldPlayer = async () => {
    const currentPlayer = players[currentIndex];
    if (!currentPlayer || !user) return;

    try {
      const config = {
        headers: { Authorization: `Bearer ${user.token}` }
      };

      await axios.put(`${BACKEND_URL}/api/players/${currentPlayer.id}/unsold`, {}, config);
      setMessage('');
    } catch (err) {
      const errMsg = err.response?.data?.error || err.message;
      setMessage(`❌ Failed to mark unsold: ${errMsg}`);
    }
  };

  const handleNextPlayer = () => {
    if (currentIndex < players.length - 1) {
      setCurrentIndex(prev => prev + 1);
      setMessage('');
    }
  };

  const handlePrevPlayer = () => {
    if (currentIndex > 0) {
      setCurrentIndex(prev => prev - 1);
      setMessage('');
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <h2>Connecting to IPL Auction Arena...</h2>
      </div>
    );
  }

  if (transitioning) {
    return (
      <div className="telemetry-warp-loader">
        <div className="warp-core">
          <div className="pulse-circle"></div>
          <div className="scan-line-hud"></div>
        </div>
        <h2 className="warp-title">ESTABLISHING NEURAL DOCK...</h2>
        <div className="warp-log">
          <div>&gt; DECRYPTING KEY: SUCCESS</div>
          <div>&gt; SECTOR RE-ENFORCING PROTOCOLS: ACTIVE</div>
          <div>&gt; LAUNCHING HEX-SYNAPSE HUD</div>
        </div>
      </div>
    );
  }

  // Render Login Card if not authenticated
  if (!user) {
    return <Login onLoginSuccess={handleLoginSuccess} backendUrl={BACKEND_URL} />;
  }

  const currentPlayer = players[currentIndex];
  const activeTeam = teams.find(t => t.id === user.teamId);

  // Augment current player displaying current highest bid price
  const displayPlayer = currentPlayer ? {
    ...currentPlayer,
    basePrice: activeHighestBid ? activeHighestBid.amount : currentPlayer.basePrice,
    team: currentPlayer.status === 'SOLD' ? currentPlayer.team : (activeHighestBid ? activeHighestBid.team : null)
  } : null;

  const currentValuation = displayPlayer 
    ? (activeHighestBid ? activeHighestBid.amount : displayPlayer.basePrice) 
    : 0;

  return (
    <div className="app-container">
      <FranchiseHeader
        user={user}
        activeTeam={activeTeam}
        totalPlayers={players.length}
        currentIndex={currentIndex}
        currentValuation={currentValuation}
        timerValue={formatTimer(timerSeconds)}
        onLogout={handleLogout}
      />

      {/* Broadcast Message Ticker */}
      {message && (
        <div className="ticker-message">
          <span className="ticker-icon">📢</span>
          <span className="ticker-text">{message}</span>
        </div>
      )}

      {/* Main Grid Layout */}
      {displayPlayer ? (
        <div className="hud-grid">
          {/* Middle Row: Orbit Arena visualization */}
          <div className="hud-middle-row">
            <OrbitArena 
              player={displayPlayer}
              teams={teams}
              activeHighestBid={activeHighestBid}
              onTeamClick={handleTeamClick}
            />
          </div>
          
          {/* Bottom Row: Left Resonance, Middle Control, Right Purse */}
          <div className="bottom-telemetry-row">
            {/* Panel 1: SQUAD RESONANCE */}
            <div className="telemetry-panel squad-resonance-panel">
              <h3 className="panel-title">📡 SQUAD RESONANCE</h3>
              <div className="resonance-display">
                <div className="oscilloscope-container">
                  <svg viewBox="0 0 100 100" className="oscilloscope-svg">
                    <circle cx="50" cy="50" r="40" className="radar-circle" />
                    <circle cx="50" cy="50" r="28" className="radar-circle-inner" />
                    <circle cx="50" cy="50" r="16" className="radar-circle-center" />
                    <path d="M 50 10 L 50 90 M 10 50 L 90 50" className="radar-cross" />
                    <line x1="50" y1="50" x2="80" y2="20" className="radar-sweep" />
                  </svg>
                </div>
                <div className="resonance-meta">
                  <div className="resonance-label">INTELLIGENCE</div>
                  <div className="resonance-value">RESONANCE: 94.2%</div>
                </div>
              </div>
            </div>

            {/* Panel 2: TACTICAL CONTROL (Bidding Console) */}
            <div className="telemetry-panel tactical-control-panel">
              <BiddingConsole
                user={user}
                player={displayPlayer}
                onPlaceBid={handlePlaceBid}
                onSellPlayer={handleSellPlayer}
                onUnsoldPlayer={handleUnsoldPlayer}
                onPrevPlayer={handlePrevPlayer}
                onNextPlayer={handleNextPlayer}
                currentIndex={currentIndex}
                totalPlayers={players.length}
              />
            </div>

            {/* Panel 3: PURSE CONTAINMENT */}
            <div className="telemetry-panel purse-containment-panel">
              <h3 className="panel-title">🔋 PURSE CONTAINMENT</h3>
              {user.role === 'TEAM_OWNER' && activeTeam ? (
                <div className="panel-content">
                  <div className="purse-telemetry">
                    <div className="telemetry-stat-label">FRANCHISE BUDGET</div>
                    <div className="telemetry-stat-value green">
                      ₹{(activeTeam.budget / 10000000).toFixed(2)} Cr
                    </div>
                    <div className="telemetry-stat-sub">MAX CAPACITY: 25 PLAYERS</div>
                  </div>
                  <div className="telemetry-status-box active">
                    <span>BUDGET SAFEGUARD: ON</span>
                  </div>
                </div>
              ) : (
                <div className="panel-content">
                  <div className="telemetry-stat-label">GLOBAL ADMIN TELEMETRY</div>
                  <div className="admin-status-indicator">
                    <span className="pulse-dot"></span> MONITORING ACTIVE
                  </div>
                  
                  {/* Sleek mini list of team budgets */}
                  <div className="mini-leaderboard">
                    {teams.slice(0, 5).map(t => {
                      const alias = t.name.split(' ').map(w => w[0]).join('');
                      return (
                        <div key={t.id} className="mini-leaderboard-row">
                          <span className="team-lbl">{alias}</span>
                          <span className="budget-val">₹{(t.budget / 10000000).toFixed(2)} Cr</span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
              <div className="panel-footer">SYSTEM SECURITY: UNFORCED</div>
            </div>
          </div>
        </div>
      ) : (
        <div className="empty-pool-message">
          <p>No players available in the auction pool.</p>
        </div>
      )}
    </div>
  );
}

export default App;