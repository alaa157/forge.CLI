# Docker Execution

Phase 10 executes pipeline steps through a JobExecutor abstraction with a Docker implementation.

## Execution flow

1. Load the persisted PipelineRun snapshot.
2. Resolve the connected GitHub repository.
3. Fetch the exact PipelineRun commit SHA into a disposable workspace.
4. Start the configured container image.
5. Mount only the disposable workspace at /workspace.
6. Execute the step command sequentially.
7. Capture exit status and timeout.
8. Remove the workspace.

## Security boundary

Containers run with:
- network disabled
- read-only root filesystem
- all Linux capabilities dropped
- no-new-privileges
- PID limit
- CPU limit
- memory limit
- bounded /tmp
- no privileged mode
- no Docker socket
- no host filesystem mounts

Repository credentials are supplied to Git through process environment configuration rather than command arguments and are never exposed to the CI container.

This is an MVP Docker isolation boundary, not a hardened multi-tenant sandbox.
