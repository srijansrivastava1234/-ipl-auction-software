# 🏏 Member 1 Complete Work & Deliverables (Weeks 1 – 12)

**Author / Lead:** Member 1 — Project Lead & Core Backend REST API Developer  
**Assigned Scope:**
1. Base Spring Boot project architecture & layered structure.
2. Initial Git branches (`main`, `develop`, `feature/member-1-core-backend`).
3. Team Management REST APIs (CRUD).
4. Player Management REST APIs (CRUD).
5. Purse deduction logic & Squad size constraints.
6. Global Exception Handler (`@RestControllerAdvice`).
7. **Week 10 Milestone additions**: Right To Match (RTM) Engine, Accelerated Auction Round, BCCI Roster & Purse Compliance Audit, Macro Auction Analytics & Franchise Roster Exports.
8. **Week 11 Milestone additions**: Real-time STOMP WebSockets, Live Bidding Topic Broadcasts, Automated 30-second Countdown Timer with Automatic Hammer Strike on Timeout.
9. **Week 12 Milestone additions**: Production Observability (Actuator + Micrometer Auction Metrics), Sliding-Window Rate Limiting, Multi-Stage Docker Containerization (`Dockerfile` & `docker-compose.yml`), and Final Release Documentation.

---

## 🔒 Scope Boundaries Enforced Across Sprints
* 🛑 **No Spring Security / JWT** (Reserved for Member 2).
* 🛑 **No Database Schema Scripts / Lock Engine** (Reserved for Member 3).
* 🛑 **No Frontend UI Code** (Reserved for Member 4).
* 🛑 **No Unit Test Suites / Postman Specs** (Reserved for Member 5).

---

## 📅 Member 1 Weekly Sprint Breakdown (Weeks 1 to 12)

### 🗓️ Week 1: Base Project Architecture & Repository Structure
* Initialized Spring Boot 3.x project with Java 17 and Maven Wrapper (`mvnw`, `mvnw.cmd`).
* Created layered package architecture:
  * `com.ipl.auction.config`
  * `com.ipl.auction.controller`
  * `com.ipl.auction.dto.request` / `dto.response`
  * `com.ipl.auction.entity` / `entity.enums`
  * `com.ipl.auction.exception`
  * `com.ipl.auction.repository`
  * `com.ipl.auction.service`
* Established initial GitHub branches (`main`, `develop`, `feature/member-1-core-backend`).
* Implemented baseline health check endpoint (`GET /api/v1/health`).

### 🗓️ Week 2: Team CRUD REST APIs
* Implemented `TeamController` and `TeamService`:
  * `POST /api/v1/teams` — Create franchise with initial budget.
  * `GET /api/v1/teams` — Fetch all franchises.
  * `GET /api/v1/teams/{id}` — Fetch franchise details by ID.
  * `PUT /api/v1/teams/{id}` — Update franchise details.
  * `DELETE /api/v1/teams/{id}` — Remove franchise.
* Built request/response DTOs: `TeamRequest.java`, `TeamResponse.java`.

### 🗓️ Week 3: Player CRUD REST APIs
* Implemented `PlayerController` and `PlayerService`:
  * `POST /api/v1/players` — Register player into auction pool.
  * `GET /api/v1/players` — List all auction players with category filters.
  * `GET /api/v1/players/{id}` — Fetch player details.
  * `PUT /api/v1/players/{id}` — Update player base price or information.
  * `DELETE /api/v1/players/{id}` — Remove player from auction staging.
* Built request/response DTOs: `PlayerRequest.java`, `PlayerResponse.java`.

### 🗓️ Week 4: Global Exception Handler (`@RestControllerAdvice`)
* Created centralized `GlobalExceptionHandler.java`:
  * `ResourceNotFoundException` $\rightarrow$ `404 NOT_FOUND`
  * `InsufficientPurseException` $\rightarrow$ `400 BAD_REQUEST`
  * `SquadLimitExceededException` $\rightarrow$ `400 BAD_REQUEST`
  * `InvalidBidException` $\rightarrow$ `400 BAD_REQUEST`
  * `MethodArgumentNotValidException` $\rightarrow$ `400 BAD_REQUEST` with field error descriptions.
* Built standardized JSON envelope: `ApiResponse<T>`.

### 🗓️ Week 5: Purse Deduction Logic & Minimum Reserve Rule
* Built team purse deduction service (`TeamPurseService`):
  * Dynamic calculation of remaining purse budget.
  * Enforcement of minimum squad reserve rule:
    $$\text{Reserve Fund} = (18 - \text{squadCount}) \times ₹20\,\text{Lakhs}$$
  * Validation that incoming purchase amounts do not breach the reserve buffer.

