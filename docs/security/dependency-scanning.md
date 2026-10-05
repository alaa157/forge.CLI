# Dependency security (Phase 20.4)

ForgeCI supports optional dependency scanners in CI. They are advisory by default
(`continue-on-error`) so a new CVE does not block merges until the team is ready.

## Recommended tools

| Tool | Scope | Command / integration |
|------|--------|------------------------|
| **OSV Scanner** | multi-ecosystem lockfiles | `.github/workflows/ci.yml` job `dependency-security` |
| **npm audit** | frontend | `cd frontend && npm audit` |
| **Trivy** | container images & FS | `trivy fs --severity high,critical .` |
| **pip-audit** | Python example projects | `pip-audit -r requirements.txt` |
| **Maven OWASP** | backend | `mvn org.owasp:dependency-check-maven:check` |

## Runner job example

Teams can add scanners as pipeline jobs in `.forgeci.yml`:

```yaml
jobs:
  security:
    image: aquasec/trivy:latest
    commands:
      - trivy fs --exit-code 1 --severity HIGH,CRITICAL .
```

Do not store scanner API keys in pipeline YAML — use organization/repository secrets.
