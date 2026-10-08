# 🏏 IPL Auction System — 5-Member Team Workspace & Role Guides

Welcome to the **IPL Auction System** team repository. This repository organizes the full project into 5 dedicated, self-contained member workspaces, comprehensive role guides, and standalone distributable packages to enable seamless parallel development across team members.

---

## 👥 5-Member Team Overview & Assigned Workspaces

| Member | Assigned Member | Role | Workspace Directory | Git Branch | Primary Tech Stack |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Member 1** | **Srijan Srivastava** (`@srijansrivastava1234`) | **Project Lead & Core Backend** | [`member-1-project-lead-core-backend`](./member-1-project-lead-core-backend/) | `feature/core-backend` | Spring Boot 3, Java 17, JPA |
| **Member 2** | **Amit Kumar Rajput** (`@amitkumarrajput1133-oss`) | **Security & Authentication Specialist** | [`member-2-security-authentication-specialist`](./member-2-security-authentication-specialist/) | `feature/security-auth` | Spring Security 6, JWT, BCrypt |
| **Member 3** | **Anshika Pandey** (`@anshikapandey-bit`) | **Database & Bidding Engine Developer** | [`member-3-database-bidding-engine-developer`](./member-3-database-bidding-engine-developer/) | `feature/database-bidding` | MySQL 8, JPA Pessimistic Locks |
| **Member 4** | **Akhilesh Sharma** (`@sharmaakhilesh8273-lgtm`) | **Frontend & API Integration Lead** | [`member-4-frontend-api-integration-lead`](./member-4-frontend-api-integration-lead/) | `feature/frontend-ui` | React 18, Vite, Cybernetic CSS |
| **Member 5** | **Suryansh** (`@suryansh-svg`) | **QA, Testing & Documentation Lead** | [`member-5-qa-testing-api-documentation-lead`](./member-5-qa-testing-api-documentation-lead/) | `feature/qa-testing-docs` | JUnit 5, Mockito, Postman, CI |

---

## 📁 Repository Directory Structure

```text
ipl_member_guide/
├── MEMBER_1_GUIDE.txt                          # Detailed action plan & guide for Member 1
├── MEMBER_2_GUIDE.txt                          # Detailed action plan & guide for Member 2
├── MEMBER_3_GUIDE.txt                          # Detailed action plan & guide for Member 3
├── MEMBER_4_GUIDE.txt                          # Detailed action plan & guide for Member 4
├── MEMBER_5_GUIDE.txt                          # Detailed action plan & guide for Member 5
├── member_packages/                            # Pre-bundled standalone distribution archives
│   ├── Member_1_Project_Lead_Core_Backend.zip
│   ├── Member_2_Security_Specialist.zip
│   ├── Member_3_Database_Bidding_Developer.zip
│   ├── Member_4_Frontend_Lead.zip
│   └── Member_5_QA_Testing_Lead.zip
├── member-1-project-lead-core-backend/         # Member 1 complete standalone workspace
├── member-2-security-authentication-specialist/# Member 2 complete standalone workspace
├── member-3-database-bidding-engine-developer/ # Member 3 complete standalone workspace
├── member-4-frontend-api-integration-lead/     # Member 4 complete standalone workspace
└── member-5-qa-testing-api-documentation-lead/ # Member 5 complete standalone workspace
```

---

## 🚀 Workspaces & Assigned Scope

### 1. Member 1: Project Lead & Core Backend
- **Workspace:** [`member-1-project-lead-core-backend`](./member-1-project-lead-core-backend/)
- **Guide:** [`MEMBER_1_GUIDE.txt`](./MEMBER_1_GUIDE.txt)
- **Scope:**
  - Spring Boot 3 foundation & Maven build configuration (`pom.xml`)
  - Team management endpoints: CRUD for franchises, budget tracking, remaining purse validation
  - Player management endpoints: Player registry, category filtering, squad constraints (max 25 players, max 8 overseas)
  - Global REST exception handling (`@ControllerAdvice`)
  - Player sale execution logic (`PlayerService.sellPlayer(...)`)

