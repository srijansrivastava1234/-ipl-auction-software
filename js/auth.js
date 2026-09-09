/**
 * Auth & JWT Storage Manager
 * Member 4 - Frontend & API Integration Lead
 * 
 * Handles JWT token storage in LocalStorage/SessionStorage
 * and user state management.
 */

const getStorage = () => {
    if (typeof localStorage !== 'undefined') return localStorage;
    return {
        _data: {},
        getItem(key) { return this._data[key] || null; },
        setItem(key, val) { this._data[key] = String(val); },
        removeItem(key) { delete this._data[key]; }
    };
};

const AuthService = {
    /**
     * Retrieve stored JWT token
     * @returns {string|null} JWT Token
     */
    getToken() {
        return getStorage().getItem(CONFIG.TOKEN_KEY);
    },

    /**
     * Save JWT token to localStorage
     * @param {string} token - Bearer JWT Token
     */
    setToken(token) {
        if (token) {
            getStorage().setItem(CONFIG.TOKEN_KEY, token);
        }
    },

    /**
     * Remove JWT token on logout
     */
    removeToken() {
        getStorage().removeItem(CONFIG.TOKEN_KEY);
        getStorage().removeItem(CONFIG.USER_KEY);
    },

    /**
     * Check if user has active JWT token
     * @returns {boolean}
     */
    isAuthenticated() {
        const token = this.getToken();
        return !!token && !this.isTokenExpired(token);
    },

    /**
     * Decode JWT token payload (Basic client-side payload reader)
     * @param {string} token 
     * @returns {object|null}
     */
    parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            if (!base64Url) return null;
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(
                atob(base64)
                    .split('')
                    .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                    .join('')
            );
            return JSON.parse(jsonPayload);
        } catch (e) {
            return null;
        }
    },

    /**
     * Check token expiry
     * @param {string} token 
     * @returns {boolean}
     */
    isTokenExpired(token) {
        const payload = this.parseJwt(token);
        if (!payload || !payload.exp) return false; // If mock token without exp
        const currentTime = Math.floor(Date.now() / 1000);
        return payload.exp < currentTime;
    },

    /**
     * Store active user details
     * @param {object} user 
     */
    setUser(user) {
        getStorage().setItem(CONFIG.USER_KEY, JSON.stringify(user));
    },

    /**
     * Get stored user details
     * @returns {object|null}
     */
    getUser() {
        const userStr = getStorage().getItem(CONFIG.USER_KEY);
        return userStr ? JSON.parse(userStr) : null;
    }
};

if (typeof window !== 'undefined') {
    window.AuthService = AuthService;
}

if (typeof module !== 'undefined') {
    module.exports = AuthService;
}
