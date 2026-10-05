package com.forgeci.application.pipeline;

import java.time.Instant;
import java.util.UUID;

public record JobMessage(
        UUID messageId,
        UUID jobId,
        UUID attemptId,
        UUID repositoryId,
        UUID pipelineRunId,
        UUID organizationId,
        UUID traceId,
        Instant createdAt) {

    public JobMessage {
        if (messageId == null) throw new IllegalArgumentException("messageId is required");
        if (jobId == null) throw new IllegalArgumentException("jobId is required");
        if (attemptId == null) throw new IllegalArgumentException("attemptId is required");
        if (repositoryId == null) throw new IllegalArgumentException("repositoryId is required");
        if (pipelineRunId == null) throw new IllegalArgumentException("pipelineRunId is required");
        if (organizationId == null) throw new IllegalArgumentException("organizationId is required");
        if (traceId == null) throw new IllegalArgumentException("traceId is required");
        if (createdAt == null) throw new IllegalArgumentException("createdAt is required");
    }

    public static JobMessage create(
            UUID jobId,
            UUID attemptId,
            UUID repositoryId,
            UUID pipelineRunId,
            UUID organizationId,
            UUID traceId) {
        return new JobMessage(UUID.randomUUID(), jobId, attemptId, repositoryId,
                pipelineRunId, organizationId, traceId, Instant.now());
    }
}
