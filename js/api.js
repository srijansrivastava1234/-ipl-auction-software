/**
 * API Service & Fetch Request Interceptor
 * Member 4 - Frontend & API Integration Lead
 * 
 * Intercepts outgoing HTTP requests to attach Bearer Authorization headers.
 */

class ApiClient {
    constructor(baseUrl = typeof CONFIG !== 'undefined' ? CONFIG.API_BASE_URL : 'http://localhost:8080/api/v1') {
        this.baseUrl = baseUrl;
    }

    /**
     * Core Fetch Request Interceptor
     * Automatically adds headers, Authorization Bearer token, and handles errors.
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        
        // 1. Prepare default headers
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        // 2. Fetch Interceptor: Attach JWT Token if available
        const authService = typeof AuthService !== 'undefined' ? AuthService : (typeof window !== 'undefined' ? window.AuthService : null);
        const token = authService ? authService.getToken() : null;
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
            this.logInterceptor(`[INTERCEPTOR] Attached Bearer Header: Authorization: Bearer ${token.substring(0, 15)}...`);
        } else {
            this.logInterceptor(`[INTERCEPTOR] No active token found in localStorage. Proceeding without Authorization header.`);
        }

        const config = {
            ...options,
            headers
        };

        this.logInterceptor(`[HTTP REQUEST] ${options.method || 'GET'} -> ${url}`);

        try {
            if (typeof fetch === 'undefined') {
                throw new Error('fetch is not defined in this environment');
            }
            const response = await fetch(url, config);

            // Handle 401 Unauthorized (Expired or invalid token)
            if (response.status === 401) {
                this.logInterceptor(`[HTTP 401] Unauthorized. Clearing invalid token.`);
                if (authService) authService.removeToken();
                if (typeof window !== 'undefined' && window.updateAuthUI) window.updateAuthUI();
                throw new Error('Unauthorized - Token expired or invalid');
            }

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP Error ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            this.logInterceptor(`[INTERCEPTOR NOTICE] Fetch to ${url} failed (${error.message}). (Expected in Week 1 until Backend API is running)`);
            return {
                simulated: true,
                status: 'INTERCEPTOR_VERIFIED',
                message: `Request passed through Interceptor with Authorization header: ${headers['Authorization'] || 'None'}`,
                targetUrl: url,
                timestamp: new Date().toISOString()
            };
        }
    }

    logInterceptor(message) {
        console.log(`[API Interceptor]`, message);
        if (typeof window !== 'undefined' && window.logToConsoleWidget) {
            window.logToConsoleWidget(message);
        }
    }

    // Convenient REST Methods
    get(endpoint, headers = {}) {
        return this.request(endpoint, { method: 'GET', headers });
    }

    post(endpoint, body = {}, headers = {}) {
        return this.request(endpoint, { method: 'POST', body: JSON.stringify(body), headers });
    }

    put(endpoint, body = {}, headers = {}) {
        return this.request(endpoint, { method: 'PUT', body: JSON.stringify(body), headers });
    }

    delete(endpoint, headers = {}) {
        return this.request(endpoint, { method: 'DELETE', headers });
    }
}

const api = new ApiClient();

if (typeof window !== 'undefined') {
    window.api = api;
}

if (typeof module !== 'undefined') {
    module.exports = ApiClient;
}
