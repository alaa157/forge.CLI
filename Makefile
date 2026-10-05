SHELL := /bin/sh
.PHONY: help infra-up infra-down backend-run backend-test frontend-check test
help:
	@echo "infra-up infra-down backend-run backend-test frontend-check test"
infra-up:
	docker compose up -d
infra-down:
	docker compose down
backend-run:
	cd backend && mvn spring-boot:run
backend-test:
	cd backend && mvn test
frontend-check:
	cd frontend && npm install && npm run typecheck && npm run build
test: backend-test frontend-check
