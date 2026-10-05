package com.forgeci.application.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.PipelineDefinition;
import com.forgeci.domain.pipeline.PipelineDag;
import org.junit.jupiter.api.Test;

class PipelineDagBuilderTest {
    private final PipelineYamlParser parser = new PipelineYamlParser(new ObjectMapper());
    private final PipelineValidator validator = new PipelineValidator();
    private final PipelineDagBuilder builder = new PipelineDagBuilder();

    @Test
    void buildsDeterministicTopologicalOrder() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: dag
                  jobs:
                    deploy:
                      image: alpine:3
                      depends_on: [test, lint]
                      commands: [echo deploy]
                    test:
                      image: alpine:3
                      depends_on: [build]
                      commands: [echo test]
                    lint:
                      image: alpine:3
                      depends_on: [build]
                      commands: [echo lint]
                    build:
                      image: alpine:3
                      commands: [echo build]
                """);

        validator.validate(definition);
        PipelineDag dag = builder.build(definition.pipeline());

        assertThat(dag.topologicalOrder()).containsExactly("build", "lint", "test", "deploy");
        assertThat(dag.dependencies().get("deploy")).containsExactlyInAnyOrder("test", "lint");
        assertThat(dag.dependents().get("build")).containsExactlyInAnyOrder("test", "lint");
    }
}