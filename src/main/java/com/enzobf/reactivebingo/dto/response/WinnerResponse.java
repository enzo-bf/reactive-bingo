package com.enzobf.reactivebingo.dto.response;

public record WinnerResponse(
        String playerId,
        String playerName,
        String playerEmail,
        String bingoCardId
) {
}
