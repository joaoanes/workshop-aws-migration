# Tasks

**Process Rule:** A task is only considered complete if it is accompanied by a passing test that verifies its functionality.

## Phase 1: Project Scaffolding & Core Domain

- [x] Initialize a multi-module Gradle project (`aws-sdk-migration-sample`).
- [x] Configure the root `build.gradle.kts` with version catalogs and Java 21 toolchain.
- [x] Set up `buildSrc` for shared build conventions (JUnit 5, AssertJ, Spotless).
- [x] Create the `modules/core` module.
- [x] Define the `Todo` domain class, DTOs, and JSON serialization/deserialization helpers in `core`.
- [x] **Test:** Write unit tests for JSON serialization/deserialization in the `core` module.

## Phase 2: Web Application (API Layer)

- [x] Create the `modules/web-app` module.
- [x] Set up Spring Framework 5 for the web layer (without Spring DI).
- [x] Implement `POST /api/todos` and `GET /api/todos` endpoints.
- [x] Create a `TodoService` to handle business logic.
- [x] Configure Google Dagger for dependency injection in the `web-app`.
- [x] Implement a Dagger `ClientsModule` to provide `AmazonSQS` and `AmazonS3` (v1) clients.
- [x] Inject the Dagger-managed `TodoService` into the Spring controllers.
- [x] Implement the `POST` endpoint logic to send a message to SQS using AWS SDK v1.
- [x] Implement the `GET` endpoint logic to list and read objects from S3 using AWS SDK v1.
- [x] **Test:** Write unit tests for the `TodoController` and `TodoService` (using mocks/fakes).
- [x] **Test:** Set up Testcontainers with LocalStack for integration tests.
- [x] **Test:** Write integration test for `POST /api/todos` to verify message is sent to SQS.
- [x] **Test:** Write integration test for `GET /api/todos` to verify it retrieves data from S3.

## Phase 3: Lambda Consumer

- [x] Create the `modules/lambda` module.
- [x] Create a Java 21 Lambda handler that consumes SQS messages.
- [x] Configure Dagger to provide dependencies for the Lambda handler.
- [x] Implement the handler logic to write the consumed To-Do JSON to an S3 bucket (`todos/{id}.json`) using AWS SDK v1.
- [x] Configure the Gradle `shadowJar` plugin to package the Lambda artifact.
- [x] **Test:** Write unit tests for the Lambda handler.
- [x] **Test:** Write integration test to execute the Lambda handler in-JVM and assert S3 object creation.
- [x] **Test:** Write integration test for DLQ scenario.
- [x] **Test:** Write integration test for idempotency (double delivery).

## Phase 4: End-to-End Flow & Documentation

- [x] **Test:** Write a full end-to-end integration test: `POST` -> SQS -> Lambda -> S3 -> `GET`.
- [x] Create the `infra-terraform` module and write Terraform scripts.
- [x] Define a `lambda_artifact_path` variable in Terraform.
- [x] Create a `docker/docker-compose.local.yml` for manual testing.
- [x] Create the `OPENAPI.yaml` specification.
- [x] Write the `README.md` with all instructions.
