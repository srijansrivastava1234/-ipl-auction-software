-- ==========================================================
-- 🏏 IPL MEGA AUCTION SYSTEM - DDL SCHEMA & INITIAL DATA (MySQL 8.0)
-- Developed by Member 3: Database & Bidding Engine Developer
-- ==========================================================

CREATE DATABASE IF NOT EXISTS ipl_auction_db;
USE ipl_auction_db;

-- 1. DROP EXISTING TABLES IN REVERSE DEPENDENCY ORDER
DROP TABLE IF EXISTS bids;
DROP TABLE IF EXISTS auctions;
DROP TABLE IF EXISTS players;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS teams;

-- 2. TEAMS TABLE
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    budget DECIMAL(15, 2) NOT NULL DEFAULT 1000000000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. USERS TABLE (Authentication & Team Owners)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    team_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. PLAYERS TABLE (Supports Concurrency & Pessimistic Row Locking)
CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    base_price DECIMAL(15, 2) NOT NULL,
    original_base_price DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNSOLD',
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    overseas BOOLEAN NOT NULL DEFAULT FALSE,
    team_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_player_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL,
    INDEX idx_player_status (status),
    INDEX idx_player_team (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. BIDS TABLE (Audit Trail of All Live Bids)
CREATE TABLE bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    bid_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bid_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    INDEX idx_bids_player (player_id),
    INDEX idx_bids_team (team_id),
    INDEX idx_bids_amount (amount)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. AUCTIONS TABLE (Current Active Auction Session & Optimistic Concurrency Control)
CREATE TABLE auctions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT UNIQUE,
    highest_bidder_team_id BIGINT NULL,
    current_bid DECIMAL(15, 2) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LIVE',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_auction_player FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE,
    CONSTRAINT fk_auction_highest_bidder FOREIGN KEY (highest_bidder_team_id) REFERENCES teams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================================
-- 7. INITIAL SEED DATA
-- ==========================================================

-- Insert 10 Official IPL Franchises (₹100 Crore purse each)
INSERT INTO teams (id, name, budget) VALUES
(1, 'Chennai Super Kings', 1000000000.00),
(2, 'Mumbai Indians', 1000000000.00),
(3, 'Royal Challengers Bengaluru', 1000000000.00),
(4, 'Kolkata Knight Riders', 1000000000.00),
(5, 'Rajasthan Royals', 1000000000.00),
(6, 'Sunrisers Hyderabad', 1000000000.00),
(7, 'Delhi Capitals', 1000000000.00),
(8, 'Gujarat Titans', 1000000000.00),
(9, 'Lucknow Super Giants', 1000000000.00),
(10, 'Punjab Kings', 1000000000.00);

-- Insert Default Admin & Franchise Owners (BCrypt hashed)
-- admin / admin123
INSERT INTO users (username, password, role, team_id) VALUES
('admin', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'ADMIN', NULL),
('csk_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 1),
('mi_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 2),
('rcb_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 3),
('kkr_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 4),
('rr_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 5),
('srh_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 6),
('dc_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 7),
('gt_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 8),
('lsg_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 9),
('pbks_owner', '$2a$10$7R9bZtL2qQe/nF0v7W9gEuR2V6I6N0V6I6N0V6I6N0V6I6N0V6I6N', 'TEAM_OWNER', 10);

-- Insert Star IPL Marquee Players
INSERT INTO players (name, role, base_price, original_base_price, status, country, overseas) VALUES
('Virat Kohli', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Rohit Sharma', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Jasprit Bumrah', 'Bowler', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('MS Dhoni', 'Wicketkeeper', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Hardik Pandya', 'All-Rounder', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Heinrich Klaasen', 'Wicketkeeper', 20000000.00, 20000000.00, 'UNSOLD', 'South Africa', TRUE),
('Travis Head', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'Australia', TRUE),
('Pat Cummins', 'All-Rounder', 20000000.00, 20000000.00, 'UNSOLD', 'Australia', TRUE),
('Rashid Khan', 'Bowler', 20000000.00, 20000000.00, 'UNSOLD', 'Afghanistan', TRUE),
('Nicholas Pooran', 'Wicketkeeper', 20000000.00, 20000000.00, 'UNSOLD', 'West Indies', TRUE),
('Shubman Gill', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Suryakumar Yadav', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Rishabh Pant', 'Wicketkeeper', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Shreyas Iyer', 'Batsman', 20000000.00, 20000000.00, 'UNSOLD', 'India', FALSE),
('Mitchell Starc', 'Bowler', 20000000.00, 20000000.00, 'UNSOLD', 'Australia', TRUE);
