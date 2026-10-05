# 🔐 Member 2: Security & Authentication Specialist
> **Workspace Folder:** `team/member-2-security-authentication-specialist`  
> **Git Branch:** `feature/security-auth`

---

## 1. 🌟 Who You Are & What You Do (In Simple Words)
You are the **Security Guard & Keymaker** of the project.  
Your job is to make sure nobody can use the system without logging in, passwords are kept secretly hashed, and only the right people can do certain actions (for example: only **ADMIN** can sell a player, while **TEAM_OWNERS** can place bids).

---

## 2. 🛑 Strict Boundaries (What You Must NOT Touch)
Other members have their own jobs. Do not write code for:
- ❌ **Member 1's job:** Team or Player CRUD business logic or squad limits.
- ❌ **Member 3's job:** MySQL schema scripts, JPA entity tables, or bidding race-condition locks.
- ❌ **Member 4's job:** Frontend HTML, CSS, JavaScript, or React code.
- ❌ **Member 5's job:** Unit tests, Postman collections, or Swagger documentation.

---

## 3. 🛠️ Tools You Use
* **Spring Security 6**: The security framework that locks and unlocks URLs.
* **BCrypt**: The tool that scrambles passwords into an uncrackable hash.
* **JWT (JSON Web Token)**: Digital passes given to users after they log in.
* **Postman**: To test your login and registration requests with `Bearer <token>` headers.

---

## 4. 📁 Your Code Files & What They Do (In Plain English)

| File Path | What It Does Simply |
| :--- | :--- |
| `src/main/java/.../config/SecurityConfig.java` | The master rulebook. Disables CSRF (because we are stateless), sets public URLs (like login), and locks private URLs behind roles. |
| `src/main/java/.../config/TokenUtil.java` | The digital token maker. Creates a 24-hour pass with user's name, role, and team ID sealed with a secret key. Also checks if a token has expired or was faked. |
| `src/main/java/.../config/TokenAuthenticationFilter.java` | Sits at the entrance of every request. Reads the `Authorization: Bearer <token>` header, verifies the token, and tells Spring "this user is verified". |
| `src/main/java/.../controller/AuthController.java` | Provides URLs for `/api/auth/register` and `/api/auth/login`. Gives back a JWT token when the password matches. |
| `src/main/java/.../config/WebSocketSecurityInterceptor.java` | Checks the token when a user connects to the live bidding WebSocket, so only logged-in users can participate in live auctions. |

---

## 5. 📦 Your `pom.xml` Dependencies (Explained in 1 Line Each)

```xml
<dependencies>
    <!-- 1. The security engine: handles password hashing, filters, and URL access rules -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- 2. Lets you expose /api/auth/login and /api/auth/register REST endpoints -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
    </dependency>

    <!-- 3. Helper to write tests for your security filters and simulated logged-in users -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 6. 🌿 Git Commands You Need to Know

```bash
# 1. Switch to your feature branch
git checkout develop
git checkout -b feature/security-auth

# 2. Check which files you changed
git status

# 3. Add your changes
git add src/ pom.xml

# 4. Save your changes with a clear message
git commit -m "feat(security): implement Spring Security 6, JWT TokenUtil, and AuthController"

# 5. Send your branch to GitHub
git push origin feature/security-auth
```

---

## 7. 🗣️ What to Say to Your Mentor (Your Speaking Script)
> *"Hello Sir. As Member 2 and Security Specialist:  
> 1. I configured Spring Security 6 in `SecurityConfig.java` to use a completely stateless session policy with JWTs.  
> 2. I built `TokenUtil.java` which generates secure tokens with HMAC-SHA256 containing the user's username, role (`ADMIN` or `TEAM_OWNER`), and franchise ID, valid for 24 hours.  
> 3. I implemented `TokenAuthenticationFilter.java` which runs on every HTTP request, reads the Bearer token, and populates the `SecurityContext`.  
> 4. I secured our endpoints with Role-Based Access Control: only `ADMIN` can stage players or trigger auction sales, while `TEAM_OWNER` can enter and place bids. Passwords are encrypted using `BCryptPasswordEncoder`."*

---

## 8. ❓ Questions the Mentor Can Ask You & Your Answers

* **Q1: Why is CSRF disabled in your `SecurityConfig`?**  
  * **Answer:** *"CSRF attacks target browser cookies in stateful sessions. Because we use stateless JWT authentication sent in HTTP headers, cookies are not used, so CSRF is disabled safely."*

* **Q2: What information is stored inside your JWT token?**  
  * **Answer:** *"The token payload stores the user's username, their role (`ADMIN` or `TEAM_OWNER`), their associated `teamId`, and the expiration timestamp (24 hours). It is digitally signed so clients cannot tamper with it."*

* **Q3: How does the server verify passwords?**  
  * **Answer:** *"We never store plain-text passwords. When a user registers or logs in, we use `BCryptPasswordEncoder.matches(rawPassword, encodedPassword)`. BCrypt automatically uses a unique salt to protect against dictionary attacks."*

* **Q4: How do you stop a regular Team Owner from calling Admin APIs?**  
  * **Answer:** *"In `SecurityConfig.java`, we define `.requestMatchers('/api/auction/active/**').hasRole('ADMIN')`. If a user with `ROLE_TEAM_OWNER` calls it, Spring Security immediately blocks the request with an HTTP 403 Forbidden."*
