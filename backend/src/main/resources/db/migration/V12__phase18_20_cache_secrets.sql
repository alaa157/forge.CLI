CREATE TABLE forgeci.secrets (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id) ON DELETE CASCADE,
 name VARCHAR(128) NOT NULL,
 ciphertext TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_secret_org_name UNIQUE (organization_id,name)
);
CREATE INDEX idx_secrets_org ON forgeci.secrets(organization_id);

CREATE TABLE forgeci.repository_secrets (
 id UUID PRIMARY KEY,
 repository_id UUID NOT NULL REFERENCES forgeci.repositories(id) ON DELETE CASCADE,
 name VARCHAR(128) NOT NULL,
 ciphertext TEXT NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT uk_repo_secret_name UNIQUE (repository_id,name)
);
CREATE INDEX idx_repository_secrets_repo ON forgeci.repository_secrets(repository_id);

ALTER TABLE forgeci.job_runs ADD COLUMN cache_key VARCHAR(512);
ALTER TABLE forgeci.job_runs ADD COLUMN cache_paths TEXT NOT NULL DEFAULT '[]';
ALTER TABLE forgeci.job_runs ADD COLUMN secret_names TEXT NOT NULL DEFAULT '[]';
