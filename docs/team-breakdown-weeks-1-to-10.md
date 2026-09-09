# 🏏 IPL Auction Software — 5-Member Team Roadmap & Deliverables (Weeks 1 – 10)

---

## 🧭 Executive Overview
The **IPL Auction Software** is an enterprise-grade full-stack portal built with **Java 17, Spring Boot 3.x, MySQL, Spring Data JPA, WebSockets/STOMP**, and a modern responsive **Frontend**.

The project development is divided cleanly across **5 specialized roles** working in parallel feature branches to ensure complete separation of concerns and zero merge collisions.

---

## 👥 5-Member Role Allocation & Scope Matrix

| Member & Role | Assigned Domain | Core Focus Areas | Dedicated Git Branch |
| :--- | :--- | :--- | :--- |
| **Member 1: Project Lead & Core Backend** | Architecture & CRUD APIs | Spring Boot base, Team & Player CRUD REST endpoints, Purse deduction rules, Squad size constraints, Global Exception Handler, RTM Engine, Accelerated Round, Compliance Audits, Macro Reports | `feature/member-1-core-backend` |
| **Member 2: Security & Auth Specialist** | Security & RBAC | Spring Security 6, JWT Token Generation/Validation, User Registration & Login REST APIs, Role-based endpoint guards (`ADMIN` vs `TEAM_OWNER`), RTM & Report RBAC permissions | `feature/security-auth` |
| **Member 3: Database & Bidding Engine** | JPA & Concurrency | MySQL DDL `schema.sql`, JPA Entities, Dynamic IPL bid increments, Pessimistic Row Locking (`SELECT ... FOR UPDATE`), Double-bid race condition prevention, RTM & Accelerated queries | `feature/database-bidding` |
| **Member 4: Frontend UI & Real-Time Portal** | React / Web UI | Responsive SPA UI, Glassmorphism & Neon theme, Auth Screens, Admin Onboarding Console, Live Auction Room, Squad & Purse Tracker, RTM Modal & Accelerated Staging UI | `feature/frontend-ui` |
| **Member 5: QA, Testing & API Docs Lead** | QA & Automation | OpenAPI / Swagger UI 3, Postman Collections, JUnit 5 & Mockito Unit/Integration Tests, Concurrency stress tests, Week 10 integration test suite, GitHub Actions CI Pipeline | `feature/qa-testing-docs` |

---

# 📅 Week-by-Week Deliverables Breakdown (Weeks 1 to 10)

---

## 🔷 Member 1: Project Lead & Core Backend REST API Developer

### 🎯 Role Scope & Strict Boundaries
* **In Scope**: Spring Boot project architecture, Maven build configuration, Team CRUD REST APIs, Player CRUD REST APIs, Purse deduction logic, Squad constraints (18–25 players, max 8 overseas), `@RestControllerAdvice` Global Exception Handler, Right To Match (RTM) Engine, Accelerated Round, Compliance Audits, Macro Reports.
* **Out of Scope (Do NOT touch)**: Spring Security / JWT (Member 2), Database schema / Locks (Member 3), Frontend UI (Member 4), Unit Tests / Swagger (Member 5).

### 🗓️ Weekly Milestones (Weeks 1 – 10)
* **Week 1 (Project Scaffolding & Base CRUD)**: Initialize Spring Boot 3.x application, directory layout (`controller`, `dto`, `entity`, `service`, `repository`, `exception`), `main` and `develop` branches, basic `Team` & `Player` controller endpoint stubs.
* **Week 2 (Full CRUD Implementation)**: Complete service layer logic for `TeamService` and `PlayerService`; implement DTO mapping (`TeamRequest`, `TeamResponse`, `PlayerRequest`, `PlayerResponse`).
* **Week 3 (Global Exception Handler & Validations)**: Implement `@RestControllerAdvice` (`GlobalExceptionHandler`), create custom exceptions (`ResourceNotFoundException`, `InsufficientPurseException`, `SquadLimitExceededException`), add `@Valid` annotations on request DTOs.
* **Week 4 (Purse Deduction Engine)**: Implement balance deduction logic upon player acquisition; calculate minimum squad reserve requirement:
  $$\text{Reserve Fund} = (18 - \text{squadCount}) \times ₹20\,\text{Lakhs}$$
