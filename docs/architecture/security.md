# Security Architecture

Local credentials come from environment variables and .env is ignored. CI execution is an untrusted boundary; secrets must not be returned or logged.