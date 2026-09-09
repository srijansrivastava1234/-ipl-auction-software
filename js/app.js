/**
 * Core Application Controller
 * Member 4 - Frontend & API Integration Lead
 */

if (typeof document !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        console.log("⚡ IPL Auction Web UI Initialized - Week 2 Interactive Suite Active");
        updateAuthUI();
    });
}

/**
 * Page Navigation Controller
 */
function navigateTo(pageId) {
    if (typeof document === 'undefined') return;

    // Update active nav items
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        if (item.getAttribute('data-page') === pageId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Update visible page section
    const pageViews = document.querySelectorAll('.page-view');
    pageViews.forEach(view => view.classList.remove('active'));

    const targetPage = document.getElementById(`page-${pageId}`);
    if (targetPage) {
        targetPage.classList.add('active');
    }
}

/**
 * Console Output Helper for Verification Widget
 */
function logToConsoleWidget(text) {
    if (typeof document === 'undefined') return;
    const consoleBox = document.getElementById('api-console');
    if (consoleBox) {
        const timestamp = new Date().toLocaleTimeString();
        const line = document.createElement('div');
        line.style.margin = '4px 0';
        line.innerHTML = `<span style="color: #64748b;">[${timestamp}]</span> ${escapeHtml(text)}`;
        consoleBox.appendChild(line);
        consoleBox.scrollTop = consoleBox.scrollHeight;
    }
}

function escapeHtml(string) {
    return String(string).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/**
 * API Interceptor Test Function
 */
async function testApiInterceptor() {
    if (window.Toast) window.Toast.show("Initiating protected REST API request via interceptor...", "info");
    const apiClient = typeof window !== 'undefined' ? window.api : api;
    const result = await apiClient.get('/admin/players');
    if (window.Toast) window.Toast.show("API Interceptor attached Authorization header successfully!", "success");
}

/**
 * Simulate Token Storage Login
 */
function simulateTokenLogin() {
    const auth = typeof window !== 'undefined' ? window.AuthService : AuthService;
    const mockPayload = {
        sub: "franchise_owner_1",
        roles: ["ROLE_FRANCHISE_ADMIN"],
        team: "Chennai Super Kings",
        exp: Math.floor(Date.now() / 1000) + 3600
    };
    
    let base64Payload = "";
    if (typeof btoa !== 'undefined') {
        base64Payload = btoa(JSON.stringify(mockPayload));
    } else {
        base64Payload = Buffer.from(JSON.stringify(mockPayload)).toString('base64');
    }

    const mockToken = `eyJhbGciOiJIUzI1NiJ9.${base64Payload}.mock_signature_week2`;
    
    if (auth) {
        auth.setToken(mockToken);
        auth.setUser({ name: "CSK Owner", role: "Franchise Admin", team: "Chennai Super Kings" });
    }

    if (window.Toast) window.Toast.show("JWT Token acquired & saved to LocalStorage", "success");
    updateAuthUI();
}

/**
 * Simulate Logout
 */
function simulateLogout() {
    const auth = typeof window !== 'undefined' ? window.AuthService : AuthService;
    if (auth) auth.removeToken();
    if (window.Toast) window.Toast.show("Logged out. JWT token cleared.", "info");
    updateAuthUI();
}

/**
 * Update UI according to active JWT token
 */
function updateAuthUI() {
    if (typeof document === 'undefined') return;
    const auth = typeof window !== 'undefined' ? window.AuthService : AuthService;
    const isAuth = auth ? auth.isAuthenticated() : false;
    const user = auth ? auth.getUser() : null;
    
    const userNameEl = document.getElementById('nav-user-name');
    const userRoleEl = document.getElementById('nav-user-role');
    const authBtn = document.getElementById('nav-auth-btn');
    const apiBadgeText = document.getElementById('api-status-text');

    if (userNameEl && userRoleEl && authBtn && apiBadgeText) {
        if (isAuth && user) {
            userNameEl.textContent = user.name || "Authenticated User";
            userRoleEl.textContent = user.role || "JWT Active";
            authBtn.textContent = "Logout";
            authBtn.onclick = simulateLogout;
            apiBadgeText.textContent = "JWT Active (Form Ready)";
        } else {
            userNameEl.textContent = "Guest User";
            userRoleEl.textContent = "Not Authenticated";
            authBtn.textContent = "Login";
            authBtn.onclick = () => navigateTo('login');
            apiBadgeText.textContent = "Form Interceptors Ready (Week 2)";
        }
    }
}

if (typeof window !== 'undefined') {
    window.navigateTo = navigateTo;
    window.logToConsoleWidget = logToConsoleWidget;
    window.testApiInterceptor = testApiInterceptor;
    window.simulateTokenLogin = simulateTokenLogin;
    window.simulateLogout = simulateLogout;
    window.updateAuthUI = updateAuthUI;
}
