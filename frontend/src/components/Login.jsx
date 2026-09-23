import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { registerCustomTheme, getCustomThemes } from '../teamThemes';

const DEFAULT_SECTORS = [
  {
    name: 'CHENNAI SUPER KINGS',
    short: 'CSK',
    username: 'csk_owner',
    password: 'csk123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'M.S. Dhoni - Director',
    purse: '₹100.00 Cr',
    retentions: '5 / 25',
    overseas: '02 / 08',
    color: '#facc15',
    accent: '#ca8a04',
    bgGradient: 'linear-gradient(135deg, #facc15 0%, #ca8a04 100%)',
    tag: '🦁 5x Champions',
    stadium: 'M.A. Chidambaram Stadium, Chennai',
    badge: '🦁'
  },
  {
    name: 'MUMBAI INDIANS',
    short: 'MI',
    username: 'mi_owner',
    password: 'mi123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Akash Ambani - Chair',
    purse: '₹100.00 Cr',
    retentions: '4 / 25',
    overseas: '03 / 08',
    color: '#00f2fe',
    accent: '#0284c7',
    bgGradient: 'linear-gradient(135deg, #00f2fe 0%, #0369a1 100%)',
    tag: '⚡ 5x Champions',
    stadium: 'Wankhede Stadium, Mumbai',
    badge: '⚡'
  },
  {
    name: 'ROYAL CHALLENGERS BENGALURU',
    short: 'RCB',
    username: 'rcb_owner',
    password: 'rcb123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Prathmesh M. - Lead',
    purse: '₹100.00 Cr',
    retentions: '6 / 25',
    overseas: '04 / 08',
    color: '#ef4444',
    accent: '#b91c1c',
    bgGradient: 'linear-gradient(135deg, #ef4444 0%, #991b1b 100%)',
    tag: '🔥 Play Bold',
    stadium: 'M. Chinnaswamy Stadium, Bengaluru',
    badge: '🔥'
  },
  {
    name: 'KOLKATA KNIGHT RIDERS',
    short: 'KKR',
    username: 'kkr_owner',
    password: 'kkr123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Venky Mysore - CEO',
    purse: '₹100.00 Cr',
    retentions: '5 / 25',
    overseas: '02 / 08',
    color: '#c084fc',
    accent: '#7e22ce',
    bgGradient: 'linear-gradient(135deg, #c084fc 0%, #581c87 100%)',
    tag: '👑 3x Champions',
    stadium: 'Eden Gardens, Kolkata',
    badge: '👑'
  },
  {
    name: 'RAJASTHAN ROYALS',
    short: 'RR',
    username: 'rr_owner',
    password: 'rr123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Manoj Badale - Lead',
    purse: '₹100.00 Cr',
    retentions: '4 / 25',
    overseas: '02 / 08',
    color: '#f472b6',
    accent: '#be185d',
    bgGradient: 'linear-gradient(135deg, #f472b6 0%, #831843 100%)',
    tag: '⚔️ Halla Bol',
    stadium: 'Sawai Mansingh Stadium, Jaipur',
    badge: '⚔️'
  },
  {
    name: 'SUNRISERS HYDERABAD',
    short: 'SRH',
    username: 'srh_owner',
    password: 'srh123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Kavya Maran - CEO',
    purse: '₹100.00 Cr',
    retentions: '3 / 25',
    overseas: '03 / 08',
    color: '#fb923c',
    accent: '#c2410c',
    bgGradient: 'linear-gradient(135deg, #fb923c 0%, #9a3412 100%)',
    tag: '🦅 Orange Army',
    stadium: 'Rajiv Gandhi Stadium, Hyderabad',
    badge: '🦅'
  },
  {
    name: 'DELHI CAPITALS',
    short: 'DC',
    username: 'dc_owner',
    password: 'dc123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Parth Jindal - Director',
    purse: '₹100.00 Cr',
    retentions: '4 / 25',
    overseas: '03 / 08',
    color: '#60a5fa',
    accent: '#1d4ed8',
    bgGradient: 'linear-gradient(135deg, #60a5fa 0%, #1e40af 100%)',
    tag: '🐯 Roar Macha',
    stadium: 'Arun Jaitley Stadium, Delhi',
    badge: '🐯'
  },
  {
    name: 'GUJARAT TITANS',
    short: 'GT',
    username: 'gt_owner',
    password: 'gt123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'CVC Capital - Lead',
    purse: '₹100.00 Cr',
    retentions: '5 / 25',
    overseas: '03 / 08',
    color: '#2dd4bf',
    accent: '#0f766e',
    bgGradient: 'linear-gradient(135deg, #2dd4bf 0%, #115e59 100%)',
    tag: '⚡ Aava De',
    stadium: 'Narendra Modi Stadium, Ahmedabad',
    badge: '⚡'
  },
  {
    name: 'LUCKNOW SUPER GIANTS',
    short: 'LSG',
    username: 'lsg_owner',
    password: 'lsg123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Sanjiv Goenka - Chair',
    purse: '₹100.00 Cr',
    retentions: '4 / 25',
    overseas: '02 / 08',
    color: '#22d3ee',
    accent: '#0e7490',
    bgGradient: 'linear-gradient(135deg, #22d3ee 0%, #155e75 100%)',
    tag: '🏹 Gazab Andaz',
    stadium: 'Ekana Cricket Stadium, Lucknow',
    badge: '🏹'
  },
  {
    name: 'PUNJAB KINGS',
    short: 'PBKS',
    username: 'pbks_owner',
    password: 'pbks123',
    clearance: 'WAR ROOM CHIEF',
    ceo: 'Preity Zinta - Director',
    purse: '₹100.00 Cr',
    retentions: '2 / 25',
    overseas: '02 / 08',
    color: '#fb7185',
    accent: '#be123c',
    bgGradient: 'linear-gradient(135deg, #fb7185 0%, #881337 100%)',
    tag: '🦁 Sadda Punjab',
    stadium: 'PCA Stadium, Mullanpur',
    badge: '🦁'
  },
  {
    name: 'GLOBAL ADMIN',
    short: 'ADMIN',
    username: 'admin',
    password: 'admin123',
    clearance: 'AUCTION COMMISSIONER',
    ceo: 'BCCI Auction Committee',
    purse: 'UNLIMITED',
    retentions: 'N/A',
    overseas: 'N/A',
    color: '#fbbf24',
    accent: '#b45309',
    bgGradient: 'linear-gradient(135deg, #fde047 0%, #b45309 100%)',
    tag: '⚖️ Master Control',
    stadium: 'BCCI Stage / Global War Room',
    badge: '⚖️'
  }
];

