package com.enzobf.reactivebingo.controller;

import com.enzobf.reactivebingo.dto.request.PlayerRequest;
import com.enzobf.reactivebingo.dto.response.ErrorResponse;
import com.enzobf.reactivebingo.dto.response.PlayerResponse;
import com.enzobf.reactivebingo.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "CRUD reativo de jogadores")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um jogador")
    @ApiResponse(responseCode = "201", description = "Jogador criado")
    @ApiResponse(responseCode = "400", description = "Dados invalidos",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Mono<PlayerResponse> create(@Valid @RequestBody PlayerRequest request) {
        return playerService.create(request);
    }

    @GetMapping
    @Operation(summary = "Lista todos os jogadores")
    public Flux<PlayerResponse> findAll() {
        return playerService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um jogador pelo id")
    @ApiResponse(responseCode = "404", description = "Jogador nao encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Mono<PlayerResponse> findById(@PathVariable String id) {
        return playerService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um jogador")
    public Mono<PlayerResponse> update(@PathVariable String id, @Valid @RequestBody PlayerRequest request) {
        return playerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um jogador")
    public Mono<Void> delete(@PathVariable String id) {
        return playerService.delete(id);
    }
}
