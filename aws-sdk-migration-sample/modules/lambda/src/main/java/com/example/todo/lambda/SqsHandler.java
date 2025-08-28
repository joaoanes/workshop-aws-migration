package com.example.todo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.example.todo.Todo;
import com.example.todo.lambda.dagger.DaggerSqsHandlerComponent;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

public class SqsHandler implements RequestHandler<SQSEvent, Void> {

    @Inject
    AmazonS3 s3Client;

    @Inject
    ObjectMapper objectMapper;

    public SqsHandler() {
        DaggerSqsHandlerComponent.create().inject(this);
    }

    // Constructor for testing
    public SqsHandler(AmazonS3 s3Client, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public Void handleRequest(SQSEvent sqsEvent, Context context) {
        for (SQSEvent.SQSMessage msg : sqsEvent.getRecords()) {
            try {
                Todo todo = objectMapper.readValue(msg.getBody(), Todo.class);
                String bucketName = getBucketName();
                s3Client.putObject(bucketName, todo.getId() + ".json", msg.getBody());
            } catch (Exception e) {
                // TODO: Add proper error handling and send to DLQ
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected String getBucketName() {
        return System.getenv("TODOS_BUCKET_NAME");
    }
}
