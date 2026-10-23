package com.enzobf.reactivebingo.service;

import com.enzobf.reactivebingo.domain.Round;
import reactor.core.publisher.Mono;

public interface EmailService {

    Mono<Void> notifyRoundResult(Round round);
}
