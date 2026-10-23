package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.Player;
import com.enzobf.reactivebingo.dto.request.PlayerRequest;
import com.enzobf.reactivebingo.exception.ConflictException;
import com.enzobf.reactivebingo.exception.ResourceNotFoundException;
import com.enzobf.reactivebingo.mapper.PlayerMapper;
import com.enzobf.reactivebingo.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, new PlayerMapper());
    }

    @Test
    void shouldCreatePlayer() {
        PlayerRequest request = new PlayerRequest("Enzo", "enzo@email.com");
        when(playerRepository.existsByEmailIgnoreCase("enzo@email.com")).thenReturn(Mono.just(false));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> {
            Player player = invocation.getArgument(0);
            player.setId("p1");
            return Mono.just(player);
        });

        StepVerifier.create(playerService.create(request))
                .assertNext(response -> {
                    assertThat(response.id()).isEqualTo("p1");
                    assertThat(response.name()).isEqualTo("Enzo");
                    assertThat(response.email()).isEqualTo("enzo@email.com");
                })
                .verifyComplete();
    }

    @Test
    void shouldNotCreatePlayerWithDuplicatedEmail() {
        PlayerRequest request = new PlayerRequest("Enzo", "enzo@email.com");
        when(playerRepository.existsByEmailIgnoreCase("enzo@email.com")).thenReturn(Mono.just(true));

        StepVerifier.create(playerService.create(request))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void shouldFindPlayerById() {
        Player player = Player.builder().id("p1").name("Enzo").email("enzo@email.com").build();
        when(playerRepository.findById("p1")).thenReturn(Mono.just(player));

        StepVerifier.create(playerService.findById("p1"))
                .assertNext(response -> assertThat(response.name()).isEqualTo("Enzo"))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenPlayerDoesNotExist() {
        when(playerRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(playerService.findById("missing"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldListPlayers() {
        when(playerRepository.findAll()).thenReturn(Flux.just(
                Player.builder().id("1").name("A").email("a@email.com").build(),
                Player.builder().id("2").name("B").email("b@email.com").build()
        ));

        StepVerifier.create(playerService.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldUpdatePlayer() {
        Player player = Player.builder().id("p1").name("Enzo").email("enzo@email.com").build();
        PlayerRequest request = new PlayerRequest("Enzo Atualizado", "novo@email.com");
        when(playerRepository.findById("p1")).thenReturn(Mono.just(player));
        when(playerRepository.existsByEmailIgnoreCaseAndIdNot("novo@email.com", "p1")).thenReturn(Mono.just(false));
        when(playerRepository.save(any(Player.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(playerService.update("p1", request))
                .assertNext(response -> {
                    assertThat(response.name()).isEqualTo("Enzo Atualizado");
                    assertThat(response.email()).isEqualTo("novo@email.com");
                })
                .verifyComplete();
    }

    @Test
    void shouldDeletePlayer() {
        Player player = Player.builder().id("p1").name("Enzo").email("enzo@email.com").build();
        when(playerRepository.findById("p1")).thenReturn(Mono.just(player));
        when(playerRepository.delete(player)).thenReturn(Mono.empty());

        StepVerifier.create(playerService.delete("p1"))
                .verifyComplete();

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        org.mockito.Mockito.verify(playerRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo("p1");
    }
}
