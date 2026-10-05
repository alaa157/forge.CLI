package com.forgeci.application.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.PipelineDefinition;
import org.junit.jupiter.api.Test;

class PipelineValidatorTest {
    private final PipelineYamlParser parser = new PipelineYamlParser(new ObjectMapper());
    private final PipelineValidator validator = new PipelineValidator();

    @Test
    void acceptsValidPipeline() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: backend
                  defaults: {timeout: 600, retries: 1}
                  jobs:
                    build:
                      image: maven:3.9-eclipse-temurin-21
                      commands: [mvn clean package]
                    test:
                      image: maven:3.9-eclipse-temurin-21
                      depends_on: [build]
                      commands: [mvn test]
                """);

        validator.validate(definition);
    }

    @Test
    void rejectsMissingDependency() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: backend
                  jobs:
                    test:
                      image: alpine:3
                      depends_on: [build]
                      commands: [echo test]
                """);

        assertThatThrownBy(() -> validator.validate(definition))
                .isInstanceOf(PipelineValidationException.class)
                .satisfies(error -> assertThat(((PipelineValidationException) error).errors())
                        .anyMatch(e -> e.path().contains("depends_on") && e.message().contains("Unknown dependency")));
    }

    @Test
    void rejectsCycles() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: cycle
                  jobs:
                    a:
                      image: alpine:3
                      depends_on: [c]
                      commands: [echo a]
                    b:
                      image: alpine:3
                      depends_on: [a]
                      commands: [echo b]
                    c:
                      image: alpine:3
                      depends_on: [b]
                      commands: [echo c]
                """);

        assertThatThrownBy(() -> validator.validate(definition))
                .isInstanceOf(PipelineValidationException.class)
                .satisfies(error -> assertThat(((PipelineValidationException) error).errors())
                        .anyMatch(e -> e.message().contains("Dependency cycle detected")));
    }

    @Test
    void rejectsUnsafeArtifactPath() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: artifacts
                  jobs:
                    build:
                      image: alpine:3
                      commands: [echo build]
                      artifacts:
                        paths: [../secret.txt, /tmp/out]
                """);

        assertThatThrownBy(() -> validator.validate(definition))
                .isInstanceOf(PipelineValidationException.class)
                .satisfies(error -> assertThat(((PipelineValidationException) error).errors())
                        .hasSize(2));
    }

    @Test
    void rejectsInvalidLimits() {
        PipelineDefinition definition = parser.parse("""
                version: 1
                pipeline:
                  name: limits
                  jobs:
                    build:
                      image: alpine:3
                      commands: [echo build]
                      timeout: 0
                      retries: 11
                """);

        assertThatThrownBy(() -> validator.validate(definition))
                .isInstanceOf(PipelineValidationException.class)
                .satisfies(error -> assertThat(((PipelineValidationException) error).errors())
                        .hasSize(2));
    }
}