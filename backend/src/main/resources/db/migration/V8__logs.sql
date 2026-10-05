CREATE TABLE forgeci.log_chunks (
    id UUID PRIMARY KEY,
    job_run_id UUID NOT NULL REFERENCES forgeci.job_runs(id) ON DELETE CASCADE,
    sequence BIGINT NOT NULL,
    stream VARCHAR(16) NOT NULL,
    content VARCHAR(16384) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_log_chunk_sequence UNIQUE (job_run_id, sequence),
    CONSTRAINT ck_log_chunk_sequence CHECK (sequence >= 0),
    CONSTRAINT ck_log_chunk_stream CHECK (stream IN ('stdout','stderr'))
);

CREATE INDEX idx_log_chunks_job_sequence ON forgeci.log_chunks(job_run_id, sequence);
