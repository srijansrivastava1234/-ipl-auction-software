import { useState, useEffect, useRef } from 'react';
import axios from 'axios';

const SECTORS = [
  { name: 'CHENNAI SUPER KINGS', username: 'csk_owner', password: 'csk123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '5 / 25', overseas: '02 / 08' },
  { name: 'MUMBAI INDIANS', username: 'mi_owner', password: 'mi123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '4 / 25', overseas: '03 / 08' },
  { name: 'ROYAL CHALLENGERS BENGALURU', username: 'rcb_owner', password: 'rcb123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '6 / 25', overseas: '04 / 08' },
  { name: 'KOLKATA KNIGHT RIDERS', username: 'kkr_owner', password: 'kkr123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '5 / 25', overseas: '02 / 08' },
  { name: 'RAJASTHAN ROYALS', username: 'rr_owner', password: 'rr123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '4 / 25', overseas: '02 / 08' },
  { name: 'SUNRISERS HYDERABAD', username: 'srh_owner', password: 'srh123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '3 / 25', overseas: '03 / 08' },
  { name: 'DELHI CAPITALs', username: 'dc_owner', password: 'dc123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '5 / 25', overseas: '01 / 08' },
  { name: 'GUJARAT TITANS', username: 'gt_owner', password: 'gt123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '4 / 25', overseas: '02 / 08' },
  { name: 'LUCKNOW SUPER GIANTS', username: 'lsg_owner', password: 'lsg123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '4 / 25', overseas: '02 / 08' },
  { name: 'PUNJAB KINGS', username: 'pbks_owner', password: 'pbks123', clearance: 'WAR ROOM CHIEF', purse: '₹100.00 Cr', retentions: '2 / 25', overseas: '02 / 08' },
  { name: 'GLOBAL ADMIN', username: 'admin', password: 'admin123', clearance: 'GLOBAL AUCTIONEER', purse: 'N/A', retentions: 'N/A', overseas: 'N/A' },
];

function Login({ onLoginSuccess, backendUrl }) {
  const [selectedSectorIndex, setSelectedSectorIndex] = useState(0);
  const [passwordInput, setPasswordInput] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isHolding, setIsHolding] = useState(false);
  const [holdPercent, setHoldPercent] = useState(0);
  const [isOpen, setIsOpen] = useState(false);

  const currentSector = SECTORS[selectedSectorIndex];
  const holdTimeoutRef = useRef(null);
  const holdIntervalRef = useRef(null);
  const dropdownRef = useRef(null);

  // Close dropdown on outside click
  useEffect(() => {
    const handleOutsideClick = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  // Focus and clear passcode when sector changes
  useEffect(() => {
    setPasswordInput('');
    setError('');
  }, [selectedSectorIndex]);

  // Global keydown listener to capture passwords
  useEffect(() => {
    const handleGlobalKeyDown = (e) => {
      if (loading || isHolding) return;

      // Handle backspace
      if (e.key === 'Backspace') {
        e.preventDefault();
        setPasswordInput((prev) => prev.slice(0, -1));
      } 
      // Handle alphanumeric characters
      else if (e.key.length === 1 && /^[a-zA-Z0-9]$/.test(e.key)) {
        e.preventDefault();
        setPasswordInput((prev) => (prev.length < 8 ? prev + e.key.toLowerCase() : prev));
      }
    };

    window.addEventListener('keydown', handleGlobalKeyDown);
    return () => window.removeEventListener('keydown', handleGlobalKeyDown);
  }, [loading, isHolding]);

  const triggerLogin = async () => {
    setError('');
    setLoading(true);

    // Verify Password matches the required "domain123" rule
    if (passwordInput !== currentSector.password) {
      setError('NEURAL KEY MATCH FAILED: ACCESS DENIED');
      setLoading(false);
      return;
    }

    try {
      const response = await axios.post(`${backendUrl}/api/auth/login`, {
        username: currentSector.username,
        password: currentSector.password
      });
      onLoginSuccess(response.data);
    } catch (err) {
      setError(err.response?.data?.error || 'DECRYPTION FAILED: SERVICE UNREACHABLE');
    } finally {
      setLoading(false);
    }
  };

  const handleStartHold = (e) => {
    e.preventDefault();
    if (loading || passwordInput.length === 0) {
      setError('AUTHORIZATION ERROR: EMPTY CREDENTIALS');
      return;
    }
    setError('');
    setIsHolding(true);
    setHoldPercent(0);

    const startTime = Date.now();
    const duration = 3000; // 3 seconds

    holdIntervalRef.current = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const pct = Math.min((elapsed / duration) * 100, 100);
      setHoldPercent(pct);
    }, 30);

    holdTimeoutRef.current = setTimeout(() => {
      clearInterval(holdIntervalRef.current);
      setIsHolding(false);
      setHoldPercent(0);
      triggerLogin();
    }, duration);
  };

  const handleCancelHold = () => {
    if (holdTimeoutRef.current) {
      clearTimeout(holdTimeoutRef.current);
      holdTimeoutRef.current = null;
    }
    if (holdIntervalRef.current) {
      clearInterval(holdIntervalRef.current);
      holdIntervalRef.current = null;
    }
    setIsHolding(false);
    setHoldPercent(0);
  };

  return (
    <div className="login-container">
      <div className="login-hud-card">
        {/* Custom Sector Selector */}
        <div className="custom-dropdown-container" ref={dropdownRef}>
          <span className="sector-select-lbl">SELECT ACTIVE SECTOR</span>
          <div 
            className={`custom-dropdown-trigger ${isOpen ? 'open' : ''}`}
            onClick={() => setIsOpen(!isOpen)}
          >
            <span className="selected-sector-name">{currentSector.name}</span>
            <span className="dropdown-chevron">{isOpen ? '▲' : '▼'}</span>
          </div>
          
          {isOpen && (
            <div className="custom-dropdown-options-list">
              {SECTORS.map((s, idx) => (
                <div 
                  key={s.name} 
                  className={`custom-dropdown-option ${idx === selectedSectorIndex ? 'active' : ''}`}
                  onClick={() => {
                    setSelectedSectorIndex(idx);
                    setIsOpen(false);
                  }}
                >
                  <span className="option-name">{s.name}</span>
                  <span className="option-sub-clearance">{s.clearance}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Stats Panel */}
        <div className="sector-stats-panel">
          <div className="stat-row">
            <span className="stat-label">AVAILABLE PURSE:</span>
            <span className="stat-val">{currentSector.purse}</span>
          </div>
          <div className="stat-row">
            <span className="stat-label">SQUAD RETENTIONS:</span>
            <span className="stat-val">{currentSector.retentions}</span>
          </div>
          <div className="stat-row">
            <span className="stat-label">OVERSEAS QUOTA:</span>
            <span className="stat-val">{currentSector.overseas}</span>
          </div>
          <div className="stat-row border-top-glow">
            <span className="stat-label">SECURITY CLEARANCE:</span>
            <span className="stat-val yellow-glow">{currentSector.clearance}</span>
          </div>
        </div>

        {/* Error Feedback */}
        {error && <div className="login-hud-error">{error}</div>}

        {/* Dial Interface */}
        <div className="dial-interface-container">
          <p className="dial-subtitle">NEURAL KEY ACCESS (TYPE PASSWORD DIRECTLY ON KEYBOARD)</p>
          
          <div className="dial-dock-line">
            {[...Array(8)].map((_, idx) => {
              const char = idx < passwordInput.length ? passwordInput[idx].toUpperCase() : '•';
              const isFilled = idx < passwordInput.length;
              return (
                <div 
                  key={idx} 
                  className={`dial-circle-wrapper dial-wrapper-${idx} ${idx === passwordInput.length ? 'active' : ''}`}
                >
                  <span className="dial-index">0{idx + 1}</span>
                  <div className={`dial-circle ${isFilled ? 'filled' : ''}`}>
                    {char}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Biometric Scanner (Hold to Unlock) */}
        <div className="bio-lock-button-wrapper">
          <div className="scanner-hold-container">
            <button 
              type="button" 
              className={`bio-lock-btn ${isHolding ? 'holding-scan' : ''} ${loading ? 'scanning' : ''}`}
              onMouseDown={handleStartHold}
              onMouseUp={handleCancelHold}
              onMouseLeave={handleCancelHold}
              onTouchStart={handleStartHold}
              onTouchEnd={handleCancelHold}
              style={{
                transform: isHolding ? 'scale(1.05)' : 'scale(1)',
                transition: isHolding ? 'transform 3s linear' : 'transform 0.2s ease'
              }}
            >
              <span className="bio-lock-inner-circle"></span>
              {isHolding && (
                <svg className="hold-progress-ring" viewBox="0 0 100 100">
                  <circle 
                    cx="50" 
                    cy="50" 
                    r="44" 
                    stroke="#fbbf24" 
                    strokeWidth="4" 
                    fill="none" 
                    strokeDasharray="276"
                    strokeDashoffset={276 - (276 * holdPercent) / 100}
                  />
                </svg>
              )}
            </button>
            <span className="hold-progress-label">
              {isHolding ? `HOLDING: ${Math.round(holdPercent)}%` : 'HOLD TO AUTHENTICATE'}
            </span>
          </div>
        </div>

        <div className="bio-lock-armed-text">
          {loading ? '| [DECRYPTING SECTOR GATEWAY...] |' : (isHolding ? '| [SCANNING BIO-PRINT...] |' : '| [BIO-LOCK: ARMED] |')}
        </div>

        <div className="login-instructions">
          Type password <strong>{currentSector.password}</strong> on keyboard and **HOLD** the biometric scan button for 3 seconds to authorize connection.
        </div>
      </div>
    </div>
  );
}

export default Login;
