# Pipeline Worker

Phase 8 adds the first execution worker. Phase 9 adds duplicate-delivery protection around its RabbitMQ boundary.

## Flow

1. RabbitMQ delivers a pipeline dispatch.
2. The worker atomically claims the message ID for the pipeline-worker consumer.
3. A duplicate delivery is acknowledged without executing the pipeline again.
4. The worker loads the persisted PipelineRun and JobRun/StepRun state.
5. QUEUED transitions to RUNNING.
6. Jobs execute sequentially and steps execute in stored order.
7. A failed job fails the pipeline and later jobs are skipped.
8. A terminal PipelineRun is treated as already processed.

## Message identity

Pipeline dispatch messages contain messageId, dispatchId, and pipelineRunId.

The dispatch ID is used as the stable message ID. If a publish succeeds but the publisher fails before persisting PUBLISHED, the same durable dispatch is republished with the same message identity.

Legacy messages without messageId are accepted by deriving messageId from dispatchId.

## Container isolation

Each step runs through Docker with --rm, --network none, --read-only, a bounded /tmp, ForgeCI run/commit labels, and no host filesystem mounts. Container image syntax is validated before invocation.

## Boundary

Phase 9 does not yet provide execution-attempt persistence, job leases, worker registration/heartbeat, retry orchestration, cancellation signaling, repository checkout/workspaces, artifact transfer, or live log persistence.