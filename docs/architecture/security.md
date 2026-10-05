# Security Architecture

Local credentials come from environment variables and `.env` is ignored. CI execution is an untrusted boundary; secrets must not be returned or logged.

## Phase 2 identity boundary

- User passwords are never stored in plaintext.
- Passwords use Spring Security Argon2.
- Email addresses are normalized before uniqueness checks.
- Refresh tokens are opaque random values; only SHA-256 hashes are persisted.
- Refresh tokens rotate and belong to a token family; reuse revokes the family.
- Refresh-token lookup uses a database write lock for concurrent rotation safety.
- Access tokens are short-lived JWTs containing only user ID and role.
- Authentication endpoints never serialize persistence entities.
- Credential failures are generic and do not reveal account existence.
- `USER` and `ADMIN` are domain roles translated at the security boundary.
- JWT signing material comes from `FORGECI_JWT_SECRET`; production must replace the local development default.

Future phases must preserve these boundaries when organizations, repositories, webhooks, and pipelines are introduced.
