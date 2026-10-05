package com.forgeci.application.pipeline;

import java.util.UUID;

public record PipelineDispatchMessage(
        UUID messageId,
        UUID dispatchId,
        UUID pipelineRunId) {

    public PipelineDispatchMessage {
        if (dispatchId == null) throw new IllegalArgumentException("dispatchId is required");
        if (pipelineRunId == null) throw new IllegalArgumentException("pipelineRunId is required");
        messageId = messageId == null ? dispatchId : messageId;
    }
}