### 🗓️ Week 6: Squad Size & Overseas Quota Constraints
* Enforced franchise roster rules:
  * Minimum squad size: **18 players**.
  * Maximum squad size cap: **25 players**.
  * Maximum overseas player quota: **8 overseas players** per franchise.
  * Rejection with `SquadLimitExceededException` upon rule breach.

### 🗓️ Week 7: Player Categorization & State Machine
* Player categories: `BATSMAN`, `BOWLER`, `ALL_ROUNDER`, `WICKET_KEEPER`.
* Player lifecycle state transitions: `AVAILABLE` $\rightarrow$ `IN_AUCTION` $\rightarrow$ `SOLD` / `UNSOLD`.

### 🗓️ Week 8: Team Purse Summary REST Endpoints
* Implemented purse query endpoints:
  * `GET /api/v1/teams/{id}/purse-summary` — Real-time remaining purse, squad count, overseas count, and max spendable budget.
  * `GET /api/v1/teams/purse-summary` — List purse summaries for all 10 franchises.

### 🗓️ Week 9: Code Refactoring & API Contract Stabilization
* Standardized all controller responses using `ApiResponse<T>`.
* Optimized database queries and verified CORS configuration for downstream frontend integration.

### 🗓️ Week 10: Right To Match (RTM), Accelerated Auction, Compliance & Macro Reporting
* **Right To Match (RTM) Engine**:
  * Implemented BCCI RTM card tracking per franchise (up to 2 cards).
  * `POST /api/v1/auction/rtm/exercise` — Atomic bid-matching, previous winning bidder refund, RTM franchise purse deduction, and roster transfer.
  * `GET /api/v1/teams/{teamId}/rtm` — Query RTM cards total, used, remaining, and eligibility.
* **Accelerated Auction Round**:
  * `GET /api/v1/auction/accelerated/pool` — Aggregated view of unsold/available pool and shortlist nominations.
  * `POST /api/v1/auction/accelerated/nominate` — Multi-franchise player nominations.
  * `POST /api/v1/auction/accelerated/stage/{playerId}` — Fast-track staging to auction podium.
* **BCCI Compliance & Squad Audit**:
  * `GET /api/v1/compliance/audit` — Macro audit of all 10 franchises checking budget floor (₹75 Cr minimum spend), roster size (18–25), and overseas limits (max 8).
  * `GET /api/v1/compliance/teams/{teamId}` — Detailed franchise-specific compliance breakdown.
* **Macro Auction Analytics & Roster Export**:
  * `GET /api/v1/reports/summary` — High-level statistics (total spent, players sold/unsold, remaining budget).
  * `GET /api/v1/reports/top-buys` — Top 10 most expensive acquisitions across all franchises.
  * `GET /api/v1/reports/teams/{teamId}/roster` — Export roster and financial breakdown per franchise.

---

### 🗓️ Week 11: Real-Time WebSockets & Automated Countdown Timer Engine
* **STOMP / WebSocket Infrastructure**:
  * Added `spring-boot-starter-websocket` to `pom.xml`.
  * Configured `WebSocketConfig.java`:
    * STOMP broker on `/topic`.
    * Application destination prefix on `/app`.
    * SockJS fallback on `/ws-auction` endpoint with unrestricted CORS.
  * Built `LiveAuctionWebSocketController.java`:
    * `@MessageMapping("/bid")` receives live bids from frontend clients.
    * Validates and submits bid via `BiddingService`.
    * Resets countdown clock on valid bid.
    * Broadcasts updated bid payload to `/topic/bids` envelope (`AuctionWebSocketMessage<BidResponse>`).
* **Automated 30-Second Countdown & Auto-Hammer Strike Engine**:
  * Enabled `@EnableScheduling` in `IplAuctionApplication.java`.
  * Implemented `AuctionTimerService.java`:
    * 30-second ticking clock emitting every 1,000ms to `/topic/timer`.
    * Resets back to 30 seconds upon every valid bid.
    * Pause, Resume, and Stop controls for auctioneer override.
    * **Auto-Hammer Strike Execution**: When timer hits `0`, automatically triggers hammer strike:
      * If a winning team exists $\rightarrow$ marks player `SOLD`, finalizes purse debit, emits `HAMMER_SOLD`.
      * If no bids placed $\rightarrow$ marks player `UNSOLD`, transitions state, emits `HAMMER_UNSOLD`.
  * Exposed REST control API in `AuctionTimerController.java`:
    * `POST /api/v1/auction/timer/start`
    * `POST /api/v1/auction/timer/pause`
    * `POST /api/v1/auction/timer/resume`
    * `POST /api/v1/auction/timer/stop`
    * `GET /api/v1/auction/timer/status`

---

