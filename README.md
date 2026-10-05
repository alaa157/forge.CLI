# ForgeCI

Self-hosted, production-style CI/CD and test-intelligence platform.

## Current implementation

- **Phase 1 — Foundation:** monorepo, Spring Boot backend, Next.js frontend, local infrastructure, Flyway, and health endpoints.
- **Phase 2 — Authentication:** users, registration/login, refresh-token rotation, and authorization foundations.
- **Phase 3 — Organizations and repositories:** multi-tenant organizations, repository connections, and GitHub integration foundations.
- **Phase 4 — Pipeline Configuration:** typed .forgeci.yml parsing, safe YAML handling, schema documentation, validation, and deterministic DAG construction.
- **Phase 5 — Pipeline Domain Model:** durable PipelineRun → JobRun → StepRun execution state machines and immutable execution snapshots.
- **Phase 6 — GitHub Webhooks:** signed push/pull_request ingestion, delivery idempotency, exact-commit pipeline loading, and automatic PipelineRun creation.
- **Phase 7 — Scheduling and Durable Dispatch:** durable dispatch records, bounded scheduling, RabbitMQ delivery, retryable pending publishes, and at-least-once dispatch semantics.
- **Phase 8 — Worker Execution:** RabbitMQ workers, deterministic job/step execution, and isolated Docker containers with network and filesystem restrictions.
- **Phase 9 — RabbitMQ Messaging:** durable job/event/log/notification queues, dead-letter queues, explicit job message contracts, and PostgreSQL-backed consumer idempotency.

## Prerequisites

Git, GNU Make, Java 21, Maven 3.9+, Node.js 20+, npm 10+, Docker Engine + Compose v2.

## Commands

```bash
cp .env.example .env
make help
make infra-up
make backend-test
make frontend-check
```

## Pipeline configuration

Pipeline definitions live in .forgeci.yml. See docs/api/pipeline-configuration.md and pipeline-schema/schema.json.

Phase 9 establishes RabbitMQ as an explicit transport boundary while PostgreSQL remains the source of truth for durable execution state. The existing Phase 8 worker path remains intact; the new messaging topology is introduced incrementally for subsequent execution, logs, events, and notifications.