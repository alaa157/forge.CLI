# ForgeCI — Master Implementation Plan

The repository follows the phased implementation plan in `ForgeCI_ROADMAP.md`.

## Completed phases

- Phase 1 — Foundation
- Phase 2 — Authentication and Users
- Phase 3 — Organizations and Repositories
- Phase 4 — Pipeline Configuration
- Phase 5 — Pipeline Domain Model
- Phase 6 — GitHub Webhooks
- **Phase 7 — Scheduling and Durable Dispatch**

## Phase 7 status

### Task 7.1 — Durable Dispatch Record
- [x] one dispatch per PipelineRun
- [x] durable pending/published state
- [x] publish attempt tracking
- [x] last-error tracking

### Task 7.2 — Scheduler
- [x] detect CREATED PipelineRuns
- [x] atomically claim dispatch records
- [x] transition PipelineRun to QUEUED
- [x] bounded batch size

### Task 7.3 — RabbitMQ Dispatch
- [x] durable exchange
- [x] durable queue
- [x] routing key
- [x] JSON dispatch contract
- [x] automatic retry of pending publishes

### Task 7.4 — Delivery Semantics
- [x] at-least-once publication contract
- [x] explicit downstream idempotency boundary

Phase 7 establishes the durable handoff from persisted pipeline state to the execution queue. Worker execution, containers, logs, retries across attempts, and cancellation orchestration remain later phases.