### 🗓️ Week 12: Observability, Security Rate Limiting, Docker & Release
* **Production Observability & Metrics**:
  * Integrated `spring-boot-starter-actuator`.
  * Configured Actuator endpoints in `application.yml` (`health`, `info`, `metrics`, `prometheus`).
  * Created `AuctionMetricsService.java` using Micrometer:
    * `auction.bids.total` — Tracks all bids submitted.
    * `auction.players.sold` — Tracks all players successfully hammered down.
* **Security Sliding-Window Rate Limiting**:
  * Built `RateLimitingFilter.java` intercepting `/api/v1/auction/bids/**` and `/api/v1/auction/timer/**`.
  * Protects live bidding engine from DoS and burst-spam (10 requests per second per IP) with HTTP 429 Too Many Requests response.
* **Docker Containerization**:
  * Created multi-stage `Dockerfile`:
    * Stage 1: Build JAR using `maven:3.9-eclipse-temurin-17-alpine`.
    * Stage 2: Production runtime on `eclipse-temurin:17-jre-alpine` exposing port 8080 with health checks.
  * Created `docker-compose.yml`:
    * Orchestrates MySQL 8.0 (`mysql:8.0`) database container and Spring Boot backend container.
    * Health checks ensure database readiness before starting application container.
    * Persistent volume mapping for MySQL data directory.
* **Final Release Documentation & Verification**:
  * All 24 integration and unit tests passing with **0 failures**.
  * Repository synchronized with GitHub `main` and `develop` branches.

---

## 📊 Summary of Member 1 API Endpoints (All 12 Weeks)

| Method | Endpoint | Description | Week |
|---|---|---|---|
| `GET` | `/api/v1/health` | Service health status check | W1 |
| `POST` | `/api/v1/teams` | Register a new franchise | W2 |
| `GET` | `/api/v1/teams` | List all franchises | W2 |
| `GET` | `/api/v1/teams/{id}` | Get franchise details | W2 |
| `PUT` | `/api/v1/teams/{id}` | Update franchise info | W2 |
| `DELETE` | `/api/v1/teams/{id}` | Remove franchise | W2 |
| `POST` | `/api/v1/players` | Register auction player | W3 |
| `GET` | `/api/v1/players` | List all auction players | W3 |
| `GET` | `/api/v1/players/{id}` | Get player details | W3 |
| `PUT` | `/api/v1/players/{id}` | Update player info | W3 |
| `DELETE` | `/api/v1/players/{id}` | Delete player | W3 |
| `GET` | `/api/v1/teams/{id}/purse-summary` | Franchise purse & squad summary | W8 |
| `GET` | `/api/v1/teams/purse-summary` | All franchises purse overview | W8 |
| `POST` | `/api/v1/auction/rtm/exercise` | Exercise Right-To-Match (RTM) card | W10 |
| `GET` | `/api/v1/teams/{teamId}/rtm` | Query franchise RTM card balance | W10 |
| `GET` | `/api/v1/auction/accelerated/pool` | List accelerated auction pool | W10 |
| `POST` | `/api/v1/auction/accelerated/nominate` | Nominate unsold players | W10 |
| `POST` | `/api/v1/auction/accelerated/stage/{id}`| Fast-track player to podium | W10 |
| `GET` | `/api/v1/compliance/audit` | BCCI all-team compliance audit | W10 |
| `GET` | `/api/v1/compliance/teams/{teamId}` | Single team compliance audit | W10 |
| `GET` | `/api/v1/reports/summary` | Global auction spend & sale statistics | W10 |
| `GET` | `/api/v1/reports/top-buys` | Top 10 highest-value bids | W10 |
| `GET` | `/api/v1/reports/teams/{teamId}/roster` | Team squad roster & purse report | W10 |
| `WS` | `/ws-auction` | STOMP WebSocket connection endpoint | W11 |
| `PUB` | `/app/bid` | Place live bid via WebSocket | W11 |
| `SUB` | `/topic/bids` | Broadcast topic for live bids | W11 |
| `SUB` | `/topic/timer` | Broadcast topic for 30s countdown ticks | W11 |
| `POST` | `/api/v1/auction/timer/start` | Start 30s player countdown | W11 |
| `POST` | `/api/v1/auction/timer/pause` | Pause active countdown | W11 |
| `POST` | `/api/v1/auction/timer/resume` | Resume paused countdown | W11 |
| `POST` | `/api/v1/auction/timer/stop` | Stop active countdown | W11 |
| `GET` | `/api/v1/auction/timer/status` | Query current clock tick state | W11 |
| `GET` | `/actuator/health` | Spring Boot Actuator health status | W12 |
| `GET` | `/actuator/metrics` | Micrometer auction metrics | W12 |
