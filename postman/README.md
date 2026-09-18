# 📮 IPL Auction — Postman Collection

> **Member 5 Deliverable** — QA, Testing & API Documentation Lead

## Quick Start

1. Open **Postman Desktop** (or [Postman Web](https://web.postman.co))
2. Click **Import** → Drag & drop `IPL_Auction_API.postman_collection.json`
3. Import environment: `IPL_Auction_DEV.postman_environment.json`
4. Select **IPL Auction — DEV** from the environment dropdown (top-right)
5. Run requests in order: **Auth → Teams → Players → Auction → Edge Cases**

---

## 📁 Collection Structure

| Folder | Requests | Purpose |
|--------|----------|---------|
| 🔐 Authentication | 3 | Register, Login (auto-saves JWT), Invalid Login |
| 🏏 Teams | 5 | Create, Get All, Get by ID, Update, Invalid ID (404) |
| 👤 Players | 4 | Create, Get All, Filter by Role, Get by ID |
| 💰 Auction & Bidding | 5 | Start, Bid, Bid History, Status, Stop |
| 🛑 Edge Cases | 5 | Below base price, Purse overflow, No token, Invalid ID, Duplicate |
| 📊 Swagger Docs | 2 | OpenAPI spec, Swagger UI page |

**Total: 24 requests with automated test scripts**

---

## 🌐 Environments

| Environment | File | Base URL | Purpose |
|-------------|------|----------|---------|
| **DEV** | `IPL_Auction_DEV.postman_environment.json` | `http://localhost:8080` | Local development |
| **CI** | `IPL_Auction_CI.postman_environment.json` | `http://localhost:8080` | GitHub Actions pipeline |

---

## 🔑 Environment Variables (Auto-populated)

| Variable | Set By | Used By |
|----------|--------|---------|
| `base_url` | Manual (pre-set) | All requests |
| `jwt_token` | Login response (auto) | All authenticated requests |
| `team_id` | Create Team response (auto) | Get/Update/Delete Team |
| `player_id` | Create Player response (auto) | Get Player, Place Bid |
| `auction_id` | Start Auction response (auto) | Auction status queries |

---

## 🏃 Running with Collection Runner

1. Click the **▶ Run** button on the collection
2. Select environment: **IPL Auction — DEV**
3. Set **Iterations**: 1
4. Set **Delay**: 100ms
5. Click **Run IPL Auction System API**

---

## 🖥️ Running via CLI (Newman)

### Install Newman
```bash
npm install -g newman newman-reporter-htmlextra
```

### Run Collection
```bash
newman run IPL_Auction_API.postman_collection.json \
  -e IPL_Auction_DEV.postman_environment.json \
  --reporters cli,junit,htmlextra \
  --reporter-junit-export results.xml \
  --reporter-htmlextra-export report.html \
  --delay-request 100 \
  --timeout-request 10000
```

### Run in CI (GitHub Actions)
The CI workflow (`.github/workflows/ci.yml`) automatically runs this collection using Newman after the build & test job passes.

---

## ✅ Test Scripts

Every request includes **Postman Test Scripts** that validate:
- ✅ HTTP status codes (200, 201, 400, 401, 404, 409)
- ✅ Response JSON structure (required fields exist)
- ✅ Response values (correct names, statuses, types)
- ✅ Auto-chaining (JWT token saved from login → used in subsequent requests)

---

## ⚠️ Prerequisites

Before running the collection:
1. Spring Boot app is running on `localhost:8080`
2. Database is initialized (H2 for local, MySQL for production)
3. No existing data conflicts (or use fresh DB)
