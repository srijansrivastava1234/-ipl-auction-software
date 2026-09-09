package com.ipl.auction.service.impl;

import com.ipl.auction.dto.request.LoginRequest;
import com.ipl.auction.dto.request.RegisterRequest;
import com.ipl.auction.dto.response.AuthResponse;
import com.ipl.auction.dto.response.UserProfileResponse;
import com.ipl.auction.entity.Role;
import com.ipl.auction.entity.User;
import com.ipl.auction.exception.ApiException;
import com.ipl.auction.repository.UserRepository;
import com.ipl.auction.security.JwtUtils;
import com.ipl.auction.security.UserDetailsImpl;
import com.ipl.auction.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for authentication, registration, and user profile operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ApiException("Username '" + request.getUsername() + "' is already taken!", HttpStatus.CONFLICT);
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new ApiException("Email '" + request.getEmail() + "' is already in use!", HttpStatus.CONFLICT);
        }

        Role assignedRole = request.getRole();
        if (assignedRole == null) {
            assignedRole = Role.ROLE_TEAM_OWNER;
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .fullName(request.getFullName().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with ID: {}, role: {}", savedUser.getId(), savedUser.getRole());

        UserDetailsImpl userDetails = UserDetailsImpl.build(savedUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtUtils.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole().name())
                .expiresInMs(jwtUtils.getExpirationMs())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail().trim(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("ROLE_TEAM_OWNER");

            log.info("User {} successfully logged in with role: {}", userDetails.getUsername(), role);

            return AuthResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .userId(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .fullName(userDetails.getFullName())
                    .role(role)
                    .expiresInMs(jwtUtils.getExpirationMs())
                    .build();

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for: {}", request.getUsernameOrEmail());
            throw new ApiException("Invalid username/email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new ApiException("User not found: " + username, HttpStatus.NOT_FOUND));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return jwtUtils.validateJwtToken(token) && !jwtUtils.isTokenExpired(token);
    }

    @Override
    public void logout() {
        log.info("Terminating current user security context");
        SecurityContextHolder.clearContext();
    }
}
