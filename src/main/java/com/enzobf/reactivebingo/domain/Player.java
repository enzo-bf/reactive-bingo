package com.enzobf.reactivebingo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "players")
public class Player {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
