# Pipeline Configuration

ForgeCI reads a `.forgeci.yml` file from the repository root. Phase 4 parses and validates this file before later phases turn it into executable pipeline and job records.

## Processing flow

```text
.forgeci.yml -> Safe YAML parser -> PipelineDefinition -> Validation -> PipelineDag
```

The parser uses SnakeYAML's `SafeConstructor`, rejects duplicate YAML keys, disables aliases, limits nesting/configuration size, and rejects unknown DTO fields.

## Required structure

```yaml
version: 1
pipeline:
  name: backend
  jobs:
    build:
      image: maven:3.9-eclipse-temurin-21
      commands:
        - mvn clean package
```

Optional job fields are `depends_on`, `environment`, `timeout`, `retries`, `artifacts.paths`, `cache.key`, `cache.paths`, and `working_directory`.

Validation rejects unsupported versions, missing images/commands, unknown or duplicate dependencies, dependency cycles, invalid timeout/retry values, malformed environment variables, unsafe paths, oversized configuration, and excessive job counts.

Artifact, cache, and working-directory paths must remain inside the job workspace: they must be relative and must not contain `..` traversal.

The DAG exposes a deterministic topological order plus prerequisite/dependent adjacency sets. Execution itself is intentionally deferred to later phases.
