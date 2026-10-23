package com.enzobf.reactivebingo.repository;

import com.enzobf.reactivebingo.domain.Player;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface PlayerRepository extends ReactiveMongoRepository<Player, String> {

    Mono<Boolean> existsByEmailIgnoreCase(String email);

    Mono<Boolean> existsByEmailIgnoreCaseAndIdNot(String email, String id);
}
