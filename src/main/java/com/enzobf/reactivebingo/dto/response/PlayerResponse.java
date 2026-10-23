package com.enzobf.reactivebingo.dto.response;

import java.time.Instant;

public record PlayerResponse(
        String id,
        String name,
        String email,
        Instant createdAt,
        Instant updatedAt
) {
}
