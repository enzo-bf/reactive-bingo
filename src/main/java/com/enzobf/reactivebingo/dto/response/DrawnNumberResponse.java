package com.enzobf.reactivebingo.dto.response;

import com.enzobf.reactivebingo.domain.RoundStatus;

import java.util.List;

public record DrawnNumberResponse(
        Integer currentNumber,
        List<Integer> drawnNumbers,
        RoundStatus status,
        WinnerResponse winner
) {
}
