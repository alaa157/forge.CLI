package com.forgeci.application.pipeline;

import java.util.UUID;

public record PipelineDispatchMessage(
        UUID messageId,
        UUID dispatchId,
        UUID pipelineRunId) {
}
