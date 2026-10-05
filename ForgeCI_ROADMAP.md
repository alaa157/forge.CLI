# ForgeCI — Master Implementation Plan

> A phased, agent-ready implementation specification for building ForgeCI: a self-hosted, production-style CI/CD and test-intelligence platform.

## 0. Product Definition

ForgeCI is a self-hosted CI/CD platform that:

1. Connects to GitHub repositories.
2. Receives repository webhooks.
3. Creates pipeline executions.
4. Reads a pipeline definition from the repository.
5. Creates jobs.
6. Schedules jobs to workers.
7. Executes jobs in isolated Docker containers.
8. Collects logs and artifacts.
9. Stores test results.
10. Shows pipeline/job history in a web dashboard.
11. Supports retries, cancellation, timeouts, and concurrency.
12. Detects flaky tests from historical executions.
13. Provides metrics and observability.
14. Eventually uses AI to explain failures.

### Core Architecture

```text
                         +----------------------+
                         |       GitHub         |
                         +----------+-----------+
                                    |
                               Webhook / API
                                    |
                                    v
+-----------------------------------------------------------+
|                    FORGECI CONTROL PLANE                  |
|                                                           |
|  +-------------+    +-------------+    +--------------+  |
|  | API Server  |--->| Scheduler   |--->| Job Queue    |  |
|  +-------------+    +-------------+    +------+-------+  |
|          |                                     |          |
|          |                                     |          |
|  +-------v------+                       +------v-------+  |
|  | PostgreSQL   |                       |    Redis     |  |
|  +--------------+                       +--------------+  |
|                                                           |
+-----------------------------------------------------------+
                                    |
                              RabbitMQ / Queue
                                    |
                    +---------------+---------------+
                    |               |               |
                    v               v               v
              +----------+    +----------+    +----------+
              | Worker 1 |    | Worker 2 |    | Worker 3 |
              |  Docker  |    |  Docker  |    |  Docker  |
              +----------+    +----------+    +----------+
                    |               |               |
                    +---------------+---------------+
                                    |
                              Results / Artifacts
                                    |
                                    v
                              +-----------+
                              | Dashboard |
                              +-----------+
```

---

# 1. Technology Decisions

Do not change these halfway through the project unless there is a strong architectural reason.

## Backend

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Validation
- Spring Security
- Spring Data JPA
- Flyway
- Spring AMQP
- Spring Data Redis
- Micrometer
- OpenTelemetry

## Frontend

- Next.js
- TypeScript
- TanStack Query
- Tailwind CSS
- Recharts
- Vitest
- Testing Library
- Playwright

## Data

- PostgreSQL
- Redis
- RabbitMQ
- MinIO initially for S3-compatible artifact storage

## Execution

- Docker
- Docker Engine API / compatible client

## Observability

- Prometheus
- Grafana
- OpenTelemetry

## Infrastructure

- Docker Compose initially
- Kubernetes later

---

# 2. Repository Structure

Create this structure before implementing business logic.

```text
forgeci/
├── README.md
├── LICENSE
├── CONTRIBUTING.md
├── SECURITY.md
├── CODE_OF_CONDUCT.md
├── Makefile
├── docker-compose.yml
├── docker-compose.dev.yml
├── .env.example
├── .gitignore
│
├── docs/
│   ├── ROADMAP.md
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── data-model.md
│   │   ├── execution-model.md
│   │   └── security.md
│   ├── architecture/
│   │   └── decisions/
│   ├── api/
│   ├── development/
│   └── operations/
│
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/forgeci/
│       │   └── resources/
│       │       └── db/migration/
│       └── test/
│
├── worker/
│   ├── go.mod
│   ├── go.sum
│   ├── cmd/
│   │   └── forge-runner/
│   ├── internal/
│   │   ├── executor/
│   │   ├── docker/
│   │   ├── queue/
│   │   ├── logs/
│   │   ├── artifacts/
│   │   └── github/
│   └── tests/
│
├── frontend/
│   ├── package.json
│   ├── next.config.ts
│   └── src/
│
├── pipeline-schema/
│   ├── schema.json
│   └── examples/
│
├── infrastructure/
│   ├── docker/
│   ├── prometheus/
│   ├── grafana/
│   ├── rabbitmq/
│   └── minio/
│
├── examples/
│   ├── node/
│   ├── python/
│   └── java/
│
└── .github/
    └── workflows/
```

---

# 3. Engineering Rules for the Coding Agent

The coding agent must follow these rules throughout the project.

## Rule 1 — Work one task at a time

Never implement multiple future phases just because they seem easy.

For each task:

1. Inspect the existing repository.
2. Identify affected modules.
3. Explain the intended change internally.
4. Implement the task.
5. Add/update tests.
6. Run relevant tests.
7. Run the full test suite when practical.
8. Run formatting/linting.
9. Review the diff.
10. Update documentation.
11. Stop.

## Rule 2 — Never silently change architecture

If implementation requires an architectural change:

1. Document the reason.
2. Add an ADR under `docs/architecture/decisions/`.
3. Update affected documentation.
4. Then implement.

## Rule 3 — Database is the source of truth

RabbitMQ, Redis, WebSockets, and caches must not become the authoritative source for pipeline/job state.

PostgreSQL owns durable state.

## Rule 4 — Assume at-least-once delivery

Every event/message handler must be safe to execute more than once.

## Rule 5 — Every state transition must be explicit

Do not scatter arbitrary status changes throughout the code.

Use domain-level transition methods and validate legal transitions.

## Rule 6 — No secrets in logs

Never log:

- passwords
- JWTs
- refresh tokens
- GitHub tokens
- webhook secrets
- repository secrets
- encryption keys

## Rule 7 — Test failure scenarios

Do not only test the happy path.

For important workflows test:

- duplicate requests
- retries
- crashes
- timeouts
- worker disappearance
- invalid input
- concurrent operations
- partial failure

---

# 4. Definition of Done

A task is not complete until applicable items below are satisfied.

