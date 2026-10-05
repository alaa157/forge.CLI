# ForgeCI — Master Implementation Plan

The repository follows the phased implementation plan in `ForgeCI_ROADMAP.md`.

## Completed phases

- Phase 1 — Foundation
- Phase 2 — Authentication and Users
- Phase 3 — Organizations and Repositories
- Phase 4 — Pipeline Configuration
- **Phase 5 — Pipeline Domain Model**

## Phase 6 status

### Task 6.1 — Webhook Endpoint
- [x] public GitHub webhook endpoint
- [x] required GitHub delivery/event/signature headers

### Task 6.2 — Signature Validation
- [x] HMAC-SHA256 verification against raw request bytes
- [x] constant-time signature comparison
- [x] configurable webhook secret

### Task 6.3 — Webhook Idempotency
- [x] durable WebhookDelivery model
- [x] unique provider + delivery ID constraint
- [x] atomic delivery claim
- [x] payload SHA-256 hash
- [x] duplicate delivery protection

### Task 6.4 — Supported Events
- [x] push events
- [x] pull_request events
- [x] exact commit/branch extraction
- [x] .forgeci.yml loading at event commit
- [x] PipelineRun creation from Phase 4 configuration

Phase 6 authenticates GitHub deliveries, deduplicates them durably, loads the exact pipeline configuration for supported events, and creates immutable PipelineRun snapshots. Scheduling, worker execution, retries, cancellation orchestration, and runtime infrastructure remain later-phase work.

## Phase 5 status

### Task 5.1 — PipelineRun
- [x] explicit execution statuses
- [x] legal state transitions
- [x] optimistic locking version
- [x] lifecycle timestamps

### Task 5.2 — JobRun
- [x] explicit job statuses
- [x] legal state transitions
- [x] optimistic locking version
- [x] lifecycle timestamps

### Task 5.3 — Execution Hierarchy
- [x] PipelineRun → JobRun → StepRun persistence model
- [x] ordered step commands
- [x] foreign-key and uniqueness constraints

### Task 5.4 — Immutable Execution Snapshot
- [x] commit SHA
- [x] branch
- [x] trigger
- [x] original pipeline YAML
- [x] resolved pipeline definition
- [x] resolved DAG/job graph
- [x] ForgeCI version
- [x] snapshot copied into JobRun/StepRun records

Phase 5 establishes durable execution state and state-machine boundaries. Webhooks, scheduling, worker execution, retries across attempts, cancellation orchestration, and runtime infrastructure remain later-phase work.
