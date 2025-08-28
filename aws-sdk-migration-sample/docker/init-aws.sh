#!/bin/bash
awslocal s3 mb s3://todo-bucket
awslocal sqs create-queue --queue-name todo-queue
awslocal lambda create-function \
    --function-name todo-lambda \
    --runtime java21 \
    --role arn:aws:iam::000000000000:role/lambda_exec_role \
    --handler com.example.todo.lambda.SqsHandler::handleRequest \
    --zip-file fileb:///opt/lambda/lambda.jar \
    --environment "Variables={TODOS_BUCKET_NAME=todo-bucket}"
awslocal lambda create-event-source-mapping \
    --function-name todo-lambda \
    --event-source-arn arn:aws:sqs:us-east-1:000000000000:todo-queue
