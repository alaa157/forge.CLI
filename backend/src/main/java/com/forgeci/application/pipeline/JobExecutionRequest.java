package com.forgeci.application.pipeline;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record JobExecutionRequest(
        UUID jobId, UUID repositoryId, String cloneUrl, String githubToken, String commitSha,
        String image, String command, Map<String, String> environment,
        Duration timeout, int cpuLimit, long memoryBytes, long pidsLimit,
        List<String> artifactPaths) {
    public JobExecutionRequest {
        if (jobId == null || repositoryId == null) throw new IllegalArgumentException("job and repository IDs are required");
        if (cloneUrl == null || cloneUrl.isBlank() || githubToken == null || githubToken.isBlank()) throw new IllegalArgumentException("repository credentials are required");
        if (commitSha == null || !commitSha.matches("[0-9a-fA-F]{7,64}")) throw new IllegalArgumentException("commitSha is invalid");
        if (image == null || !image.matches("[A-Za-z0-9./:_@-]+")) throw new IllegalArgumentException("Unsafe container image");
        if (command == null || command.isBlank()) throw new IllegalArgumentException("command is required");
        if (timeout == null || timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("timeout must be positive");
        if (cpuLimit <= 0 || memoryBytes <= 0 || pidsLimit <= 0) throw new IllegalArgumentException("resource limits must be positive");
        environment = environment == null ? Map.of() : Map.copyOf(environment);
        artifactPaths = artifactPaths == null ? List.of() : List.copyOf(artifactPaths);\n        cachePaths = cachePaths == null ? List.of() : List.copyOf(cachePaths);\n        if (cacheKey != null && cacheKey.length() > 512) throw new IllegalArgumentException("cacheKey is too long");
    }
}
