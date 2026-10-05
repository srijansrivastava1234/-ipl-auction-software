// Comprehensive Player Career Intelligence & 2026 AI Forecast Engine

const SUPERSTAR_STATS = {
  'VIRAT KOHLI': {
    matches: 252,
    runs: '8,004',
    wickets: 4,
    avg: '38.6',
    strikeRate: '131.0',
    highScore: '113',
    centuries: 8,
    fifties: 55,
    dotBallPct: '28.4%',
    forecastRuns: '640+ RUNS',
    forecastHighlight: 'ORANGE CAP FRONTRUNNER',
    impactScore: 98.6,
    impactTier: 'ELITE ANCHOR',
    powerplayRating: 94,
    deathRating: 96,
    tacticalNote: 'Supreme anchor with 145+ death SR acceleration'
  },
  'ROHIT SHARMA': {
    matches: 257,
    runs: '6,628',
    wickets: 15,
    avg: '29.7',
    strikeRate: '131.1',
    highScore: '109*',
    centuries: 2,
    fifties: 43,
    dotBallPct: '32.1%',
    forecastRuns: '560+ RUNS',
    forecastHighlight: 'POWERPLAY BLITZKRIEG',
    impactScore: 96.2,
    impactTier: 'HITMAN LEADER',
    powerplayRating: 98,
    deathRating: 91,
    tacticalNote: 'Ultra-aggressive Powerplay intent (170+ PP strike rate)'
  },
  'MS DHONI': {
    matches: 264,
    runs: '5,243',
    wickets: 0,
    avg: '39.1',
    strikeRate: '137.5',
    highScore: '84*',
    centuries: 0,
    fifties: 24,
    dismissals: 142,
    forecastRuns: '280+ RUNS',
    forecastHighlight: 'DEATH OVERS FINISHER',
    impactScore: 99.1,
    impactTier: 'TACTICAL MASTER',
    powerplayRating: 88,
    deathRating: 99,
    tacticalNote: '220+ Death Overs Strike Rate & Master Strategist'
  },
  'JASPRIT BUMRAH': {
    matches: 133,
    wickets: 165,
    runs: '68',
    economy: '7.30',
    avg: '22.5',
    bestBowling: '5/10',
    dotBallPct: '48.6%',
    forecastRuns: '26+ WICKETS',
    forecastHighlight: 'PURPLE CAP FAVORITE',
    impactScore: 99.4,
    impactTier: 'LETHAL YORKER',
    powerplayRating: 97,
    deathRating: 99,
    tacticalNote: 'Sub-6.50 death economy with unplayable yorkers'
  },
  'HARDIK PANDYA': {
    matches: 137,
    runs: '2,525',
    wickets: 64,
    avg: '28.8',
    strikeRate: '145.8',
    economy: '8.85',
    bestBowling: '3/17',
    forecastRuns: '450+ R / 16+ W',
    forecastHighlight: 'ALL-ROUND RESONANCE',
    impactScore: 95.8,
    impactTier: 'DUAL ENGINE',
    powerplayRating: 91,
    deathRating: 95,
    tacticalNote: 'Middle-overs wicket-taker & heavy hitting finisher'
  },
  'RISHABH PANT': {
    matches: 111,
    runs: '3,284',
    wickets: 0,
    avg: '35.3',
    strikeRate: '148.5',
    highScore: '128*',
    centuries: 1,
    fifties: 18,
    forecastRuns: '540+ RUNS',
    forecastHighlight: 'DYNAMIC GAME CHANGER',
    impactScore: 97.0,
    impactTier: 'EXPLOSIVE SOUTHPAW',
    powerplayRating: 93,
    deathRating: 97,
    tacticalNote: '360° boundary finder against pace and spin'
  },
  'TRAVIS HEAD': {
    matches: 26,
    runs: '891',
    wickets: 4,
    avg: '40.5',
    strikeRate: '178.6',
    highScore: '102',
    centuries: 1,
    fifties: 5,
    forecastRuns: '620+ RUNS',
    forecastHighlight: 'POWERPLAY DEMOLITION',
    impactScore: 97.8,
    impactTier: 'MAXIMUM VELOCITY',
    powerplayRating: 99,
    deathRating: 89,
    tacticalNote: 'Fastest 50s in tournament history'
  },
  'HEINRICH KLAASEN': {
    matches: 35,
    runs: '993',
    wickets: 0,
    avg: '39.7',
    strikeRate: '168.3',
    highScore: '104',
    centuries: 1,
    fifties: 6,
    forecastRuns: '510+ RUNS',
    forecastHighlight: 'SPIN DESTROYER',
    impactScore: 97.5,
    impactTier: 'POWER CLEAN HITTER',
    powerplayRating: 88,
    deathRating: 98,
    tacticalNote: 'Highest boundary % against spin bowling'
  },
  'RASHID KHAN': {
    matches: 121,
    wickets: 149,
    runs: '443',
    economy: '6.82',
    avg: '21.8',
    bestBowling: '4/24',
    dotBallPct: '44.2%',
    forecastRuns: '24+ WICKETS',
    forecastHighlight: 'MYSTERY MAESTRO',
    impactScore: 98.4,
    impactTier: 'SPIN WIZARD',
    powerplayRating: 92,
    deathRating: 96,
    tacticalNote: 'Lethal wrong-un & explosive late-order batting'
  },
  'MITCHELL STARC': {
    matches: 41,
    wickets: 51,
    runs: '96',
    economy: '7.98',
    avg: '21.0',
    bestBowling: '4/15',
    forecastRuns: '22+ WICKETS',
    forecastHighlight: 'HIGH-VELOCITY SWING',
    impactScore: 94.5,
    impactTier: 'PACE ENFORCER',
    powerplayRating: 96,
    deathRating: 95,
    tacticalNote: '148+ km/h inswinging yorkers with new ball & death'
  },
  'SURYAKUMAR YADAV': {
    matches: 150,
    runs: '3,594',
    wickets: 0,
    avg: '32.1',
    strikeRate: '145.3',
    highScore: '103*',
    centuries: 2,
    fifties: 24,
    forecastRuns: '580+ RUNS',
    forecastHighlight: '360° INNOVATION',
    impactScore: 98.8,
    impactTier: 'MR. 360',
    powerplayRating: 92,
    deathRating: 98,
    tacticalNote: 'Unmatched boundary mapping behind square leg'
  },
  'SHUBMAN GILL': {
    matches: 103,
    runs: '3,216',
    wickets: 0,
    avg: '37.8',
    strikeRate: '135.7',
    highScore: '129',
    centuries: 4,
    fifties: 20,
    forecastRuns: '650+ RUNS',
    forecastHighlight: 'ORANGE CAP THREAT',
    impactScore: 97.4,
    impactTier: 'ELEGANT STROKEMAKER',
    powerplayRating: 96,
    deathRating: 92,
    tacticalNote: 'Flawless technique and match-winning hundreds'
  },
  'SANJU SAMSON': {
    matches: 167,
    runs: '4,419',
    wickets: 0,
    avg: '30.7',
    strikeRate: '138.9',
    highScore: '119',
    centuries: 3,
    fifties: 25,
    forecastRuns: '520+ RUNS',
    forecastHighlight: 'CAPTAIN CLUTCH',
    impactScore: 95.2,
    impactTier: 'CLEAN STRIKER',
    powerplayRating: 95,
    deathRating: 93,
    tacticalNote: 'Effortless lofted sixes over extra cover'
  },
  'ANDRE RUSSELL': {
    matches: 127,
    runs: '2,484',
    wickets: 115,
    avg: '29.2',
    strikeRate: '174.9',
    economy: '9.15',
    bestBowling: '5/15',
    forecastRuns: '420+ R / 18+ W',
    forecastHighlight: 'DRE RUSS MUSCLE',
    impactScore: 98.2,
    impactTier: 'MATCH DESTROYER',
    powerplayRating: 86,
    deathRating: 99,
    tacticalNote: 'Most destructive strike rate in death overs'
  },
  'SUNIL NARINE': {
    matches: 177,
    runs: '1,534',
    wickets: 180,
    economy: '6.73',
    avg: '25.4',
    strikeRate: '165.8',
    bestBowling: '5/19',
    forecastRuns: '400+ R / 20+ W',
    forecastHighlight: 'MVP TITAN',
    impactScore: 98.9,
    impactTier: 'ALL-ROUND WEAPON',
    powerplayRating: 98,
    deathRating: 94,
    tacticalNote: 'Pinch hitter century maker & sub-7 economy spinner'
  },
  'YUZVENDRA CHAHAL': {
    matches: 160,
    wickets: 205,
    runs: '43',
    economy: '7.66',
    avg: '21.3',
    bestBowling: '5/40',
    forecastRuns: '23+ WICKETS',
    forecastHighlight: 'ALL-TIME WICKET KING',
    impactScore: 96.8,
    impactTier: 'LEG-SPIN WIZARD',
    powerplayRating: 85,
    deathRating: 95,
    tacticalNote: 'Brave flight and turn outside off stump'
  }
};

