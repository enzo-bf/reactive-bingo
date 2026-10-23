package com.enzobf.reactivebingo.repository;

import com.enzobf.reactivebingo.domain.Round;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RoundRepository extends ReactiveMongoRepository<Round, String> {
}