* **Week 5 (Squad Quotas & Overseas Rules)**: Enforce roster size rules (minimum 18 players, maximum 25 players) and overseas quota cap (maximum 8 overseas players per franchise).
* **Week 6 (Player State Transitions)**: Build player status state machine (`AVAILABLE` $\rightarrow$ `IN_AUCTION` $\rightarrow$ `SOLD` / `UNSOLD`) and category filtering (`BATSMAN`, `BOWLER`, `ALL_ROUNDER`, `WICKET_KEEPER`).
* **Week 7 (Auctioneer REST Endpoints)**: Implement `/api/v1/auction/stage` (bring player to podium), `/api/v1/auction/players/{id}/hammer/sold` (atomic sale and squad assignment), `/api/v1/auction/players/{id}/hammer/unsold`.
* **Week 8 (Franchise Purse Summary APIs)**: Expose real-time team purse queries (`/api/v1/teams/{id}/purse-summary`, `/api/v1/teams/purse-summary`) and wallet audit ledger integration.
* **Week 9 (Performance & Refactoring)**: Refactor response wrappers (`ApiResponse<T>`), optimize database queries for batch team queries, and stabilize all REST API contracts for team integration.
* **Week 10 (RTM Engine, Accelerated Phase, BCCI Compliance & Macro Analytics)**:
  * Built `RtmService` & `RtmController` with card limits (max 2), live bid matching, purse adjustments, and wallet refunds.
  * Built `AcceleratedAuctionService` & `AcceleratedAuctionController` for unsold shortlist nominations and podium staging.
  * Built `ComplianceAuditService` & `ComplianceController` for BCCI 18–25 roster limits, foreign quotas, and 75% purse spend checks.
  * Built `AuctionReportService` & `ReportController` for whole-auction spend statistics and franchise squad roster exports.

---

## 🔷 Member 2: Security & Authentication Specialist

### 🎯 Role Scope & Strict Boundaries
* **In Scope**: Spring Security 6 configuration, Password encoder (`BCryptPasswordEncoder`), JWT utilities (`JwtUtils`/`TokenUtil`), JWT request authentication filter (`JwtAuthenticationFilter`), User registration & Login REST APIs (`/api/v1/auth/register`, `/api/v1/auth/login`), Role-Based Access Control (`ADMIN`, `TEAM_OWNER`).
* **Out of Scope (Do NOT touch)**: Team/Player CRUD logic (Member 1), Database schema/Locks (Member 3), Frontend UI (Member 4), Unit Tests/Swagger (Member 5).

### 🗓️ Weekly Milestones (Weeks 1 – 10)
* **Week 1 (Security Configuration Foundation)**: Create branch `feature/security-auth`, configure `SecurityConfig` with disabled CSRF, stateless session management, and `BCryptPasswordEncoder` bean.
* **Week 2 (JWT Provider Utilities)**: Build `TokenUtil` / `JwtUtils` to generate HMAC-SHA256 tokens, extract username/roles from claims, and validate expiration times.
* **Week 3 (JWT Filter Implementation)**: Build `JwtAuthenticationFilter` (extending `OncePerRequestFilter`) to extract `Authorization: Bearer <token>`, validate tokens, and populate `SecurityContextHolder`.
* **Week 4 (User Model & Details Service)**: Create `User` entity, `Role` enum (`ROLE_ADMIN`, `ROLE_TEAM_OWNER`), `UserRepository`, and custom `UserDetailsService`.
* **Week 5 (Authentication REST APIs)**: Implement `AuthController` with `/api/v1/auth/register` and `/api/v1/auth/login` returning JWT access tokens upon successful authentication.
* **Week 6 (Role-Based Authorization & Method Security)**: Enable `@EnableMethodSecurity`; restrict auctioneer/admin controls to `ADMIN` (`@PreAuthorize("hasRole('ADMIN')")`) and bidding endpoints to authenticated `TEAM_OWNER`.
* **Week 7 (WebSocket Security Interceptor)**: Build `WebSocketSecurityInterceptor` to extract and validate JWT tokens during STOMP `CONNECT` frames over WebSocket channels.
* **Week 8 (CORS & Security Hardening)**: Configure production CORS headers (`allowedOrigins`, `allowedMethods`, `allowedHeaders`), rate-limiting hooks, and token refresh/blacklist handling.
* **Week 9 (Security Audit & Protection Testing)**: Audit all endpoint authorization rules, verify protection against unauthorized bidding attempts, and finalize production security filters.
* **Week 10 (Advanced Endpoint Security Alignment)**: Configure role access guards for RTM endpoints, accelerated round permissions, compliance audits, and analytics exports.

---

## 🔷 Member 3: Database & Bidding Engine Developer

