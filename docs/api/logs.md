# Logs

Phase 11 provides durable and live job logs.

## Durable storage

Each chunk contains:
- job run ID
- global sequence
- stream (stdout or stderr)
- content
- timestamp

Chunks are limited to 16 KiB and each job is limited to 10 MiB of persisted log content.

## HTTP API

GET /api/v1/jobs/{id}/logs?tail=500

Use after to continue from a sequence:
GET /api/v1/jobs/{id}/logs?after=120&tail=500

Use since to continue from a timestamp (ISO-8601):
GET /api/v1/jobs/{id}/logs?since=2026-10-05T10:00:00Z&tail=500

The API bounds tail to 5000 chunks.

## Live stream

Connect a STOMP client to /ws and subscribe to:
 /topic/jobs/{jobId}/logs

Each published message contains the newly captured log chunk:

```json
{"sequence": 42, "stream": "stdout", "content": "...", "timestamp": "2026-10-05T10:00:00Z"}
```

## Limits and truncation

When a job reaches the 10 MiB persisted-log limit, further output is dropped,
a single truncation marker chunk is appended and published, and the dropped
chunks are counted in the `forgeci.logs.dropped_chunks` metric. Writes are
synchronous, which applies natural backpressure to the producing stream.

## Reliability

PostgreSQL is authoritative for durable logs. WebSocket delivery is best-effort; clients can recover missed output through the HTTP cursor API.