### 2. Member 2: Security & Authentication Specialist
- **Workspace:** [`member-2-security-authentication-specialist`](./member-2-security-authentication-specialist/)
- **Guide:** [`MEMBER_2_GUIDE.txt`](./MEMBER_2_GUIDE.txt)
- **Scope:**
  - Spring Security 6 stateless filter chain & CORS security configuration
  - JWT token generation, parsing, validation, and Bearer extraction (`TokenUtil`)
  - HTTP authentication filter (`TokenAuthenticationFilter`)
  - WebSocket STOMP handshake & channel interceptors for secure real-time connections
  - Authentication controller (`/api/v1/auth/login`, `/api/v1/auth/register`) with BCrypt hashing

### 3. Member 3: Database & Bidding Engine Developer
- **Workspace:** [`member-3-database-bidding-engine-developer`](./member-3-database-bidding-engine-developer/)
- **Guide:** [`MEMBER_3_GUIDE.txt`](./MEMBER_3_GUIDE.txt)
- **Scope:**
  - MySQL database schema DDL & initialization script (`schema.sql`)
  - JPA entity mappings: `User`, `Team`, `Player`, `Bid`, `Auction`
  - High-concurrency pessimistic row locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to eliminate race conditions
  - Live bidding service & API endpoints (`/api/v1/bids/**`) with incremental validation rules
  - Docker Compose service configuration for MySQL 8 containerization

### 4. Member 4: Frontend & API Integration Lead
- **Workspace:** [`member-4-frontend-api-integration-lead`](./member-4-frontend-api-integration-lead/)
- **Guide:** [`MEMBER_4_GUIDE.txt`](./MEMBER_4_GUIDE.txt)
- **Scope:**
  - Vite + React 18 single page application architecture
  - Cybernetic Dark Cricket Design System with glowing neon HUD accents
  - Quick-switch team owner and admin authentication view (`Login.jsx`)
  - Active lot player spotlight card (`PlayerCard.jsx`) with stats, overseas badges, and countdown
  - Circular stadium visualization of all 10 franchises (`OrbitArena.jsx`)
  - Real-time bidding console (`BiddingConsole.jsx`) with dynamic quick-bid increments (+₹20L, +₹50L, +₹1 Cr)
  - Live leaderboard and remaining purse tracker (`Leaderboard.jsx`)

### 5. Member 5: QA, Testing & API Documentation Lead
- **Workspace:** [`member-5-qa-testing-api-documentation-lead`](./member-5-qa-testing-api-documentation-lead/)
- **Guide:** [`MEMBER_5_GUIDE.txt`](./MEMBER_5_GUIDE.txt)
- **Scope:**
  - OpenAPI 3 / Swagger API documentation and interactive UI (`OpenApiConfig.java`)
  - Automated unit and integration test suites (`BidServiceTest`, `PlayerServiceTest`)
  - Comprehensive Postman collections and environment files for all REST and WebSocket endpoints
  - GitHub Actions CI/CD automation pipeline (`.github/workflows/ci.yml`)
  - Code ownership and branch protection policies (`.github/CODEOWNERS`)

---

## 📦 Standalone Distribution Packages

For members who prefer a standalone archive without cloning the full repository, download the relevant zip file from [`member_packages/`](./member_packages/):

- `Member_1_Project_Lead_Core_Backend.zip`
- `Member_2_Security_Specialist.zip`
- `Member_3_Database_Bidding_Developer.zip`
- `Member_4_Frontend_Lead.zip`
- `Member_5_QA_Testing_Lead.zip`

---

## 🔄 Git Workflow & Branching Guidelines

To avoid merge conflicts during concurrent development:
1. **Feature Branches:** Each member creates and works on their designated feature branch:
   ```bash
   git checkout -b feature/<member-feature-name>
   ```
2. **Local Verification:** Run test/build verification before pushing:
   - Backend members: `mvn clean test` or `mvn compile`
   - Frontend member: `npm run build`
3. **Pull Requests:** Open PRs targeting `main` (or `develop`) with descriptions of changes made.
