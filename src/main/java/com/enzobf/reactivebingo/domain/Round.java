package com.enzobf.reactivebingo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rounds")
public class Round {

    @Id
    private String id;

    @Builder.Default
    private RoundStatus status = RoundStatus.OPEN;

    @Builder.Default
    private List<Integer> drawnNumbers = new ArrayList<>();

    @Builder.Default
    private List<String> playerIds = new ArrayList<>();

    @Builder.Default
    private List<BingoCard> bingoCards = new ArrayList<>();

    private Winner winner;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant finishedAt;

    @Version
    private Long version;

    public boolean hasDrawsStarted() {
        return drawnNumbers != null && !drawnNumbers.isEmpty();
    }

    public boolean isFinished() {
        return RoundStatus.FINISHED == status;
    }

    public Integer currentNumber() {
        if (drawnNumbers == null || drawnNumbers.isEmpty()) {
            return null;
        }
        return drawnNumbers.getLast();
    }
}
