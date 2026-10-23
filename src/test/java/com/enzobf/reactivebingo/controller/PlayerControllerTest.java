package com.enzobf.reactivebingo.controller;

import com.enzobf.reactivebingo.dto.request.PlayerRequest;
import com.enzobf.reactivebingo.dto.response.PlayerResponse;
import com.enzobf.reactivebingo.exception.ConflictException;
import com.enzobf.reactivebingo.exception.GlobalExceptionHandler;
import com.enzobf.reactivebingo.exception.ResourceNotFoundException;
import com.enzobf.reactivebingo.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PlayerController.class)
@Import(GlobalExceptionHandler.class)
class PlayerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void shouldCreatePlayer() {
        PlayerResponse response = new PlayerResponse("p1", "Enzo", "enzo@email.com", Instant.now(), null);
        when(playerService.create(any(PlayerRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Enzo","email":"enzo@email.com"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1")
                .jsonPath("$.email").isEqualTo("enzo@email.com");
    }

    @Test
    void shouldReturnConflictWhenEmailExists() {
        when(playerService.create(any(PlayerRequest.class)))
                .thenReturn(Mono.error(new ConflictException("Ja existe um jogador com o e-mail informado")));

        webTestClient.post()
                .uri("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Enzo","email":"enzo@email.com"}
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldValidatePlayerPayload() {
        webTestClient.post()
                .uri("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"","email":"invalido"}
                        """)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldListPlayers() {
        when(playerService.findAll()).thenReturn(Flux.just(
                new PlayerResponse("p1", "Enzo", "enzo@email.com", Instant.now(), null)
        ));

        webTestClient.get()
                .uri("/players")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("p1");
    }

    @Test
    void shouldReturnNotFound() {
        when(playerService.findById("missing"))
                .thenReturn(Mono.error(new ResourceNotFoundException("Jogador nao encontrado: missing")));

        webTestClient.get()
                .uri("/players/missing")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldUpdatePlayer() {
        PlayerResponse response = new PlayerResponse("p1", "Novo", "novo@email.com", Instant.now(), Instant.now());
        when(playerService.update(eq("p1"), any(PlayerRequest.class))).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/players/p1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Novo","email":"novo@email.com"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Novo");
    }

    @Test
    void shouldDeletePlayer() {
        when(playerService.delete("p1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/players/p1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