- [ ] Implementation complete.
- [ ] Unit tests added.
- [ ] Integration tests added when external infrastructure is involved.
- [ ] Error handling implemented.
- [ ] Validation implemented.
- [ ] Security implications reviewed.
- [ ] Logging added where useful.
- [ ] Metrics added where useful.
- [ ] API documentation updated.
- [ ] Architecture documentation updated if needed.
- [ ] Formatting passes.
- [ ] Linting passes.
- [ ] Tests pass.
- [ ] No unrelated files changed.
- [ ] No secrets added.
- [ ] No TODOs left for behavior required by the task.

---

# PHASE 1 — Foundation

## Goal

Create a clean monorepo that builds, tests, and runs locally.

## Task 1.1 — Initialize Repository

- [ ] Initialize Git repository.
- [ ] Create root directory structure.
- [ ] Add README.
- [ ] Add LICENSE.
- [ ] Add CONTRIBUTING.md.
- [ ] Add SECURITY.md.
- [ ] Add CODE_OF_CONDUCT.md.
- [ ] Add `.gitignore`.
- [ ] Add `.env.example`.
- [ ] Add Makefile.
- [ ] Document local prerequisites.

### Acceptance Criteria

- Repository initializes cleanly.
- No secrets are committed.
- README explains the project.
- `.env.example` documents every environment variable.
- `make help` or equivalent lists available development commands.

---

## Task 1.2 — Bootstrap Backend

- [ ] Create Spring Boot application.
- [ ] Configure Java 21.
- [ ] Configure Maven.
- [ ] Add required Spring dependencies.
- [ ] Create package structure:
  - [ ] `api`
  - [ ] `application`
  - [ ] `domain`
  - [ ] `infrastructure`
  - [ ] `config`
- [ ] Add application configuration profiles:
  - [ ] `local`
  - [ ] `test`
  - [ ] `production`

### Acceptance Criteria

- Application starts.
- Context loads.
- Tests run.
- No database is required for a basic application-context test.

---

## Task 1.3 — Bootstrap Frontend

- [ ] Create Next.js application.
- [ ] Configure TypeScript.
- [ ] Configure Tailwind.
- [ ] Configure TanStack Query.
- [ ] Create application shell.
- [ ] Add basic error boundary.
- [ ] Add loading UI.
- [ ] Add not-found UI.

### Acceptance Criteria

- Frontend starts locally.
- Production build succeeds.
- Type checking succeeds.

---

## Task 1.4 — Docker Compose

Create local services:

- [ ] PostgreSQL.
- [ ] Redis.
- [ ] RabbitMQ.
- [ ] MinIO.
- [ ] Prometheus.
- [ ] Grafana.

Configure:

- [ ] Persistent development volumes.
- [ ] Health checks.
- [ ] Explicit service dependencies.
- [ ] Local-only credentials from environment variables.

### Acceptance Criteria

`docker compose up` starts the required infrastructure.

---

## Task 1.5 — Database Migration System

- [ ] Configure Flyway.
- [ ] Create `V1__initial_schema.sql`.
- [ ] Configure migration validation.
- [ ] Add migration test.

Do not put all future schema changes into V1.

### Acceptance Criteria

- Fresh database migrates successfully.
- Existing database validates successfully.
- Migration failures fail application startup where appropriate.

---

## Task 1.6 — Health Endpoints

Implement:

```http
GET /api/v1/health
GET /actuator/health
GET /actuator/prometheus
```

Example response:

```json
{
  "status": "UP",
  "version": "0.1.0"
}
```

Add:

- [ ] Liveness.
- [ ] Readiness.
- [ ] Database health.
- [ ] Redis health.
- [ ] RabbitMQ health.

---

## Task 1.7 — Global Error Handling

Use RFC 7807 / Spring `ProblemDetail`.

Example:

```json
{
  "type": "https://forgeci.dev/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/v1/repositories",
  "requestId": "..."
}
```

Handle:

- [ ] validation errors
- [ ] authentication errors
- [ ] authorization errors
- [ ] resource-not-found errors
- [ ] conflict errors
- [ ] domain errors
- [ ] unexpected errors

Never expose stack traces to clients.

---

## Task 1.8 — Structured Logging

Every relevant log event should support:

- [ ] timestamp
- [ ] level
- [ ] service
- [ ] request ID
- [ ] trace ID
- [ ] user ID when available
- [ ] organization ID
- [ ] repository ID
- [ ] pipeline ID
- [ ] job ID
- [ ] message

Never log credentials or secrets.

---

# PHASE 2 — Authentication and Users

## Goal

Build identity before repositories and pipelines.

---

## Task 2.1 — User Model

Create:

```text
User
```

Fields:

```text
id UUID
email
password_hash
display_name
status
created_at
updated_at
last_login_at
```

Statuses:

```text
ACTIVE
SUSPENDED
DELETED
```

Add:

- [ ] database constraints
- [ ] unique email index
- [ ] audit timestamps

---

## Task 2.2 — Registration

Endpoint:

```http
POST /api/v1/auth/register
```

Requirements:

- [ ] Normalize email.
- [ ] Validate email.
- [ ] Validate password strength.
- [ ] Hash password using Argon2id.
- [ ] Prevent duplicate accounts.
- [ ] Never return password hash.
- [ ] Add rate limiting later.

Tests:

- [ ] valid registration
- [ ] duplicate email
- [ ] malformed email
- [ ] weak password
- [ ] password never appears in response

---

## Task 2.3 — Login

Endpoint:

```http
POST /api/v1/auth/login
```

Return:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "expiresIn": 900
}
```

Access token:

- [ ] Short lived.
- [ ] Contains minimal claims.
- [ ] Includes user ID.
- [ ] Includes roles.

Refresh token:

- [ ] Long lived.
- [ ] Stored securely.
- [ ] Rotated.

---

## Task 2.4 — Refresh Token Rotation

Flow:

```text
old refresh token
        |
        v
validate
        |
        v
invalidate old token
        |
        v
