package com.enzobf.reactivebingo.mapper;

import com.enzobf.reactivebingo.domain.BingoCard;
import com.enzobf.reactivebingo.domain.Round;
import com.enzobf.reactivebingo.domain.Winner;
import com.enzobf.reactivebingo.dto.response.BingoCardResponse;
import com.enzobf.reactivebingo.dto.response.DrawnNumberResponse;
import com.enzobf.reactivebingo.dto.response.RoundResponse;
import com.enzobf.reactivebingo.dto.response.WinnerResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoundMapper {

    public RoundResponse toResponse(Round round) {
        return new RoundResponse(
                round.getId(),
                round.getStatus(),
                round.getDrawnNumbers(),
                round.currentNumber(),
                round.getPlayerIds(),
                toCardResponses(round.getBingoCards()),
                toWinnerResponse(round.getWinner()),
                round.getCreatedAt(),
                round.getFinishedAt()
        );
    }

    public DrawnNumberResponse toDrawnNumberResponse(Round round) {
        return new DrawnNumberResponse(
                round.currentNumber(),
                round.getDrawnNumbers(),
                round.getStatus(),
                toWinnerResponse(round.getWinner())
        );
    }

    public BingoCardResponse toCardResponse(BingoCard card) {
        return new BingoCardResponse(
                card.getId(),
                card.getPlayerId(),
                card.getPlayerName(),
                card.getPlayerEmail(),
                card.getNumbers()
        );
    }

    private List<BingoCardResponse> toCardResponses(List<BingoCard> cards) {
        if (cards == null) {
            return List.of();
        }
        return cards.stream().map(this::toCardResponse).toList();
    }

    private WinnerResponse toWinnerResponse(Winner winner) {
        if (winner == null) {
            return null;
        }
        return new WinnerResponse(
                winner.getPlayerId(),
                winner.getPlayerName(),
                winner.getPlayerEmail(),
                winner.getBingoCardId()
        );
    }
}
