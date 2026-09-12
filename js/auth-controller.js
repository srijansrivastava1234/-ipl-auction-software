/**
 * Auth Controller - Form Submissions & JWT Authentication
 * Member 4 - Frontend & API Integration Lead
 */

const AuthController = {
    init() {
        if (typeof document === 'undefined') return;
        
        const loginForm = document.getElementById('login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', (e) => this.handleLogin(e));
        }

        const registerForm = document.getElementById('register-form');
        if (registerForm) {
            registerForm.addEventListener('submit', (e) => this.handleRegister(e));
        }
    },

    /**
     * Login form submission handler
     */
    async handleLogin(e) {
        if (e) e.preventDefault();
        
        const usernameInput = document.getElementById('login-username');
        const passwordInput = document.getElementById('login-password');

        const username = usernameInput ? usernameInput.value.trim() : '';
        const password = passwordInput ? passwordInput.value.trim() : '';

        if (!username || !password) {
            if (window.Toast) window.Toast.show('Please enter both username and password', 'danger');
            return;
        }

        if (password.length < 4) {
            if (window.Toast) window.Toast.show('Password must be at least 4 characters long', 'warning');
            return;
        }

        const submitBtn = e ? e.target.querySelector('button[type="submit"]') : null;
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Authenticating...';
        }

        try {
            const apiClient = typeof window !== 'undefined' ? window.api : null;
            let response = null;

            // Attempt actual REST call via interceptor
            if (apiClient) {
                response = await apiClient.post(CONFIG.ENDPOINTS.AUTH.LOGIN, { username, password });
            }

            if (response && response.token) {
                AuthService.setToken(response.token);
                AuthService.setUser(response.user || { name: username, role: "Franchise Admin" });
            } else {
                // Fallback mock login for Week 2 testing when backend is offline
                this.mockLoginSuccess(username);
            }

            if (window.Toast) window.Toast.show(`Welcome back, ${username}! JWT Token acquired.`, 'success');
            if (window.updateAuthUI) window.updateAuthUI();
            if (window.navigateTo) window.navigateTo('welcome');

        } catch (error) {
            console.warn("Backend login offline, activating mock login fallback for UI testing.");
            this.mockLoginSuccess(username);
            if (window.Toast) window.Toast.show(`Logged in as ${username} (Mock Mode)`, 'info');
            if (window.updateAuthUI) window.updateAuthUI();
            if (window.navigateTo) window.navigateTo('welcome');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Login & Acquire JWT Token';
            }
        }
    },

    /**
     * Fallback mock login helper
     */
    mockLoginSuccess(username) {
        const role = username.toLowerCase().includes('admin') ? 'Super Admin' : 'Franchise Admin';
        const team = username.toUpperCase().startsWith('CSK') ? 'Chennai Super Kings' :
                     username.toUpperCase().startsWith('MI') ? 'Mumbai Indians' : 'Royal Challengers Bengaluru';

        const mockPayload = {
            sub: username,
            roles: [role === 'Super Admin' ? 'ROLE_ADMIN' : 'ROLE_FRANCHISE_ADMIN'],
            team: team,
            exp: Math.floor(Date.now() / 1000) + 7200
        };

        const base64Payload = btoa(JSON.stringify(mockPayload));
        const mockToken = `eyJhbGciOiJIUzI1NiJ9.${base64Payload}.mock_signature_week2`;

        AuthService.setToken(mockToken);
        AuthService.setUser({ name: username, role: role, team: team });
    },

    /**
     * Register form submission handler
     */
    async handleRegister(e) {
        if (e) e.preventDefault();

        const teamInput = document.getElementById('reg-team');
        const emailInput = document.getElementById('reg-email');
        const passwordInput = document.getElementById('reg-password');

        const teamName = teamInput ? teamInput.value.trim() : '';
        const email = emailInput ? emailInput.value.trim() : '';
        const password = passwordInput ? passwordInput.value.trim() : '';

        if (!teamName || !email || !password) {
            if (window.Toast) window.Toast.show('Please fill in all registration fields', 'danger');
            return;
        }

        if (!email.includes('@')) {
            if (window.Toast) window.Toast.show('Please enter a valid email address', 'warning');
            return;
        }

        try {
            const apiClient = typeof window !== 'undefined' ? window.api : null;
            if (apiClient) {
                await apiClient.post(CONFIG.ENDPOINTS.AUTH.REGISTER, { teamName, email, password });
            }
            
            if (window.Toast) window.Toast.show(`Franchise registration submitted for ${teamName}!`, 'success');
            
            // Auto login mock
            this.mockLoginSuccess(teamName);
            if (window.updateAuthUI) window.updateAuthUI();
            if (window.navigateTo) window.navigateTo('welcome');
        } catch (err) {
            if (window.Toast) window.Toast.show(`Registration registered locally for ${teamName}!`, 'info');
            this.mockLoginSuccess(teamName);
            if (window.updateAuthUI) window.updateAuthUI();
            if (window.navigateTo) window.navigateTo('welcome');
        }
    }
};

if (typeof window !== 'undefined') {
    window.AuthController = AuthController;
    document.addEventListener('DOMContentLoaded', () => AuthController.init());
}

if (typeof module !== 'undefined') {
    module.exports = AuthController;
}
