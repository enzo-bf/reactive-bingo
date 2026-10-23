package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.BingoCard;
import com.enzobf.reactivebingo.domain.Player;
import com.enzobf.reactivebingo.domain.Round;
import com.enzobf.reactivebingo.domain.RoundStatus;
import com.enzobf.reactivebingo.exception.BusinessRuleException;
import com.enzobf.reactivebingo.exception.ConflictException;
import com.enzobf.reactivebingo.exception.ResourceNotFoundException;
import com.enzobf.reactivebingo.mapper.RoundMapper;
import com.enzobf.reactivebingo.repository.RoundRepository;
import com.enzobf.reactivebingo.util.BingoCardGenerator;
import com.enzobf.reactivebingo.util.BingoNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoundServiceTest {

    @Mock
    private RoundRepository roundRepository;
    @Mock
    private PlayerService playerService;
    @Mock
    private EmailService emailService;
    @Mock
    private BingoCardGenerator bingoCardGenerator;
    @Mock
    private BingoNumberGenerator bingoNumberGenerator;

    private RoundService roundService;

    @BeforeEach
    void setUp() {
        roundService = new RoundService(
                roundRepository,
                playerService,
                emailService,
                new RoundMapper(),
                bingoCardGenerator,
                bingoNumberGenerator
        );
    }

    @Test
    void shouldCreateRound() {
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            round.setId("r1");
            return Mono.just(round);
        });

        StepVerifier.create(roundService.create())
                .assertNext(response -> {
                    assertThat(response.id()).isEqualTo("r1");
                    assertThat(response.status()).isEqualTo(RoundStatus.OPEN);
                    assertThat(response.drawnNumbers()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldGenerateBingoCard() {
        Round round = openRound();
        Player player = Player.builder().id("p1").name("Enzo").email("enzo@email.com").build();
        Set<Integer> numbers = new LinkedHashSet<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20));

        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));
        when(playerService.findEntityById("p1")).thenReturn(Mono.just(player));
        when(bingoCardGenerator.generate()).thenReturn(numbers);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(roundService.generateBingoCard("r1", "p1"))
                .assertNext(card -> {
                    assertThat(card.playerId()).isEqualTo("p1");
                    assertThat(card.numbers()).hasSize(20);
                    assertThat(card.numbers()).containsExactlyElementsOf(numbers);
                })
                .verifyComplete();
    }

    @Test
    void shouldNotGenerateSecondCardForSamePlayer() {
        Round round = openRound();
        round.getBingoCards().add(BingoCard.builder().playerId("p1").numbers(Set.of(1)).build());
        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));

        StepVerifier.create(roundService.generateBingoCard("r1", "p1"))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void shouldNotGenerateCardAfterDrawsStarted() {
        Round round = openRound();
        round.getDrawnNumbers().add(7);
        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));

        StepVerifier.create(roundService.generateBingoCard("r1", "p1"))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void shouldGenerateNumberWithoutWinner() {
        Round round = openRound();
        round.getBingoCards().add(BingoCard.builder()
                .id("c1")
                .playerId("p1")
                .playerName("Enzo")
                .playerEmail("enzo@email.com")
                .numbers(new LinkedHashSet<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)))
                .build());
        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));
        when(bingoNumberGenerator.generateExcluding(any())).thenReturn(42);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(roundService.generateNumber("r1"))
                .assertNext(response -> {
                    assertThat(response.currentNumber()).isEqualTo(42);
                    assertThat(response.status()).isEqualTo(RoundStatus.OPEN);
                    assertThat(response.winner()).isNull();
                })
                .verifyComplete();

        verify(emailService, never()).notifyRoundResult(any());
    }

    @Test
    void shouldFinishRoundWhenCardIsCompleted() {
        Set<Integer> cardNumbers = new LinkedHashSet<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20));
        Round round = openRound();
        round.getDrawnNumbers().addAll(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19));
        round.getBingoCards().add(BingoCard.builder()
                .id("c1")
                .playerId("p1")
                .playerName("Enzo")
                .playerEmail("enzo@email.com")
                .numbers(cardNumbers)
                .build());
        round.getBingoCards().add(BingoCard.builder()
                .id("c2")
                .playerId("p2")
                .playerName("Ana")
                .playerEmail("ana@email.com")
                .numbers(new LinkedHashSet<>(List.of(21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40)))
                .build());

        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));
        when(bingoNumberGenerator.generateExcluding(any())).thenReturn(20);
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(emailService.notifyRoundResult(any(Round.class))).thenReturn(Mono.empty());

        StepVerifier.create(roundService.generateNumber("r1"))
                .assertNext(response -> {
                    assertThat(response.status()).isEqualTo(RoundStatus.FINISHED);
                    assertThat(response.currentNumber()).isEqualTo(20);
                    assertThat(response.winner()).isNotNull();
                    assertThat(response.winner().playerEmail()).isEqualTo("enzo@email.com");
                })
                .verifyComplete();

        verify(emailService).notifyRoundResult(any(Round.class));
    }

    @Test
    void shouldNotGenerateNumberWhenRoundIsFinished() {
        Round round = openRound();
        round.setStatus(RoundStatus.FINISHED);
        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));

        StepVerifier.create(roundService.generateNumber("r1"))
                .expectError(BusinessRuleException.class)
                .verify();
    }

    @Test
    void shouldReturnCurrentNumber() {
        Round round = openRound();
        round.getDrawnNumbers().addAll(List.of(8, 15));
        when(roundRepository.findById("r1")).thenReturn(Mono.just(round));

        StepVerifier.create(roundService.currentNumber("r1"))
                .assertNext(response -> assertThat(response.currentNumber()).isEqualTo(15))
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundWhenNoNumberWasDrawn() {
        when(roundRepository.findById("r1")).thenReturn(Mono.just(openRound()));

        StepVerifier.create(roundService.currentNumber("r1"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    private Round openRound() {
        return Round.builder()
                .id("r1")
                .status(RoundStatus.OPEN)
                .drawnNumbers(new ArrayList<>())
                .playerIds(new ArrayList<>())
                .bingoCards(new ArrayList<>())
                .build();
    }
}
