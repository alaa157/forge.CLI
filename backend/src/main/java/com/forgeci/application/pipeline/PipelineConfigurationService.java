package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.PipelineDefinition;
import org.springframework.stereotype.Service;

@Service
public class PipelineConfigurationService {
    private final PipelineYamlParser parser;
    private final PipelineValidator validator;
    private final PipelineDagBuilder dagBuilder;

    public PipelineConfigurationService(
            PipelineYamlParser parser,
            PipelineValidator validator,
            PipelineDagBuilder dagBuilder) {
        this.parser = parser;
        this.validator = validator;
        this.dagBuilder = dagBuilder;
    }

    public PipelineConfiguration load(String yaml) {
        PipelineDefinition definition = parser.parse(yaml);
        validator.validate(definition);
        return new PipelineConfiguration(definition, dagBuilder.build(definition.pipeline()));
    }
}