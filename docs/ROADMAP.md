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
- Phase 9 — RabbitMQ Messaging
- **Phase 10 — Docker Execution**
- **Phase 11 — Logs**
- **Phase 12 — Artifacts**
- **Phase 13 — Test Result Intelligence**

## Phase 10 status

### Task 10.1 — Executor Abstraction
- [x] JobExecutor interface
- [x] execution request/result contracts
- [x] DockerJobExecutor implementation

### Task 10.2 — Repository Checkout
- [x] exact commit SHA checkout
- [x] disposable per-job workspace
- [x] GitHub-only clone URL validation
- [x] repository credential injection without putting tokens in command arguments

### Task 10.3 — Container Execution
- [x] image
- [x] command
- [x] workspace
- [x] timeout
- [x] CPU limit
- [x] memory limit
- [x] PID limit
- [x] network policy

### Task 10.4 — Container Security
- [x] no privileged mode
- [x] no Docker socket
- [x] no host filesystem mounts
- [x] dropped Linux capabilities
- [x] no-new-privileges
- [x] read-only root filesystem
- [x] isolated temporary filesystem
- [x] PID/CPU/memory limits
- [x] execution timeout

### Task 10.5 — Command Execution
- [x] sequential commands
- [x] stop after first failed step

Phase 10 replaces the earlier direct Docker boundary with an executor abstraction and exact-commit workspace execution. Docker isolation remains an MVP boundary, not a hardened hostile-workload sandbox.

## Phase 11 status

### Task 11.1 — Worker Log Capture
- [x] stdout capture
- [x] stderr capture
- [x] sequence numbers
- [x] timestamps

### Task 11.2 — Log Storage
- [x] PostgreSQL chunk storage
- [x] 16 KiB chunk limit
- [x] 10 MiB per-job log limit
- [x] truncation by storage ceiling

### Task 11.3 — Live Logs
- [x] STOMP WebSocket endpoint
- [x] per-job log topic
- [x] live chunk publication

### Task 11.4 — Log API
- [x] GET /api/v1/jobs/{id}/logs
- [x] tail parameter
- [x] sequence cursor via after parameter

### Task 11.5 — Log Limits
- [x] maximum chunk size
- [x] maximum log size
- [x] bounded API tail
- [x] storage backpressure

Phase 11 provides durable chunked logs plus a live WebSocket/STOMP stream. Object-storage archival and richer log metadata remain future scaling work.


## Phase 12 status

- Artifact metadata persisted in PostgreSQL.
- Storage abstraction with local filesystem and S3-compatible MinIO backends.
- ZIP-based artifact collection with path-traversal and size/file-count limits.
- Artifact listing and download endpoint with short-lived S3 signed URLs.
- Configuration is controlled by `FORGECI_ARTIFACT_*` environment variables.

## Phase 13 status

- Secure JUnit XML parser with external entity/DTD protections.
- Canonical JUnit test identity: `forgeci::junit::<classname>::<testName>`.
- Durable test execution records with repository, commit, branch, job, status, duration, and failure context.
- Test result ingestion endpoint.
- Pass/failure rate, average duration, p95 duration, recent failure rate, consecutive failures, and execution count analytics.

## Phase 14–15 status

- Flaky-test scoring and classification implemented over durable Phase 13 history.
- Historical and dashboard endpoints implemented.
- Run cancellation/retry and job/test resource endpoints implemented.
- Retry uses immutable pipeline snapshots and the existing durable dispatch boundary.
