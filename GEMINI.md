# Project Overview

This project is a sample To-Do application designed to demonstrate a migration path from AWS SDK v1 to v2. The application is built with Java 21 and follows a multi-module Gradle structure.

The core functionality involves a simple To-Do service that uses SQS for queuing tasks, a Lambda function for processing them, and S3 for storage. The web layer is built with Spring Framework 5, and dependency injection is handled by Google Dagger.

The project also includes infrastructure as code using Terraform to provision the necessary AWS resources (S3, SQS, IAM, Lambda).

## Key Technologies

*   **Backend:** Java 21, Spring Framework 5
*   **Build:** Gradle (Kotlin DSL)
*   **Dependency Injection:** Google Dagger
*   **AWS:** SQS, Lambda, S3 (using AWS SDK v1)
*   **Testing:** JUnit 5, AssertJ, Testcontainers (with LocalStack)
*   **Infrastructure:** Terraform

## Project Structure

The project is organized into the following modules:

*   `core`: Contains the domain model, DTOs, and utility classes.
*   `web-app`: The Spring-based web application that exposes the REST API for managing To-Dos.
*   `lambda`: An AWS Lambda function that consumes messages from SQS and writes To-Do items to S3.
*   `infra-terraform`: Terraform scripts for provisioning the AWS infrastructure.

## Building and Running

### Prerequisites

*   Java 21
*   Docker (for running LocalStack via Testcontainers)

### Building the Application

To build the entire project and run the tests, execute the following command from the root directory:

```bash
./gradlew build
```

### Running the Application Locally

To run the web application locally, you can use the following command:

```bash
./gradlew :modules:web-app:bootRun
```

This will start the Spring application, which will be available at `http://localhost:8080`.

### Running Tests

To run all unit and integration tests, use the following command:

```bash
./gradlew test
```

The integration tests will automatically start a LocalStack container using Testcontainers to simulate the AWS environment.

## Development Conventions

*   **Code Style:** The project uses Spotless for code formatting.
*   **Testing:** The project emphasizes a strong testing culture, with both unit and integration tests. Integration tests are written using Testcontainers and LocalStack to ensure a high degree of confidence in the application's behavior.
*   **Dependency Management:** Dependencies are managed using Gradle's version catalogs.
