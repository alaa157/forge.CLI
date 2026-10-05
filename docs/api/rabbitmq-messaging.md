# RabbitMQ Messaging

Phase 9 establishes ForgeCI's messaging foundation around durable RabbitMQ topology, explicit message contracts, and idempotent consumption.

## Queue topology

The forgeci.messaging direct exchange provides:

| Queue | Routing key | Dead-letter queue |
|---|---|---|
| forgeci.jobs | jobs | forgeci.jobs.dlq |
| forgeci.events | events | forgeci.events.dlq |
| forgeci.logs | logs | forgeci.logs.dlq |
| forgeci.notifications | notifications | — |

Queues and dead-letter queues are durable.

The existing Phase 7 pipeline-dispatch queue remains intact so the Phase 8 execution path is not broken while the messaging topology is introduced incrementally.

## Job message contract

JobMessage contains only identifiers and timestamps:

- messageId
- jobId
- attemptId
- repositoryId
- pipelineRunId
- organizationId
- traceId
- createdAt

Long-lived credentials and repository secrets must never be placed in queue messages.

## Idempotent consumers

processed_messages records a message ID and consumer name.

The uniqueness boundary is:

(message_id, consumer)

Consumers claim a message atomically with PostgreSQL:

INSERT ... ON CONFLICT (message_id, consumer) DO NOTHING

A claim result of 0 means the message was already processed by that consumer and must be acknowledged without re-running business logic.

The Phase 8 pipeline worker now uses this boundary for its RabbitMQ dispatch messages. The dispatch ID is also the stable message ID, so a republish of the same durable dispatch cannot create a new logical delivery identity.

## Reliability boundary

RabbitMQ is transport, not the source of truth. PostgreSQL remains authoritative for pipeline and execution state.

The system assumes at-least-once delivery. Duplicate delivery is therefore expected and handled explicitly.

## Deferred work

Phase 9 does not implement:

- execution attempts as a first-class persisted entity
- job leasing
- worker registration/heartbeat
- retry orchestration
- cancellation
- live log streaming
- artifact messages
