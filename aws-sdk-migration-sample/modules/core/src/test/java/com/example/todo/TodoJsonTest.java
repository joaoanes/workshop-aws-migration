package com.example.todo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TodoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testSerialization() throws Exception {
        Todo todo = new Todo(UUID.randomUUID(), "Test Todo", Instant.now());
        String json = objectMapper.writeValueAsString(todo);
        Todo deserializedTodo = objectMapper.readValue(json, Todo.class);
        assertThat(deserializedTodo).usingRecursiveComparison().isEqualTo(todo);
    }

    @Test
    void testDeserialization() throws Exception {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        String json = String.format("{\"id\":\"%s\",\"text\":\"Test Todo\",\"createdAt\":\"%s\"}", id, createdAt);
        Todo todo = objectMapper.readValue(json, Todo.class);
        assertThat(todo.getId()).isEqualTo(id);
        assertThat(todo.getText()).isEqualTo("Test Todo");
        assertThat(todo.getCreatedAt()).isEqualTo(createdAt);
    }
}
