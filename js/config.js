/**
 * Application Configuration
 * Member 4 - Frontend & API Integration Lead
 */
const CONFIG = {
    // Spring Boot Backend Base API URL (Managed by Member 1 & 2 in upcoming weeks)
    API_BASE_URL: 'http://localhost:8080/api/v1',
    
    // Auth Token Key in LocalStorage
    TOKEN_KEY: 'ipl_auction_jwt_token',
    USER_KEY: 'ipl_auction_user_info',

    // API Endpoints Mapping
    ENDPOINTS: {
        AUTH: {
            LOGIN: '/auth/login',
            REGISTER: '/auth/register',
            ME: '/auth/me'
        },
        ADMIN: {
            PLAYERS: '/admin/players',
            TEAMS: '/admin/teams'
        },
        AUCTION: {
            ROOM: '/auction/live',
            BID: '/auction/bid'
        },
        SQUAD: {
            PURSE: '/squad/purse'
        }
    }
};

// Freeze config object to prevent accidental mutation
Object.freeze(CONFIG);
