package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.BingoCard;
import com.enzobf.reactivebingo.domain.Round;
import com.enzobf.reactivebingo.domain.RoundStatus;
import com.enzobf.reactivebingo.domain.Winner;
import com.enzobf.reactivebingo.dto.response.BingoCardResponse;
import com.enzobf.reactivebingo.dto.response.DrawnNumberResponse;
import com.enzobf.reactivebingo.dto.response.RoundResponse;
import com.enzobf.reactivebingo.exception.BusinessRuleException;
import com.enzobf.reactivebingo.exception.ConflictException;
import com.enzobf.reactivebingo.exception.ResourceNotFoundException;
import com.enzobf.reactivebingo.mapper.RoundMapper;
import com.enzobf.reactivebingo.repository.RoundRepository;
import com.enzobf.reactivebingo.util.BingoCardGenerator;
import com.enzobf.reactivebingo.util.BingoNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoundService {

    private final RoundRepository roundRepository;
    private final PlayerService playerService;
    private final EmailService emailService;
    private final RoundMapper roundMapper;
    private final BingoCardGenerator bingoCardGenerator;
    private final BingoNumberGenerator bingoNumberGenerator;

    public Mono<RoundResponse> create() {
        Round round = Round.builder()
                .status(RoundStatus.OPEN)
                .drawnNumbers(new ArrayList<>())
                .playerIds(new ArrayList<>())
                .bingoCards(new ArrayList<>())
                .createdAt(Instant.now())
                .build();
        return roundRepository.save(round).map(roundMapper::toResponse);
    }

    public Flux<RoundResponse> findAll() {
        return roundRepository.findAll().map(roundMapper::toResponse);
    }

    public Mono<RoundResponse> findById(String id) {
        return findEntityById(id).map(roundMapper::toResponse);
    }

    public Mono<BingoCardResponse> generateBingoCard(String roundId, String playerId) {
        return findEntityById(roundId)
                .flatMap(round -> validateCardCreation(round, playerId)
                        .then(Mono.defer(() -> playerService.findEntityById(playerId)))
                        .flatMap(player -> {
                            BingoCard card = BingoCard.builder()
                                    .id(UUID.randomUUID().toString())
                                    .playerId(player.getId())
                                    .playerName(player.getName())
                                    .playerEmail(player.getEmail())
                                    .numbers(bingoCardGenerator.generate())
                                    .build();
                            ensureMutableCollections(round);
                            if (!round.getPlayerIds().contains(player.getId())) {
                                round.getPlayerIds().add(player.getId());
                            }
                            round.getBingoCards().add(card);
                            return roundRepository.save(round).thenReturn(card);
                        }))
                .map(roundMapper::toCardResponse);
    }

    public Mono<DrawnNumberResponse> generateNumber(String roundId) {
        return findEntityById(roundId)
                .flatMap(round -> {
                    if (round.isFinished()) {
                        return Mono.error(new BusinessRuleException("A rodada ja foi finalizada"));
                    }
                    ensureMutableCollections(round);
                    int number = bingoNumberGenerator.generateExcluding(round.getDrawnNumbers());
                    round.getDrawnNumbers().add(number);
                    detectWinner(round).ifPresent(winner -> finishRound(round, winner));
                    return roundRepository.save(round);
                })
                .flatMap(saved -> notifyIfFinished(saved).thenReturn(saved))
                .map(roundMapper::toDrawnNumberResponse);
    }

    public Mono<DrawnNumberResponse> currentNumber(String roundId) {
        return findEntityById(roundId)
                .flatMap(round -> {
                    if (!round.hasDrawsStarted()) {
                        return Mono.error(new ResourceNotFoundException("Nenhum numero foi sorteado nesta rodada"));
                    }
                    return Mono.just(roundMapper.toDrawnNumberResponse(round));
                });
    }

    private Mono<Round> findEntityById(String id) {
        return roundRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Rodada nao encontrada: " + id)));
    }

    private Mono<Void> validateCardCreation(Round round, String playerId) {
        if (round.isFinished()) {
            return Mono.error(new BusinessRuleException("Nao e possivel gerar cartela para uma rodada finalizada"));
        }
        if (round.hasDrawsStarted()) {
            return Mono.error(new BusinessRuleException("Nao e possivel gerar cartelas apos o inicio dos sorteios"));
        }
        boolean alreadyHasCard = round.getBingoCards() != null
                && round.getBingoCards().stream().anyMatch(card -> playerId.equals(card.getPlayerId()));
        if (alreadyHasCard) {
            return Mono.error(new ConflictException("O jogador ja possui uma cartela nesta rodada"));
        }
        return Mono.empty();
    }

    private java.util.Optional<Winner> detectWinner(Round round) {
        Set<Integer> drawn = new HashSet<>(round.getDrawnNumbers());
        return round.getBingoCards().stream()
                .filter(card -> card.getNumbers() != null && !card.getNumbers().isEmpty())
                .filter(card -> drawn.containsAll(card.getNumbers()))
                .findFirst()
                .map(card -> Winner.builder()
                        .playerId(card.getPlayerId())
                        .playerName(card.getPlayerName())
                        .playerEmail(card.getPlayerEmail())
                        .bingoCardId(card.getId())
                        .build());
    }

    private void finishRound(Round round, Winner winner) {
        round.setStatus(RoundStatus.FINISHED);
        round.setWinner(winner);
        round.setFinishedAt(Instant.now());
    }

    private Mono<Void> notifyIfFinished(Round round) {
        if (!round.isFinished() || round.getWinner() == null) {
            return Mono.empty();
        }
        return emailService.notifyRoundResult(round)
                .onErrorResume(error -> Mono.empty());
    }

    private void ensureMutableCollections(Round round) {
        if (round.getDrawnNumbers() == null || !(round.getDrawnNumbers() instanceof ArrayList<?>)) {
            round.setDrawnNumbers(round.getDrawnNumbers() == null ? new ArrayList<>() : new ArrayList<>(round.getDrawnNumbers()));
        }
        if (round.getPlayerIds() == null || !(round.getPlayerIds() instanceof ArrayList<?>)) {
            round.setPlayerIds(round.getPlayerIds() == null ? new ArrayList<>() : new ArrayList<>(round.getPlayerIds()));
        }
        if (round.getBingoCards() == null || !(round.getBingoCards() instanceof ArrayList<?>)) {
            round.setBingoCards(round.getBingoCards() == null ? new ArrayList<>() : new ArrayList<>(round.getBingoCards()));
        }
    }
}
