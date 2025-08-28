package com.example.todo.webapp.integration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.example.todo.Todo;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.webapp.services.TodoService;
import com.example.todo.webapp.services.TodoServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class TodoIntegrationTest {

    private static final DockerImageName LOCALSTACK_IMAGE = DockerImageName.parse("localstack/localstack");
    private static final String QUEUE_NAME = "todo-queue";
    private static final String BUCKET_NAME = "todo-bucket";

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withServices(LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3);

    private static AmazonSQS sqs;
    private static AmazonS3 s3;
    private static String queueUrl;
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private TodoService todoService;

    @BeforeEach
    void setUp() {
        sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString(),
                        localstack.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())))
                .build();

        s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                        localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString(),
                        localstack.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(localstack.getAccessKey(), localstack.getSecretKey())))
                .enablePathStyleAccess()
                .build();

        s3.createBucket(BUCKET_NAME);
        queueUrl = sqs.createQueue(QUEUE_NAME).getQueueUrl();
        todoService = new TodoServiceImpl(sqs, s3, objectMapper, localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
    }

    @Test
    void testCreateAndGetTodos() throws Exception {
        // Create a new Todo
        CreateTodoDto createTodoDto = new CreateTodoDto();
        createTodoDto.setText("Test Todo");
        todoService.createTodo(createTodoDto, queueUrl);

        // Verify that the message was sent to SQS
        String normalizedQueueUrl = normalizeQueueUrl(queueUrl, localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        String message = sqs.receiveMessage(normalizedQueueUrl).getMessages().get(0).getBody();
        Todo todo = objectMapper.readValue(message, Todo.class);
        assertThat(todo.getText()).isEqualTo("Test Todo");

        // Put the new Todo in S3
        s3.putObject(BUCKET_NAME, "todo.json", objectMapper.writeValueAsString(todo));

        // Verify that the Todo can be retrieved from S3
        assertThat(todoService.getTodos()).hasSize(1);
    }

    private static String normalizeQueueUrl(String originalQueueUrl, String baseEndpoint) {
        if (originalQueueUrl == null || baseEndpoint == null) {
            return originalQueueUrl;
        }
        try {
            URI q = URI.create(originalQueueUrl);
            URI base = URI.create(baseEndpoint);
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
