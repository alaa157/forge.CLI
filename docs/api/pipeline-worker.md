# Pipeline Worker

Phase 8 adds the first execution worker.

## Flow

1. RabbitMQ delivers a pipeline dispatch.
2. The worker loads the persisted PipelineRun and JobRun/StepRun state.
3. QUEUED transitions to RUNNING.
4. Jobs execute sequentially and steps execute in stored order.
5. A failed job fails the pipeline and later jobs are skipped.
6. A terminal PipelineRun is treated as already processed, making redelivery safe.

## Container isolation

Each step runs through Docker with `--rm`, `--network none`, `--read-only`, a bounded `/tmp`, ForgeCI run/commit labels, and no host filesystem mounts. Container image syntax is validated before invocation.

## Boundary

Phase 8 does not yet provide repository checkout/workspaces, artifact transfer, live log persistence, execution-attempt retries, or cancellation signaling.
