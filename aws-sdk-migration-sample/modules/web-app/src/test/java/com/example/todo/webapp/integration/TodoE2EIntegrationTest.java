package com.example.todo.webapp.integration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.example.todo.dtos.CreateTodoDto;
import com.example.todo.lambda.SqsHandler;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class TodoE2EIntegrationTest {

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
    void e2eTest() throws Exception {
        // 1. Create a new Todo via service (simulating POST /api/todos)
        CreateTodoDto createTodoDto = new CreateTodoDto();
        createTodoDto.setText("E2E Test Todo");
        todoService.createTodo(createTodoDto, queueUrl);

        // 2. Verify message is in SQS and retrieve it
        String normalizedQueueUrl = normalizeQueueUrl(queueUrl, localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        String messageBody = sqs.receiveMessage(normalizedQueueUrl).getMessages().get(0).getBody();
        assertThat(messageBody).isNotNull();

        // 3. Manually trigger Lambda handler to process the SQS message
        SqsHandler sqsHandler = new SqsHandler(s3, objectMapper) {
            @Override
            protected String getBucketName() {
                return BUCKET_NAME;
            }
        };
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(messageBody);
        sqsEvent.setRecords(List.of(sqsMessage));
        sqsHandler.handleRequest(sqsEvent, null); // Context can be null for this test

        // 4. Verify that the Todo can be retrieved from S3 via service (simulating GET /api/todos)
        List<com.example.todo.dtos.TodoDto> todos = todoService.getTodos();
        assertThat(todos).hasSize(1);
        assertThat(todos.get(0).getText()).isEqualTo("E2E Test Todo");
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
