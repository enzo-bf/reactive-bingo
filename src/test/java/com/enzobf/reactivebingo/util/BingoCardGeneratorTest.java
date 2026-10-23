package com.enzobf.reactivebingo.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BingoCardGeneratorTest {

    private final BingoCardGenerator generator = new BingoCardGenerator();

    @Test
    void shouldGenerateTwentyUniqueNumbersInRange() {
        Set<Integer> numbers = generator.generate();

        assertThat(numbers).hasSize(BingoConstants.CARD_SIZE);
        assertThat(numbers).allMatch(number -> number >= BingoConstants.MIN_NUMBER && number <= BingoConstants.MAX_NUMBER);
    }
}
