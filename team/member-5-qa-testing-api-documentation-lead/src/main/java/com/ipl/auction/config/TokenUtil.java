package com.ipl.auction.config;

import com.ipl.auction.model.User;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class TokenUtil {

    private static final String SECRET = "IPL_AUCTION_SECRET_KEY_2026";
    private static final long EXPIRY_MS = 86400000; // 24 hours

    public static class UserTokenState {
        private final String username;
        private final String role;
        private final Long teamId;

        public UserTokenState(String username, String role, Long teamId) {
            this.username = username;
            this.role = role;
            this.teamId = teamId;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
        public Long getTeamId() { return teamId; }
    }

    public String generateToken(User user) {
        long expiry = System.currentTimeMillis() + EXPIRY_MS;
        String teamIdStr = user.getTeam() != null ? user.getTeam().getId().toString() : "null";
        String payload = user.getUsername() + ":" + user.getRole() + ":" + teamIdStr + ":" + expiry;
        String signature = sign(payload);
        String rawToken = payload + ":" + signature;
        return Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
    }

    public UserTokenState validateToken(String tokenStr) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(tokenStr);
            String rawToken = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = rawToken.split(":");
            if (parts.length != 5) {
                return null;
            }
            String username = parts[0];
            String role = parts[1];
            String teamIdStr = parts[2];
            long expiry = Long.parseLong(parts[3]);
            String signature = parts[4];

            // Verify expiry
            if (System.currentTimeMillis() > expiry) {
                return null;
            }

            // Verify signature
            String payload = username + ":" + role + ":" + teamIdStr + ":" + expiry;
            if (!sign(payload).equals(signature)) {
                return null;
            }

            Long teamId = "null".equals(teamIdStr) ? null : Long.parseLong(teamIdStr);
            return new UserTokenState(username, role, teamId);
        } catch (Exception e) {
            return null;
        }
    }

    private String sign(String payload) {
        try {
            String input = payload + SECRET;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error signing token", e);
        }
    }
}
