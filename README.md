# 🎨 Member 4: Frontend & API Integration Lead
> **Workspace Folder:** `team/member-4-frontend-api-integration-lead`  
> **Git Branch:** `feature/frontend-ui`

---

## 1. 🌟 Who You Are & What You Do (In Simple Words)
You are the **Designer & Stage Creator** of the application.  
Your job is to build the visual website that users actually see and interact with. You create the dark cyber stadium theme, the login screen, the bidding buttons (+₹20L, +₹50L, +₹1 Cr), and the live leaderboard showing all 10 teams' remaining budgets.

---

## 2. 🛑 Strict Boundaries (What You Must NOT Touch)
Other members have their own jobs. Do not write code for:
- ❌ **Member 1's job:** Spring Boot backend APIs, player management controllers.
- ❌ **Member 2's job:** Java Spring Security filters or backend JWT token makers.
- ❌ **Member 3's job:** MySQL schema DDL scripts, JPA entity classes, or database row locks.
- ❌ **Member 5's job:** Java JUnit tests, Mockito mocks, or Swagger Java configs.

---

## 3. 🛠️ Tools You Use
* **Node.js (v18+ / v20+)**: The JavaScript environment to run frontend build tools.
* **Vite 8**: The modern build tool that starts your website in milliseconds on `http://localhost:5173`.
* **React 19**: The component-based library to build reactive user interfaces.
* **Axios**: Sends HTTP requests and attaches the `Bearer <token>` to headers.
* **Modern Vanilla CSS**: Custom glassmorphism styling without heavy frameworks.

> ⚠️ **Important Note:** You do **NOT** use Maven or a `pom.xml`. You work entirely with Node.js and a **`package.json`** file.

---

## 4. 📁 Your Code Files & What They Do (In Plain English)

| File Path | What It Does Simply |
| :--- | :--- |
| `frontend/src/App.jsx` | The main dashboard. Holds the active player, current bid price, user login state, and refreshes live data. |
| `frontend/src/index.css` | The styling engine. Defines the dark cybernetic cricket stadium look, neon glows, glass card borders, and animations. |
| `frontend/src/components/Login.jsx` | The login screen. Allows clicking between "Admin" and "Team Owner" with pre-filled test accounts. Saves JWT token in `localStorage`. |
| `frontend/src/components/BiddingConsole.jsx` | The interactive bidding pad with quick increment buttons (+₹20 Lakh, +₹50 Lakh, +₹1 Crore) and budget status. |
| `frontend/src/components/OrbitArena.jsx` | Arranges all 10 IPL team logos in a glowing circle around the current player being auctioned. |
| `frontend/src/components/PlayerCard.jsx` | Displays the player's photo, country flag, playing role (Batsman, Bowler, etc.), base price, and current winning bid. |
| `frontend/src/components/Leaderboard.jsx` | Live budget tracker table showing each team's remaining purse and how many players they have bought. |

---

## 5. 📦 Your `package.json` Dependencies (Explained in 1 Line Each)

```json
{
  "dependencies": {
    "react": "^19.2.7",          // 1. Core library to build reactive UI components
    "react-dom": "^19.2.7",      // 2. Renders React components inside browser HTML
    "axios": "^1.18.1",          // 3. Sends GET/POST API calls with the JWT token
    "sockjs-client": "^1.6.1",   // 4. WebSocket client fallback for live auction rooms
    "stompjs": "^2.3.3"          // 5. STOMP messaging protocol for real-time bid updates
  },
  "devDependencies": {
    "vite": "^8.1.1",            // 6. Fast development server and production bundler
    "@vitejs/plugin-react": "^6.0.3" // 7. Enables JSX syntax and fast hot-reloading
  }
}
```

---

## 6. 🌿 Git Commands You Need to Know

```bash
# 1. Switch to your feature branch
git checkout develop
git checkout -b feature/frontend-ui

# 2. Check which files you changed
git status

# 3. Add your changes
git add frontend/

# 4. Save your changes with a clear message
git commit -m "feat(ui): React SPA with cybernetic dark theme, BiddingConsole, and OrbitArena"

# 5. Send your branch to GitHub
git push origin feature/frontend-ui
```

---

## 7. 🗣️ What to Say to Your Mentor (Your Speaking Script)
> *"Hello Sir. As Member 4 and Frontend & API Integration Lead:  
> 1. I built our Single Page Application using **React 19** and **Vite 8** under `frontend/`.  
> 2. I created a custom **Cybernetic Dark Stadium Design System** in `index.css` using CSS glassmorphism, responsive grid layouts, and neon stadium lighting.  
> 3. I developed the interactive **Orbit Arena** which places all 10 franchises in a circular visual ring around the active player card, highlighting the active bidder in real time.  
> 4. I built the **Bidding Console** with rapid increment buttons (+₹20L, +₹50L, +₹1 Cr) and integrated Axios to send JWT Bearer tokens to Member 2's secure endpoints."*

---

## 8. ❓ Questions the Mentor Can Ask You & Your Answers

* **Q1: Where do you store the JWT token in the frontend?**  
  * **Answer:** *"When the user logs in via `Login.jsx`, the server returns a token which we save in `localStorage.setItem('token', jwt)`. For every subsequent request, Axios attaches `headers: { Authorization: 'Bearer ' + token }`."*

* **Q2: How does the UI update when someone places a bid?**  
  * **Answer:** *"When a user clicks a bid button in `BiddingConsole.jsx`, Axios posts to `/api/v1/bids/place`. When the backend responds with the new bid, `App.jsx` immediately updates the state of `currentPlayer`, highlights the leading franchise in `OrbitArena`, and deducts the amount from the `Leaderboard`."*

* **Q3: Why didn't you use Bootstrap or TailwindCSS?**  
  * **Answer:** *"We used Vanilla CSS with CSS custom properties (variables) to have 100% fine-grained control over our cybernetic cricket stadium aesthetic, custom glowing animations, and radial circular positioning without bloated third-party CSS overhead."*
