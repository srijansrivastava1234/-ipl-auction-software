package com.ipl.auction.service;

import com.ipl.auction.dto.request.LoginRequest;
import com.ipl.auction.dto.request.RegisterRequest;
import com.ipl.auction.dto.response.AuthResponse;
import com.ipl.auction.dto.response.UserProfileResponse;

/**
 * Service interface defining authentication and user registration operations.
 */
public interface AuthService {

    /**
     * Registers a new user with encrypted password and specified role.
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates user credentials and generates JWT token.
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieves the profile information for the authenticated user.
     */
    UserProfileResponse getCurrentUserProfile(String username);

    /**
     * Validates an active JWT token string.
     */
    boolean validateToken(String token);

    /**
     * Terminates the current security context and handles session teardown.
     */
    void logout();
}
