# ForgeCI

Self-hosted, production-style CI/CD and test-intelligence platform.

## Current implementation

- **Phase 1 — Foundation:** monorepo, Spring Boot backend, Next.js frontend, local infrastructure, Flyway, and health endpoints.
- **Phase 2 — Authentication:** users, registration/login, refresh-token rotation, and authorization foundations.
- **Phase 3 — Organizations and repositories:** multi-tenant organizations, repository connections, and GitHub integration foundations.
- **Phase 4 — Pipeline Configuration:** typed `.forgeci.yml` parsing, safe YAML handling, schema documentation, validation, and deterministic DAG construction.
- **Phase 5 — Pipeline Domain Model:** durable PipelineRun → JobRun → StepRun execution state machines and immutable execution snapshots.
- **Phase 6 — GitHub Webhooks:** signed push/pull_request ingestion, delivery idempotency, exact-commit pipeline loading, and automatic PipelineRun creation.
- **Phase 7 — Scheduling and Durable Dispatch:** durable dispatch records, bounded scheduling, RabbitMQ delivery, retryable pending publishes, and at-least-once dispatch semantics.
- **Phase 8 — Worker Execution:** RabbitMQ workers, deterministic job/step execution, and isolated Docker containers with network and filesystem restrictions.

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

Pipeline definitions live in `.forgeci.yml`. See `docs/api/pipeline-configuration.md` and `pipeline-schema/schema.json`.

Phase 8 consumes queued PipelineRuns from RabbitMQ and executes their jobs inside isolated Docker containers. Repository checkout/workspaces, artifacts, persistent live logs, execution-attempt retries, and cancellation orchestration remain later phases.
