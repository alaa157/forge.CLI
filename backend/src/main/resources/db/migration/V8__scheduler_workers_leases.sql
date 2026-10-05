-- Phase 7/8: job priority, leases, and worker registry

ALTER TABLE forgeci.pipeline_runs
    ADD COLUMN IF NOT EXISTS priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE forgeci.job_runs
    ADD COLUMN IF NOT EXISTS priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN IF NOT EXISTS lease_id UUID,
    ADD COLUMN IF NOT EXISTS lease_worker_id UUID,
    ADD COLUMN IF NOT EXISTS lease_expires_at TIMESTAMPTZ;

ALTER TABLE forgeci.pipeline_runs
    DROP CONSTRAINT IF EXISTS ck_pipeline_run_priority;
ALTER TABLE forgeci.pipeline_runs
    ADD CONSTRAINT ck_pipeline_run_priority CHECK (priority IN ('HIGH','NORMAL','LOW'));

ALTER TABLE forgeci.job_runs
    DROP CONSTRAINT IF EXISTS ck_job_run_priority;
ALTER TABLE forgeci.job_runs
    ADD CONSTRAINT ck_job_run_priority CHECK (priority IN ('HIGH','NORMAL','LOW'));

CREATE INDEX IF NOT EXISTS idx_job_runs_schedule
    ON forgeci.job_runs (status, priority DESC, created_at ASC);

CREATE INDEX IF NOT EXISTS idx_job_runs_lease_expires
    ON forgeci.job_runs (lease_expires_at)
    WHERE lease_expires_at IS NOT NULL;

CREATE TABLE IF NOT EXISTS forgeci.workers (
    id UUID PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL,
    version VARCHAR(64) NOT NULL,
    capabilities TEXT NOT NULL DEFAULT '[]',
    max_concurrency INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL,
    last_heartbeat_at TIMESTAMPTZ NOT NULL,
    registered_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_worker_status CHECK (status IN ('STARTING','READY','BUSY','DRAINING','OFFLINE')),
    CONSTRAINT ck_worker_max_concurrency CHECK (max_concurrency > 0 AND max_concurrency <= 256)
);

CREATE INDEX IF NOT EXISTS idx_workers_status ON forgeci.workers(status);
CREATE INDEX IF NOT EXISTS idx_workers_heartbeat ON forgeci.workers(last_heartbeat_at);
