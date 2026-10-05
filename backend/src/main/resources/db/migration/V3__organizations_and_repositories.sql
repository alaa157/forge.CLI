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
 user_id UUID NOT NULL REFERENCES forgeci.users(id),
 github_user_id VARCHAR(120) NOT NULL,
 github_login VARCHAR(120) NOT NULL,
 access_token_ciphertext VARCHAR(2000) NOT NULL,
 connected_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE UNIQUE INDEX uk_github_connection_user ON forgeci.github_connections(user_id);
