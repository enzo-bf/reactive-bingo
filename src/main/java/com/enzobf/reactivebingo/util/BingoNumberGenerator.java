package com.enzobf.reactivebingo.util;

import com.enzobf.reactivebingo.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class BingoNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    public int generateExcluding(Collection<Integer> alreadyDrawn) {
        Set<Integer> drawn = alreadyDrawn == null ? Set.of() : new HashSet<>(alreadyDrawn);
        if (drawn.size() >= BingoConstants.POOL_SIZE) {
            throw new BusinessRuleException("Todos os numeros ja foram sorteados");
        }
        int number;
        do {
            number = random.nextInt(BingoConstants.POOL_SIZE) + BingoConstants.MIN_NUMBER;
        } while (drawn.contains(number));
        return number;
    }
}
