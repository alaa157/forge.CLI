# ForgeCI — Master Implementation Plan

The repository follows the phased implementation plan in ForgeCI_ROADMAP.md.

## Completed phases

- Phase 1 — Foundation
- Phase 2 — Authentication and Users
- Phase 3 — Organizations and Repositories
- Phase 4 — Pipeline Configuration
- Phase 5 — Pipeline Domain Model
- Phase 6 — GitHub Webhooks
- Phase 7 — Scheduling and Durable Dispatch
- Phase 8 — Worker Execution
- **Phase 9 — RabbitMQ Messaging**

## Phase 9 status

### Task 9.1 — Queue Topology
- [x] durable jobs queue
- [x] durable events queue
- [x] durable logs queue
- [x] durable notifications queue
- [x] jobs/events/logs dead-letter queues
- [x] explicit routing keys

### Task 9.2 — Job Message Contract
- [x] stable message ID
- [x] job ID
- [x] attempt ID
- [x] repository ID
- [x] pipeline run ID
- [x] organization ID
- [x] trace ID
- [x] creation timestamp
- [x] no long-lived secrets in message payloads

### Task 9.3 — Idempotent Consumers
- [x] durable processed-message record
- [x] `(message_id, consumer)` uniqueness boundary
- [x] atomic PostgreSQL claim
- [x] Phase 8 pipeline consumer protected against duplicate delivery
- [x] stable dispatch identity across publish retries

Phase 9 adds the messaging boundary without replacing the existing Phase 7 pipeline-dispatch path. The new jobs/events/logs/notifications topology is available for subsequent execution, logging, and notification phases.