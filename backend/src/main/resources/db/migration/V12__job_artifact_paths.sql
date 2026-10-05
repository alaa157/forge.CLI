ALTER TABLE forgeci.job_runs
    ADD COLUMN artifact_paths TEXT NOT NULL DEFAULT '[]';
