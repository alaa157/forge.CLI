CREATE TABLE forgeci.organizations (
 id UUID PRIMARY KEY,
 name VARCHAR(120) NOT NULL,
 slug VARCHAR(80) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX uk_org_slug ON forgeci.organizations(slug);

CREATE TABLE forgeci.organization_members (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id),
 user_id UUID NOT NULL REFERENCES forgeci.users(id),
 role VARCHAR(20) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_org_member_role CHECK (role IN ('OWNER','ADMIN','MEMBER'))
);
CREATE UNIQUE INDEX uk_org_member ON forgeci.organization_members(organization_id,user_id);
CREATE INDEX idx_org_members_user ON forgeci.organization_members(user_id);

CREATE TABLE forgeci.repositories (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id),
 provider VARCHAR(20) NOT NULL,
 external_id VARCHAR(120) NOT NULL,
 name VARCHAR(200) NOT NULL,
 full_name VARCHAR(400) NOT NULL,
 clone_url VARCHAR(1000) NOT NULL,
 default_branch VARCHAR(255) NOT NULL,
 is_private BOOLEAN NOT NULL,
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 CONSTRAINT ck_repository_provider CHECK (provider IN ('GITHUB'))
);
CREATE UNIQUE INDEX uk_repo_external ON forgeci.repositories(organization_id,provider,external_id);
CREATE INDEX idx_repositories_org ON forgeci.repositories(organization_id);

CREATE TABLE forgeci.github_connections (
 id UUID PRIMARY KEY,
 organization_id UUID NOT NULL REFERENCES forgeci.organizations(id),
 provider VARCHAR(30) NOT NULL DEFAULT 'GITHUB',
 external_account_id VARCHAR(120) NOT NULL,
 account_login VARCHAR(120) NOT NULL,
 encrypted_access_token TEXT NOT NULL,
 token_expires_at TIMESTAMPTZ,
 status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 disconnected_at TIMESTAMPTZ,
 CONSTRAINT ck_github_connection_status CHECK (status IN ('ACTIVE','EXPIRED','REVOKED','DISCONNECTED'))
);
CREATE INDEX idx_github_connections_org ON forgeci.github_connections(organization_id);
CREATE UNIQUE INDEX uk_github_connection_org_account ON forgeci.github_connections(organization_id,external_account_id);
