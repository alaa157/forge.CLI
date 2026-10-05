# ForgeCI REST API (Phase 15)

Base path: `/api/v1`

## Resources

| Resource | Paths |
|----------|--------|
| Auth | `/auth` |
| Users | `/users` |
| Organizations | `/organizations` |
| Repositories | `/repositories`, `/organizations/{orgId}/repositories` |
| Pipelines | `/pipelines` |
| Runs | `/runs` |
| Jobs | `/jobs` |
| Workers | `/workers`, `/internal/workers` |
| Artifacts | `/jobs/{id}/artifacts`, `/artifacts/{id}/download` |
| Tests | `/tests` |
| Flaky tests | `/flaky-tests` |
| Webhooks | `/webhooks/github` |

## Core endpoints

```text
POST   /auth/register
POST   /auth/login
POST   /auth/refresh

GET    /users/me

GET    /organizations
POST   /organizations

GET    /repositories?organizationId=
GET    /repositories/{id}
POST   /repositories/{id}/connect
DELETE /repositories/{id}/connect
POST   /organizations/{orgId}/repositories
DELETE /organizations/{orgId}/repositories/{id}

GET    /pipelines?repositoryId=
GET    /pipelines/{id}

GET    /runs?repositoryId=
GET    /runs/{id}
GET    /runs/{id}/jobs
POST   /runs/{id}/cancel
POST   /runs/{id}/retry

GET    /jobs/{id}
GET    /jobs/{id}/logs
GET    /jobs/{id}/artifacts

GET    /tests?repositoryId=
GET    /tests/{id}/history
GET    /tests/{id}/history/windows

GET    /flaky-tests?repositoryId=
GET    /flaky-tests/assessment?testId=
GET    /repositories/{id}/tests/dashboard

POST   /webhooks/github
```

Authentication: Bearer JWT (except auth, health, GitHub webhook/callback, internal worker routes).
