package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.BingoCard;
import com.enzobf.reactivebingo.domain.Round;
import com.enzobf.reactivebingo.domain.Winner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Override
    public Mono<Void> notifyRoundResult(Round round) {
        return Mono.fromRunnable(() -> {
            Winner winner = round.getWinner();
            if (winner != null) {
                log.info("E-mail para o vencedor: {} ({})", winner.getPlayerEmail(), winner.getPlayerName());
            }
            if (round.getBingoCards() == null) {
                return;
            }
            round.getBingoCards().stream()
                    .map(BingoCard::getPlayerEmail)
                    .filter(email -> winner == null || !email.equalsIgnoreCase(winner.getPlayerEmail()))
                    .distinct()
                    .forEach(email -> log.info("E-mail para participante: {}", email));
        });
    }
}
