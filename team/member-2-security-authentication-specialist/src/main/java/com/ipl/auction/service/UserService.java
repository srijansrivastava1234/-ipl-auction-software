package com.ipl.auction.service;

import java.math.BigDecimal;
import com.ipl.auction.config.TokenUtil;
import com.ipl.auction.dto.LoginRequest;
import com.ipl.auction.dto.LoginResponse;
import com.ipl.auction.dto.RegisterTeamRequest;
import com.ipl.auction.model.Team;
import com.ipl.auction.model.User;
import com.ipl.auction.repository.TeamRepository;
import com.ipl.auction.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;

    public UserService(UserRepository userRepository, TeamRepository teamRepository, BCryptPasswordEncoder passwordEncoder, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenUtil = tokenUtil;
    }

    public LoginResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = tokenUtil.generateToken(user);
        Long teamId = user.getTeam() != null ? user.getTeam().getId() : null;
        String teamName = user.getTeam() != null ? user.getTeam().getName() : null;

        return new LoginResponse(token, user.getUsername(), user.getRole(), teamId, teamName);
    }

    public User register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public LoginResponse registerTeam(RegisterTeamRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
        }

        // Check if team name exists or create new
        Team team = teamRepository.findByName(request.getTeamName()).orElseGet(() -> {
            Team newTeam = new Team();
            newTeam.setName(request.getTeamName());
            double budgetVal = request.getBudget() != null ? request.getBudget() : 1000000000.00;
            newTeam.setBudget(BigDecimal.valueOf(budgetVal));
            return teamRepository.save(newTeam);
        });

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_TEAM_OWNER");
        user.setTeam(team);
        User savedUser = userRepository.save(user);

        String token = tokenUtil.generateToken(savedUser);
        return new LoginResponse(token, savedUser.getUsername(), savedUser.getRole(), team.getId(), team.getName());
    }
}
