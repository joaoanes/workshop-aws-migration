package com.example.todo.lambda;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.example.todo.Todo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Testcontainers
@ExtendWith(SystemStubsExtension.class)
class SqsHandlerIntegrationTest {

    @Container
    private static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.1.0"))
            .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

    private static AmazonS3 s3;
    private static AmazonSQS sqs;

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @SystemStub
    private EnvironmentVariables environmentVariables;

    @BeforeAll
    static void beforeAll() {
        s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString(), localstack.getRegion()))
                .withPathStyleAccessEnabled(true)
                .build();
        sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration(localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString(), localstack.getRegion()))
                .build();
    }

    @AfterAll
    static void afterAll() {
        // No-op
    }

    @Test
    void handleRequest_shouldCreateS3Object() throws Exception {
        // Given
        String bucketName = "test-bucket";
        s3.createBucket(bucketName);
        environmentVariables.set("TODOS_BUCKET_NAME", bucketName);

        Todo todo = new Todo(UUID.randomUUID(), "Test Todo", Instant.now());
        String todoJson = objectMapper.writeValueAsString(todo);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(todoJson);
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Collections.singletonList(sqsMessage));

        SqsHandler handler = new SqsHandler(s3, objectMapper);

        // When
        handler.handleRequest(sqsEvent, null);

        // Then
        String s3Object = s3.getObjectAsString(bucketName, todo.getId() + ".json");
        assertThat(s3Object).isEqualTo(todoJson);
    }

    @Test
    void handleRequest_whenInvokedTwice_shouldBeIdempotent() throws Exception {
        // Given
        String bucketName = "test-bucket-idempotent";
        s3.createBucket(bucketName);
        environmentVariables.set("TODOS_BUCKET_NAME", bucketName);

        Todo todo = new Todo(UUID.randomUUID(), "Test Todo", Instant.now());
        String todoJson = objectMapper.writeValueAsString(todo);

        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(todoJson);
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Collections.singletonList(sqsMessage));

        SqsHandler handler = new SqsHandler(s3, objectMapper);

        // When
        handler.handleRequest(sqsEvent, null);
        handler.handleRequest(sqsEvent, null);

        // Then
        assertThat(s3.listObjects(bucketName).getObjectSummaries()).hasSize(1);
    }
}