issue new token
```

Detect token reuse.

If reuse is detected:

- [ ] Revoke token family.
- [ ] Log security event.
- [ ] Require reauthentication.

---

## Task 2.5 — Authorization

Initial roles:

```text
USER
ADMIN
```

Create authorization abstraction so later roles can be added without rewriting controllers.

---

# PHASE 3 — Organizations and Repositories

## Goal

Support multi-tenant repository management.

---

## Task 3.1 — Organization

Entities:

```text
Organization
OrganizationMember
```

Roles:

```text
OWNER
ADMIN
MEMBER
```

Relationship:

```text
User
 |
 +-- OrganizationMember
          |
          +-- Organization
```

---

## Task 3.2 — Repository

Fields:

```text
id
organization_id
provider
external_id
name
full_name
clone_url
default_branch
private
created_at
updated_at
```

Provider:

```text
GITHUB
```

Design it so additional providers can be added later.

---

## Task 3.3 — GitHub Connection

Implement GitHub OAuth/App connection.

Requirements:

- [ ] Encrypt provider credentials at rest.
- [ ] Never return raw provider tokens.
- [ ] Record connection metadata.
- [ ] Support disconnect.
- [ ] Handle expired/revoked credentials.

---

## Task 3.4 — Repository Synchronization

Support:

- [ ] list repositories
- [ ] connect repository
- [ ] disconnect repository
- [ ] refresh metadata

---

# PHASE 4 — Pipeline Configuration

## Goal

A repository can contain `.forgeci.yml`.

Example:

```yaml
version: 1

pipeline:
  name: backend

  triggers:
    - push
    - pull_request

  jobs:
    build:
      image: maven:3.9-eclipse-temurin-21
      commands:
        - mvn clean package

    test:
      image: maven:3.9-eclipse-temurin-21
      depends_on:
        - build
      commands:
        - mvn test
```

---

## Task 4.1 — Pipeline Schema

Required:

```text
version
pipeline
pipeline.name
pipeline.jobs
```

Job:

```text
name
image
commands
```

Optional:

```text
depends_on
environment
timeout
retries
artifacts
cache
working_directory
```

---

## Task 4.2 — YAML Parser

Pipeline loading flow:

```text
.forgeci.yml
    |
    v
YAML parser
    |
    v
DTO
    |
    v
Schema validation
    |
    v
Domain PipelineDefinition
```

Never pass raw YAML objects directly into business logic.

Use safe YAML parsing.

Reject custom/unsafe YAML tags.

---

## Task 4.3 — Pipeline Validation

Reject:

- [ ] duplicate jobs
- [ ] missing job image
- [ ] empty commands
- [ ] missing dependencies
- [ ] dependency cycles
- [ ] invalid timeout
- [ ] invalid retry count
- [ ] malformed environment values
- [ ] invalid artifact paths
- [ ] oversized configuration
- [ ] excessive job count

---

## Task 4.4 — DAG Engine

Convert jobs into a directed acyclic graph.

Example:

```text
        build
       /     \
      v       v
    test     lint
      |
      v
 integration
```

Detect cycles:

```text
A -> B
B -> C
C -> A
```

Return a clear configuration error.

---

# PHASE 5 — Pipeline Domain Model

## Goal

Create explicit domain state machines.

---

## Task 5.1 — PipelineRun

States:

```text
CREATED
QUEUED
RUNNING
SUCCEEDED
FAILED
CANCELLED
TIMED_OUT
```

Legal transitions:

```text
CREATED -> QUEUED
QUEUED -> RUNNING
QUEUED -> CANCELLED

RUNNING -> SUCCEEDED
RUNNING -> FAILED
RUNNING -> CANCELLED
RUNNING -> TIMED_OUT
```

Invalid transitions must fail.

---

## Task 5.2 — JobRun

States:

```text
PENDING
QUEUED
RUNNING
SUCCEEDED
FAILED
CANCELLED
TIMED_OUT
SKIPPED
```

---

## Task 5.3 — Execution Hierarchy

```text
PipelineRun
    |
    +-- JobRun
          |
          +-- StepRun
          +-- StepRun
```

---

## Task 5.4 — Immutable Execution Snapshot

When a run starts, persist:

- [ ] commit SHA
- [ ] branch
- [ ] trigger
- [ ] pipeline YAML
- [ ] resolved pipeline definition
- [ ] job graph
- [ ] ForgeCI version
- [ ] relevant configuration

A run must remain reproducible even if the repository changes later.

---

# PHASE 6 — GitHub Webhooks

## Goal

A Git push or pull request automatically creates a pipeline.

---

## Task 6.1 — Webhook Endpoint

```http
POST /api/v1/webhooks/github
```

Read:

```text
X-GitHub-Event
X-GitHub-Delivery
X-Hub-Signature-256
```

---

## Task 6.2 — Signature Validation

Compute:

```text
HMAC-SHA256(payload, webhook_secret)
```

Use constant-time comparison.

Reject invalid signatures.

---
## Task 6.3 — Webhook Idempotency

Create:

```text
WebhookDelivery
```

Fields:

```text
delivery_id
provider
event_type
received_at
processed_at
status
payload_hash
```

Unique:

```text
(provider, delivery_id)
```

Duplicate webhook deliveries must not create duplicate pipeline runs.

---

## Task 6.4 — Supported Events

Initially:

```text
push
pull_request
```

Push flow:

```text
GitHub
  |
  v
webhook
  |
  v
find repository
  |
  v
load .forgeci.yml
  |
  v
