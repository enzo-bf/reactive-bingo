package com.enzobf.reactivebingo.dto.response;

import java.util.Set;

public record BingoCardResponse(
        String id,
        String playerId,
        String playerName,
        String playerEmail,
        Set<Integer> numbers
) {
}
