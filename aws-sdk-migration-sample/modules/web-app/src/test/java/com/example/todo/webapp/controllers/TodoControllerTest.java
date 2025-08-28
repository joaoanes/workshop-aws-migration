package com.example.todo.webapp.controllers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.dtos.TodoDto;
import com.example.todo.webapp.services.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @Mock
    private AmazonSQS sqs;

    private TodoController todoController;

    @BeforeEach
    void setUp() {
        // Use the test-only constructor to inject the mock
        todoController = new TodoController(todoService, sqs);
    }

    @Test
    void testCreateTodo() {
        CreateTodoDto createTodoDto = new CreateTodoDto();
        createTodoDto.setText("Test Todo");
        TodoDto todoDto = new TodoDto(UUID.randomUUID(), "Test Todo", Instant.now());
        when(sqs.getQueueUrl(anyString())).thenReturn(new GetQueueUrlResult().withQueueUrl("test-queue-url"));
        when(todoService.createTodo(any(CreateTodoDto.class), anyString())).thenReturn(todoDto);

        TodoDto result = todoController.createTodo(createTodoDto);

        assertThat(result).isEqualTo(todoDto);
    }

    @Test
    void testGetTodos() {
        TodoDto todoDto = new TodoDto(UUID.randomUUID(), "Test Todo", Instant.now());
        List<TodoDto> todoDtos = Collections.singletonList(todoDto);
        when(todoService.getTodos()).thenReturn(todoDtos);

        List<TodoDto> result = todoController.getTodos();

        assertThat(result).isEqualTo(todoDtos);
    }
}