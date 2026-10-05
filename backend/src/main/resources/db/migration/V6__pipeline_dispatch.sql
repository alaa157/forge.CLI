CREATE TABLE forgeci.pipeline_dispatches (
 id UUID PRIMARY KEY,
 pipeline_run_id UUID NOT NULL REFERENCES forgeci.pipeline_runs(id) ON DELETE CASCADE,
 status VARCHAR(20) NOT NULL,
 attempts INTEGER NOT NULL DEFAULT 0,
 created_at TIMESTAMPTZ NOT NULL,
 published_at TIMESTAMPTZ,
 last_error VARCHAR(2000),
 CONSTRAINT uk_pipeline_dispatch_run UNIQUE (pipeline_run_id),
 CONSTRAINT ck_pipeline_dispatch_status CHECK (status IN ('PENDING','PUBLISHED')),
 CONSTRAINT ck_pipeline_dispatch_attempts CHECK (attempts >= 0)
);
CREATE INDEX idx_pipeline_dispatches_status ON forgeci.pipeline_dispatches(status);
CREATE INDEX idx_pipeline_dispatches_created_at ON forgeci.pipeline_dispatches(created_at);
