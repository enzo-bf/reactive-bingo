package com.enzobf.reactivebingo.util;

import com.enzobf.reactivebingo.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BingoNumberGeneratorTest {

    private final BingoNumberGenerator generator = new BingoNumberGenerator();

    @Test
    void shouldGenerateNumberOutsideDrawnSet() {
        Set<Integer> drawn = new HashSet<>(Set.of(0, 1, 2));
        int number = generator.generateExcluding(drawn);
        assertThat(number).isBetween(BingoConstants.MIN_NUMBER, BingoConstants.MAX_NUMBER);
        assertThat(drawn).doesNotContain(number);
    }

    @Test
    void shouldRejectWhenPoolIsExhausted() {
        Set<Integer> all = new HashSet<>();
        IntStream.rangeClosed(BingoConstants.MIN_NUMBER, BingoConstants.MAX_NUMBER).forEach(all::add);

        assertThatThrownBy(() -> generator.generateExcluding(all))
                .isInstanceOf(BusinessRuleException.class);
    }
}
