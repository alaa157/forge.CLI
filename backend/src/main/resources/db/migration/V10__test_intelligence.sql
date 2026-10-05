CREATE TABLE forgeci.test_executions (
 id UUID PRIMARY KEY,
 test_id VARCHAR(512) NOT NULL,
 job_run_id UUID NOT NULL REFERENCES forgeci.job_runs(id) ON DELETE CASCADE,
 repository_id UUID NOT NULL REFERENCES forgeci.repositories(id),
 suite VARCHAR(255) NOT NULL,
 class_name VARCHAR(512) NOT NULL,
 test_name VARCHAR(512) NOT NULL,
 framework VARCHAR(64) NOT NULL,
 status VARCHAR(16) NOT NULL,
 duration_ms BIGINT NOT NULL,
 failure_message TEXT,
 stdout TEXT,
 stderr TEXT,
 commit_sha VARCHAR(64) NOT NULL,
 branch VARCHAR(255) NOT NULL,
 executed_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_test_execution_status CHECK (status IN ('PASSED','FAILED','ERROR','SKIPPED')),
 CONSTRAINT ck_test_execution_duration CHECK (duration_ms >= 0)
);
CREATE INDEX idx_test_exec_test ON forgeci.test_executions(test_id, executed_at);
CREATE INDEX idx_test_exec_job ON forgeci.test_executions(job_run_id);
CREATE INDEX idx_test_exec_repo ON forgeci.test_executions(repository_id);
