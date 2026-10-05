package com.ipl.auction.dto;

public class LoginResponse {

    private String token;
    private String username;
    private String role;
    private Long teamId;
    private String teamName;

    public LoginResponse() {}

    public LoginResponse(String token, String username, String role, Long teamId, String teamName) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
