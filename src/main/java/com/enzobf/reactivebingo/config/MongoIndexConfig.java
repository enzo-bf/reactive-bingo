package com.enzobf.reactivebingo.config;

import com.enzobf.reactivebingo.domain.Player;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class MongoIndexConfig {

    private final ReactiveMongoTemplate mongoTemplate;

    @PostConstruct
    public void ensureIndexes() {
        mongoTemplate.indexOps(Player.class)
                .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique())
                .onErrorResume(error -> Mono.empty())
                .subscribe();
    }
}
