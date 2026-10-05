# ADR 0002 — Transitional Java worker alongside Go runner

## Status
Accepted

## Context
Phase 8 requires an independently running **Go** worker. The control plane already had an in-process Java `PipelineWorker` consuming RabbitMQ for local development.

## Decision
1. Keep the Java `PipelineWorker` as a **dev/fallback** executor until the Go runner implements full job execution.
2. Implement the Phase 8 **control-plane contracts** now: worker registry, heartbeat, job leases, fencing tokens.
3. Ship a Go `forge-runner` that registers and heartbeats; job pull/execute lands in a follow-up.

## Consequences
- Production target remains the Go worker process under `worker/`.
- Scheduler and lease recovery work independently of which runtime executes steps.
