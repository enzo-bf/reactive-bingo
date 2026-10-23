package com.enzobf.reactivebingo.controller;

import com.enzobf.reactivebingo.domain.RoundStatus;
import com.enzobf.reactivebingo.dto.response.BingoCardResponse;
import com.enzobf.reactivebingo.dto.response.DrawnNumberResponse;
import com.enzobf.reactivebingo.dto.response.RoundResponse;
import com.enzobf.reactivebingo.dto.response.WinnerResponse;
import com.enzobf.reactivebingo.exception.BusinessRuleException;
import com.enzobf.reactivebingo.exception.GlobalExceptionHandler;
import com.enzobf.reactivebingo.service.RoundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@WebFluxTest(controllers = RoundController.class)
@Import(GlobalExceptionHandler.class)
class RoundControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private RoundService roundService;

    @Test
    void shouldCreateRound() {
        when(roundService.create()).thenReturn(Mono.just(roundResponse()));

        webTestClient.post()
                .uri("/rounds")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("r1")
                .jsonPath("$.status").isEqualTo("OPEN");
    }

    @Test
    void shouldListRounds() {
        when(roundService.findAll()).thenReturn(Flux.just(roundResponse()));

        webTestClient.get()
                .uri("/rounds")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("r1");
    }

    @Test
    void shouldGenerateBingoCard() {
        BingoCardResponse card = new BingoCardResponse("c1", "p1", "Enzo", "enzo@email.com", Set.of(1, 2, 3));
        when(roundService.generateBingoCard("r1", "p1")).thenReturn(Mono.just(card));

        webTestClient.post()
                .uri("/rounds/r1/bingo-card/p1")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.playerId").isEqualTo("p1");
    }

    @Test
    void shouldGenerateNumber() {
        DrawnNumberResponse response = new DrawnNumberResponse(
                20,
                List.of(20),
                RoundStatus.FINISHED,
                new WinnerResponse("p1", "Enzo", "enzo@email.com", "c1")
        );
        when(roundService.generateNumber("r1")).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/rounds/r1/generate-number")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentNumber").isEqualTo(20)
                .jsonPath("$.status").isEqualTo("FINISHED")
                .jsonPath("$.winner.playerEmail").isEqualTo("enzo@email.com");
    }

    @Test
    void shouldRejectDrawWhenRoundIsFinished() {
        when(roundService.generateNumber("r1"))
                .thenReturn(Mono.error(new BusinessRuleException("A rodada ja foi finalizada")));

        webTestClient.post()
                .uri("/rounds/r1/generate-number")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("A rodada ja foi finalizada");
    }

    @Test
    void shouldGetCurrentNumber() {
        DrawnNumberResponse response = new DrawnNumberResponse(8, List.of(8), RoundStatus.OPEN, null);
        when(roundService.currentNumber("r1")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/rounds/r1/current-number")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.currentNumber").isEqualTo(8);
    }

    private RoundResponse roundResponse() {
        return new RoundResponse(
                "r1",
                RoundStatus.OPEN,
                List.of(),
                null,
                List.of(),
                List.of(),
                null,
                Instant.now(),
                null
        );
    }
}
