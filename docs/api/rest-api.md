# REST API

Phase 15 formalizes the execution and test resources currently backed by durable domain services.

## Runs

- GET /api/v1/runs?repositoryId=...
- GET /api/v1/runs/{id}
- POST /api/v1/runs/{id}/cancel
- POST /api/v1/runs/{id}/retry

Retry creates a new pipeline run from the immutable pipeline snapshot and enters the existing durable scheduler/dispatch boundary.

## Jobs

- GET /api/v1/jobs/{id}
- GET /api/v1/jobs/{id}/logs
- GET /api/v1/jobs/{id}/artifacts

## Tests

- POST /api/v1/jobs/{jobRunId}/test-results/junit
- GET /api/v1/repositories/{repositoryId}/tests
- GET /api/v1/repositories/{repositoryId}/tests/analytics
- GET /api/v1/tests/{id}/history

## Flaky tests

- GET /api/v1/flaky-tests
- GET /api/v1/flaky-tests/assessment
- GET /api/v1/repositories/{repositoryId}/tests/dashboard

Existing authentication and webhook endpoints remain unchanged; Phase 15 adds formal resource surfaces without bypassing the existing domain services.
