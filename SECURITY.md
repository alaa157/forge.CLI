# Security Policy

## Reporting a Vulnerability

Please do not disclose security vulnerabilities in public issues.

For a suspected vulnerability, contact the project maintainer privately with:

- A clear description of the vulnerability.
- Steps required to reproduce it.
- Potential impact.
- Any suggested mitigation.

Do not include secrets or sensitive credentials in reports.

## Security Principles

ForgeCI will follow these principles throughout development:

- Secrets must never be returned to clients.
- External input must be validated.
- CI containers are untrusted execution environments.
- Security boundaries must be explicit.
- Secrets must not be logged.