create PipelineRun
```

---

# PHASE 7 — Scheduler

## Goal

Create the system that decides which jobs are ready to run.

---

## Task 7.1 — Scheduler Service

Responsibilities:

- [ ] Find queued jobs.
- [ ] Validate dependencies.
- [ ] Check concurrency limits.
- [ ] Find eligible workers.
- [ ] Dispatch jobs.
- [ ] Record dispatch state.
- [ ] Recover stale jobs.

---

## Task 7.2 — Dependency Resolution

A job becomes runnable when:

```text
all dependencies == SUCCEEDED
```

If a required dependency fails:

```text
job -> SKIPPED
```

---

## Task 7.3 — Scheduling Loop

Initial implementation can poll PostgreSQL.

Configurable interval:

```text
scheduler.interval=500ms
```

Avoid busy loops.

---

## Task 7.4 — Queue Ordering

Initial strategy:

```text
priority DESC
created_at ASC
```

Priority:

```text
HIGH
NORMAL
LOW
```

---

## Task 7.5 — Concurrency Limits

Support:

```text
global concurrency
organization concurrency
repository concurrency
```

Example:

```text
global = 20
organization = 5
repository = 2
```

---

# PHASE 8 — Worker System

## Goal

Create independently running ForgeCI workers.

The worker should be implemented as a separate Go service.

---

## Task 8.1 — Worker Registration

Endpoint:

```http
POST /internal/workers/register
```

Information:

```text
worker_id
hostname
version
capabilities
max_concurrency
```

---

## Task 8.2 — Worker Heartbeat

Worker sends heartbeat every 10 seconds.

Server considers worker unhealthy after approximately 30 seconds.

---

## Task 8.3 — Worker States

```text
STARTING
READY
BUSY
DRAINING
OFFLINE
```

---

## Task 8.4 — Job Lease

When worker receives job:

```text
QUEUED
   |
   v
lease acquired
   |
   v
RUNNING
```

Lease contains:

```text
lease_id
worker_id
expires_at
```

---

## Task 8.5 — Lease Renewal

Worker renews lease periodically.

If worker dies:

```text
lease expires
    |
    v
scheduler detects stale lease
    |
    v
job requeued
```

Use fencing/lease tokens so an old worker cannot overwrite a newer attempt.

---

# PHASE 9 — RabbitMQ Messaging

## Goal

Decouple scheduler and workers.

---

## Task 9.1 — Queue Topology

Create queues:

```text
forgeci.jobs
forgeci.events
forgeci.logs
forgeci.notifications
```

Dead-letter queues:

```text
forgeci.jobs.dlq
forgeci.events.dlq
forgeci.logs.dlq
```

---

## Task 9.2 — Job Message Contract

Example:

```json
{
  "messageId": "uuid",
  "jobId": "uuid",
  "attemptId": "uuid",
  "repositoryId": "uuid",
  "pipelineRunId": "uuid",
  "organizationId": "uuid",
  "traceId": "uuid",
  "createdAt": "timestamp"
}
```

Never place long-lived secrets directly into queue messages.

---

## Task 9.3 — Idempotent Consumers

Every consumer must tolerate duplicate messages.

Use:

```text
message_id
```

and/or:

```text
job_id + attempt_id
```

for deduplication.

---

# PHASE 10 — Docker Execution

## Goal

Actually execute CI commands.

---

## Task 10.1 — Executor Abstraction

Create:

```java
interface JobExecutor {
    ExecutionResult execute(JobExecutionRequest request);
}
```

Implement:

```text
DockerJobExecutor
```

Future implementation:

```text
KubernetesJobExecutor
```

---

## Task 10.2 — Repository Checkout

Worker receives:

```text
repository URL
commit SHA
```

Clone the exact commit.

Never blindly execute the current default branch.

---

## Task 10.3 — Container Execution

Container receives:

```text
image
commands
environment
working_directory
timeout
CPU limit
memory limit
network policy
```

---

## Task 10.4 — Container Security

Do not run arbitrary CI workloads with:

```text
--privileged
```

Do not mount:

```text
/var/run/docker.sock
```

Do not expose host filesystem.

Use where supported:

- [x] non-root user
- [x] dropped Linux capabilities
- [x] `no-new-privileges`
- [x] read-only root filesystem where practical
- [x] isolated temporary workspace
- [x] PID limits
- [x] CPU limits
- [x] memory limits
- [x] execution timeout
- [x] network disabled by default

Important:

> Docker isolation for the MVP is not a hardened multi-tenant sandbox. For genuinely hostile untrusted workloads, later use stronger isolation such as isolated VMs, gVisor, Firecracker, or equivalent infrastructure.

---

## Task 10.5 — Command Execution

Execute commands sequentially.

Example:

```text
npm ci
npm test
npm run build
```

If command 2 fails:

```text
stop
```

unless explicitly configured otherwise.

---

# PHASE 11 — Logs

## Goal

Provide live CI logs.

---

## Task 11.1 — Worker Log Capture

Capture:

```text
stdout
stderr
```

Each log chunk should include:

```text
timestamp
stream
sequence
content
```

---

## Task 11.2 — Log Storage

Do not store entire logs in one PostgreSQL row.

Use chunking.

Potential model:

```text
LogChunk
  job_attempt_id
  sequence
  content
  created_at
```

For larger scale, move durable logs to object storage.

---

## Task 11.3 — Live Logs

Flow:

```text
Docker
  |
  v
Worker
  |
  v
RabbitMQ / Redis
  |
  v
API
  |
  v
WebSocket
  |
  v