const PRESET_BADGES = ['🦁', '⚡', '🔥', '👑', '⚔️', '🦅', '🐯', '🛡️', '🏹', '🚀', '💎', '🐉', '🏏', '🐺', '🌪️', '🎯'];
const PRESET_COLORS = [
  '#facc15', '#00f2fe', '#ef4444', '#c084fc', '#f472b6', 
  '#fb923c', '#60a5fa', '#2dd4bf', '#22d3ee', '#fb7185', 
  '#10b981', '#a855f7', '#f97316', '#3b82f6', '#14b8a6', '#e11d48'
];

function Login({ onLoginSuccess, backendUrl }) {
  const [authMode, setAuthMode] = useState('login'); // 'login' | 'register'
  const [sectorsList, setSectorsList] = useState(DEFAULT_SECTORS);
  const [selectedSectorIndex, setSelectedSectorIndex] = useState(0);

  // Login form state
  const [passwordInput, setPasswordInput] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [isHolding, setIsHolding] = useState(false);
  const [holdPercent, setHoldPercent] = useState(0);
  const [isVerified, setIsVerified] = useState(false);

  // Registration form state
  const [regTeamName, setRegTeamName] = useState('');
  const [regShortCode, setRegShortCode] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regSlogan, setRegSlogan] = useState('');
  const [regStadium, setRegStadium] = useState('');
  const [regBadge, setRegBadge] = useState('🦁');
  const [regColor, setRegColor] = useState('#10b981');
  const [regBudgetCr, setRegBudgetCr] = useState(100);

  const holdTimeoutRef = useRef(null);
  const holdIntervalRef = useRef(null);

  // Load custom themes from localStorage
  useEffect(() => {
    const custom = getCustomThemes();
    const customSectors = Object.values(custom).map(c => ({
      name: c.name,
      short: c.short,
      username: c.username,
      password: 'CustomPassword',
      clearance: 'WAR ROOM CHIEF',
      ceo: `${c.short} Franchise Owner`,
      purse: '₹100.00 Cr',
      retentions: '0 / 25',
      overseas: '00 / 08',
      color: c.color,
      accent: c.accent,
      bgGradient: c.bgGradient || `linear-gradient(135deg, ${c.color} 0%, #0f172a 100%)`,
      tag: `${c.badge} ${c.tag || 'New Franchise'}`,
      stadium: c.stadium || 'Home Arena',
      badge: c.badge || '🏏'
    }));

    if (customSectors.length > 0) {
      const existingNames = new Set(DEFAULT_SECTORS.map(s => s.name));
      const filtered = customSectors.filter(c => !existingNames.has(c.name));
      setSectorsList([...DEFAULT_SECTORS, ...filtered]);
    }
  }, []);

  const currentSector = sectorsList[selectedSectorIndex] || sectorsList[0];

  useEffect(() => {
    setPasswordInput('');
    setError('');
    setIsVerified(false);
  }, [selectedSectorIndex, authMode]);

  // Login execution
  const triggerLogin = async () => {
    if (!passwordInput || !passwordInput.trim()) {
      setError('PLEASE ENTER PASSWORD TO LOGIN');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const response = await axios.post(`${backendUrl}/api/auth/login`, {
        username: currentSector.username,
        password: passwordInput.trim()
      });
      setIsVerified(true);
      setTimeout(() => {
        onLoginSuccess(response.data);
      }, 500);
    } catch (err) {
      setError(err.response?.data?.error || 'INVALID CREDENTIALS: ACCESS DENIED');
      setLoading(false);
      setIsVerified(false);
    }
  };

  // Hold to Authenticate (3 seconds)
  const handleStartHold = (e) => {
    e.preventDefault();
    if (loading) return;

    if (!passwordInput || !passwordInput.trim()) {
      setError('PLEASE ENTER PASSWORD BEFORE SCANNING');
      return;
    }

    setError('');
    setIsHolding(true);
    setHoldPercent(0);

    const startTime = Date.now();
    const duration = 3000;

    holdIntervalRef.current = setInterval(() => {
      const elapsed = Date.now() - startTime;
      const pct = Math.min((elapsed / duration) * 100, 100);
      setHoldPercent(pct);
    }, 20);

    holdTimeoutRef.current = setTimeout(() => {
      clearInterval(holdIntervalRef.current);
      setIsHolding(false);
      setHoldPercent(100);
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

  // Team Registration execution
  const handleRegisterSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!regTeamName.trim()) {
      setError('PLEASE ENTER FRANCHISE NAME');
      return;
    }
    if (!regUsername.trim()) {
      setError('PLEASE ENTER OWNER USERNAME');
      return;
    }
    if (!regPassword.trim() || regPassword.length < 4) {
      setError('PASSWORD MUST BE AT LEAST 4 CHARACTERS');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const budgetAmount = (Number(regBudgetCr) || 100) * 10000000;
      const shortTag = (regShortCode.trim() || regTeamName.substring(0, 3)).toUpperCase();

      const payload = {
        teamName: regTeamName.trim(),
        username: regUsername.trim(),
        password: regPassword.trim(),
        budget: budgetAmount
      };

      const response = await axios.post(`${backendUrl}/api/auth/register-team`, payload);

      const customThemeObj = {
        name: regTeamName.trim().toUpperCase(),
        short: shortTag,
        username: regUsername.trim(),
        color: regColor,
        accent: regColor,
        badge: regBadge,
        tag: regSlogan.trim() || 'Official Franchise',
        slogan: regSlogan.trim() || 'Roar of Champions',
        stadium: regStadium.trim() || 'Home Arena'
      };
      registerCustomTheme(customThemeObj);

      setIsVerified(true);
      setTimeout(() => {
        onLoginSuccess(response.data);
      }, 500);
    } catch (err) {
      setError(err.response?.data?.error || 'REGISTRATION FAILED: PLEASE TRY AGAIN');
      setLoading(false);
    }
  };

  // SVG circular stroke calculation
  const radius = 58;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (holdPercent / 100) * circumference;

  const dotCount = 12;
  const activeDotCount = Math.floor((holdPercent / 100) * dotCount);

  return (
    <div
      className="war-room-auth-stage"
      style={{
        '--sector-color': currentSector.color,
        '--sector-accent': currentSector.accent,
        '--sector-gradient': currentSector.bgGradient
      }}
    >
      <div className="auth-ambient-mesh"></div>

      <div className="war-room-auth-card">
        {/* Header Ribbon & Mode Switcher */}
        <div className="auth-card-header">
          <div className="auth-brand-row">
            <div className="auth-crest-circle">
              <span className="crest-emoji">{currentSector.badge}</span>
            </div>
            <div>
              <h1 className="auth-main-title">[ IPL 2026 AUCTION WAR ROOM ]</h1>
              <p className="auth-sub-title">SECURE QUANTUM BIDDING PROTOCOL • CLEARANCE SYSTEM</p>
            </div>
          </div>

          <div className="auth-mode-switch-group font-mono">
            <button
              type="button"
              className={`auth-tab-btn ${authMode === 'login' ? 'active' : ''}`}
              onClick={() => setAuthMode('login')}
            >
              🔐 FRANCHISE LOGIN
            </button>
            <button
              type="button"
              className={`auth-tab-btn ${authMode === 'register' ? 'active' : ''}`}
              onClick={() => setAuthMode('register')}
            >
              ➕ REGISTER NEW TEAM
            </button>
          </div>
        </div>

        {/* 1-Click Interactive Franchise Selector Bar */}
        <div className="franchise-strip-container">
          <div className="franchise-strip-header font-mono">
            <span className="franchise-strip-label">SELECT FRANCHISE SECTOR:</span>
            {authMode === 'login' && (
              <button
                type="button"
                className="add-new-team-pill-btn font-mono"
                onClick={() => setAuthMode('register')}
              >
                + NEW FRANCHISE
              </button>
            )}
          </div>
          <div className="franchise-pill-grid">
            {sectorsList.map((sector, index) => {
              const isSelected = selectedSectorIndex === index && authMode === 'login';
              return (
                <button
                  key={sector.name}
                  type="button"
                  onClick={() => {
                    setSelectedSectorIndex(index);
                    setAuthMode('login');
                  }}
                  className={`franchise-pill-btn ${isSelected ? 'selected' : ''}`}
                  style={{
                    borderColor: isSelected ? sector.color : 'rgba(255,255,255,0.12)',
                    backgroundColor: isSelected ? `${sector.color}22` : 'rgba(0,0,0,0.4)',
                    color: isSelected ? sector.color : 'rgba(255,255,255,0.85)',
                    boxShadow: isSelected ? `0 0 12px ${sector.color}50` : 'none'
                  }}
                >
                  <span className="pill-badge">{sector.badge}</span>
                  <span className="pill-dot" style={{ backgroundColor: sector.color }}></span>
                  <span className="pill-short">{sector.short}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* LOGIN MODE: 3-Column Tactical Command Hub */}
        {authMode === 'login' && (
          <div className="auth-tactical-grid">
            {/* Left Column: Franchise Dossier */}
            <div className="auth-panel dossier-panel">
              <div className="panel-header-row">
                <span className="panel-subhead">FRANCHISE DOSSIER</span>
                <span className="panel-tag font-mono" style={{ color: currentSector.color }}>
                  {currentSector.tag}
                </span>
              </div>

              <div className="franchise-brand-card">
                <div className="brand-team-name font-display" style={{ color: currentSector.color }}>
                  {currentSector.name}
                </div>
                <div className="brand-stadium font-mono">
                  🏟️ {currentSector.stadium}
                </div>
              </div>

              <div className="dossier-metrics font-mono">
                <div className="metric-box">
                  <span className="metric-label">FRANCHISE CLEARANCE</span>
                  <span className="metric-value gold font-mono">{currentSector.clearance}</span>
                </div>

                <div className="metric-box">
                  <span className="metric-label">AVAILABLE PURSE</span>
                  <span className="metric-value purse-glow font-mono" style={{ color: currentSector.color }}>
                    {currentSector.purse}
                  </span>
                </div>

                <div className="metric-box-split">
                  <div className="sub-metric">
                    <span className="sub-label">SQUAD LIMIT</span>
                    <span className="sub-val">25 MAX</span>
                  </div>
                  <div className="sub-metric">
                    <span className="sub-label">OVERSEAS</span>
                    <span className="sub-val">08 MAX</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Center Column: Central Biometric Scanner & Password Entry */}
            <div className="auth-panel scanner-central-panel">
              <div className="password-entry-deck">
                <label className="password-entry-label font-mono" htmlFor="auth-password-input">
                  <span className="label-text">ENTER PASSWORD</span>
                  <span className="preset-hint font-mono">(TARGET: <strong>{currentSector.password}</strong>)</span>
                </label>
                <div className="password-input-row" style={{ borderColor: `${currentSector.color}50` }}>
                  <span className="input-key-glyph">🔑</span>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    id="auth-password-input"
                    value={passwordInput}
                    onChange={(e) => { setPasswordInput(e.target.value); setError(''); }}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        triggerLogin();
                      }
                    }}
                    placeholder="Enter password..."
                    className="tactical-password-field font-mono"
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    className="reveal-pwd-btn"
                    onClick={() => setShowPassword(!showPassword)}
                    title={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? '👁️' : '🔒'}
                  </button>
                </div>
              </div>

              {/* Circular Biometric Scanner with Reference Fingerprint */}
              <div className="scanner-outer-frame">
                <div className="hud-ring-outer" style={{ borderColor: `${currentSector.color}35` }}></div>
                <div className="hud-ring-inner" style={{ borderColor: `${currentSector.color}45` }}></div>

                <button
                  type="button"
                  className={`biometric-hold-trigger ${isHolding ? 'is-holding' : ''} ${loading ? 'is-authenticating' : ''}`}
                  onMouseDown={handleStartHold}
                  onMouseUp={handleCancelHold}
                  onMouseLeave={handleCancelHold}
                  onTouchStart={handleStartHold}
                  onTouchEnd={handleCancelHold}
                  title="Click and hold for 3 seconds to authenticate"
                >
                  <svg className="biometric-progress-svg" viewBox="0 0 140 140">
                    <circle cx="70" cy="70" r={radius} className="progress-bg-track" />
                    <circle
                      cx="70"
                      cy="70"
                      r={radius}
                      className="progress-fill-glow"
                      style={{
                        stroke: currentSector.color,
                        strokeDasharray: circumference,
                        strokeDashoffset: strokeDashoffset
                      }}
                    />
                  </svg>

                  {/* Fingerprint Badge Center matching user's image */}
                  <div 
                    className="scanner-core-content"
                    style={{
                      borderColor: isVerified ? '#10b981' : `${currentSector.color}60`,
                      boxShadow: isHolding ? `0 0 25px ${currentSector.color}60` : `0 0 12px ${currentSector.color}25`
                    }}
                  >
                    {isHolding ? (
                      <div className="holding-readout font-mono" style={{ color: currentSector.color }}>
                        <span className="hold-percent-number">{Math.round(holdPercent)}%</span>
                        <span className="hold-pulse-text">SCANNING</span>
                      </div>
                    ) : (
                      <svg
                        viewBox="0 0 100 100"
                        className="fingerprint-dermal-svg"
                        style={{ color: currentSector.color }}
                      >
                        {/* Outer Solid Circle Rim */}
                        <circle cx="50" cy="50" r="45" fill="none" stroke="currentColor" strokeWidth="5.5" />

                        {/* Outermost Dashed Perimeter Ridges */}
                        <path d="M 40 15 C 47 13 53 13 60 15" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 68 20 C 76 26 82 34 83 45" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 32 20 C 24 26 18 34 17 45" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 18 54 C 19 62 23 70 29 77" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 82 54 C 81 62 77 70 71 77" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 69 82 C 67 85 65 87 62 89" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 31 82 C 33 85 35 87 38 89" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />

                        {/* Second Ring */}
                        <path d="M 44 23 C 48 21.5 52 21.5 56 23" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 63 27 C 71 33 75 42 74 54 C 73 65 70 76 68 84" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 37 27 C 29 33 25 42 26 54 C 27 63 29 72 32 80" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />

                        {/* Third Ring (Sweeping Arch) */}
                        <path d="M 39 37 C 46 30 54 30 61 37 C 67 43 68 51 66 62 C 65 71 63 80 61 88" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 33 46 C 34 57 37 70 40 81" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />

                        {/* Central Loop & Core Hairpin */}
                        <path d="M 41 53 C 41 64 43 75 45 85" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 45 47 C 45 39 55 39 55 47 C 56 58 57 69 58 81" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                        <path d="M 49 54 C 49 48 53 48 53 54 C 53 64 54 74 54 84" fill="none" stroke="currentColor" strokeWidth="4.2" strokeLinecap="round" />
                      </svg>
                    )}

                    <div 
                      className={`laser-scanner-line ${isHolding ? 'scanning' : ''}`}
                      style={{ backgroundColor: currentSector.color }}
                    ></div>
                  </div>
                </button>
              </div>

              {/* Hold Status & Direct Submit Action */}
              <div className="scanner-action-dock">
                <div className="scanner-status-caption font-mono">
                  {loading
                    ? 'AUTHENTICATING ENCRYPTED KEY...'
                    : isHolding
                    ? `DERMAL HOLD: ${Math.round(holdPercent)}% (KEEP HOLDING)`
                    : 'HOLD 3s OR HIT ENTER TO LOGIN'}
                </div>

                <button
                  type="button"
                  className="quick-auth-submit-btn font-mono"
                  onClick={triggerLogin}
                  disabled={loading}
                  style={{
                    borderColor: currentSector.color,
                    boxShadow: `0 0 14px ${currentSector.color}30`
                  }}
                >
                  {loading ? 'AUTHENTICATING...' : `AUTHORIZE ${currentSector.short} ACCESS ➔`}
                </button>
              </div>
            </div>

            {/* Right Column: Franchise Stats Summary */}
            <div className="auth-panel stats-panel">
              <div className="panel-header-row">
                <span className="panel-subhead">FRANCHISE STATS</span>
                <span className="online-beacon font-mono">🟢 NOMINAL</span>
              </div>

              <div className="readiness-card">
                <div className="readiness-title font-display" style={{ color: currentSector.color }}>
                  {currentSector.short} COMMAND
                </div>
                <div className="readiness-status-line font-mono">
                  WAR ROOM SESSION ACTIVE
                </div>
              </div>

              <div className="readiness-list font-mono">
                <div className="ready-item">
                  <span className="ready-icon">🛡️</span>
                  <div className="ready-info">
                    <span className="ready-label">FRANCHISE LEAD</span>
                    <span className="ready-val">{currentSector.ceo}</span>
                  </div>
                </div>

                <div className="ready-item">
                  <span className="ready-icon">👥</span>
                  <div className="ready-info">
                    <span className="ready-label">ROSTER RETENTIONS</span>
                    <span className="ready-val">{currentSector.retentions}</span>
                  </div>
                </div>

                <div className="ready-item">
                  <span className="ready-icon">✈️</span>
                  <div className="ready-info">
                    <span className="ready-label">OVERSEAS QUOTA</span>
                    <span className="ready-val">{currentSector.overseas}</span>
                  </div>
                </div>

                <div className="ready-item">
                  <span className="ready-icon">⚡</span>
                  <div className="ready-info">
                    <span className="ready-label">BIDDING ENGINE</span>
                    <span className="ready-val" style={{ color: '#10b981' }}>STOMP CONNECTED</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* REGISTER MODE: Interactive Franchise Commissioning */}
        {authMode === 'register' && (
          <div className="register-franchise-grid">
            {/* Left Preview Dossier */}
            <div className="auth-panel dossier-panel">
              <div className="panel-header-row">
                <span className="panel-subhead">NEW FRANCHISE DOSSIER</span>
                <span className="panel-tag font-mono" style={{ color: regColor }}>
                  {regBadge} COMMISSIONING
                </span>
              </div>

              <div className="franchise-brand-card">
                <div className="brand-team-name font-display" style={{ color: regColor }}>
                  {regTeamName.trim().toUpperCase() || 'NEW FRANCHISE NAME'}
                </div>
                <div className="brand-stlogan font-mono" style={{ color: '#94a3b8', fontSize: '0.75rem' }}>
                  "{regSlogan.trim() || 'Roar of Champions'}"
                </div>
                <div className="brand-stadium font-mono">
                  🏟️ {regStadium.trim() || 'Home Arena'}
                </div>
              </div>

              <div className="dossier-metrics font-mono">
                <div className="metric-box">
                  <span className="metric-label">REGISTERED OWNER</span>
                  <span className="metric-value gold font-mono">{regUsername.trim() || 'owner_username'}</span>
                </div>

                <div className="metric-box">
                  <span className="metric-label">STARTING PURSE</span>
                  <span className="metric-value purse-glow font-mono" style={{ color: regColor }}>
                    ₹{regBudgetCr}.00 Cr
                  </span>
                </div>
              </div>
            </div>

            {/* Right Registration Form */}
            <form onSubmit={handleRegisterSubmit} className="register-form-panel">
              <div className="reg-form-fields-grid">
                <div className="reg-field-group">
                  <label className="reg-label font-mono">TEAM NAME *</label>
                  <input
                    type="text"
                    placeholder="e.g. Ahmedabad Aces"
                    value={regTeamName}
                    onChange={(e) => { setRegTeamName(e.target.value); setError(''); }}
                    className="reg-input font-display"
                    required
                  />
                </div>

                <div className="reg-field-group">
                  <label className="reg-label font-mono">SHORT CODE (3-4 LTR)</label>
                  <input
                    type="text"
                    maxLength={4}
                    placeholder="e.g. ACES"
                    value={regShortCode}
                    onChange={(e) => setRegShortCode(e.target.value.toUpperCase())}
                    className="reg-input font-mono"
                  />
                </div>

                <div className="reg-field-group">
                  <label className="reg-label font-mono">OWNER USERNAME *</label>
                  <input
                    type="text"
                    placeholder="e.g. aces_owner"
                    value={regUsername}
                    onChange={(e) => { setRegUsername(e.target.value); setError(''); }}
                    className="reg-input font-mono"
                    required
                  />
                </div>

                <div className="reg-field-group">
                  <label className="reg-label font-mono">PASSWORD *</label>
                  <input
                    type="password"
                    placeholder="At least 4 characters"
                    value={regPassword}
                    onChange={(e) => { setRegPassword(e.target.value); setError(''); }}
                    className="reg-input font-mono"
                    required
                  />
                </div>

                {/* Crest selector */}
                <div className="reg-field-group full-width">
                  <label className="reg-label font-mono">FRANCHISE CREST BADGE</label>
                  <div className="badge-selector-row">
                    {PRESET_BADGES.map((b) => (
                      <button
                        key={b}
                        type="button"
                        onClick={() => setRegBadge(b)}
                        className={`badge-select-btn ${regBadge === b ? 'selected' : ''}`}
                      >
                        {b}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Color Selector */}
                <div className="reg-field-group full-width">
                  <label className="reg-label font-mono">FRANCHISE COLOR THEME: <strong style={{ color: regColor }}>{regColor}</strong></label>
                  <div className="color-picker-row">
                    {PRESET_COLORS.map((c) => (
                      <button
                        key={c}
                        type="button"
                        onClick={() => setRegColor(c)}
                        className={`color-swatch-btn ${regColor === c ? 'selected' : ''}`}
                        style={{ backgroundColor: c }}
                      />
                    ))}
                    <input
                      type="color"
                      value={regColor}
                      onChange={(e) => setRegColor(e.target.value)}
                      className="custom-color-picker-input"
                    />
                  </div>
                </div>

                <div className="reg-field-group">
                  <label className="reg-label font-mono">SLOGAN / MOTTO</label>
                  <input
                    type="text"
                    placeholder="e.g. Roar of Champions"
                    value={regSlogan}
                    onChange={(e) => setRegSlogan(e.target.value)}
                    className="reg-input font-mono"
                  />
                </div>

                <div className="reg-field-group">
                  <label className="reg-label font-mono">HOME STADIUM</label>
                  <input
                    type="text"
                    placeholder="e.g. Narendra Modi Stadium"
                    value={regStadium}
                    onChange={(e) => setRegStadium(e.target.value)}
                    className="reg-input font-mono"
                  />
                </div>
              </div>

              <div className="reg-action-buttons-row">
                <button
                  type="button"
                  onClick={() => setAuthMode('login')}
                  className="reg-cancel-btn font-mono"
                >
                  CANCEL
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="reg-submit-btn font-mono"
                  style={{
                    background: `linear-gradient(135deg, ${regColor} 0%, #0f172a 100%)`,
                    borderColor: regColor
                  }}
                >
                  {loading ? 'COMMISSIONING...' : '✦ COMMISSION FRANCHISE & ENTER ➔'}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Error Feedback */}
        {error && (
          <div className="auth-error-alert font-mono">
            <span>⚠️ {error}</span>
          </div>
        )}

        {/* Bottom Cybernetic Authentication Indicator Dots */}
        <div className="auth-bottom-dock">
          <div className="auth-dots-track">
            {[...Array(dotCount)].map((_, i) => (
              <div
                key={i}
                className={`cyber-auth-pip ${i < activeDotCount ? 'pip-illuminated' : ''}`}
                style={{
                  backgroundColor: i < activeDotCount ? currentSector.color : 'rgba(255,255,255,0.06)',
                  boxShadow: i < activeDotCount ? `0 0 8px ${currentSector.color}` : 'none'
                }}
              ></div>
            ))}
          </div>
          <div className="auth-prompt-note font-mono">
            FRANCHISE SECTOR: <strong>{currentSector.name}</strong> • ENTER KEY OR HOLD SCANNER 3 SECONDS
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
