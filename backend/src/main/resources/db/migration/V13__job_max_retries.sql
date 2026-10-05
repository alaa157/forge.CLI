ALTER TABLE forgeci.job_runs ADD COLUMN IF NOT EXISTS max_retries INTEGER NOT NULL DEFAULT 0;
ALTER TABLE forgeci.job_runs ADD CONSTRAINT ck_job_max_retries CHECK (max_retries >= 0 AND max_retries <= 10);
