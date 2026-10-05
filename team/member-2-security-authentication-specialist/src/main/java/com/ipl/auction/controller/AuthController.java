package com.ipl.auction.controller;

import com.ipl.auction.dto.LoginRequest;
import com.ipl.auction.dto.LoginResponse;
import com.ipl.auction.dto.RegisterTeamRequest;
import com.ipl.auction.model.User;
import com.ipl.auction.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.authenticate(request));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/register-team")
    public ResponseEntity<LoginResponse> registerTeam(@Valid @RequestBody RegisterTeamRequest request) {
        return ResponseEntity.ok(userService.registerTeam(request));
    }
}
