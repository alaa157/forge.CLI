# ForgeCI Security Threat Model (Phase 20.1)

## Trust boundaries

1. GitHub webhook requests → authenticated ForgeCI control plane.
2. User-authored pipeline YAML → validated immutable pipeline snapshot.
3. Control plane → RabbitMQ messages (no secrets in payloads).
4. RabbitMQ → disposable worker execution.
5. Worker → untrusted repository code inside Docker.
6. Worker → PostgreSQL / object storage for durable results.
7. Secrets → execution environment only (envelope-encrypted at rest).

## Primary threats and controls

| Threat | Control |
|---|---|
| **Malicious repository** | Disposable workspace, isolated Docker network (`--network none`), dropped capabilities, no-new-privileges |
| **Malicious pull request** | Same isolation; secrets only injected when job explicitly declares secret names |
| **Container escape** | No privileged mode, no Docker socket mount, read-only root FS, resource/PID limits, optional non-root user |
| **Secret exfiltration** | Envelope encryption at rest, runtime-only injection, log masking (`SecretMasker`) before durable storage |
| **Resource exhaustion** | CPU, memory, PID, step timeout, cache/artifact/log size limits |
| **SSRF** | `OutboundUrlPolicy` blocks localhost, link-local, site-local, metadata (169.254.169.254); clone URLs validated |
| **Webhook forgery** | GitHub HMAC-SHA256 constant-time signature verification |
| **Token theft** | GitHub credentials never placed in RabbitMQ job payloads; checkout uses process-local HTTP header |
| **Artifact poisoning** | Workspace-confined collection, path traversal rejection, bounded ZIP |
| **Dependency attacks** | Optional OSV / npm audit / Trivy / pip-audit (see `dependency-scanning.md`) |
| **Log injection / secret leakage** | Structured chunking + runtime secret masking before persist |
| **Path traversal** | Artifact and cache paths must remain inside the workspace |

## Runner isolation checklist (Phase 20.2)

- [x] Non-root containers (configurable `forgeci.worker.non-root`)
- [x] Resource limits (CPU, memory)
- [x] Ephemeral workspaces (temp dir + cleanup)
- [x] Network restrictions (`--network none`)
- [x] Capability dropping (`--cap-drop ALL`)
- [x] No privileged mode
- [x] No Docker socket
- [x] PID limits
- [x] Timeouts

## Remaining risks

The Docker boundary is an engineering isolation boundary, not a formally hardened
hostile-workload sandbox. Production deployments should consider dedicated runner
hosts, stronger kernel isolation, and a KMS-backed secret manager.
