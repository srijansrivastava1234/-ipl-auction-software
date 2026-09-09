# 🏏 Member 1 Complete Work & Deliverables (Weeks 1 – 10)

**Author / Lead:** Member 1 — Project Lead & Core Backend REST API Developer  
**Assigned Scope:**
1. Base Spring Boot project architecture & layered structure.
2. Initial Git branches (`main`, `develop`, `feature/member-1-core-backend`).
3. Team Management REST APIs (CRUD).
4. Player Management REST APIs (CRUD).
5. Purse deduction logic & Squad size constraints.
6. Global Exception Handler (`@RestControllerAdvice`).
7. **Week 10 Milestone additions**: Right To Match (RTM) Engine, Accelerated Auction Round, BCCI Roster & Purse Compliance Audit, Macro Auction Analytics & Franchise Roster Exports.

---

## 🔒 Scope Boundaries Enforced
* 🛑 **No Spring Security / JWT** (Reserved for Member 2).
* 🛑 **No Database Schema Scripts / Lock Engine** (Reserved for Member 3).
* 🛑 **No Frontend UI Code** (Reserved for Member 4).
* 🛑 **No Unit Test Suites / Postman Specs** (Reserved for Member 5).

---

## 📅 Member 1 Weekly Sprint Breakdown (Weeks 1 to 10)

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
* **BCCI Roster & Purse Compliance Audit**:
  * `GET /api/v1/teams/{teamId}/compliance` — Single franchise audit against 18–25 squad limits, max 8 foreign players, minimum 75% purse spend rule, and role compositions.
  * `GET /api/v1/teams/compliance-overview` — League-wide compliance audit table across all 10 franchises.
* **Macro Auction Analytics & Team Roster Export**:
  * `GET /api/v1/reports/auction-summary` — Comprehensive auction-wide spend statistics, category distribution, record buys, and unsold pool counts.
  * `GET /api/v1/reports/teams/{teamId}/roster-export` — Full squad composition with acquisition timestamps and financial metrics.
* **Centralized Exception Mappings Added**:
  * `RtmNotAvailableException` $\rightarrow$ `400 BAD_REQUEST`
  * `ComplianceViolationException` $\rightarrow$ `422 UNPROCESSABLE_ENTITY`
  * `InvalidAuctionStateException` $\rightarrow$ `400 BAD_REQUEST`

---

## 📂 Member 1 Core Files Managed (Weeks 1 – 10)

| File Path | Description |
| :--- | :--- |
| `src/main/java/com/ipl/auction/IplAuctionApplication.java` | Main Spring Boot application bootstrap. |
| `src/main/java/com/ipl/auction/controller/TeamController.java` | REST endpoints for franchise CRUD operations. |
| `src/main/java/com/ipl/auction/controller/PlayerController.java` | REST endpoints for player CRUD operations. |
| `src/main/java/com/ipl/auction/controller/TeamPurseController.java` | REST endpoints for franchise purse and quota summaries. |
| `src/main/java/com/ipl/auction/controller/RtmController.java` | REST endpoints for BCCI Right To Match (RTM) audits and execution. |
| `src/main/java/com/ipl/auction/controller/AcceleratedAuctionController.java` | REST endpoints for accelerated round nominations and staging. |
| `src/main/java/com/ipl/auction/controller/ComplianceController.java` | REST endpoints for BCCI franchise roster and purse compliance audits. |
| `src/main/java/com/ipl/auction/controller/ReportController.java` | REST endpoints for macro auction summaries and team roster exports. |
| `src/main/java/com/ipl/auction/service/TeamService.java` | Core business logic for franchise management. |
| `src/main/java/com/ipl/auction/service/PlayerService.java` | Core business logic for player management. |
| `src/main/java/com/ipl/auction/service/TeamPurseService.java` | Purse calculation, reserve calculation, and quota logic. |
| `src/main/java/com/ipl/auction/service/RtmService.java` | RTM card tracking and atomic acquisition transfers. |
| `src/main/java/com/ipl/auction/service/AcceleratedAuctionService.java` | Accelerated round nomination aggregation and pool management. |
| `src/main/java/com/ipl/auction/service/ComplianceAuditService.java` | BCCI regulatory audit engine for roster and purse rules. |
| `src/main/java/com/ipl/auction/service/AuctionReportService.java` | Whole-auction macro statistics and squad export engine. |
| `src/main/java/com/ipl/auction/exception/GlobalExceptionHandler.java` | Centralized `@RestControllerAdvice` error handler. |
| `src/main/java/com/ipl/auction/dto/response/ApiResponse.java` | Generic standard API response envelope. |

---

## 🧪 Verification & Build Commands

```bash
# 1. Clean and compile
./mvnw.cmd clean compile

# 2. Run test compilation
./mvnw.cmd test-compile

# 3. Run full automated test suite (including Week 10 tests)
./mvnw.cmd test

# 4. Boot application
./mvnw.cmd spring-boot:run
```
