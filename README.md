# 🏏 IPL Mega Auction Software

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebSockets](https://img.shields.io/badge/WebSockets-STOMP-blueviolet.svg)](https://spring.io/)
[![Security](https://img.shields.io/badge/Spring%20Security-6.x%20%7C%20JWT-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/MySQL-8.0%20%7C%20JPA%20Hibernate-blueviolet.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/JUnit%205-24%2F24%20Passed-success.svg)](https://junit.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

An enterprise-grade, real-time **IPL Mega Auction Software** platform engineered with **Java 17, Spring Boot 3.x, Spring Data JPA, MySQL 8.0, WebSockets (STOMP), Spring Security 6 (JWT + RBAC), Micrometer Actuator, Docker**, and a modern responsive **Single Page Application (SPA)** frontend.

The platform simulates live BCCI Indian Premier League auctions with **high-concurrency live bidding, pessimistic write locking, real-time WebSocket ticker updates, automated 30-second countdown timers with auto-hammer strikes, purse deduction engines, Right To Match (RTM) card management, accelerated auction rounds, BCCI regulatory compliance audits**, and **macro auction analytics**.

---

## 👥 5-Member Team Roles & Scope Matrix

The project development is distributed cleanly across **5 specialized engineering roles** working in parallel branches to guarantee separation of concerns:

| Member & Role | Assigned Domain | Core Focus Areas | Dedicated Branch |
| :--- | :--- | :--- | :--- |
| **Member 1: Project Lead & Core Backend Developer** | Architecture, Core REST & Real-Time | Spring Boot project architecture, Team CRUD, Player CRUD, Purse deduction logic, Squad reserve constraints, Global Exception Handler, RTM Engine, Accelerated Phase, Compliance Audits, STOMP WebSockets, Countdown Timer, Dockerization | `main` / `develop` |
| **Member 2: Security & Authentication Specialist** | Security & RBAC | Spring Security 6, Stateless JWT Provider (`JwtUtils`), Request Filter (`JwtAuthenticationFilter`), Auth REST APIs (`/api/v1/auth/register`, `/api/v1/auth/login`), Role-Based Access Control (`ADMIN` vs `TEAM_OWNER`), Rate limiting | `feature/security-auth` |
| **Member 3: Database & Bidding Engine Developer** | JPA & Concurrency | MySQL DDL `schema.sql`, JPA Entities, Flyway migrations, Dynamic IPL bid increments, Pessimistic Row Locking (`SELECT ... FOR UPDATE`), Double-bid race condition prevention, Financial Audit Ledger | `feature/database-bidding` |
| **Member 4: Frontend UI & Real-Time Portal Lead** | Web UI & Client Integration | Glassmorphism & Neon theme, Auth Screens (Login/Register), Admin Onboarding Console, Live Auction Room (Player card, dynamic increment controls, active bidder), Team Squad & Purse Tracker, Real-time WebSocket bid ticker | `feature/frontend-ui` |
| **Member 5: QA, Testing & API Documentation Lead** | QA, Automation & CI/CD | OpenAPI / Swagger UI 3 (`/swagger-ui.html`), Postman Collections & Environments, JUnit 5 & Mockito Unit Test Suite, `@SpringBootTest` Integration Tests, Multi-threaded Concurrency Stress Tests, Full 12-week test suite, GitHub Actions CI Pipeline | `feature/qa-testing-docs` |

---

## 📅 Complete 12-Week Roadmap & Milestones (100% Completed)

```
Week 1  ──► Architecture Scaffolding & Git Branching Setup [COMPLETED]
Week 2  ──► Team CRUD Management APIs & DTO Models [COMPLETED]
Week 3  ──► Player CRUD Management APIs & Category Staging Pools [COMPLETED]
Week 4  ──► Centralized Global Exception Handler (@RestControllerAdvice) [COMPLETED]
Week 5  ──► Team Purse Deduction Engine & Minimum Squad Reserve Rule [COMPLETED]
Week 6  ──► Franchise Roster Constraints (18–25 Squad Size, Max 8 Overseas) [COMPLETED]
Week 7  ──► Player Lifecycle State Machine & Auctioneer Podium Endpoints [COMPLETED]
Week 8  ──► Franchise Purse Queries & Financial Audit Ledger Integration [COMPLETED]
Week 9  ──► API Contract Stabilization, CORS Config & Unified Response Envelope [COMPLETED]
Week 10 ──► Advanced IPL Mechanics: RTM Engine, Accelerated Round, BCCI Compliance Audit & Macro Reports [COMPLETED]
Week 11 ──► Real-Time WebSockets (STOMP), Live Bid Broadcasting & 30s Countdown Timer with Auto-Hammer [COMPLETED]
Week 12 ──► Production Observability (Actuator Metrics), Rate Limiting, Multi-Stage Docker & Full Release [COMPLETED]
```

---

## 🌟 Key Platform Modules & Features

### 1. Franchise & Player Management
* **Franchise Onboarding**: Register teams with starting purse budget (default ₹100 Crore).
* **Purse & Squad Constraints**:
  - Minimum squad size: **18 players**; Maximum squad size cap: **25 players**.
  - Maximum foreign players: **8 overseas** per franchise.
  - Minimum squad reserve fund rule enforced on every bid:
    $$\text{Reserve Fund} = (18 - \text{squadCount}) \times ₹20\,\text{Lakhs}$$
* **Player Lifecycle State Machine**:
  - Categorization: `BATSMAN`, `BOWLER`, `ALL_ROUNDER`, `WICKET_KEEPER`.
  - State transitions: `AVAILABLE` $\rightarrow$ `IN_AUCTION` $\rightarrow$ `SOLD` / `UNSOLD`.

### 2. Concurrency-Controlled Bidding Engine
* **Dynamic IPL Bid Increments**:
  - Current Bid $< ₹1.00\,\text{Cr} \implies \mathbf{+\,₹10\,\text{Lakhs}}$
  - $₹1.00\,\text{Cr} \le \text{Current Bid} < ₹5.00\,\text{Cr} \implies \mathbf{+\,₹20\,\text{Lakhs}}$
  - $₹5.00\,\text{Cr} \le \text{Current Bid} < ₹10.00\,\text{Cr} \implies \mathbf{+\,₹25\,\text{Lakhs}}$
  - Current Bid $\ge ₹10.00\,\text{Cr} \implies \mathbf{+\,₹50\,\text{Lakhs}}$
* **Pessimistic Concurrency Locking**: Uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` to serialize simultaneous bids on the same player, eliminating race conditions.
* **Self-Outbidding Prevention**: Blocks a franchise holding the winning bid from bidding against itself.

### 3. Real-Time WebSockets & Automated Countdown Timer Engine (Week 11)
* **STOMP / SockJS WebSocket Broker**:
  - Connection endpoint on `/ws-auction`.
  - Client bid destination on `/app/bid`.
  - Instant live bid broadcasts on `/topic/bids`.
* **Automated 30-Second Countdown**:
  - Second-by-second ticker published to `/topic/timer`.
  - Automatic clock reset to 30 seconds upon every valid bid.
  - Auctioneer controls: start, pause, resume, stop.
* **Automatic Hammer Strike Engine**:
  - When the countdown reaches 0 seconds, automatically strikes the hammer:
    - Sells player to leading team, debits purse, records squad roster, emits `HAMMER_SOLD`.
    - If no bids were placed, marks player `UNSOLD` and emits `HAMMER_UNSOLD`.

### 4. Observability, Security Rate Limiting & Containerization (Week 12)
* **Production Observability & Metrics**:
  - Spring Boot Actuator integration (`/actuator/health`, `/actuator/metrics`).
  - Micrometer counters tracking live metrics: `auction.bids.total`, `auction.players.sold`.
* **Sliding-Window Rate Limiting**:
  - In-memory rate limiting filter protecting live bidding endpoints against burst DoS (10 req/sec limit) with HTTP 429.
* **Multi-Stage Docker Containerization**:
  - Optimized multi-stage `Dockerfile` (`maven:3.9` build $\rightarrow$ `eclipse-temurin:17` JRE runtime).
  - Complete `docker-compose.yml` deploying MySQL 8.0 with health checks and application container.

### 5. Advanced IPL Rules & Compliance (Week 10)
* **Right To Match (RTM) Engine**:
  - Enforces BCCI limit of **2 RTM cards** per franchise.
  - Matches the highest hammer bid price, automatically refunds previous bidder's purse, debits matching team, and reassigns squad roster atomically.
* **Accelerated Auction Round**:
  - Automated pooling of unsold and available cricketers.
  - Franchise nomination mechanism (`POST /api/v1/auction/accelerated/nominate`) and fast-track podium staging.
* **BCCI Roster & Purse Compliance Audit**:
  - League-wide and single-team audits verifying:
    1. Squad size between 18 and 25 players.
    2. Overseas quota $\le 8$.
    3. Minimum purse expenditure: At least **75% of total purse** ($₹75\,\text{Cr}$) spent.
    4. Balanced role composition: $\ge 3$ batsmen, $\ge 3$ bowlers, $\ge 2$ all-rounders, $\ge 1$ wicket-keeper.
* **Macro Auction Analytics & Team Roster Exports**:
  - Macro statistics on total spend across franchises, record buys, category spend breakdown, and full team roster exports.

---

## 🔌 REST & WebSocket API Catalog

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Register a new user (`ADMIN` or `TEAM_OWNER`) | Public |
| `POST` | `/api/v1/auth/login` | Authenticate and obtain JWT access token | Public |

### 🛡️ Teams (`/api/v1/teams`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/teams` | Register a new IPL franchise with budget | Admin |
| `GET` | `/api/v1/teams` | List all registered IPL franchises | All Authenticated |
| `GET` | `/api/v1/teams/{id}` | Fetch franchise details by ID | All Authenticated |
| `PUT` | `/api/v1/teams/{id}` | Update franchise name, code, or budget | Admin |
| `DELETE` | `/api/v1/teams/{id}` | Remove a franchise | Admin |
| `GET` | `/api/v1/teams/{id}/purse-summary` | Real-time purse balance & squad quota check | All Authenticated |
| `GET` | `/api/v1/teams/purse-summary` | Roster and purse status for all 10 franchises | All Authenticated |

### 🏏 Players (`/api/v1/players`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/players` | Register cricketer into auction staging pool | Admin |
| `GET` | `/api/v1/players` | List all auction players (with role/status filters) | All Authenticated |
| `GET` | `/api/v1/players/{id}` | Fetch player details by ID | All Authenticated |
| `PUT` | `/api/v1/players/{id}` | Update player profile, base price, or category | Admin |
| `DELETE` | `/api/v1/players/{id}` | Remove player from auction staging pool | Admin |

### 🔨 Live Auctioneer Controls (`/api/v1/auction`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auction/stage` | Bring player to podium (`IN_AUCTION`) | Admin |
| `POST` | `/api/v1/auction/players/{id}/hammer/sold` | Strike hammer: sold to highest bidder | Admin |
| `POST` | `/api/v1/auction/players/{id}/hammer/unsold` | Strike hammer: mark player unsold | Admin |

### ⏱️ Live Auction Countdown Timer (`/api/v1/auction/timer`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auction/timer/start` | Start 30s countdown for player | Admin |
| `POST` | `/api/v1/auction/timer/pause` | Pause active timer | Admin |
| `POST` | `/api/v1/auction/timer/resume` | Resume paused timer | Admin |
| `POST` | `/api/v1/auction/timer/stop` | Stop countdown clock | Admin |
| `GET` | `/api/v1/auction/timer/status` | Current timer status & seconds remaining | All Authenticated |

### ⚡ Real-Time WebSockets (STOMP)
| Channel | Destination | Description | Access |
| :--- | :--- | :--- | :--- |
| `WS` | `/ws-auction` | SockJS / STOMP connection handshake | Public / Auth |
| `PUB` | `/app/bid` | Submit real-time bid via WebSocket | Team Owner |
| `SUB` | `/topic/bids` | Broadcast topic for live bid announcements | All Connected |
| `SUB` | `/topic/timer` | Broadcast topic for 30s countdown ticks & auto-hammer | All Connected |

### 💰 Live Bidding (`/api/v1/bids`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/bids/place` | Submit live bid under pessimistic locking | Team Owner |
| `GET` | `/api/v1/bids/player/{id}/current`| Fetch active highest bid for player | All Authenticated |
| `GET` | `/api/v1/bids/player/{id}/history`| Fetch entire chronological bid history | All Authenticated |

### 🔄 Right To Match (RTM) (`/api/v1/auction/rtm`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auction/rtm/exercise` | Exercise RTM card: match bid & transfer player | Team Owner |
| `GET` | `/api/v1/teams/{teamId}/rtm` | Query franchise RTM card balance & eligibility | All Authenticated |

### ⚡ Accelerated Auction Phase (`/api/v1/auction/accelerated`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/auction/accelerated/pool` | List unsold & available pool with nomination counts | All Authenticated |
| `POST` | `/api/v1/auction/accelerated/nominate` | Nominate shortlisted cricketers | Team Owner |
| `POST` | `/api/v1/auction/accelerated/stage/{id}`| Fast-track nominated cricketer to podium | Admin |

### 📋 BCCI Compliance Auditing (`/api/v1/compliance`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/compliance/audit` | Macro audit of all 10 franchises | Admin |
| `GET` | `/api/v1/compliance/teams/{teamId}` | Detailed single franchise compliance breakdown | All Authenticated |

### 📊 Macro Reporting & Analytics (`/api/v1/reports`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/reports/summary` | Global auction metrics: spend, sold, unsold | All Authenticated |
| `GET` | `/api/v1/reports/top-buys` | Top 10 most expensive acquisitions | All Authenticated |
| `GET` | `/api/v1/reports/teams/{teamId}/roster` | Team squad roster & financial breakdown | All Authenticated |

### 📈 Observability & Health (`/actuator`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/actuator/health` | Spring Boot application health check | Public |
| `GET` | `/actuator/metrics` | Micrometer auction metrics (`auction.bids.total`, etc.) | Admin |

---

## 🚀 Quick Start Guide

### Prerequisites
* **Java 17 JDK** (Eclipse Temurin, OpenJDK, or Oracle)
* **Maven 3.8+** (or use included `./mvnw.cmd`)
* **MySQL 8.0+** (or Docker / Docker Compose)

### Option A: Run via Docker Compose (Recommended)
```bash
# Clone the repository
git clone https://github.com/srijansrivastava1234/-ipl-auction-software.git
cd -ipl-auction-software

# Launch MySQL 8 and Spring Boot backend
docker-compose up --build
```
* **Backend API**: `http://localhost:8080`
* **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Option B: Run Locally
```bash
# 1. Clean and compile
./mvnw.cmd clean compile

# 2. Run all 24 unit, concurrency, and integration tests
./mvnw.cmd test
```
```text
[INFO] Results:
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

```bash
# 3. Launch Spring Boot application
./mvnw.cmd spring-boot:run
```

### Launch the Frontend SPA
Open `index.html` directly in any web browser or serve with:
```bash
npx -y serve .
```

---

## 📁 Repository Directory Structure

```text
├── .github/
│   └── workflows/
│       └── ci.yml                         # Automated GitHub Actions CI pipeline
├── css/
│   ├── components.css                     # Glassmorphism cards, buttons & modals
│   ├── style.css                          # Base layout & typography
│   └── variables.css                      # Dark neon IPL color scheme variables
├── js/
│   ├── admin-controller.js                # Admin player & franchise onboarding UI
│   ├── api.js                             # API wrapper with JWT Bearer injection
│   ├── app.js                             # SPA navigation & tab router
│   ├── auction-controller.js              # Live bidding podium & WebSocket receiver
│   ├── auth-controller.js                 # Login & registration forms
│   └── squad-controller.js                # Team roster & purse progress bars
├── docs/
│   ├── MEMBER_1_WEEKS_1_TO_12_WORK.md     # Detailed Member 1 complete 12-week sprint log
│   ├── member-1-progress.md               # Member 1 milestone audit log (100% complete)
│   ├── team-breakdown-weeks-1-to-12.md    # 5-member team roadmap across all 12 weeks
│   ├── MEMBER_1_WEEKS_1_TO_10_WORK.md     # Historical sprint log (Weeks 1-10)
│   └── team-breakdown-weeks-1-to-10.md    # Historical roadmap (Weeks 1-10)
├── postman/
│   ├── IPL_Auction_Collection.postman_collection.json # Automated API test collection
│   └── IPL_Auction_Environment.postman_environment.json# Environment configurations
├── src/
│   ├── main/
│   │   ├── java/com/ipl/auction/
│   │   │   ├── config/                    # SecurityConfig, WebSocketConfig, OpenApiConfig
│   │   │   ├── controller/                # REST & WebSocket Controllers (Team, Player, Timer, etc.)
│   │   │   ├── dto/                       # Request, Response & WebSocket DTO envelopes
│   │   │   ├── entity/                    # JPA Entities (Team, Player, Bid, Auction, AuditLog)
│   │   │   ├── exception/                 # Centralized GlobalExceptionHandler & Custom Exceptions
│   │   │   ├── repository/                # Spring Data JPA Repositories with LockModeType
│   │   │   ├── security/                  # JwtUtils, RateLimitingFilter, UserDetails
│   │   │   └── service/                   # Core business services, AuctionTimerService, Metrics
│   │   └── resources/
│   │       ├── application.yml            # Production / MySQL / WebSocket / Actuator config
│   │       ├── application-test.yml       # H2 in-memory test configuration
│   │       ├── data.sql                   # Seed data (10 IPL teams, 15 marquee players)
│   │       ├── schema.sql                 # MySQL DDL schema
│   │       └── db/migration/              # Flyway SQL migration scripts
│   └── test/
│       └── java/com/ipl/auction/
│           ├── concurrency/               # BiddingEngineConcurrencyTest (multi-threaded)
│           └── service/                   # Integration test suites (Week 10, Week 11 & 12)
├── Dockerfile                             # Multi-stage container build (Java 17)
├── docker-compose.yml                     # Multi-container orchestration (MySQL 8 + Backend)
├── ER_DIAGRAM.md                          # Comprehensive Database ER Specifications
├── index.html                             # Frontend Single Page Application entry point
├── pom.xml                                # Maven project configuration & dependencies
└── README.md                              # Master project documentation
```

---

## 👥 Contributors

* **Member 1**: Srijan Srivastava ([@srijansrivastava1234](https://github.com/srijansrivastava1234)) — *Project Lead & Core Backend REST API Developer*
* **Member 2**: *Security & Authentication Specialist*
* **Member 3**: *Database & Bidding Engine Developer*
* **Member 4**: *Frontend UI & Real-Time Portal Lead*
* **Member 5**: *QA, Testing & API Documentation Lead*

---

## 📜 License
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
