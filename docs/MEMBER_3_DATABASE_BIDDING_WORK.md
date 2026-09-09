# Member 3: Database & Bidding Engine Developer - Weeks 1 to 10 Documentation

## 📌 Scope & Responsibilities
- **Database Schema & Migrations**: Relational DDL MySQL schema, ER diagram design, and Flyway migration script (`V1__init_ipl_bidding_schema.sql`).
- **JPA Persistence & Entities**: Mapping of core entities (`User`, `Team`, `Player`, `AuctionSession`, `Bid`).
- **Bidding Rules & Engine**: IPL price slab calculator (`BidIncrementCalculator`), purse balance, squad limits (25 max total, 8 max foreign) in `PurseAndSlotValidator`.
- **Concurrency Control**: Pessimistic `@Lock(LockModeType.PESSIMISTIC_WRITE)` and optimistic `@Version` locking for simultaneous bid execution without race conditions.
- **Auction Session Management**: Player sale finalization (`sellCurrentPlayer`), unsold marking, and player session advancement.

---

## 🗄️ Database Tables & Specification
1. `users`: System users and franchise owners.
2. `teams`: Franchise details, remaining purse balance, total and foreign squad slots.
3. `players`: Player category, role, base price, current bid, sold status, optimistic locking version.
4. `auction_sessions`: Session name, status (UPCOMING, LIVE, PAUSED, COMPLETED), current player, lead team.
5. `bids`: Complete audit trail of placed bids. Indexed on `(player_id, bid_amount DESC)` and `(auction_session_id, bid_time DESC)`.

---

## 🚀 Key Endpoints (Member 3 Scope)
- `POST /api/v1/bids/place`: Atomic bid placement with validation and lock acquisition.
- `GET /api/v1/bids/live/{playerId}`: Real-time highest bid, lead team, and next minimum bid.
- `GET /api/v1/bids/history/player/{playerId}`: Player bidding logs.
- `GET /api/v1/bids/history/session/{sessionId}`: Session bidding logs.
- `POST /api/v1/bids/session/{sessionId}/sell-current-player`: Finalize player sale, deduct purse, update slots.
- `POST /api/v1/bids/session/{sessionId}/unsold-current-player`: Mark player unsold.
- `POST /api/v1/bids/session/{sessionId}/advance/{nextPlayerId}`: Advance session to next player.
