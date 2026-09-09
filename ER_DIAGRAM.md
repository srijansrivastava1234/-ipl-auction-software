# Database Entity-Relationship (ER) Diagram Documentation

## 📊 Overview
This document specifies the MySQL relational schema design for the **IPL Auction Software**, developed by **Member 3 (Database & Bidding Engine Developer)**.

---

## 🗂️ Mermaid ER Diagram

```mermaid
erDiagram
    USERS {
        bigint id PK
        string username UK
        string email UK
        string password_hash
        string role
        timestamp created_at
    }

    TEAMS {
        bigint id PK
        string name UK
        string short_name UK
        decimal purse_balance
        int total_slots
        int max_foreign_slots
        int total_slots_filled
        int foreign_slots_filled
        bigint user_id FK
        timestamp created_at
    }

    PLAYERS {
        bigint id PK
        string name
        string category
        string player_role
        decimal base_price
        decimal current_bid
        bigint team_id FK
        boolean is_foreign
        boolean is_sold
        bigint version
        timestamp created_at
    }

    AUCTION_SESSIONS {
        bigint id PK
        string session_name
        string status
        bigint current_player_id FK
        decimal current_highest_bid
        bigint current_highest_bidder_team_id FK
        bigint version
        timestamp start_time
        timestamp end_time
        timestamp created_at
    }

    BIDS {
        bigint id PK
        bigint auction_session_id FK
        bigint player_id FK
        bigint team_id FK
        decimal bid_amount
        timestamp bid_time
        string status
        bigint version
    }

    USERS ||--o| TEAMS : "owns/manages"
    TEAMS ||--o{ PLAYERS : "owns squad"
    PLAYERS ||--o{ BIDS : "receives bids"
    TEAMS ||--o{ BIDS : "places bids"
    AUCTION_SESSIONS ||--o{ BIDS : "logs bids"
    AUCTION_SESSIONS }|--o| PLAYERS : "current player"
    AUCTION_SESSIONS }|--o| TEAMS : "current lead team"
```

---

## 🔑 Table Specifications

### 1. `users` Table
- Stores user credentials and system roles.
- Role Constraint: `ADMIN`, `TEAM_OWNER`, `AUCTIONEER`.

### 2. `teams` Table
- Tracks franchise name, remaining purse balance (default ₹100 Crore = 100,000,000.00), and squad constraints (total slots 25, foreign slots 8).
- Foreign Key: `user_id` -> `users.id`.

### 3. `players` Table
- Stores player profile, base price, overseas flag, sold status, and current winning bid.
- Category: `BATSMAN`, `BOWLER`, `ALL_ROUNDER`, `WICKET_KEEPER`.
- Role: `INDIAN`, `FOREIGN`.
- Contains `version` column for JPA Optimistic Locking `@Version`.

### 4. `auction_sessions` Table
- Manages active live auction state, currently bidding player, and current lead franchise.
- Status: `UPCOMING`, `LIVE`, `PAUSED`, `COMPLETED`.

### 5. `bids` Table
- High-concurrency transaction ledger recording every bid attempt by a team.
- Status: `ACCEPTED`, `REJECTED`, `OUTBID`.
- Indexed on `(player_id, bid_amount DESC)` and `(auction_session_id, bid_time DESC)`.
