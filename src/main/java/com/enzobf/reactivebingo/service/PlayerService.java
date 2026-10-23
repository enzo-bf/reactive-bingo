package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.Player;
import com.enzobf.reactivebingo.dto.request.PlayerRequest;
import com.enzobf.reactivebingo.dto.response.PlayerResponse;
import com.enzobf.reactivebingo.exception.ConflictException;
import com.enzobf.reactivebingo.exception.ResourceNotFoundException;
import com.enzobf.reactivebingo.mapper.PlayerMapper;
import com.enzobf.reactivebingo.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    public Mono<PlayerResponse> create(PlayerRequest request) {
        String email = playerMapper.normalizeEmail(request.email());
        return playerRepository.existsByEmailIgnoreCase(email)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new ConflictException("Ja existe um jogador com o e-mail informado"));
                    }
                    return playerRepository.save(playerMapper.toEntity(request));
                })
                .map(playerMapper::toResponse);
    }

    public Flux<PlayerResponse> findAll() {
        return playerRepository.findAll().map(playerMapper::toResponse);
    }

    public Mono<PlayerResponse> findById(String id) {
        return findEntityById(id).map(playerMapper::toResponse);
    }

    public Mono<Player> findEntityById(String id) {
        return playerRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Jogador nao encontrado: " + id)));
    }

    public Mono<PlayerResponse> update(String id, PlayerRequest request) {
        String email = playerMapper.normalizeEmail(request.email());
        return findEntityById(id)
                .flatMap(player -> playerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return Mono.error(new ConflictException("Ja existe um jogador com o e-mail informado"));
                            }
                            playerMapper.updateEntity(player, request);
                            return playerRepository.save(player);
                        }))
                .map(playerMapper::toResponse);
    }

    public Mono<Void> delete(String id) {
        return findEntityById(id).flatMap(playerRepository::delete);
    }
}
