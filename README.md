# ForgeCI

Self-hosted, production-style CI/CD and test-intelligence platform.

## Current implementation

- Phase 1 — Foundation
- Phase 2 — Authentication
- Phase 3 — Organizations and repositories
- Phase 4 — Pipeline Configuration
- Phase 5 — Pipeline Domain Model
- Phase 6 — GitHub Webhooks
- Phase 7 — Scheduling and Durable Dispatch
- Phase 8 — Worker Execution
- Phase 9 — RabbitMQ Messaging
- Phase 10 — Docker Execution: exact-commit checkout, disposable workspaces, executor abstraction, resource limits, and container hardening.
- Phase 11 — Logs: chunked durable stdout/stderr storage, bounded log retention, HTTP retrieval, and live STOMP/WebSocket delivery.
- Phase 12 — Artifacts: durable ZIP artifact collection, local/MinIO S3-compatible storage, metadata, and download APIs.
- Phase 13 — Test Intelligence: secure JUnit ingestion, canonical test identities, execution history, and failure/duration analytics.

## Prerequisites

Git, GNU Make, Java 21, Maven 3.9+, Node.js 20+, npm 10+, Docker Engine + Compose v2.

## Commands

cp .env.example .env
make help
make infra-up
make backend-test
make frontend-check

See docs/api/docker-execution.md and docs/api/logs.md for the Phase 10/11 execution and log boundaries.

- Phase 14 — Flaky Test Detection: bounded flakiness scoring, classifications, historical analysis, and dashboard data.
- Phase 15 — REST API: formal run, job, test, flaky-test, artifact, and execution resource endpoints.
