import { useState } from 'react';
import axios from 'axios';

const ROLE_OPTIONS = [
  { label: 'Batsman', icon: '🏏', desc: 'Top/Middle Order' },
  { label: 'Bowler', icon: '🥎', desc: 'Pace / Spin' },
  { label: 'All-Rounder', icon: '⚡', desc: 'Dual Discipline' },
  { label: 'Wicketkeeper-Batsman', icon: '🧤', desc: 'Gloveman & Batter' }
];

const COUNTRY_PRESETS = [
  { name: 'India', flag: '🇮🇳', isOverseas: false },
  { name: 'Australia', flag: '🇦🇺', isOverseas: true },
  { name: 'England', flag: '🏴󠁧󠁢󠁥󠁮󠁧󠁿', isOverseas: true },
  { name: 'South Africa', flag: '🇿🇦', isOverseas: true },
  { name: 'West Indies', flag: '🌴', isOverseas: true },
  { name: 'New Zealand', flag: '🇳🇿', isOverseas: true },
  { name: 'Afghanistan', flag: '🇦🇫', isOverseas: true },
  { name: 'Sri Lanka', flag: '🇱🇰', isOverseas: true }
];

const PRICE_PRESETS = [
  { label: '₹20 L', value: 2000000 },
  { label: '₹50 L', value: 5000000 },
  { label: '₹1.00 Cr', value: 10000000 },
  { label: '₹1.50 Cr', value: 15000000 },
  { label: '₹2.00 Cr', value: 20000000 }
];

