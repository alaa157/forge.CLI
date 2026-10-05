# ForgeCI

**ForgeCI** is a self-hosted, production-style CI/CD and test-intelligence platform built around durable pipeline execution, isolated Docker workers, GitHub integration, test analytics, and an operator-focused web interface.

## What ForgeCI provides

- GitHub-connected repositories with signed webhook processing
- Declarative `.forgeci.yml` pipeline configuration
- Immutable pipeline/run snapshots
- Durable scheduling and RabbitMQ dispatch
- Isolated Docker-based job execution against exact Git commits
- Resource limits and container hardening
- Durable stdout/stderr logs with live WebSocket streaming
- Artifact collection with local filesystem or S3-compatible/MinIO storage
- Secure JUnit test-result ingestion
- Historical test execution analytics
- Flaky-test detection and classification
- REST APIs for runs, jobs, tests, artifacts, and test intelligence
- Retry-aware job attempts
- Persistent pipeline/job cancellation
- Next.js engineering dashboard for runs, jobs, logs, and test health

## Architecture

```text
GitHub -> signed webhooks -> ForgeCI Backend -> Scheduler -> RabbitMQ -> Docker Workers
                                      |                         |
                                      +-> PostgreSQL <----------+
                                      |                         |
                                      +-> Logs / Artifacts / Test Intelligence
                                      |
                                      +-> REST API + STOMP/WebSocket
                                                        |
                                                        v
                                                Next.js Dashboard
```

## Implementation status

### Phase 1 — Foundation
Monorepo, Spring Boot backend, Next.js frontend, local infrastructure, Flyway, and health endpoints.

### Phase 2 — Authentication
Registration, login, refresh-token rotation, and authorization foundations.

### Phase 3 — Organizations & Repositories
Multi-tenant organizations, repository connections, and GitHub integration foundations.

### Phase 4 — Pipeline Configuration
Typed pipeline definitions, secure YAML parsing, validation, dependency/DAG validation, limits, and path-traversal protection.

### Phase 5 — Pipeline Execution Domain
Immutable pipeline snapshots, pipeline/job/step runs, lifecycle state machines, persistence, optimistic locking, and deterministic materialization.

### Phase 6 — GitHub Webhooks
Signed webhook verification, idempotent delivery handling, push/pull-request triggers, exact-commit pipeline loading, and pipeline creation.

### Phase 7 — Scheduling & Durable Dispatch
Durable dispatch records, scheduled discovery, RabbitMQ publishing, retryable pending publishes, and at-least-once dispatch semantics.

### Phase 8 — Worker Execution
RabbitMQ workers, sequential job/step execution, Docker execution, job/run state transitions, and failure propagation.

### Phase 9 — Durable Messaging
Durable RabbitMQ topology, queues/DLQs, stable message contracts, processed-message claims, and duplicate-consumption protection.

### Phase 10 — Secure Docker Execution
Executor abstraction, exact-commit Git checkout, disposable workspaces, GitHub-token-safe authentication, resource limits, dropped capabilities, no-new-privileges, isolated networking, and no host Docker socket.

### Phase 11 — Durable Logs
Bounded stdout/stderr persistence, HTTP cursor/tail retrieval, and live STOMP/WebSocket delivery with HTTP recovery.

### Phase 12 — Artifacts
ZIP artifact collection with traversal protection and collection limits, durable metadata, local storage, and S3-compatible/MinIO storage with short-lived download URLs.

### Phase 13 — Test Intelligence
Secure JUnit XML parsing, canonical test identities, durable test execution history, and repository/test analytics.

### Phase 14 — Flaky Test Detection
Historical instability scoring using failure frequency, outcome inconsistency, recency behavior, and duration instability, with stable/likely-flaky/flaky/newly-flaky/persistent-failure classifications.

### Phase 15 — REST API
Run, job, artifact, test-history, flaky-test, dashboard, cancellation, and retry API surfaces.

### Phase 16 — Engineering Dashboard
Next.js application shell, engineering overview, run/job detail pages, pipeline/repository routes, flaky-test views, live job logs over WebSocket/STOMP, and HTTP recovery polling.

### Phase 17 — Retry & Cancellation
Durable job attempt identities, attempt-aware message delivery, configurable retries for infrastructure/worker failures, persistent cancellation requests, cooperative worker cancellation, and explicit termination of active Docker processes.

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Persistence | PostgreSQL, JPA/Hibernate, Flyway |
| Messaging | RabbitMQ |
| Cache / infrastructure | Redis |
| Artifacts | Local filesystem / S3-compatible MinIO |
| Execution | Docker |
| Source control | Git / GitHub |
| Frontend | Next.js |
| Live updates | STOMP / WebSocket |
| Testing | JUnit / Spring Boot tests |

## Prerequisites

Git, GNU Make, Java 21, Maven 3.9+, Node.js 20+, npm 10+, Docker Engine + Compose v2.

## Getting Started

```bash
cp .env.example .env
make help
make infra-up
make backend-test
make frontend-check
```

Configure GitHub OAuth/webhook values and artifact backend settings in `.env` before enabling GitHub-connected execution.

## Documentation

Detailed subsystem documentation is under `docs/api/`, including pipeline configuration, GitHub webhooks, dispatch, worker execution, Docker execution, logs, artifacts, test intelligence, flaky tests, and REST APIs.

The project roadmap is maintained in `docs/ROADMAP.md`.

## Project Direction

ForgeCI is intentionally being built as a production-oriented CI/CD platform rather than a simple CI demo. The implementation emphasizes durable state, idempotent message processing, immutable execution context, isolated execution, bounded resource usage, observable workers, secure artifact/test ingestion, and operationally useful test intelligence.
