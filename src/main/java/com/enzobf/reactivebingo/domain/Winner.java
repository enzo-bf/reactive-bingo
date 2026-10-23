package com.enzobf.reactivebingo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Winner {

    private String playerId;
    private String playerName;
    private String playerEmail;
    private String bingoCardId;
}
