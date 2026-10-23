package com.enzobf.reactivebingo.dto.response;

import com.enzobf.reactivebingo.domain.RoundStatus;

import java.time.Instant;
import java.util.List;

public record RoundResponse(
        String id,
        RoundStatus status,
        List<Integer> drawnNumbers,
        Integer currentNumber,
        List<String> playerIds,
        List<BingoCardResponse> bingoCards,
        WinnerResponse winner,
        Instant createdAt,
        Instant finishedAt
) {
}
