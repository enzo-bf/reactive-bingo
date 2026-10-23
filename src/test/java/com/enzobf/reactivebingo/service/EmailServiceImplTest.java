package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.BingoCard;
import com.enzobf.reactivebingo.domain.Round;
import com.enzobf.reactivebingo.domain.Winner;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;

class EmailServiceImplTest {

    private final EmailServiceImpl emailService = new EmailServiceImpl();

    @Test
    void shouldNotifyWithoutError() {
        Round round = Round.builder()
                .winner(Winner.builder().playerEmail("winner@email.com").playerName("Enzo").build())
                .bingoCards(List.of(
                        BingoCard.builder().playerEmail("winner@email.com").numbers(Set.of(1)).build(),
                        BingoCard.builder().playerEmail("other@email.com").numbers(Set.of(2)).build()
                ))
                .build();

        StepVerifier.create(emailService.notifyRoundResult(round))
                .verifyComplete();
    }
}
