package com.example.todo.dtos;

import java.time.Instant;
import java.util.UUID;

public class TodoDto {
    private UUID id;
    private String text;
    private Instant createdAt;

    public TodoDto(UUID id, String text, Instant createdAt) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
