# Flaky Test Intelligence

Phase 14 derives a bounded 0–100 flakiness score from durable test executions.

## Signals

- Failure frequency
- Outcome inconsistency
- Recency-weighted failure behavior
- Duration instability

## Classifications

- STABLE
- LIKELY_FLAKY
- FLAKY
- NEWLY_FLAKY
- PERSISTENT_FAILURE

Historical samples are capped at the latest 1,000 executions per test.

## API

- GET /api/v1/flaky-tests?repositoryId=...&limit=20
- GET /api/v1/flaky-tests/assessment?testId=...
- GET /api/v1/repositories/{repositoryId}/tests/dashboard

Dashboard buckets include top flaky, newly flaky, worsening, slowest, and recently failed tests.
