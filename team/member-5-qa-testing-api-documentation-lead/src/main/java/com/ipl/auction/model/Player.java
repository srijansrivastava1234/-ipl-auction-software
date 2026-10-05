package com.ipl.auction.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role; // e.g., "Batsman", "Bowler", "All-Rounder"

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(name = "original_base_price")
    private BigDecimal originalBasePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status;

    @Column(nullable = false)
    private String country = "India";

    @Column(nullable = false)
    private boolean overseas = false;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    public enum PlayerStatus {
        UNSOLD,
        SOLD
    }

    public Player() {}

    public Player(String name, String role, BigDecimal basePrice, PlayerStatus status) {
        this.name = name;
        this.role = role;
        this.basePrice = basePrice;
        this.originalBasePrice = basePrice;
        this.status = status;
        this.country = "India";
        this.overseas = false;
    }

    public Player(String name, String role, BigDecimal basePrice, PlayerStatus status, String country, boolean overseas) {
        this.name = name;
        this.role = role;
        this.basePrice = basePrice;
        this.originalBasePrice = basePrice;
        this.status = status;
        this.country = country;
        this.overseas = overseas;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getOriginalBasePrice() {
        return originalBasePrice != null ? originalBasePrice : basePrice;
    }
    public void setOriginalBasePrice(BigDecimal originalBasePrice) {
        this.originalBasePrice = originalBasePrice;
    }

    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isOverseas() { return overseas; }
    public void setOverseas(boolean overseas) { this.overseas = overseas; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}