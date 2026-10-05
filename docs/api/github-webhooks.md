# GitHub Webhooks

ForgeCI accepts GitHub push and pull_request deliveries at POST /api/v1/webhooks/github.

## Security

The endpoint validates the raw request body using X-Hub-Signature-256 with HMAC-SHA256. The webhook secret is supplied through GITHUB_WEBHOOK_SECRET and must never be committed.

## Idempotency

Each delivery is identified by GitHub's X-GitHub-Delivery GUID. ForgeCI persists a unique (provider, delivery_id) record and atomically claims it before processing. A processed redelivery returns DUPLICATE and cannot create another PipelineRun.

Only a SHA-256 payload hash is stored; raw webhook bodies are not persisted.

## Supported events

push: uses repository.id, after, and refs/heads/* to identify the repository, commit, and branch. Branch deletion and all-zero SHAs are ignored.

pull_request: creates runs for opened, synchronize, reopened, and ready_for_review. The source branch and head SHA come from pull_request.head.ref and pull_request.head.sha. Other actions are recorded and ignored.

## Pipeline loading

For a supported event, ForgeCI locates the connected repository by GitHub repository ID, reads .forgeci.yml at the exact event commit through the GitHub Contents API, validates it with the Phase 4 parser/validator/DAG engine, and creates the Phase 5 immutable PipelineRun snapshot.

If .forgeci.yml is absent at that commit, the delivery is processed without creating a run.

Execution is deliberately not part of Phase 6. Created PipelineRuns remain in CREATED until later scheduler work.

## Required headers

X-GitHub-Event, X-GitHub-Delivery, and X-Hub-Signature-256.