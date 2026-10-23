package com.enzobf.reactivebingo.controller;

import com.enzobf.reactivebingo.dto.response.BingoCardResponse;
import com.enzobf.reactivebingo.dto.response.DrawnNumberResponse;
import com.enzobf.reactivebingo.dto.response.ErrorResponse;
import com.enzobf.reactivebingo.dto.response.RoundResponse;
import com.enzobf.reactivebingo.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/rounds")
@RequiredArgsConstructor
@Tag(name = "Rounds", description = "Rodadas, cartelas e sorteios de Bingo")
public class RoundController {

    private final RoundService roundService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Abre uma nova rodada")
    public Mono<RoundResponse> create() {
        return roundService.create();
    }

    @GetMapping
    @Operation(summary = "Lista todas as rodadas")
    public Flux<RoundResponse> findAll() {
        return roundService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma rodada pelo id")
    @ApiResponse(responseCode = "404", description = "Rodada nao encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Mono<RoundResponse> findById(@PathVariable String id) {
        return roundService.findById(id);
    }

    @PostMapping("/{roundId}/bingo-card/{playerId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Gera uma cartela para o jogador na rodada")
    public Mono<BingoCardResponse> generateBingoCard(
            @PathVariable String roundId,
            @PathVariable String playerId
    ) {
        return roundService.generateBingoCard(roundId, playerId);
    }

    @PostMapping("/{id}/generate-number")
    @Operation(summary = "Sorteia o proximo numero da rodada")
    public Mono<DrawnNumberResponse> generateNumber(@PathVariable String id) {
        return roundService.generateNumber(id);
    }

    @GetMapping("/{id}/current-number")
    @Operation(summary = "Consulta o numero atual sorteado")
    public Mono<DrawnNumberResponse> currentNumber(@PathVariable String id) {
        return roundService.currentNumber(id);
    }
}
