CREATE TABLE forgeci.organizations (
 id UUID PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 slug VARCHAR(80) NOT NULL UNIQUE,
 created_by UUID NOT NULL REFERENCES forgeci.users(id),
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
CREATE TABLE forgeci.organization_members (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id) ON DELETE CASCADE,
 user_id UUID NOT NULL REFERENCES forgeci.users(id) ON DELETE CASCADE,
 role VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_org_member_role CHECK (role IN ('OWNER','ADMIN','MEMBER')),
 CONSTRAINT uk_org_member UNIQUE (organization_id,user_id)
);
CREATE INDEX idx_org_members_user ON forgeci.organization_members(user_id);
CREATE TABLE forgeci.github_connections (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id) ON DELETE CASCADE,
 provider VARCHAR(30) NOT NULL DEFAULT 'GITHUB',
 external_account_id VARCHAR(120) NOT NULL,
 account_login VARCHAR(120) NOT NULL,
 encrypted_access_token TEXT NOT NULL,
 token_expires_at TIMESTAMPTZ,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 disconnected_at TIMESTAMPTZ,
 CONSTRAINT ck_github_provider CHECK (provider IN ('GITHUB')),
 CONSTRAINT ck_github_connection_status CHECK (status IN ('ACTIVE','EXPIRED','REVOKED','DISCONNECTED')),
 CONSTRAINT uk_github_connection_org UNIQUE (organization_id,provider,external_account_id)
);
CREATE TABLE forgeci.oauth_states (
 id UUID PRIMARY KEY,
 user_id UUID NOT NULL REFERENCES forgeci.users(id) ON DELETE CASCADE,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id) ON DELETE CASCADE,
 state_hash VARCHAR(64) NOT NULL UNIQUE,
 expires_at TIMESTAMPTZ NOT NULL,
 consumed_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_oauth_states_expiry ON forgeci.oauth_states(expires_at);
CREATE TABLE forgeci.repositories (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id) ON DELETE CASCADE,
 provider VARCHAR(30) NOT NULL,
 external_id VARCHAR(120) NOT NULL,
 name VARCHAR(200) NOT NULL,
 full_name VARCHAR(400) NOT NULL,
 clone_url TEXT NOT NULL,
 default_branch VARCHAR(255),
 private BOOLEAN NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_repository_provider CHECK (provider IN ('GITHUB')),
 CONSTRAINT uk_repository_external UNIQUE (organization_id,provider,external_id)
);
CREATE INDEX idx_repositories_org ON forgeci.repositories(organization_id);