### 🎯 Role Scope & Strict Boundaries
* **In Scope**: MySQL DDL database schema (`schema.sql`), JPA Entities & Mappings (`Team`, `Player`, `Bid`, `Auction`, `TeamSquad`, `WalletAuditLog`), Live Bidding REST APIs (`/api/v1/bids/place`, `/api/v1/bids/player/{id}/current`, `/api/v1/bids/player/{id}/history`), Concurrency & Race Condition Control (Pessimistic row write locking), Bidding increment engine.
* **Out of Scope (Do NOT touch)**: CRUD management APIs (Member 1), Spring Security filters (Member 2), Frontend UI (Member 4), Swagger docs / Test suites (Member 5).

### 🗓️ Weekly Milestones (Weeks 1 – 10)
* **Week 1 (Database Schema & DDL)**: Create branch `feature/database-bidding`, design ER diagram, write initial `schema.sql` defining `teams`, `players`, `bids`, `auctions`, and `team_squads`.
* **Week 2 (JPA Entities & Relationships)**: Implement JPA entity mappings (`@Entity`, `@Table`, `@ManyToOne`, `@OneToMany`, `@Version`) and auditing metadata (`createdAt`, `updatedAt`).
* **Week 3 (Spring Data Repositories)**: Implement repository interfaces (`TeamRepository`, `PlayerRepository`, `BidRepository`, `AuctionRepository`) with custom JPQL queries.
* **Week 4 (IPL Bidding Increment Engine)**: Implement dynamic IPL bid increment calculation:
  * Current Bid $< ₹1.00\,\text{Cr} \implies \mathbf{+\,₹10\,\text{Lakhs}}$
  * $₹1.00\,\text{Cr} \le \text{Current Bid} < ₹5.00\,\text{Cr} \implies \mathbf{+\,₹20\,\text{Lakhs}}$
  * $₹5.00\,\text{Cr} \le \text{Current Bid} < ₹10.00\,\text{Cr} \implies \mathbf{+\,₹25\,\text{Lakhs}}$
  * Current Bid $\ge ₹10.00\,\text{Cr} \implies \mathbf{+\,₹50\,\text{Lakhs}}$
* **Week 5 (Live Bidding REST APIs)**: Implement `BidController` with `/api/v1/bids/place`, `/api/v1/bids/player/{id}/current`, and `/api/v1/bids/player/{id}/history`.
* **Week 6 (Pessimistic Concurrency Locking)**: Add `@Lock(LockModeType.PESSIMISTIC_WRITE)` to `PlayerRepository` and `AuctionRepository` queries to prevent race conditions during simultaneous bids.
* **Week 7 (Self-Outbidding Prevention & Indexing)**: Block franchises holding the active winning bid from self-outbidding; add database composite indexes on `(player_id, status)` and `(team_id, created_at)`.
* **Week 8 (Financial Ledger & Audit Logging)**: Implement `WalletAuditLog` entity and repository to record immutable DEBIT/CREDIT ledger entries for every financial transaction.
* **Week 9 (Query Optimization & Load Profiling)**: Tune SQL queries with JPA entity graphs to eliminate $N+1$ query issues; benchmark database locking throughput under high simulated concurrency.
* **Week 10 (Transaction Isolation & Multi-Entity Rollbacks)**: Enforce `READ_COMMITTED` transaction boundaries across RTM transfers, ensuring atomic refunds and debits without lock deadlocks.

---

## 🔷 Member 4: Frontend UI & Real-Time Portal Lead

### 🎯 Role Scope & Strict Boundaries
* **In Scope**: Frontend SPA architecture, responsive CSS theme (glassmorphism, neon styling, variables), Auth UI (Login/Register), Admin Onboarding Console, Live Auction Room UI (Player card, dynamic increment controls, active bidder), Team Squad & Purse Tracker dashboard.
* **Out of Scope (Do NOT touch)**: Spring Boot Java APIs (Member 1), Java Security backend (Member 2), MySQL SQL schema / JPA entities (Member 3), JUnit backend tests (Member 5).

