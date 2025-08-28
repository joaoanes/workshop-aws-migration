package com.example.todo.webapp.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.sqs.AmazonSQS;
import com.example.todo.Todo;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.dtos.TodoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.net.URI;

public class TodoServiceImpl implements TodoService {

    private final AmazonSQS sqs;
    private final AmazonS3 s3;
    private final ObjectMapper objectMapper;
    private final String sqsEndpointUrl;

    @Inject
    public TodoServiceImpl(AmazonSQS sqs, AmazonS3 s3, ObjectMapper objectMapper) {
        this(sqs, s3, objectMapper, null);
    }

    public TodoServiceImpl(AmazonSQS sqs, AmazonS3 s3, ObjectMapper objectMapper, String sqsEndpointUrl) {
        this.sqs = sqs;
        this.s3 = s3;
        this.objectMapper = objectMapper;
        this.sqsEndpointUrl = sqsEndpointUrl;
    }

    @Override
    public TodoDto createTodo(CreateTodoDto createTodoDto, String queueUrl) {
        try {
            Todo todo = new Todo(UUID.randomUUID(), createTodoDto.getText(), Instant.now());
            String effectiveQueueUrl = normalizeQueueUrl(queueUrl);
            sqs.sendMessage(new com.amazonaws.services.sqs.model.SendMessageRequest(effectiveQueueUrl, objectMapper.writeValueAsString(todo)));
            return new TodoDto(todo.getId(), todo.getText(), todo.getCreatedAt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TodoDto> getTodos() {
        return s3.listObjectsV2("todo-bucket").getObjectSummaries().stream()
                .map(summary -> {
                    try {
                        String content = s3.getObjectAsString("todo-bucket", summary.getKey());
                        Todo todo = objectMapper.readValue(content, Todo.class);
                        return new TodoDto(todo.getId(), todo.getText(), todo.getCreatedAt());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private String normalizeQueueUrl(String originalQueueUrl) {
        if (sqsEndpointUrl == null || originalQueueUrl == null) {
            return originalQueueUrl;
        }
        try {
            URI q = URI.create(originalQueueUrl);
            URI base = URI.create(sqsEndpointUrl);
            String path = q.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            URI normalized = new URI(
                    base.getScheme(),
                    base.getUserInfo(),
                    base.getHost(),
                    base.getPort(),
                    path,
                    q.getQuery(),
                    q.getFragment()
            );
            return normalized.toString();
        } catch (Exception e) {
            return originalQueueUrl;
        }
    }
}