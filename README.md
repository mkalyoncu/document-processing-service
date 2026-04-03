# Document Processing Orchestration Service

This project implements a document processing orchestration service for documents such as invoices, contracts, and reports.

The service processes documents asynchronously through the following pipeline:

DMS Fetch → OCR → Classification → NER

Each step is simulated behind an interface and may randomly fail or take time, in order to test failure handling, retry, and state transitions.

---

## Features

- REST API for document submission and tracking
- Asynchronous pipeline execution
- Step-by-step result persistence
- Manual retry support for failed documents
- Manual cancel support for in-progress documents
- Structured JSON logging for workflow transitions
- Health check endpoint
- Swagger / OpenAPI documentation
- Flyway migration support
- Docker Compose support

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger / Springdoc OpenAPI
- Docker & Docker Compose
- Lombok

---

## Architecture Overview

The system is built around a workflow orchestration model.

### Main entities

#### 1. `DocumentWorkflow`
Represents the current (latest) state of a document in the workflow.
Each document is stored as a single row.

Examples:
- RECEIVED
- DMS_FETCHING
- OCR_PROCESSING
- FAILED
- COMPLETED

#### 2. `StepResult`
Stores the execution result of each pipeline step independently.

Each step result includes:
- `id` → Primary key
- `documentId` → Related document
- `step` → Processing step (e.g. DMS_FETCHING, OCR_PROCESSING)
- `status` → Step status (PROCESSING, COMPLETED, FAILED)
- `startedAt` → Step start time
- `completedAt` → Step completion time
- `durationMs` → Execution duration in milliseconds
- `resultJson` → Serialized result of the step
- `errorMessage` → Error message if step failed

Each step result is uniquely identified by (documentId, step).
This design ensures recoverability and allows retry from the failed step without re-executing already completed steps.

---

## Why PostgreSQL?

PostgreSQL was chosen because:

- it provides strong consistency for workflow state transitions
- it supports reliable transactional updates
- it works well for filtering by workflow status
- it allows flexible storage of step results as JSON strings
- it is easy to containerize with Docker

---

## Workflow

A document moves through the following states:

RECEIVED → DMS_FETCHING → OCR_PROCESSING → CLASSIFYING → NER_PROCESSING → COMPLETED

Failure states:
- DMS_FETCH_FAILED
- OCR_FAILED
- CLASSIFICATION_FAILED
- NER_FAILED

Any failure transitions the workflow into:
- FAILED

Retry is manual and resumes from the failed step.

Cancel is manual and transitions the document into:
- FAILED with reason `CANCELLED_BY_USER`
Cancel is treated as a terminal state and does not trigger retry.
---

## Simulated External Services

The following external services are simulated:

- DMS Client
- OCR Service
- Classifier Service
- NER Service

Each simulated service:
- runs with random delay
- may fail with configurable failure rate
- returns realistic sample output

---

## DMS Configuration

The application uses externalized configuration for DMS integration:

- `DMS_BASE_URL`
- `DMS_API_KEY`

These are provided via environment variables and injected into the application.

For this assignment, values are set via Docker and no secret management is required.

---

### Design Notes
- Step execution order is controlled via Spring @Order annotation.
- The system follows a **single source of truth** approach:
    - `DocumentWorkflow` → current state
    - `StepResult` → step-level history

- This separation allows:
    - independent step persistence
    - retry from failed step
    - detailed observability

---

## How to Run

### Prerequisites

- Docker
- Docker Compose

### Run with Docker Compose

```bash
docker compose up --build
```

Application will be available at:

- API: `http://localhost:8080`

---

## API Endpoints

### Submit a document

```
curl -X POST http://localhost:8080/api/v1/documents
```

Request body:

```
{
  "docRef": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
}
```

---

### Get document details

```
GET /api/v1/documents/{documentId}
```

---

### Get a specific step result

```
curl http://localhost:8080/api/v1/documents/{documentId}/steps/{stepName}
```

Supported step names:
- `dms-fetch`
- `ocr`
- `classification`
- `ner`

---

### Retry a failed document

```
curl -X POST http://localhost:8080/api/v1/documents/{documentId}/retry
```

---

### Cancel a processing document

```
curl -X POST http://localhost:8080/api/v1/documents/{documentId}/cancel
```
---

### List documents

```
curl http://localhost:8080/api/v1/documents
```

### 6. Filter documents by status

```
curl "http://localhost:8080/api/v1/documents?status={status}"
```

### Supported Processing Statuses
- RECEIVED
- DMS_FETCHING
- DMS_FETCH_COMPLETED
- OCR_PROCESSING
- OCR_COMPLETED
- CLASSIFYING
- CLASSIFICATION_COMPLETED
- NER_PROCESSING
- NER_COMPLETED
- COMPLETED

### Supported Failure Statuses
- FAILED
- DMS_FETCH_FAILED
- OCR_FAILED
- CLASSIFICATION_FAILED
- NER_FAILED

## Health Check

```
GET http://localhost:8080/actuator/health
```

## Swagger

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

## Notes

- The pipeline is asynchronous, so submission returns `202 Accepted`.
- No real OCR / NLP / DMS integration is used.
- All external dependencies are simulated.
- Large documents above 20 MB are rejected during DMS fetch.
- Large payloads are simulated through metadata instead of storing huge hardcoded content.
- Structured logs are emitted for workflow transitions and failures.
- Failure-specific statuses (e.g. NER_FAILED) are derived from failedAtStep
  while the overall workflow state is stored in currentStep.
