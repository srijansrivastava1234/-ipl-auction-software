-- Flyway Database Migration V1__init_ipl_bidding_schema.sql
-- Relational DB Schema for Member 3 (Database & Bidding Engine)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'TEAM_OWNER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'TEAM_OWNER', 'AUCTIONEER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    short_name VARCHAR(10) NOT NULL UNIQUE,
    purse_balance DECIMAL(15, 2) NOT NULL DEFAULT 100000000.00,
    total_slots INT NOT NULL DEFAULT 25,
    max_foreign_slots INT NOT NULL DEFAULT 8,
    total_slots_filled INT NOT NULL DEFAULT 0,
    foreign_slots_filled INT NOT NULL DEFAULT 0,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_purse CHECK (purse_balance >= 0.00),
    CONSTRAINT chk_total_slots CHECK (total_slots_filled <= total_slots),
    CONSTRAINT chk_foreign_slots CHECK (foreign_slots_filled <= max_foreign_slots)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    player_role VARCHAR(20) NOT NULL,
    base_price DECIMAL(15, 2) NOT NULL,
    current_bid DECIMAL(15, 2) DEFAULT 0.00,
    team_id BIGINT DEFAULT NULL,
    is_foreign BOOLEAN NOT NULL DEFAULT FALSE,
    is_sold BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT chk_player_category CHECK (category IN ('BATSMAN', 'BOWLER', 'ALL_ROUNDER', 'WICKET_KEEPER')),
    CONSTRAINT chk_player_role CHECK (player_role IN ('INDIAN', 'FOREIGN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS auction_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    current_player_id BIGINT DEFAULT NULL,
    current_highest_bid DECIMAL(15, 2) DEFAULT 0.00,
    current_highest_bidder_team_id BIGINT DEFAULT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_session_player FOREIGN KEY (current_player_id) REFERENCES players(id) ON DELETE SET NULL,
    CONSTRAINT fk_session_team FOREIGN KEY (current_highest_bidder_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    CONSTRAINT chk_auction_status CHECK (status IN ('UPCOMING', 'LIVE', 'PAUSED', 'COMPLETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auction_session_id BIGINT NOT NULL,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    bid_amount DECIMAL(15, 2) NOT NULL,
    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACCEPTED',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_bid_session FOREIGN KEY (auction_session_id) REFERENCES auction_sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT chk_bid_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'OUTBID'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bids_player_amount ON bids(player_id, bid_amount DESC);
CREATE INDEX idx_bids_session_time ON bids(auction_session_id, bid_time DESC);
CREATE INDEX idx_players_sold_category ON players(is_sold, category);
CREATE INDEX idx_teams_purse ON teams(purse_balance);
