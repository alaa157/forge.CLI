CREATE TABLE forgeci.users (
 id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL, password_hash VARCHAR(255) NOT NULL,
 display_name VARCHAR(120) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
 role VARCHAR(20) NOT NULL DEFAULT 'USER', created_at TIMESTAMPTZ NOT NULL,
 updated_at TIMESTAMPTZ NOT NULL, last_login_at TIMESTAMPTZ,
 CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE','SUSPENDED','DELETED')),
 CONSTRAINT ck_users_role CHECK (role IN ('USER','ADMIN'))
);
CREATE UNIQUE INDEX uk_users_email ON forgeci.users(email);
CREATE TABLE forgeci.refresh_tokens (
 id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES forgeci.users(id),
 token_hash VARCHAR(64) NOT NULL UNIQUE, family_id UUID NOT NULL,
 expires_at TIMESTAMPTZ NOT NULL, created_at TIMESTAMPTZ NOT NULL,
 used_at TIMESTAMPTZ, revoked_at TIMESTAMPTZ
);
CREATE INDEX idx_refresh_tokens_family ON forgeci.refresh_tokens(family_id);
CREATE INDEX idx_refresh_tokens_user ON forgeci.refresh_tokens(user_id);
