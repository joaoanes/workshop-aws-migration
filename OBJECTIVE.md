You are an expert Java/Spring architect. Build a runnable sample that implements the To-Do flow with **AWS SDK v1** today, making it easy to migrate to v2 later, but **do not** segregate SDK code into a “v1 adapters” module or folder. Use neutral class/package names and place AWS calls in the regular code of each module. The domain is a simple To-Do service using SQS + Lambda + S3.

## Objectives

* Multi-module Gradle project targeting **Java 21**; runs on Linux/Mac/Windows.
* End-to-end flow (using **AWS SDK v1**):

  * **POST /api/todos** accepts `{ text }` → enqueue JSON to **SQS**.
  * **Lambda** (Java 21) subscribed to SQS consumes messages and writes each To-Do JSON to **S3** at `todos/{id}.json`.
  * **GET /api/todos** lists the S3 prefix and returns aggregated To-Dos.
* Web: **Spring Framework 5** (use Spring only for HTTP; no Spring DI).
* DI: **Google Dagger** for services/config wiring.
* Tests:

  * **Unit tests** for domain/services.
  * **Integration tests** with **LocalStack** via **Testcontainers**; tests programmatically create SQS/S3 in LocalStack (no Terraform in the test loop).
* Infra: Provide **minimal Terraform** only to illustrate real deployment (S3, SQS + DLQ, IAM, Lambda, event source mapping). Terraform must **reference the Lambda artifact built by Gradle** via a variable (e.g., `var.lambda_artifact_path`). Do not require Terraform for local runs/tests.
* Docs: **README only** (no MIGRATION.md). Include run/test instructions and a brief note on using the Terraform with a built artifact.

## Domain & Contracts

```
Todo { id: UUID, text: String, createdAt: Instant }
SQS message: JSON { id, text, createdAt }
S3 object key: todos/{id}.json   // contents: same JSON
```

## HTTP API

* POST /api/todos → returns created { id, text, createdAt }
* GET /api/todos → returns \[ { id, text, createdAt } ]

## Project Structure (no “v1” module/folder; neutral naming)

```
aws-sdk-migration-sample/
  settings.gradle.kts
  build.gradle.kts                  // version catalogs, Java toolchain 21, quality
  buildSrc/                         // shared conventions (Spotless, JUnit 5, AssertJ)

  modules/
    core/                           // domain, DTOs, small utility helpers
      src/main/java/...
      src/test/java/...
    web-app/                        // Spring 5 web app (controllers, services)
      src/main/java/...             // uses AWS SDK v1 directly in service classes
      src/test/java/...
    lambda/                         // SQS→S3 consumer (AWS Lambda Java 21)
      src/main/java/...             // uses AWS SDK v1 directly in handler/service
      src/test/java/...
    infra-terraform/                // minimal TF for real deployment (optional)
      variables.tf                  // includes lambda_artifact_path, region, names
      lambda.tf                     // uses var.lambda_artifact_path for deployment
      sqs.tf s3.tf iam.tf esm.tf outputs.tf

  docker/docker-compose.local.yml   // LocalStack for manual tinkering if desired
  README.md
  OPENAPI.yaml
```

## AWS SDK Usage (in normal code, neutral names)

* Place AWS client creation and calls inside standard service classes within `web-app` and `lambda` modules (e.g., `AwsQueueService`, `AwsObjectStore`, `TodoService`), but **do not** label them “v1”.
* Provide the clients via Dagger **within the same module** (e.g., a `ClientsModule` that builds `AmazonSQS` and `AmazonS3` with timeouts/retries/endpoint override for LocalStack).
* Keep class/package names neutral so a future v2 migration can swap types in place without moving files.

## Dagger Composition

* Dagger component per module:

  * `web-app`: provides `AmazonSQS`, `AmazonS3`, `ObjectMapper`, config, and `TodoService`.
  * `lambda`: provides same plus the Lambda handler dependencies.
* Spring obtains a Dagger-built `TodoService` via a small bridge.

## Testing

* **Unit**: controllers/handlers (with fakes), JSON serialization, basic services.
* **Integration** (Testcontainers LocalStack):

  * Bring up S3 + SQS.
  * Create bucket/queues programmatically in test setup.
  * POST → assert SQS message exists.
  * Execute Lambda handler in-JVM (acceptable) → assert S3 object `todos/{id}.json` content.
  * GET → assert aggregated To-Dos match S3 contents.
  * DLQ scenario (force a failure once) and idempotency (double delivery → single S3 object by key).

## Build & Quality

* Gradle Kotlin DSL; Java toolchain 21.
* Lambda packaged as Shadow/Fat JAR.
* Spotless and Error Prone or Checkstyle.
* JUnit 5, AssertJ, Testcontainers; Jackson for JSON.
* OpenAPI spec committed for the REST API.

## Terraform (optional, minimal)

* Providers: `aws` (for real), and optionally a `localstack` config example (commented).
* Resources: S3 bucket, SQS queue + DLQ, IAM role/policies, Lambda function, event source mapping.
* Var: `lambda_artifact_path` pointing to the Gradle-built JAR under `modules/lambda/build/libs/...`.
* Outputs: bucket name, queue URL/ARN, DLQ ARN, Lambda name.
* README section explains how to:

  * build the Lambda JAR
  * set `-var lambda_artifact_path=...`
  * `init/plan/apply` (deployment is not required for tests)

## Acceptance Criteria

* Fresh checkout → single command runs unit + integration tests against LocalStack (no Terraform), all green.
* Web app (Spring 5) works locally: POST enqueues, Lambda writes to S3, GET lists To-Dos.
* All AWS SDK calls live in regular code paths with neutral names (no “v1 adapters” folder).
* Tests exist and cover most flows, both unit and integration tests. These integration tests should use testcontainers + localstack.
* Terraform exists and can deploy using the built Lambda artifact path, but is not part of the developer test loop.
* README is complete; **no MIGRATION.md** present.
