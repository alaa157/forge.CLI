# Pipeline Dispatch

Phase 7 introduces durable scheduling and message dispatch for pipeline runs.

## Flow

1. A GitHub webhook creates a PipelineRun in `CREATED`.
2. `PipelineScheduler` claims one unique dispatch record in the same database transaction.
3. The pipeline transitions to `QUEUED`.
4. `PipelineDispatchPublisher` publishes a JSON message to RabbitMQ.
5. The dispatch is marked `PUBLISHED`.

The dispatch record is the durable handoff boundary between database state and the message broker.

## Delivery semantics

Publishing is **at-least-once**. If the process crashes after RabbitMQ accepts a message but before the dispatch is marked `PUBLISHED`, the same dispatch can be published again. The future worker/consumer phase must therefore treat `dispatchId` and `pipelineRunId` as idempotency keys.

A failed publish remains `PENDING`, records the attempt count and last error, and is retried automatically.

## Queue contract

- Exchange: `forgeci.pipeline`
- Queue: `forgeci.pipeline.dispatch`
- Routing key: `pipeline.dispatch`
- Durable exchange and queue
- JSON message:
  - `dispatchId`
  - `pipelineRunId`

## Scope boundary

Phase 7 does not execute jobs, start containers, stream logs, implement retries across execution attempts, or manage cancellation. It establishes durable scheduling and broker dispatch only.
