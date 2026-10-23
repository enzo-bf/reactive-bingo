package com.enzobf.reactivebingo.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class BingoCardGenerator {

    private final SecureRandom random = new SecureRandom();

    public Set<Integer> generate() {
        Set<Integer> numbers = new LinkedHashSet<>();
        while (numbers.size() < BingoConstants.CARD_SIZE) {
            numbers.add(random.nextInt(BingoConstants.POOL_SIZE) + BingoConstants.MIN_NUMBER);
        }
        return numbers;
    }
}