### 🗓️ Weekly Milestones (Weeks 1 – 10)
* **Week 1 (UI Setup & Design System)**: Create branch `feature/frontend-ui`, set up UI folder layout, configure `variables.css` with dark theme palette, font typography, and glassmorphism card tokens.
* **Week 2 (Layout & SPA Navigation)**: Create master `index.html` layout, responsive navbar with live auction status badge, and dynamic tab switcher.
* **Week 3 (Auth UI & Form Validation)**: Build Login and Registration modal forms with client-side input validation and error feedback banners.
* **Week 4 (API Interceptor & Token Storage)**: Implement `api.js` client wrapper to store JWT in `sessionStorage` and inject `Authorization: Bearer <token>` into all outgoing HTTP requests.
* **Week 5 (Admin Management Console)**: Build Admin dashboard screens: franchise registration form, player staging pool table, and purse budget configurator.
* **Week 6 (Live Auction Room Podium)**: Build active player auction card displaying player photo, category badge, base price, live highest bid, and current winning franchise badge.
* **Week 7 (Interactive Bidding Console)**: Implement quick-bid increment buttons (`+10L`, `+20L`, `+25L`, `+50L`), custom bid input with purse validation, and live countdown timer ring.
* **Week 8 (Franchise Squad & Purse Tracker)**: Build team dashboard showing real-time purse progress bars, available squad slots, overseas player count (`X / 8`), and detailed squad roster lists.
* **Week 9 (Polish, Responsive & Micro-interactions)**: Add sound effect triggers for hammer strikes, responsive mobile/tablet breakpoints, and animated toast alerts for bid acceptance.
* **Week 10 (RTM Notification & Accelerated Round UI)**: Build interactive RTM prompt modal on live hammer drop, accelerated shortlist submission checkboxes, compliance audit scorecard, and printable squad roster export view.

---

## 🔷 Member 5: QA, Testing & API Documentation Lead

### 🎯 Role Scope & Strict Boundaries
* **In Scope**: OpenAPI / Swagger 3 Spring Boot Integration (`springdoc-openapi-starter-webmvc-ui`), Postman Collections & Environments, JUnit 5 & Mockito Unit Test Suite, `@SpringBootTest` Integration Tests, Multi-threaded Concurrency Stress Tests, GitHub Actions CI Workflow (`.github/workflows/ci.yml`).
* **Out of Scope (Do NOT touch)**: Team/Player CRUD logic (Member 1), Security filter provider (Member 2), SQL schema / Bidding engine (Member 3), Frontend UI code (Member 4).

### 🗓️ Weekly Milestones (Weeks 1 – 10)
* **Week 1 (CI Automation Pipeline)**: Create branch `feature/qa-testing-docs`, set up GitHub Actions CI workflow (`.github/workflows/ci.yml`) to automatically compile and test PRs on Java 17.
* **Week 2 (OpenAPI / Swagger 3 Integration)**: Add `springdoc-openapi-starter-webmvc-ui` dependency and build `OpenApiConfig.java` with JWT security scheme definition and grouped API tags.
* **Week 3 (Postman Collection & Automated Tests)**: Create Postman collection and environment variables covering all Team and Player CRUD endpoints with test assertion scripts.
* **Week 4 (Unit Testing Suite)**: Build JUnit 5 & Mockito unit tests for `TeamService` and `PlayerService` covering success scenarios, validation rejections, and not-found exceptions.
* **Week 5 (Controller Integration Tests)**: Implement `@SpringBootTest` and `MockMvc` integration tests for authentication, team onboarding, and player staging endpoints.
* **Week 6 (Edge-Case Testing Suite)**: Write test suites asserting edge-case rules: purse overflow rejection, self-outbidding prevention, bidding on unsold/sold players, and squad cap enforcement.
* **Week 7 (Multi-Threaded Concurrency Test Suite)**: Build `BiddingEngineConcurrencyTest` using `ExecutorService` and `CountDownLatch` simulating 6–10 franchises bidding concurrently on the same player.
* **Week 8 (End-to-End Auction Lifecycle Test)**: Implement automated integration test verifying complete auction flow: Staging $\rightarrow$ Multiple Bids $\rightarrow$ Hammer Strike $\rightarrow$ Purse Deduction $\rightarrow$ Squad Roster Allocation $\rightarrow$ Wallet Audit.
* **Week 9 (JaCoCo Code Coverage & Documentation Export)**: Configure JaCoCo code coverage plugin (targeting $\ge 80\%$ branch coverage) and generate finalized HTML API documentation.
* **Week 10 (Advanced Rules Test Suite — RTM & Compliance)**: Implement `Week10ServicesTest` covering RTM card limits, atomic transfers, accelerated nominations, BCCI regulatory roster audits, and macro reporting exports.

---

## 🛠️ Verification & Test Commands

To verify the entire backend, unit test suite, and concurrency guarantees locally:
```bash
# 1. Compile backend
./mvnw.cmd clean test-compile

# 2. Run test suite (Unit tests + Concurrency tests + Week 10 tests)
./mvnw.cmd test

# 3. Launch Spring Boot application
./mvnw.cmd spring-boot:run
```
