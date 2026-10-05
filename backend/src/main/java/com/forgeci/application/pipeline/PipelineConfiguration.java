package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.PipelineDag;
import com.forgeci.domain.pipeline.PipelineDefinition;

public record PipelineConfiguration(
        PipelineDefinition definition,
        PipelineDag dag
) {}