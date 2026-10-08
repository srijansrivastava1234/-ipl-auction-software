# 🏏 Team Collaboration & Roles Guide (12-Week IPL Auction System)

This directory structure divides the **IPL Auction System** project among 5 team members based on their specific roles. Each member has their own assigned folder under the `/team` directory containing their dedicated copy of work, code files, and Week 1 AI Mentor action plans.

---

## 👥 5-Member Team Roles & Assigned Workspace Folders

1. **[Member 1: Project Lead & Core Backend REST API Developer](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-1-project-lead-core-backend/README.md)**
   - **Assigned Folder:** [`team/member-1-project-lead-core-backend`](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-1-project-lead-core-backend/)
   - **Git Branch:** `feature/core-backend` (Collaborating on `main` / `develop`)
   - **Assigned Scope:** Spring Boot architecture & folder structure, Team CRUD REST APIs, Player CRUD REST APIs, purse deduction logic, squad constraints (max 25, overseas max 8), Global Exception Handler (`@ControllerAdvice`).

2. **[Member 2: Security & Authentication Specialist](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-2-security-authentication-specialist/README.md)**
   - **Assigned Folder:** [`team/member-2-security-authentication-specialist`](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-2-security-authentication-specialist/)
   - **Git Branch:** `feature/security-auth`
   - **Assigned Scope:** Spring Security 6 filter chain, `PasswordEncoder` (BCrypt), JWT token utilities (`TokenUtil`), JWT request filter (`TokenAuthenticationFilter`), registration & login REST APIs (`/api/v1/auth/**`), role-based access control (`ADMIN` vs `TEAM_OWNER`).

3. **[Member 3: Database & Bidding Engine Developer — Anshika Pandey (`@anshikapandey-bit`)](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-3-database-bidding-engine-developer/README.md)**
   - **Assigned Folder:** [`team/member-3-database-bidding-engine-developer`](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-3-database-bidding-engine-developer/)
   - **Git Branch:** `feature/database-bidding`
   - **Assigned Scope:** MySQL ER diagram & DDL SQL schema (`schema.sql`), JPA entities & Hibernate mappings (`User`, `Team`, `Player`, `Bid`, `Auction`), pessimistic row locks (`@Lock(LockModeType.PESSIMISTIC_WRITE)`), live bidding REST APIs (`/api/v1/bids/**`), bidding increment rules & race condition prevention.

4. **[Member 4: Frontend & API Integration Lead — Akhilesh Sharma (`@sharmaakhilesh8273-lgtm`)](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-4-frontend-api-integration-lead/README.md)**
   - **Assigned Folder:** [`team/member-4-frontend-api-integration-lead`](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-4-frontend-api-integration-lead/)
   - **Git Branch:** `feature/frontend-ui`
   - **Assigned Scope:** Vite + React SPA architecture, Cybernetic Dark Cricket Design System (`index.css`), Auth UI (`Login.jsx`), JWT storage & Bearer header interceptor, Live Player Card (`PlayerCard.jsx`), Orbit Arena (`OrbitArena.jsx`), Bidding Console (`BiddingConsole.jsx`), Franchise Leaderboard & Purse Tracker (`Leaderboard.jsx`).

5. **[Member 5: QA, Testing & API Documentation Lead](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-5-qa-testing-api-documentation-lead/README.md)**
   - **Assigned Folder:** [`team/member-5-qa-testing-api-documentation-lead`](file:///c:/Users/hp/ipl_auction_system%20complete/team/member-5-qa-testing-api-documentation-lead/)
   - **Git Branch:** `feature/qa-testing-docs`
   - **Assigned Scope:** OpenAPI / Swagger 3 integration (`OpenApiConfig.java`), automated Postman collection & environment, unit tests (JUnit 5 + Mockito), integration tests (`@SpringBootTest` + `MockMvc`), edge-case validation suites, GitHub Actions CI workflow (`.github/workflows/ci.yml`), and `CODEOWNERS`.

---

## 🛠️ Git Collaboration Workflow
To prevent merge collisions while working simultaneously:
- **Feature Branches**: Each member works strictly within their assigned Git feature branch.
- **Strict Boundaries**: Never modify code outside your assigned role scope without coordination.
- **Pull Requests**: Submit PRs targeting `develop` (or `main`) with passing automated CI builds.
- **Week 1 Unlocking**: Complete Week 1 verification, push to GitHub, and notify mentor to unlock Week 2 tasks.
