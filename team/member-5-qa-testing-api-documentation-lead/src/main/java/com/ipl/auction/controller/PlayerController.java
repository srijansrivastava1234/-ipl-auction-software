package com.ipl.auction.controller;

import com.ipl.auction.config.TokenUtil;
import com.ipl.auction.dto.SellPlayerRequest;
import com.ipl.auction.model.Player;
import com.ipl.auction.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TokenUtil tokenUtil;

    public PlayerController(PlayerService playerService, SimpMessagingTemplate messagingTemplate, TokenUtil tokenUtil) {
        this.playerService = playerService;
        this.messagingTemplate = messagingTemplate;
        this.tokenUtil = tokenUtil;
    }

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Player> createPlayer(@Valid @RequestBody Player player) {
        Player created = playerService.createPlayer(player);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @Valid @RequestBody Player playerDetails) {
        try {
            Player updated = playerService.updatePlayer(id, playerDetails);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayer(@PathVariable Long id) {
        try {
            playerService.deletePlayer(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/sell")
    public ResponseEntity<?> sellPlayer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody SellPlayerRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid authorization token"));
        }

        String token = authHeader.substring(7);
        TokenUtil.UserTokenState tokenState = tokenUtil.validateToken(token);
        if (tokenState == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        // Only ADMIN (Auctioneer) can sell a player
        if (!"ADMIN".equals(tokenState.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the Auctioneer (Admin) can finalize a sale!"));
        }

        Player soldPlayer = playerService.sellPlayer(id, request.getTeamId(), request.getFinalPrice());

        // Broadcast real-time player sale update to all connected devices
        messagingTemplate.convertAndSend("/topic/players", soldPlayer);

        return ResponseEntity.ok(soldPlayer);
    }

    @PutMapping("/{id}/unsold")
    public ResponseEntity<?> unsoldPlayer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid authorization token"));
        }

        String token = authHeader.substring(7);
        TokenUtil.UserTokenState tokenState = tokenUtil.validateToken(token);
        if (tokenState == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired token"));
        }

        // Only ADMIN (Auctioneer) can mark unsold
        if (!"ADMIN".equals(tokenState.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the Auctioneer (Admin) can mark a player unsold!"));
        }

        Player unsoldPlayer = playerService.markUnsold(id);

        // Broadcast real-time player sale/unsold update to all connected devices
        messagingTemplate.convertAndSend("/topic/players", unsoldPlayer);

        return ResponseEntity.ok(unsoldPlayer);
    }
}