# 🛡️ Member 2 Progress & Milestone Verification Log

**Author:** Member 2 - Security & Authentication Specialist  
**Git Branch:** `feature/security-auth`  
**Date:** September 2026  
**Status:** Weeks 1–10 Security & Authentication Milestone Completed (100% of Member 2 Scope)

---

## 📌 Scope & Architecture Summary (Weeks 1 – 10)

### 🗓️ Week 1: Git Branch Setup & Security Architecture Baseline
- **Branch Initialization:** Isolated feature branch `feature/security-auth` branched off `origin/main`.
- **Dependencies Setup:** Integrated Spring Security 6 and modern JJWT (`jjwt-api:0.12.6`, `jjwt-impl`, `jjwt-jackson`) in `pom.xml`.
- **Password Encoder:** Created `PasswordConfig` with `BCryptPasswordEncoder(10)` bean.
- **Identity Models:** Implemented `User` entity (`@Entity`, `@Table(name = "users")`) and `Role` enum (`ROLE_ADMIN`, `ROLE_TEAM_OWNER`).
- **Data Access:** Implemented `UserRepository` extending `JpaRepository<User, Long>` with lookup methods (`findByUsername`, `findByEmail`, `findByUsernameOrEmail`, `existsByUsername`, `existsByEmail`).

### 🗓️ Week 2: Spring Security 6 Filter Chain & Stateless Architecture
- **Stateless Configuration:** Configured `SecurityFilterChain` in `SecurityConfig` with `SessionCreationPolicy.STATELESS`.
- **CORS & CSRF:** Disabled CSRF (REST stateless token standard) and configured permissive `CorsConfigurationSource` for cross-origin frontend communication.
- **DaoAuthenticationProvider:** Bound `CustomUserDetailsService` and `PasswordEncoder` into the Spring Security provider chain.

### 🗓️ Week 3: Cryptographic JWT Utilities (`JwtUtils`)
- **Key Derivation:** HMAC-SHA256 signing using Base64/raw secret key with `Keys.hmacShaKeyFor`.
- **Token Generation:** Configured `generateToken(Authentication)` and `generateTokenFromUsername(username, userDetails)` embedding subject, user ID, email, and granted authority roles.
- **Validation Engine:** Defensive claim parser capturing `ExpiredJwtException`, `MalformedJwtException`, `SecurityException`, `UnsupportedJwtException`, and `IllegalArgumentException`.

### 🗓️ Week 4: JWT Request Interception Filter (`JwtAuthenticationFilter`)
- **Filter Lifecycle:** Extended `OncePerRequestFilter` to intercept each HTTP call once per dispatch.
- **Header Parsing:** Extracted standard `Authorization: Bearer <token>` token format.
- **Context Injection:** Populated `SecurityContextHolder.getContext().setAuthentication(authToken)` with `WebAuthenticationDetailsSource`.

### 🗓️ Week 5: User Details & Principal Modeling
- **Custom User Details:** Created `UserDetailsImpl` implementing `org.springframework.security.core.userdetails.UserDetails` with mapped authorities.
- **UserDetailsService:** Built `CustomUserDetailsService` with dual-lookup capability (`findByUsernameOrEmail`) allowing login via either username or email.

### 🗓️ Week 6: Authentication DTOs & Service Layer Logic
- **Data Transfer Objects:**
  - `RegisterRequest`: Username, email, password, full name, role validation.
  - `LoginRequest`: Username/email and raw password validation.
  - `AuthResponse`: Access token, token type (`Bearer`), expiration, user metadata, role.
  - `UserProfileResponse`: ID, username, email, full name, role, timestamp payload.
- **AuthService Implementation:**
  - `AuthServiceImpl.register`: Unique checks, BCrypt password hashing, persistence, auto-authentication, and token generation.
  - `AuthServiceImpl.login`: Credentials verification via `AuthenticationManager`, principal extraction, and token minting.
  - `AuthServiceImpl.getCurrentUserProfile`: Current authenticated user profile retrieval.

### 🗓️ Week 7: Authentication REST API Endpoints (`AuthController`)
- **`POST /api/v1/auth/register`**: Registers new system users with HTTP 201 Created and immediate JWT token issuance.
- **`POST /api/v1/auth/login`**: Authenticates credentials and returns JWT bearer token.
- **`GET /api/v1/auth/me`**: Protected route fetching the authenticated user's profile using `@AuthenticationPrincipal`.

### 🗓️ Week 8: Role-Based Access Control (RBAC) Enforcement
- **Method Security:** Enabled `@EnableMethodSecurity(prePostEnabled = true)`.
- **URL Matching:** Configured endpoint security matching in `SecurityConfig`:
  - `/api/v1/auth/**` -> `permitAll()`
  - `/api/v1/test/public` -> `permitAll()`
  - `/api/v1/test/admin` -> `hasRole('ADMIN')`
  - `/api/v1/test/owner` -> `hasRole('TEAM_OWNER')`
- **RBAC Controller:** Created `RbacTestController` demonstrating `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasRole('TEAM_OWNER')")`, and `@PreAuthorize("hasAnyRole('ADMIN', 'TEAM_OWNER')")`.

### 🗓️ Week 9: Custom Exception & Access Denial Handling
- **401 Unauthorized:** Built `JwtAuthenticationEntryPoint` returning structured JSON error when unauthenticated users access protected endpoints.
- **403 Forbidden:** Built `CustomAccessDeniedHandler` returning structured JSON error when authenticated users lack required roles.
- **Integration:** Registered both handlers into `HttpSecurity.exceptionHandling()`.

### 🗓️ Week 10: Production Hardening, Security Audit & Integration Readiness
- **Audit & Clean Architecture:** Verified that passwords are never returned in responses (`@JsonIgnore` on `UserDetailsImpl.password`).
- **Configuration Externalization:** Externalized `jwt.secret` and `jwt.expiration-ms` to environment variables in `application.yml` with secure defaults.
- **Inter-branch Readiness:** Verified that all routes needed by Member 1 (Teams/Players) and Member 4 (Frontend) can either pass JWT headers or configure role permissions cleanly.

---

## 🔒 Strict Boundaries Verification
- ✅ **No Team / Player CRUD logic written:** Fully reserved for Member 1.
- ✅ **No SQL schema locks / Bidding concurrency written:** Fully reserved for Member 3.
- ✅ **No Frontend UI code / HTML / CSS / JS written:** Fully reserved for Member 4.
- ✅ **No Postman collections or OpenAPI Swagger configuration written:** Fully reserved for Member 5.

---

## 🧪 Verification & Test Endpoints
| HTTP Method | Endpoint | Authorization | Expected Result |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | None (Public) | 201 Created + JWT token |
| `POST` | `/api/v1/auth/login` | None (Public) | 200 OK + JWT token |
| `GET` | `/api/v1/auth/me` | Bearer `<token>` | 200 OK + User Profile |
| `GET` | `/api/v1/test/public` | None (Public) | 200 OK |
| `GET` | `/api/v1/test/admin` | Bearer `<ADMIN_TOKEN>` | 200 OK (403 if TEAM_OWNER, 401 if No Token) |
| `GET` | `/api/v1/test/owner` | Bearer `<OWNER_TOKEN>` | 200 OK (403 if ADMIN, 401 if No Token) |
| `GET` | `/api/v1/test/user` | Bearer `<ANY_TOKEN>` | 200 OK for either role |
