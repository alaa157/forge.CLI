package com.forgeci.application.pipeline;

import java.util.List;

public final class PipelineValidationException extends RuntimeException {
    private final List<PipelineValidationError> errors;

    public PipelineValidationException(List<PipelineValidationError> errors) {
        super("Invalid ForgeCI pipeline configuration");
        this.errors = List.copyOf(errors);
    }

    public List<PipelineValidationError> errors() {
        return errors;
    }
}