function AddPlayerModal({ isOpen, onClose, onPlayerAdded, user, backendUrl }) {
  const [name, setName] = useState('');
  const [role, setRole] = useState('Batsman');
  const [country, setCountry] = useState('India');
  const [overseas, setOverseas] = useState(false);
  const [basePrice, setBasePrice] = useState(20000000); // default 2 Cr
  const [customPriceCr, setCustomPriceCr] = useState('2.00');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  if (!isOpen) return null;

  const handleCountrySelect = (preset) => {
    setCountry(preset.name);
    setOverseas(preset.isOverseas);
  };

  const handlePricePreset = (val) => {
    setBasePrice(val);
    setCustomPriceCr((val / 10000000).toFixed(2));
  };

  const handleCustomPriceChange = (valStr) => {
    setCustomPriceCr(valStr);
    const num = parseFloat(valStr);
    if (!isNaN(num) && num > 0) {
      setBasePrice(Math.round(num * 10000000));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('PLAYER NAME IS MANDATORY');
      return;
    }
    if (!basePrice || basePrice <= 0) {
      setError('VALID BASE PRICE IS REQUIRED');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const config = {
        headers: {
          Authorization: `Bearer ${user?.token}`,
          'Content-Type': 'application/json'
        }
      };

      const payload = {
        name: name.trim(),
        role: role,
        basePrice: basePrice,
        originalBasePrice: basePrice,
        status: 'UNSOLD',
        country: country.trim() || 'India',
        overseas: Boolean(overseas)
      };

      const response = await axios.post(`${backendUrl}/api/players`, payload, config);
      
      setSuccessMsg(`✓ ${response.data.name} SUCCESSFULLY DRAFTED!`);
      
      if (onPlayerAdded) {
        onPlayerAdded(response.data);
      }

      setTimeout(() => {
        setLoading(false);
        setSuccessMsg('');
        setName('');
        onClose();
      }, 900);

    } catch (err) {
      const msg = err.response?.data?.error || err.response?.data?.message || 'Failed to create player';
      setError(msg);
      setLoading(false);
    }
  };

  const currentRoleObj = ROLE_OPTIONS.find(r => r.label === role) || ROLE_OPTIONS[0];

  return (
    <div className="player-modal-backdrop" onClick={onClose}>
      <div className="player-modal-card" onClick={(e) => e.stopPropagation()}>
        
        {/* Modal Top Header */}
        <div className="player-modal-header">
          <div className="modal-header-left">
            <span className="modal-header-badge">🏏 BCCI AUCTION DRAFT</span>
            <h2 className="modal-title font-display">COMMISSION NEW PLAYER</h2>
            <span className="modal-subtitle font-mono">OFFICIAL REGISTRATION FOR IPL 2026 AUCTION POOL</span>
          </div>
          <button onClick={onClose} className="modal-close-btn" title="Close Draft Terminal">
            ✕
          </button>
        </div>

        {error && (
          <div className="modal-alert-error font-mono">
            <span>⚠️</span> {error}
          </div>
        )}

        {successMsg && (
          <div className="modal-alert-success font-mono">
            <span>🎉</span> {successMsg}
          </div>
        )}

        <div className="player-modal-grid">
          {/* Left Column: Form Fields */}
          <form onSubmit={handleSubmit} className="player-form-col">
            
            {/* Player Name */}
            <div className="form-group">
              <label className="form-label font-mono">PLAYER FULL NAME *</label>
              <input 
                type="text"
                placeholder="e.g. Travis Head, Heinrich Klaasen"
                value={name}
                onChange={(e) => { setName(e.target.value); setError(''); }}
                className="modal-input font-display"
                autoFocus
                required
              />
            </div>

            {/* Specialist Role */}
            <div className="form-group">
              <label className="form-label font-mono">SPECIALIST DISCIPLINE *</label>
              <div className="role-options-grid">
                {ROLE_OPTIONS.map((r) => (
                  <button
                    key={r.label}
                    type="button"
                    onClick={() => setRole(r.label)}
                    className={`role-choice-card ${role === r.label ? 'selected' : ''}`}
                  >
                    <span className="role-choice-icon">{r.icon}</span>
                    <span className="role-choice-title font-mono">{r.label}</span>
                    <span className="role-choice-sub font-mono">{r.desc}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Country & Overseas */}
            <div className="form-group">
              <div className="form-row-header">
                <label className="form-label font-mono">NATIONALITY &amp; JURISDICTION</label>
                <label className="overseas-toggle-label font-mono">
                  <input 
                    type="checkbox"
                    checked={overseas}
                    onChange={(e) => setOverseas(e.target.checked)}
                    className="overseas-checkbox"
                  />
                  <span>✈️ OVERSEAS QUOTA</span>
                </label>
              </div>

              <div className="country-presets-bar">
                {COUNTRY_PRESETS.map((p) => (
                  <button
                    key={p.name}
                    type="button"
                    onClick={() => handleCountrySelect(p)}
                    className={`country-preset-btn ${country === p.name ? 'active' : ''}`}
                  >
                    <span>{p.flag}</span>
                    <span className="font-mono">{p.name}</span>
                  </button>
                ))}
              </div>

              <input 
                type="text"
                placeholder="Or custom country name..."
                value={country}
                onChange={(e) => {
                  setCountry(e.target.value);
                  setOverseas(e.target.value.toLowerCase() !== 'india');
                }}
                className="modal-input-compact font-mono"
              />
            </div>

            {/* Base Price */}
            <div className="form-group">
              <label className="form-label font-mono">AUCTION BASE PRICE *</label>
              <div className="price-presets-bar">
                {PRICE_PRESETS.map((p) => (
                  <button
                    key={p.value}
                    type="button"
                    onClick={() => handlePricePreset(p.value)}
                    className={`price-preset-btn font-mono ${basePrice === p.value ? 'active' : ''}`}
                  >
                    {p.label}
                  </button>
                ))}
              </div>

              <div className="custom-price-row">
                <div className="price-input-wrapper">
                  <span className="currency-tag font-mono">₹</span>
                  <input 
                    type="number"
                    step="0.05"
                    min="0.1"
                    placeholder="2.00"
                    value={customPriceCr}
                    onChange={(e) => handleCustomPriceChange(e.target.value)}
                    className="modal-input font-mono"
                  />
                  <span className="cr-tag font-mono">CRORES (Cr)</span>
                </div>
                <div className="exact-inr-display font-mono">
                  = ₹{basePrice.toLocaleString('en-IN')} INR
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="modal-actions-row">
              <button 
                type="button" 
                onClick={onClose} 
                className="modal-cancel-btn font-mono"
                disabled={loading}
              >
                CANCEL
              </button>
              <button 
                type="submit" 
                className="modal-submit-btn font-mono"
                disabled={loading}
              >
                {loading ? 'DRAFTING TO POOL...' : '✦ DRAFT PLAYER INTO AUCTION'}
              </button>
            </div>
          </form>

          {/* Right Column: Live Holographic Player Card Preview */}
          <div className="player-preview-col">
            <span className="preview-label font-mono">LIVE HOLOGRAM PREVIEW</span>
            <div className="hologram-player-card">
              <div className="card-ambient-glow"></div>
              
              <div className="card-top-hud">
                <span className="preview-lot-badge font-mono">LOT: NEW DRAFT</span>
                <span className="preview-status-pill font-mono">⚪ UNSOLD POOL</span>
              </div>

              <div className="preview-avatar-circle">
                <span className="preview-avatar-icon">{currentRoleObj.icon}</span>
              </div>

              <div className="preview-identity">
                <h3 className="preview-name font-display">
                  {name.trim() || 'PLAYER NAME'}
                </h3>
                <div className="preview-role-tag font-mono">
                  {role.toUpperCase()}
                </div>
              </div>

              <div className="preview-metrics-grid">
                <div className="preview-metric-box">
                  <span className="metric-lbl font-mono">NATIONALITY</span>
                  <span className="metric-val font-mono">
                    {country.toUpperCase()} {overseas ? '✈️' : '🇮🇳'}
                  </span>
                </div>
                <div className="preview-metric-box">
                  <span className="metric-lbl font-mono">BASE PRICE</span>
                  <span className="metric-val highlight font-mono">
                    ₹{(basePrice / 10000000).toFixed(2)} Cr
                  </span>
                </div>
              </div>

              <div className="card-bottom-telemetry font-mono">
                <span className="telemetry-spark-dot">●</span> READY FOR AUCTION BLOCK
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}

export default AddPlayerModal;
