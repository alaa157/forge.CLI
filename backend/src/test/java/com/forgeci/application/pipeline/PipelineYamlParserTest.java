package com.forgeci.application.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.PipelineDefinition;
import org.junit.jupiter.api.Test;

class PipelineYamlParserTest {
    private final PipelineYamlParser parser = new PipelineYamlParser(new ObjectMapper());

    @Test
    void parsesValidConfiguration() {
        PipelineDefinition result = parser.parse("""
                version: 1
                pipeline:
                  name: backend
                  triggers: [push, pull_request]
                  jobs:
                    build:
                      image: maven:3.9-eclipse-temurin-21
                      commands:
                        - mvn clean package
                    test:
                      image: maven:3.9-eclipse-temurin-21
                      depends_on: [build]
                      commands:
                        - mvn test
                """);

        assertThat(result.version()).isEqualTo(1);
        assertThat(result.pipeline().name()).isEqualTo("backend");
        assertThat(result.pipeline().jobs()).containsKeys("build", "test");
        assertThat(result.pipeline().jobs().get("test").dependsOn()).containsExactly("build");
    }

    @Test
    void rejectsDuplicateYamlKeys() {
        assertThatThrownBy(() -> parser.parse("""
                version: 1
                version: 1
                pipeline:
                  name: backend
                  jobs:
                    build:
                      image: alpine:3
                      commands: [echo ok]
                """))
                .isInstanceOf(PipelineValidationException.class)
                .hasMessageContaining("Invalid ForgeCI pipeline configuration");
    }

    @Test
    void rejectsNonMappingRoot() {
        assertThatThrownBy(() -> parser.parse("- one\n- two"))
                .isInstanceOf(PipelineValidationException.class);
    }

    @Test
    void rejectsUnknownFields() {
        assertThatThrownBy(() -> parser.parse("""
                version: 1
                pipeline:
                  name: backend
                  jobs:
                    build:
                      image: alpine:3
                      commands: [echo ok]
                      unsupported: true
                """))
                .isInstanceOf(PipelineValidationException.class);
    }
}