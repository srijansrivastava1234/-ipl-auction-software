# 🧪 Member 5: QA, Testing & API Documentation Lead
> **Workspace Folder:** `team/member-5-qa-testing-api-documentation-lead`  
> **Git Branch:** `feature/qa-testing-docs`

---

## 1. 🌟 Who You Are & What You Do (In Simple Words)
You are the **Quality Inspector & Automation Guardian** of the team.  
Your job is to make sure nothing is broken. You write automated tests to try and break the bidding engine (for example: trying to bid with negative money or when the purse is empty), create the interactive **Swagger API documentation page**, and set up **GitHub Actions CI** to automatically test everyone's code whenever they push to GitHub.

---

## 2. 🛑 Strict Boundaries (What You Must NOT Touch)
Other members have their own jobs. Do not write code for:
- ❌ **Member 1's job:** Team or Player CRUD business controllers or services.
- ❌ **Member 2's job:** Spring Security filter chain or JWT token utility code.
- ❌ **Member 3's job:** MySQL DDL schema scripts or live bidding concurrency algorithms.
- ❌ **Member 4's job:** Frontend HTML, CSS, JavaScript, or React code.

---

## 3. 🛠️ Tools You Use
* **OpenAPI 3 / Swagger**: Creates an interactive web UI at `http://localhost:8080/swagger-ui/index.html` where anyone can test APIs in a browser.
* **JUnit 5**: The standard Java framework to write and run automated test cases.
* **Mockito**: Creates "mock" (fake) databases and services so you can test logic in isolation without needing a real database running.
* **GitHub Actions**: Automated cloud robot that compiles and runs all tests on every push.
* **Postman**: To create automated API test collections.

---

## 4. 📁 Your Code Files & What They Do (In Plain English)

| File Path | What It Does Simply |
| :--- | :--- |
| `src/test/java/.../service/BidServiceTest.java` | Tests the bidding engine: asserts that bids below base price fail, teams cannot outbid themselves, and purse overflow is blocked. |
| `src/test/java/.../service/PlayerServiceTest.java` | Tests player sales: verifies that sold players deduct money from the team, and blocks adding a 26th player or a 9th overseas player. |
| `src/main/java/.../config/OpenApiConfig.java` | Configures Swagger 3 so it shows API documentation and lets users paste their Bearer JWT token to test protected routes. |
| `.github/workflows/ci.yml` | The CI Pipeline script. Automatically spins up an Ubuntu cloud runner with Java 17, compiles code, and runs tests on every Pull Request. |
| `.github/CODEOWNERS` | Enforces that Member 2 reviews security changes, Member 3 reviews database changes, etc., before any code is merged into `main`. |

---

## 5. 📦 Your `pom.xml` Dependencies (Explained in 1 Line Each)

```xml
<dependencies>
    <!-- 1. Generates the interactive Swagger UI page at /swagger-ui/index.html -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>

    <!-- 2. The core testing starter: contains JUnit 5, Mockito, and AssertJ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- 3. Lets you test database repositories with @DataJpaTest -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- 4. Lets you test secure endpoints by simulating logged-in users with @WithMockUser -->
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
git checkout -b feature/qa-testing-docs

# 2. Check which files you changed
git status

# 3. Add your changes
git add src/test/ .github/ pom.xml

# 4. Save your changes with a clear message
git commit -m "test(qa): add JUnit 5 test suites, Swagger 3 config, and GitHub Actions CI workflow"

# 5. Send your branch to GitHub
git push origin feature/qa-testing-docs
```

---

## 7. 🗣️ What to Say to Your Mentor (Your Speaking Script)
> *"Hello Sir. As Member 5 and QA, Testing & API Documentation Lead:  
> 1. I configured **SpringDoc OpenAPI 3 / Swagger UI** at `/swagger-ui/index.html` with Bearer JWT security scheme support so anyone can test our endpoints interactively.  
> 2. I authored automated unit test suites using **JUnit 5 and Mockito** in `BidServiceTest` and `PlayerServiceTest`.  
> 3. I specifically verified edge cases: asserting that transactions throw exceptions when a team attempts to outbid itself, bid higher than its remaining purse, or breach the 25-player / 8-overseas limits.  
> 4. I set up our **GitHub Actions CI/CD Pipeline** (`ci.yml`) and `.github/CODEOWNERS` so that all unit tests must pass in an automated Ubuntu container before any code can be merged into `main`."*

---

## 8. ❓ Questions the Mentor Can Ask You & Your Answers

* **Q1: What does your GitHub Actions workflow actually do?**  
  * **Answer:** *"On every push and Pull Request to `main` or `develop`, GitHub Actions automatically provisions an Ubuntu runner with JDK 17, downloads dependencies from Maven cache, compiles the source code, and runs all JUnit 5 tests via `./mvnw clean test`. If a single test fails, the build turns red and merge is blocked."*

* **Q2: Why do you use Mockito instead of testing with a real database?**  
  * **Answer:** *"Unit tests should be fast and isolated. Mockito lets us simulate ('mock') repository responses like `playerRepository.findByIdForUpdate()` so we can verify business logic in milliseconds without depending on an external MySQL database being turned on."*

* **Q3: What edge cases did you test for the bidding system?**  
  * **Answer:** *"In `BidServiceTest`, I wrote tests for:
    1. A team trying to bid more money than their current purse balance.  
    2. A team placing two bids in a row on the same player.  
    3. A bid that is equal to or lower than the existing highest bid.  
    All three scenarios are asserted to throw clear `RuntimeExceptions`."*

* **Q4: How do users test protected APIs inside Swagger UI?**  
  * **Answer:** *"In Swagger UI, there is an 'Authorize' button at the top. You first call `/api/auth/login` to get a JWT token, click 'Authorize', paste the token, and Swagger automatically injects `Bearer <token>` into all subsequent API calls."*
