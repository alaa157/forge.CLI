package com.forgeci.application.pipeline;

public record PipelineValidationError(
        String path,
        String message
) {}