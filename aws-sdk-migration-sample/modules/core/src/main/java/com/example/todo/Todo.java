package com.example.todo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public class Todo {
    private final UUID id;
    private final String text;
    private final Instant createdAt;

    @JsonCreator
    public Todo(@JsonProperty("id") UUID id,
                @JsonProperty("text") String text,
                @JsonProperty("createdAt") Instant createdAt) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