// Deterministic generator for custom / drafted players
export function getPlayerAnalytics(player) {
  if (!player) return null;

  const normalizedName = player.name.trim().toUpperCase();

  // Check exact superstar profile
  for (const [key, stats] of Object.entries(SUPERSTAR_STATS)) {
    if (normalizedName.includes(key) || key.includes(normalizedName)) {
      return {
        ...stats,
        isSuperstar: true,
        name: player.name,
        role: player.role || 'Batsman'
      };
    }
  }

  // Generate deterministic realistic profile based on name hash + role + price
  let hash = 0;
  for (let i = 0; i < normalizedName.length; i++) {
    hash = (hash * 31 + normalizedName.charCodeAt(i)) % 100000;
  }

  const role = (player.role || 'Batsman').toUpperCase();
  const basePriceCr = (player.originalBasePrice || player.basePrice || 20000000) / 10000000;
  const isBowler = role.includes('BOWL');
  const isAllRounder = role.includes('ALL') || role.includes('ROUND');
  const isWicketKeeper = role.includes('KEEP') || role.includes('WK');

  const matches = 30 + (hash % 110);
  const impactScore = Math.min(99.0, Math.max(82.0, Number((85 + (hash % 13) + (basePriceCr * 1.5)).toFixed(1))));

  if (isBowler) {
    const wickets = Math.round(matches * (0.95 + ((hash % 40) / 100)));
    const economy = (7.10 + ((hash % 180) / 100)).toFixed(2);
    const avg = (21.0 + ((hash % 90) / 10)).toFixed(1);
    const forecastWickets = Math.round(14 + (basePriceCr * 1.8) + (hash % 8));

    return {
      name: player.name,
      role: player.role,
      matches,
      wickets,
      economy,
      avg,
      dotBallPct: `${Math.round(40 + (hash % 12))}%`,
      forecastRuns: `${forecastWickets}+ WICKETS`,
      forecastHighlight: 'STRIKE BOWLER',
      impactScore,
      impactTier: impactScore > 93 ? 'LETHAL WEAPON' : 'TACTICAL BOWLER',
      powerplayRating: 88 + (hash % 9),
      deathRating: 89 + (hash % 10),
      tacticalNote: `Economy control (${economy}) with swing and seam variation`
    };
  }

  if (isAllRounder) {
    const runs = Math.round(matches * (18 + (hash % 15)));
    const wickets = Math.round(matches * (0.6 + ((hash % 30) / 100)));
    const strikeRate = (136.0 + ((hash % 350) / 10)).toFixed(1);
    const economy = (7.80 + ((hash % 140) / 100)).toFixed(2);

    return {
      name: player.name,
      role: player.role,
      matches,
      runs: runs.toLocaleString('en-IN'),
      wickets,
      strikeRate,
      economy,
      forecastRuns: `${Math.round(300 + (hash % 180))}R / ${Math.round(12 + (hash % 8))}W`,
      forecastHighlight: 'DUAL THREAT',
      impactScore,
      impactTier: 'ALL-ROUND BALANCE',
      powerplayRating: 90 + (hash % 8),
      deathRating: 92 + (hash % 7),
      tacticalNote: `High-impact batting SR (${strikeRate}) & middle over control`
    };
  }

  // Default Batsman / Wicketkeeper
  const runs = Math.round(matches * (26 + (hash % 18)));
  const avg = (30.0 + ((hash % 140) / 10)).toFixed(1);
  const strikeRate = (132.0 + ((hash % 320) / 10)).toFixed(1);
  const fifties = Math.round(runs / 180);
  const centuries = Math.floor(runs / 1200);
  const forecastRuns = Math.round(380 + (basePriceCr * 25) + (hash % 180));

  return {
    name: player.name,
    role: player.role,
    matches,
    runs: runs.toLocaleString('en-IN'),
    avg,
    strikeRate,
    fifties,
    centuries,
    dismissals: isWicketKeeper ? Math.round(matches * 0.75) : undefined,
    forecastRuns: `${forecastRuns}+ RUNS`,
    forecastHighlight: isWicketKeeper ? 'CLUTCH KEEPER-BATSMAN' : 'TOP-ORDER SCORER',
    impactScore,
    impactTier: impactScore > 94 ? 'ELITE ANCHOR' : 'SOLID BATTER',
    powerplayRating: 91 + (hash % 8),
    deathRating: 90 + (hash % 9),
    tacticalNote: `Consistent anchor (${avg} Avg) with ${strikeRate} SR`
  };
}
