package com.enzobf.reactivebingo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BingoCard {

    private String id;
    private String playerId;
    private String playerName;
    private String playerEmail;

    @Builder.Default
    private Set<Integer> numbers = new LinkedHashSet<>();
}
