package com.ipl.auction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterTeamRequest {

    @NotBlank(message = "Team name is required")
    private String teamName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    private Double budget = 1000000000.00; // Default 100 Cr

    public RegisterTeamRequest() {}

    public RegisterTeamRequest(String teamName, String username, String password, Double budget) {
        this.teamName = teamName;
        this.username = username;
        this.password = password;
        this.budget = budget != null ? budget : 1000000000.00;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }
}
