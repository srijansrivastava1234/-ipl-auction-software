package com.ipl.auction.config;

import com.ipl.auction.model.Player;
import com.ipl.auction.model.Player.PlayerStatus;
import com.ipl.auction.model.Team;
import com.ipl.auction.model.User;
import com.ipl.auction.repository.PlayerRepository;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Keep the constructor so Spring can inject dependencies
    public DataInitializer(TeamRepository teamRepository, PlayerRepository playerRepository,
            UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Check if database is already seeded
        if (userRepository.count() > 0 || teamRepository.count() > 0 || playerRepository.count() > 0) {
            System.out.println("🌱 Database already initialized. Skipping seeding.");
            return;
        }

        // 2. Seed All 10 Official IPL Teams

        
        // Seed All 10 Official IPL Teams
        List<Team> iplTeams = Arrays.asList(
                new Team("Chennai Super Kings", new BigDecimal("1000000000")),
                new Team("Mumbai Indians", new BigDecimal("1000000000")),
                new Team("Royal Challengers Bengaluru", new BigDecimal("1000000000")),
                new Team("Kolkata Knight Riders", new BigDecimal("1000000000")),
                new Team("Rajasthan Royals", new BigDecimal("1000000000")),
                new Team("Sunrisers Hyderabad", new BigDecimal("1000000000")),
                new Team("Delhi Capitals", new BigDecimal("1000000000")),
                new Team("Gujarat Titans", new BigDecimal("1000000000")),
                new Team("Lucknow Super Giants", new BigDecimal("1000000000")),
                new Team("Punjab Kings", new BigDecimal("1000000000")));
        List<Team> savedTeams = teamRepository.saveAll(iplTeams);
        System.out.println("=================================================");
        System.out.println("✅ SUCCESSFULLY SEEDED 10 IPL TEAMS!");

        // Seed Users
        // 1. Seed Admin
        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), "ADMIN"));

        // 2. Seed Team Owners (mapped to each official team)
        for (Team team : savedTeams) {
            String alias = getTeamAlias(team.getName());
            String username = alias.toLowerCase() + "_owner";
            String rawPassword = alias.toLowerCase() + "123";
            userRepository.save(new User(username, passwordEncoder.encode(rawPassword), "TEAM_OWNER", team));
            System.out.println(
                    "   👤 Seeded Owner: " + username + " (password: " + rawPassword + ") for " + team.getName());
        }
        System.out.println("✅ SUCCESSFULLY SEEDED AUTHENTICATED USERS!");

        // Seed Expanded 23-Player Pool
        List<Player> playerPool = Arrays.asList(
                // Marquee Batsmen
                createPlayer("Virat Kohli", "Batsman", "20000000"),
                createPlayer("Rohit Sharma", "Batsman", "20000000"),
                createPlayer("Suryakumar Yadav", "Batsman", "20000000"),
                createPlayer("Shubman Gill", "Batsman", "20000000"),
                createPlayer("Yashasvi Jaiswal", "Batsman", "20000000"),
                createPlayer("Travis Head", "Batsman", "20000000", "Australia", true),

                // Wicketkeeper Batsmen
                createPlayer("MS Dhoni", "Wicketkeeper-Batsman", "20000000"),
                createPlayer("Rishabh Pant", "Wicketkeeper-Batsman", "20000000"),
                createPlayer("KL Rahul", "Wicketkeeper-Batsman", "20000000"),
                createPlayer("Heinrich Klaasen", "Wicketkeeper-Batsman", "20000000", "South Africa", true),
                createPlayer("Sanju Samson", "Wicketkeeper-Batsman", "20000000"),

                // All-Rounders
                createPlayer("Hardik Pandya", "All-Rounder", "15000000"),
                createPlayer("Ravindra Jadeja", "All-Rounder", "20000000"),
                createPlayer("Axar Patel", "All-Rounder", "15000000"),
                createPlayer("Sunil Narine", "All-Rounder", "15000000", "West Indies", true),
                createPlayer("Marcus Stoinis", "All-Rounder", "15000000", "Australia", true),

                // Bowlers
                createPlayer("Jasprit Bumrah", "Bowler", "20000000"),
                createPlayer("Kuldeep Yadav", "Bowler", "15000000"),
                createPlayer("Rashid Khan", "Bowler", "20000000", "Afghanistan", true),
                createPlayer("Mitchell Starc", "Bowler", "20000000", "Australia", true),
                createPlayer("Pat Cummins", "Bowler", "20000000", "Australia", true),
                createPlayer("Yuzvendra Chahal", "Bowler", "15000000"),
                createPlayer("Mohammed Siraj", "Bowler", "15000000"));

        playerRepository.saveAll(playerPool);
        System.out.println("✅ SUCCESSFULLY SEEDED 23 IPL PLAYERS!");
        System.out.println("=================================================");
    }

    private Player createPlayer(String name, String role, String basePriceStr, String country, boolean overseas) {
        Player player = new Player();
        player.setName(name);
        player.setRole(role);
        BigDecimal basePrice = new BigDecimal(basePriceStr);
        player.setBasePrice(basePrice);
        player.setOriginalBasePrice(basePrice);
        player.setStatus(PlayerStatus.UNSOLD);
        player.setCountry(country);
        player.setOverseas(overseas);
        return player;
    }

    private Player createPlayer(String name, String role, String basePriceStr) {
        return createPlayer(name, role, basePriceStr, "India", false);
    }

    private String getTeamAlias(String teamName) {
        switch (teamName) {
            case "Chennai Super Kings":
                return "CSK";
            case "Mumbai Indians":
                return "MI";
            case "Royal Challengers Bengaluru":
                return "RCB";
            case "Kolkata Knight Riders":
                return "KKR";
            case "Rajasthan Royals":
                return "RR";
            case "Sunrisers Hyderabad":
                return "SRH";
            case "Delhi Capitals":
                return "DC";
            case "Gujarat Titans":
                return "GT";
            case "Lucknow Super Giants":
                return "LSG";
            case "Punjab Kings":
                return "PBKS";
            default:
                return "TEAM";
        }
    }
}