CREATE TABLE forgeci.pipeline_runs (
 id UUID PRIMARY KEY,
 repository_id UUID NOT NULL REFERENCES forgeci.repositories(id),
 commit_sha VARCHAR(64) NOT NULL,
 branch VARCHAR(255) NOT NULL,
 trigger VARCHAR(50) NOT NULL,
 pipeline_yaml TEXT NOT NULL,
 resolved_pipeline TEXT NOT NULL,
 job_graph TEXT NOT NULL,
 forgeci_version VARCHAR(64) NOT NULL,
 status VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 started_at TIMESTAMPTZ,
 finished_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_pipeline_run_status CHECK (status IN ('CREATED','QUEUED','RUNNING','SUCCEEDED','FAILED','CANCELLED','TIMED_OUT'))
);
CREATE INDEX idx_pipeline_runs_repository ON forgeci.pipeline_runs(repository_id);
CREATE INDEX idx_pipeline_runs_status ON forgeci.pipeline_runs(status);
CREATE INDEX idx_pipeline_runs_created_at ON forgeci.pipeline_runs(created_at);

CREATE TABLE forgeci.job_runs (
 id UUID PRIMARY KEY,
 pipeline_run_id UUID NOT NULL REFERENCES forgeci.pipeline_runs(id) ON DELETE CASCADE,
 name VARCHAR(128) NOT NULL,
 image VARCHAR(512) NOT NULL,
 commands TEXT NOT NULL,
 depends_on TEXT NOT NULL,
 status VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 started_at TIMESTAMPTZ,
 finished_at TIMESTAMPTZ,
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT ck_job_run_status CHECK (status IN ('PENDING','QUEUED','RUNNING','SUCCEEDED','FAILED','CANCELLED','TIMED_OUT','SKIPPED'))
);
CREATE INDEX idx_job_runs_pipeline ON forgeci.job_runs(pipeline_run_id);
CREATE INDEX idx_job_runs_status ON forgeci.job_runs(status);

CREATE TABLE forgeci.step_runs (
 id UUID PRIMARY KEY,
 job_run_id UUID NOT NULL REFERENCES forgeci.job_runs(id) ON DELETE CASCADE,
 position INTEGER NOT NULL,
 command VARCHAR(8192) NOT NULL,
 status VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 started_at TIMESTAMPTZ,
 finished_at TIMESTAMPTZ,
 CONSTRAINT ck_step_run_position CHECK (position >= 0),
 CONSTRAINT ck_step_run_status CHECK (status IN ('PENDING','RUNNING','SUCCEEDED','FAILED','CANCELLED','TIMED_OUT','SKIPPED')),
 CONSTRAINT uk_step_run_position UNIQUE (job_run_id, position)
);
CREATE INDEX idx_step_runs_job ON forgeci.step_runs(job_run_id);
