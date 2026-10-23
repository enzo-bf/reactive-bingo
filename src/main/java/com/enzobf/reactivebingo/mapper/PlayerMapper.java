package com.enzobf.reactivebingo.mapper;

import com.enzobf.reactivebingo.domain.Player;
import com.enzobf.reactivebingo.dto.request.PlayerRequest;
import com.enzobf.reactivebingo.dto.response.PlayerResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PlayerMapper {

    public Player toEntity(PlayerRequest request) {
        return Player.builder()
                .name(request.name().trim())
                .email(normalizeEmail(request.email()))
                .createdAt(Instant.now())
                .build();
    }

    public void updateEntity(Player player, PlayerRequest request) {
        player.setName(request.name().trim());
        player.setEmail(normalizeEmail(request.email()));
        player.setUpdatedAt(Instant.now());
    }

    public PlayerResponse toResponse(Player player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getEmail(),
                player.getCreatedAt(),
                player.getUpdatedAt()
        );
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
