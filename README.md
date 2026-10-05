# ForgeCI

ForgeCI is a self-hosted, production-style CI/CD and test-intelligence platform.

The project connects GitHub repositories to reproducible CI pipeline execution, with an eventual focus on job scheduling, isolated execution, logs, artifacts, test history, and test intelligence.

## Project Status

ForgeCI is currently in the foundation stage. This repository is being built incrementally according to the master implementation plan.

## Repository Structure

The repository is a monorepo. The backend, frontend, infrastructure, documentation, and shared project configuration will be added in subsequent implementation tasks.

## Local Prerequisites

Task 1.1 currently requires:

- Git
- GNU Make

Later phases will add prerequisites such as Java 21, Maven, Node.js, Docker, and Docker Compose.

## Development Commands

Run:

```bash
make help
```

to list the development commands currently available.

## Configuration

Environment-specific configuration will be introduced as the corresponding components are implemented.

See `.env.example` for the environment-variable contract currently defined by the project.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

ForgeCI is licensed under the MIT License. See [LICENSE](LICENSE).
