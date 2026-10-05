# Contributing to ForgeCI

Thank you for contributing to ForgeCI.

## Development Workflow

Follow the project workflow defined in the master implementation plan:

1. Issue
2. Design
3. Domain model
4. Database change
5. Application logic
6. API
7. Tests
8. Integration tests
9. Observability
10. Security review
11. Documentation
12. Code review

For each task, keep changes focused and avoid implementing future phases unless a minimal dependency is strictly required.

## Implementation Guidelines

- Keep domain logic separate from infrastructure concerns.
- Do not put business logic inside controllers.
- Do not expose persistence entities directly through the API.
- Validate external input.
- Do not log secrets.
- Make message handlers idempotent.
- Respect existing state-machine rules.
- Avoid unnecessary dependencies.
- Do not rewrite unrelated code.

## Testing

New business logic should include unit tests. Infrastructure behavior should include integration tests where appropriate, including negative-path and concurrency/idempotency tests when relevant.

## Pull Requests

Pull requests should explain:

- What changed.
- Why it changed.
- Tests that were added or run.
- Any remaining risks or follow-up work.

Keep each pull request scoped to its intended roadmap task.
