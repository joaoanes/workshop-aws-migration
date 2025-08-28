package com.example.todo.webapp.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.example.todo.Todo;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.dtos.TodoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private AmazonSQS sqs;

    @Mock
    private AmazonS3 s3;

    private TodoServiceImpl todoService;

    @BeforeEach
    void setUp() {
        todoService = new TodoServiceImpl(sqs, s3, new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void testCreateTodo() {
        CreateTodoDto createTodoDto = new CreateTodoDto();
        createTodoDto.setText("Test Todo");

        TodoDto result = todoService.createTodo(createTodoDto, "test-queue-url");

        assertThat(result.getText()).isEqualTo("Test Todo");
    }

    @Test
    void testGetTodos() throws Exception {
        ListObjectsV2Result listObjectsV2Result = new ListObjectsV2Result();
        S3ObjectSummary summary = new S3ObjectSummary();
        summary.setBucketName("todo-bucket");
        summary.setKey("todo.json");
        listObjectsV2Result.getObjectSummaries().add(summary);
        when(s3.listObjectsV2("todo-bucket")).thenReturn(listObjectsV2Result);
        Todo todo = new Todo(UUID.randomUUID(), "Test Todo", Instant.now());
        when(s3.getObjectAsString("todo-bucket", "todo.json")).thenReturn(new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(todo));

        assertThat(todoService.getTodos()).hasSize(1);
    }
}