Browser
```

---

## Task 11.4 — Log API

```http
GET /api/v1/jobs/{id}/logs
```

Support:

```text
tail
cursor
since
```

Example:

```text
/jobs/{id}/logs?tail=500
```

---

## Task 11.5 — Log Limits

Implement:

- [x] maximum log chunk size
- [x] maximum log size
- [x] truncation indicator
- [x] backpressure
- [x] dropped-chunk metrics

---

# PHASE 12 — Artifacts

## Goal

Allow jobs to upload build outputs and reports.

Example:

```yaml
artifacts:
  paths:
    - target/*.jar
    - reports/**
```

---

## Task 12.1 — Artifact Collection

After job:

```text
workspace
   |
   v
match configured paths
   |
   v
compress
   |
   v
upload
   |
   v
persist metadata
```

---

## Task 12.2 — Artifact Storage Abstraction

Create:

```java
interface ArtifactStore
```

Implement:

```text
LocalArtifactStore
S3ArtifactStore
```

Use MinIO locally.

---

## Task 12.3 — Object Key Design

Example:

```text
org/{orgId}/projects/{projectId}/runs/{runId}/attempts/{attemptId}/{artifact}
```

---

## Task 12.4 — Artifact API

```http
GET /api/v1/jobs/{id}/artifacts
GET /api/v1/artifacts/{id}/download
```

Use short-lived signed URLs where appropriate.

---

# PHASE 13 — Test Result Intelligence

## Goal

Connect ForgeCI to the test-intelligence work already developed.

---

## Task 13.1 — JUnit XML Parser

Extract:

```text
suite
classname
test name
duration
status
failure
error
stdout
stderr
```

---

## Task 13.2 — Canonical Test Identity

Create deterministic identity:

```text
repository
+
framework
+
classname
+
test name
```

Example:

```text
forgeci::junit::com.example.UserTest::createsUser
```

---

## Task 13.3 — TestExecution Model

Store:

```text
test_id
job_run_id
status
duration
failure_message
commit_sha
branch
executed_at
```

Statuses:

```text
PASSED
FAILED
ERROR
SKIPPED
```

---

## Task 13.4 — Analytics

Calculate:

- [x] pass rate
- [x] failure rate
- [x] average duration
- [x] p95 duration
- [x] recent failure rate
- [x] consecutive failures
- [x] execution count

---

# PHASE 14 — Flaky Test Detection

Reuse the strongest parts of the existing `flaky-test-intelligence` project.

Do not blindly copy implementation. Define a stable service contract and migrate behavior with golden tests.

---

## Task 14.1 — Flakiness Score

Use:

- [x] failure frequency
- [x] outcome inconsistency
- [x] recency weighting
- [x] duration instability

Output:

```text
0-100
```

---

## Task 14.2 — Classification

```text
STABLE
LIKELY_FLAKY
FLAKY
NEWLY_FLAKY
PERSISTENT_FAILURE
```

---

## Task 14.3 — Historical Comparison

For each test show:

```text
last 10 runs
last 30 runs
last 100 runs
```

---

## Task 14.4 — Dashboard Data

Expose:

```text
top flaky tests
new flaky tests
tests getting worse
slowest tests
recently failed tests
```

---

# PHASE 15 — REST API

Formalize API resources.

---

## Resources

```text
/auth
/users
/organizations
/repositories
/pipelines
/runs
/jobs
/workers
/artifacts
/tests
/flaky-tests
/webhooks
```

---

## Core Endpoints

```text
POST   /auth/register
POST   /auth/login
POST   /auth/refresh

GET    /repositories
POST   /repositories/{id}/connect
DELETE /repositories/{id}/connect

GET    /pipelines
GET    /pipelines/{id}

GET    /runs
GET    /runs/{id}
POST   /runs/{id}/cancel
POST   /runs/{id}/retry

GET    /jobs/{id}
GET    /jobs/{id}/logs
GET    /jobs/{id}/artifacts

GET    /tests
GET    /tests/{id}/history

GET    /flaky-tests

POST   /webhooks/github
```

---

# PHASE 16 — Frontend

## Goal

Build a useful dashboard rather than a decorative frontend.

---

## Task 16.1 — Application Shell

Pages:

```text
/login
/dashboard
/repositories
/repositories/[id]
/pipelines
/pipelines/[id]
/runs/[id]
/jobs/[id]
/tests
/flaky-tests
/settings
```

---

## Task 16.2 — Dashboard

Show:

```text
Total pipelines
Success rate
Failure rate
Average duration
Running jobs
Queued jobs
Flaky tests
```

Charts:

```text
pipeline success over time
duration over time
failure rate
```

---

## Task 16.3 — Pipeline Page

Show:

```text
pipeline status
commit
branch
author
duration
trigger
```

Render dependency graph:

```text
       build
       /   \
      v     v
    test   lint
      |
      v
 integration
```

---

## Task 16.4 — Live Job Page

Show:

```text
RUNNING
00:04:31
```

Then terminal output:

```text
$ mvn test

[INFO] Running tests...
[INFO] Tests run: 142
...
```

Use WebSocket for live updates.

---

# PHASE 17 — Retry and Cancellation

## Task 17.1 — Cancellation

Endpoint:

```http
POST /api/v1/runs/{id}/cancel
```

Flow:

```text
API
 |
 v
cancellation requested
 |
 v
worker receives cancellation
 |
 v
terminate container
 |
 v
job -> CANCELLED
```

Do not merely change the database status while leaving the container running.

---

## Task 17.2 — Job Attempts

Track:

```text
attempt_number
```

Example:

```text
Job #42
  attempt 1 -> FAILED
  attempt 2 -> FAILED
  attempt 3 -> SUCCEEDED
```

---

## Task 17.3 — Automatic Retry

Pipeline:

```yaml
retries: 2
```

Only retry appropriate failures.

Classify:

```text
INFRASTRUCTURE_FAILURE
TEST_FAILURE
CONFIGURATION_FAILURE
TIMEOUTWORKER_FAILURE
```

Example:

```text
assertion failure -> don't retry by default
worker lost       -> retry
infrastructure failure -> retry
```

---

# PHASE 18 — Caching

Only implement after execution is stable.

---

## Task 18.1 — Cache Configuration

Example:

```yaml
cache:
  key: maven-${{ hashFiles('pom.xml') }}
  paths:
    - ~/.m2
```

---

## Task 18.2 — Cache Flow

```text
Worker
 |
 v
calculate cache key
 |
 v
cache lookup
 |
 +-- hit --> restore
 |
 v
execute job
 |
 v
save cache
```

---

## Task 18.3 — Cache Safety

Implement:

- [ ] size limits
- [ ] TTL
- [ ] namespace isolation
- [ ] repository isolation
- [ ] cache invalidation
- [ ] no cross-tenant cache leakage

---

# PHASE 19 — Secrets

## Task 19.1 — Secret Models

Create:

```text
Secret
RepositorySecret
OrganizationSecret
```

Never return secret values from API.

---

## Task 19.2 — Encryption

Use envelope encryption:

```text
master key
    |
    v
data encryption key
    |
    v
secret ciphertext
```

For local development, a dedicated environment-provided master key can be used.

Production should use a proper secret/KMS mechanism.

---

## Task 19.3 — Secret Injection

Secrets exist only during execution.

They must never appear in:

```text
database logs
API responses
frontend
pipeline YAML
audit events
```

---

## Task 19.4 — Log Masking

If:

```text
MY_TOKEN=abc123
```

appears in logs:

```text
MY_TOKEN=********
```

Implement masking before logs are sent to persistent storage.

---

# PHASE 20 — Security Hardening

## Task 20.1 — Threat Model

Document threats:

- [ ] malicious repository
- [ ] malicious pull request
- [ ] container escape
- [ ] secret exfiltration
- [ ] resource exhaustion
- [ ] SSRF
- [ ] webhook forgery
- [ ] token theft
- [ ] artifact poisoning
- [ ] dependency attacks
- [ ] log injection
- [ ] path traversal

---

## Task 20.2 — Runner Isolation

Implement:

- [ ] non-root containers
- [ ] resource limits
- [ ] ephemeral workspaces
- [ ] network restrictions
- [ ] capability dropping
- [ ] no privileged mode
- [ ] no Docker socket
- [ ] PID limits
- [ ] timeouts

---

## Task 20.3 — SSRF Protection

For any user-controlled outbound URL, validate destination.

Block internal/private destinations where appropriate:

```text
127.0.0.1
localhost
169.254.169.254
RFC1918 private ranges
container-network addresses
```

---

## Task 20.4 — Dependency Security

Add optional security tools:

```text
Trivy
OSV
npm audit
pip-audit
```

---

# PHASE 21 — Observability

## Task 21.1 — Metrics

Expose metrics such as:

```text
forgeci_pipeline_runs_total
forgeci_pipeline_success_total
forgeci_pipeline_failure_total
forgeci_job_duration_seconds
forgeci_job_queue_depth
forgeci_worker_count
forgeci_worker_busy
forgeci_job_retries_total
forgeci_webhook_total
forgeci_artifact_upload_bytes_total
forgeci_outbox_lag_seconds
forgeci_dlq_messages_total
```

---

## Task 21.2 — Distributed Tracing

Trace:

```text
Webhook
  |
  v
Pipeline creation
  |
  v
Scheduling
  |
  v
RabbitMQ
  |
  v
Worker
  |
  v
Docker
```

Use OpenTelemetry trace IDs across services.

---

## Task 21.3 — Grafana Dashboards

Create:

### Platform dashboard

- [ ] requests/sec
- [ ] latency
- [ ] errors

### Scheduler dashboard

- [ ] queue depth
- [ ] dispatch latency
- [ ] throughput

### Worker dashboard

- [ ] CPU
- [ ] memory
- [ ] worker count
- [ ] busy workers
- [ ] dead workers

### Pipeline dashboard

- [ ] success rate
- [ ] duration
- [ ] failure rate

---

# PHASE 22 — Reliability Engineering

This phase makes ForgeCI a serious systems project.

---

## Task 22.1 — Idempotency

Critical operations must tolerate retries:

- [ ] webhook processing
- [ ] job dispatch
- [ ] artifact upload
- [ ] pipeline creation
- [ ] worker registration
- [ ] event consumption

---

## Task 22.2 — Outbox Pattern

Use:

```text
database transaction
       |
       +-- domain state
       |
       +-- outbox event
```

Then:

```text
Outbox publisher
       |
       v
RabbitMQ
```

Never rely on:

```text
DB commit
then
RabbitMQ publish
```

without recovery.

---

## Task 22.3 — Dead Letter Queues

Configure DLQs.

Investigate:

- [ ] message age
- [ ] retry count
- [ ] failure reason

---

## Task 22.4 — Backpressure

Ensure that:

- [ ] queue growth doesn't crash API
- [ ] workers have bounded concurrency
- [ ] logs have bounded buffers
- [ ] artifact uploads have limits
- [ ] database connection pools are bounded

---

# PHASE 23 — GitHub PR Integration

## Goal

Show ForgeCI results directly inside pull requests.

---

## Task 23.1 — GitHub Checks

Create Check Runs.

States:

```text
queued
in_progress
completed
```

Conclusions:

```text
success
failure
cancelled
timed_out
```

---

## Task 23.2 — PR Summary

Example:

```text
ForgeCI

✓ 143 tests passed
✗ 2 tests failed
⚠ 3 flaky tests
⏱ 4m 21s
```

---

## Task 23.3 — Failure Links

Link directly to:

- [ ] failed job
- [ ] failing test
- [ ] logs
- [ ] artifacts
- [ ] historical failures

---

# PHASE 24 — AI Failure Analysis

Only implement after test history and execution data are reliable.

---

## Task 24.1 — Failure Context

Collect:

```text
failed test
stack trace
logs
commit diff
previous executions
related tests
dependency versions
environment
```

---

## Task 24.2 — Retrieval

Search historical data for:

- [ ] similar stack traces
- [ ] similar failures
- [ ] previous fixes
- [ ] related commits
- [ ] same test history

This is where the existing local RAG work can become useful.

---

## Task 24.3 — AI Output Contract

Return structured data:

```json
{
  "classification": "LIKELY_REGRESSION",
  "confidence": 0.87,
  "summary": "...",
  "likely_root_cause": "...",
  "suspected_files": [],
  "related_failures": [],
  "suggested_actions": []
}
```

AI must not directly:

- modify production systems
- merge pull requests
- deploy infrastructure
- expose secrets

---

# PHASE 25 — Kubernetes

Only begin after Docker Compose deployment is stable.

Target:

```text
                     Ingress
                        |
                +-------v-------+
                | API Deployment |
                +-------+-------+
                        |
              +---------+---------+
              |                   |
              v                   v
         Scheduler           WebSocket
              |
           RabbitMQ
              |
       +------+------+------+
       |      |      |      |
       v      v      v      v
    Worker Worker Worker Worker
```

---

## Task 25.1

Create:

- [ ] Kubernetes namespace
- [ ] ConfigMaps
- [ ] Secrets
- [ ] Deployments
- [ ] Services
- [ ] Ingress
- [ ] HorizontalPodAutoscaler
- [ ] Persistent volumes where required

---

## Task 25.2

Move workers to Kubernetes Jobs or an equivalent execution model.

Do not assume Docker-in-Docker is automatically secure.

---

# PHASE 26 — Horizontal Scaling

Test:

```text
1 worker
5 workers
20 workers
100 workers
```

Measure:

```text
jobs/minute
queue latency
API latency
scheduler throughput
database load
```

---

## Task 26.1 — Scheduler Scaling

Ensure multiple scheduler instances do not dispatch the same job.

Use:

- [ ] database locking
- [ ] leases
- [ ] idempotency keys
- [ ] fencing tokens

---

## Task 26.2 — API Scaling

Run multiple API instances.

Verify:

- [ ] stateless authentication
- [ ] WebSocket strategy
- [ ] Redis-backed ephemeral state where required
- [ ] shared artifact storage
- [ ] no local-instance source of truth

---

# PHASE 27 — Chaos Testing

Kill components while jobs are running.

Test:

- [ ] worker crash
- [ ] scheduler crash
- [ ] RabbitMQ restart
- [ ] Redis restart
- [ ] database connection failure
- [ ] API restart
- [ ] network interruption

Expected:

```text
worker dies
   |
   v
lease expires
   |
   v
job requeued
   |
   v
another worker
   |
   v
job completes
```

No job should permanently disappear.

---

# PHASE 28 — Performance Testing

Use:

```text
k6
```

Test:

- [ ] 1000 webhook events
- [ ] 100 concurrent pipelines
- [ ] 1000 queued jobs
- [ ] 100 workers
- [ ] high-volume log streaming
- [ ] large artifact uploads

Measure:

```text
p50
p95
p99
error rate
throughput
queue wait time
execution time
database utilization
```

---

# PHASE 29 — Documentation

README must eventually contain:

- [ ] What is ForgeCI?
- [ ] Why ForgeCI?
- [ ] Architecture.
- [ ] Screenshots.
- [ ] Quickstart.
- [ ] Pipeline syntax.
- [ ] Architecture diagrams.
- [ ] Database model.
- [ ] Execution model.
- [ ] Security model.
- [ ] Worker architecture.
- [ ] API documentation.
- [ ] Deployment guide.
- [ ] Observability guide.
- [ ] Performance results.
- [ ] Contributing guide.
- [ ] Roadmap.

Create Mermaid diagrams for:

- [ ] system architecture
- [ ] pipeline lifecycle
- [ ] job state machine
- [ ] worker lifecycle
- [ ] webhook flow
- [ ] scheduling flow
- [ ] artifact flow

---

# PHASE 30 — 1.0 Quality Gate

ForgeCI should not be called 1.0 until the following work.

## Core CI

- [ ] GitHub integration
- [ ] Webhooks
- [ ] Pipeline YAML
- [ ] DAG execution
- [ ] Scheduler
- [ ] Docker workers
- [ ] Worker heartbeat
- [ ] Job leases
- [ ] Retry
- [ ] Cancellation
- [ ] Timeouts

## Developer Experience

- [ ] Live logs
- [ ] Artifacts
- [ ] GitHub PR checks
- [ ] Pipeline history
- [ ] Job history
- [ ] Test history

## Intelligence

- [ ] Flaky test detection
- [ ] Failure classification
- [ ] Performance regression detection

## Security

- [ ] Secret encryption
- [ ] Secret masking
- [ ] Container isolation
- [ ] Webhook signatures
- [ ] RBAC
- [ ] Audit logs

## Operations

- [ ] Prometheus
- [ ] Grafana
- [ ] OpenTelemetry
- [ ] Structured logs
- [ ] Health checks
- [ ] Graceful shutdown

## Reliability

- [ ] Idempotency
- [ ] Outbox
- [ ] DLQ
- [ ] Lease recovery
- [ ] Backpressure
- [ ] Chaos tests

---

# 31. Milestones

## Milestone 1 — Control Plane

Complete:

```text
Phase 1
Phase 2
Phase 3
Phase 4
Phase 5
Phase 6
```

Result:

```text
GitHub
   |
   v
Webhook
   |
   v
ForgeCI
   |
   v
Read .forgeci.yml
   |
   v
Create PipelineRun
   |
   v
Persist pipeline
   |
   v
Expose API
```

---

## Milestone 2 — Actual CI Execution

Complete:

```text
Phase 7
Phase 8
Phase 9
Phase 10
```

Result:

```text
Pipeline
   |
   v
Scheduler
   |
   v
RabbitMQ
   |
   v
Worker
   |
   v
Docker
   |
   v
Run commands
   |
   v
Persist result
```

---

## Milestone 3 — Developer Experience

Complete:

```text
Phase 11
Phase 12
Phase 15
Phase 16
Phase 17
```

Result:

```text
live logs
artifacts
dashboard
retries
cancellation
```

---

## Milestone 4 — Test Intelligence

Complete:

```text
Phase 13
Phase 14
```

Result:

```text
CI
 |
 +-- Test history
 |
 +-- Flaky detection
 |
 +-- Failure analytics
```

---

## Milestone 5 — Production Engineering

Complete:

```text
Phase 18
Phase 19
Phase 20
Phase 21
Phase 22
Phase 23
```

Result:

```text
secure
observable
reliable
distributed
GitHub-integrated CI platform
```

---

## Milestone 6 — Advanced Systems

Complete:

```text
Phase 24
Phase 25
Phase 26
Phase 27
Phase 28
```

Result:

```text
AI-assisted
Kubernetes-capable
horizontally scalable
chaos-tested
performance-measured
```

---

# 32. First End-to-End Demo

The first impressive demo should be:

```text
1. Create GitHub repository.

2. Add:

   .forgeci.yml

3. Push code.

4. GitHub sends webhook.

5. ForgeCI validates signature.

6. ForgeCI creates PipelineRun.

7. Scheduler sees jobs.

8. RabbitMQ receives job.

9. Worker receives job.

10. Worker clones exact commit.

11. Worker starts Docker container.

12. Docker executes:

    npm ci
    npm test

13. Logs stream into browser.

14. JUnit results are collected.

15. Pipeline completes.

16. Dashboard changes to SUCCESS.

17. GitHub PR check becomes green.

18. Test execution is stored.

19. Artifact is available.

20. Pipeline appears in history.
```

This is the first point where ForgeCI becomes a real product rather than a collection of backend components.

---

# 33. Recommended Initial `.forgeci.yml`

Use this as the first example repository:

```yaml
version: 1

pipeline:
  name: forgeci-example

  triggers:
    - push
    - pull_request

  defaults:
    timeout: 600
    retries: 0

  jobs:
    install:
      image: node:22-bookworm
      commands:
        - npm ci

    test:      image: node:22-bookworm
      depends_on:
        - install
      commands:
        - npm test -- --runInBand

    build:
      image: node:22-bookworm
      depends_on:
        - test
      commands:
        - npm run build
      artifacts:
        paths:
          - dist/**
```

---

# 34. Development Workflow

Use this workflow for every feature:

```text
Issue
  |
  v
Design
  |
  v
Domain model
  |
  v
Database change
  |
  v
Application logic
  |
  v
API
  |
  v
Tests
  |
  v
Integration tests
  |
  v
Observability
  |
  v
Security review
  |
  v
Documentation
  |
  v
Code review
```

---

# 35. Coding-Agent Prompt

Use the following pattern when asking a coding agent to implement each task.

```text
You are implementing ForgeCI according to docs/ROADMAP.md.

Implement ONLY:

<PHASE AND TASK>

Do not implement future phases unless the current task strictly requires a minimal dependency.

Before modifying code:

1. Inspect the repository structure.
2. Read the relevant architecture documentation.
3. Identify existing abstractions that should be reused.
4. Identify database changes required.
5. Identify security implications.
6. Identify tests required.

Implementation requirements:

- Follow the architecture in docs/ROADMAP.md.
- Keep domain logic separate from infrastructure concerns.
- Do not put business logic inside controllers.
- Do not expose persistence entities directly through the API.
- Validate all external input.
- Do not log secrets.
- Make message handlers idempotent.
- Respect existing state-machine rules.
- Do not introduce unnecessary dependencies.
- Do not rewrite unrelated code.

Testing requirements:

- Add unit tests for new business logic.
- Add integration tests when infrastructure behavior is involved.
- Add negative-path tests.
- Add concurrency/idempotency tests when relevant.
- Run formatting.
- Run linting.
- Run the relevant test suite.
- Run the full test suite if practical.

Documentation requirements:

- Update docs when behavior or architecture changes.
- Add an ADR if an architectural decision is introduced.

At the end:

1. Summarize implemented changes.
2. List files changed.
3. List tests added.
4. Report commands executed.
5. Report test results.
6. Report any remaining risks.
7. Do not mark future tasks as complete.
```

---

# 36. Non-Negotiable Architecture Principles

These principles should survive the entire project.

1. **PostgreSQL is the source of truth for durable state.**
2. **Messages are at-least-once.**
3. **Consumers must be idempotent.**
4. **Every job execution has an explicit attempt.**
5. **Every worker execution has a lease.**
6. **Every state transition is validated.**
7. **Pipeline configuration is snapshotted at execution time.**
8. **Workers are disposable.**
9. **Workspaces are disposable.**
10. **Secrets are never returned to clients.**
11. **CI containers are never trusted.**
12. **The API must remain stateless enough to scale horizontally.**
13. **External systems can fail at any time.**
14. **A process can die between any two operations.**
15. **The system must converge to a correct state after failures.**
16. **Observability is part of implementation, not an afterthought.**
17. **Security boundaries must be explicit.**
18. **Do not build AI until the underlying execution data is trustworthy.**
19. **Do not build Kubernetes until Docker Compose works reliably.**
20. **Do not optimize before measuring.**

---

# 37. Final Architecture

The eventual ForgeCI architecture should look approximately like:

```text
                              GitHub
                                |
                     +----------+----------+
                     |                     |
                  Webhooks             GitHub API
                     |                     |
                     +----------+----------+
                                |
                                v
                    +----------------------+
                    |    ForgeCI API       |
                    |    Spring Boot       |
                    +----------+-----------+
                               |
              +----------------+----------------+
              |                |                |
              v                v                v
         PostgreSQL          Redis          RabbitMQ
              |                                 |
              |                       +---------+---------+
              |                       |         |         |
              |                       v         v         v
              |                    Worker    Worker    Worker
              |                       |         |         |
              |                       +----+----+---------+
              |                            |
              |                         Docker
              |                            |
              |                       CI Workload
              |                            |
              +-------------+--------------+
                            |
                     Results / Logs
                            |
              +-------------+-------------+
              |                           |
              v                           v
        Object Storage               Test Engine
        MinIO / S3                       |
              |                           v
              |                     Flaky Analysis
              |                           |
              +-------------+-------------+
                            |
                            v
                       Next.js UI
                            |
                 +----------+----------+
                 |                     |
                 v                     v
             Dashboard             WebSocket
```

---

# 38. Ultimate Goal

By the end, ForgeCI should demonstrate that you understand:

- distributed systems
- CI/CD architecture
- asynchronous processing
- message queues
- scheduling
- DAG execution
- worker orchestration
- Docker execution
- concurrency
- leases
- retries
- idempotency
- database transactions
- outbox pattern
- event-driven architecture
- WebSockets
- artifact storage
- GitHub APIs
- authentication
- authorization
- secrets management
- container security
- observability
- test analytics
- flaky-test detection
- Kubernetes
- performance testing
- chaos engineering
- AI/RAG integration

The point is not to recreate GitHub Actions perfectly.

The point is to build a **small but genuinely engineered CI platform** where every major subsystem exists because you understand the problem it solves.