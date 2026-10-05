# ForgeCI

Self-hosted, production-style CI/CD and test-intelligence platform.

## Phase 1
This phase provides the monorepo foundation, Spring Boot backend, Next.js frontend, local PostgreSQL/Redis/RabbitMQ/MinIO/Prometheus/Grafana infrastructure, Flyway, and health endpoints.

## Prerequisites
Git, GNU Make, Java 21, Maven 3.9+, Node.js 20+, npm 10+, Docker Engine + Compose v2.

## Commands
```bash
cp .env.example .env
make help
make infra-up
make backend-test
make frontend-check
```

Endpoints: `/api/v1/health`, `/actuator/health`, `/actuator/prometheus`.
