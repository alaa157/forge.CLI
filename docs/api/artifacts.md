# Artifacts

Phase 12 lets jobs publish build outputs and reports.

## Collection

Jobs declare artifact paths in the pipeline definition:

```yaml
artifacts:
  paths:
    - target/*.jar
    - reports/**
```

After a step finishes, the executor matches the configured relative glob
patterns against the workspace, compresses the matches into a ZIP archive,
uploads it through the configured `ArtifactStore`, and persists the metadata
(name, object key, size, SHA-256, content type). Collection is bounded to
1000 files and 500 MiB per job.

## Storage backends

- `LocalArtifactStore` (default): stores objects under `forgeci.artifacts.local-root`; downloads are streamed through the API.
- `S3ArtifactStore` (`forgeci.artifacts.backend=s3`): stores objects in an S3-compatible service such as MinIO; the bucket is created automatically on startup and downloads use short-lived pre-signed URLs.

Object keys have the form:

```text
jobs/{jobRunId}/{uuid}/{name}
```

## API

```http
GET /api/v1/jobs/{id}/artifacts
GET /api/v1/artifacts/{id}/download
```

The download endpoint redirects to a signed URL when the storage backend
supports it, and streams the content otherwise.
