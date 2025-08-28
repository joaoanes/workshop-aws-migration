package com.example.todo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.example.todo.Todo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(SystemStubsExtension.class)
class SqsHandlerTest {

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Context context;

    private SqsHandler sqsHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sqsHandler = new SqsHandler(s3Client, objectMapper);
    }

    @Test
    void handleRequest_shouldProcessSqsMessageAndSaveToS3() throws Exception {
        // Given
        Todo todo = new Todo(UUID.randomUUID(), "Test Todo", Instant.now());
        String todoJson = "{\"id\":\"" + todo.getId() + "\",\"task\":\"Test Todo\",\"createdAt\":\"" + todo.getCreatedAt() + "\"}";

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(todoJson);
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Collections.singletonList(sqsMessage));

        when(objectMapper.readValue(todoJson, Todo.class)).thenReturn(todo);
        environmentVariables.set("TODOS_BUCKET_NAME", "test-bucket");

        // When
        sqsHandler.handleRequest(sqsEvent, context);

        // Then
        verify(s3Client, times(1)).putObject("test-bucket", todo.getId() + ".json", todoJson);
    }
}
