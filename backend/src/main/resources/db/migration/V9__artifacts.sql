CREATE TABLE forgeci.artifacts (
 id UUID PRIMARY KEY,
 job_run_id UUID NOT NULL REFERENCES forgeci.job_runs(id) ON DELETE CASCADE,
 name VARCHAR(255) NOT NULL,
 object_key VARCHAR(1024) NOT NULL UNIQUE,
 size_bytes BIGINT NOT NULL,
 sha256 VARCHAR(64) NOT NULL,
 content_type VARCHAR(255) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_artifact_size CHECK (size_bytes >= 0)
);
CREATE INDEX idx_artifacts_job ON forgeci.artifacts(job_run_id);
CREATE INDEX idx_artifacts_created ON forgeci.artifacts(created_at);

