package com.forgeci.application.pipeline;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.PipelineDefinition;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@Component
public class PipelineYamlParser {
    private static final int MAX_CONFIGURATION_BYTES = 256 * 1024;

    private final ObjectMapper objectMapper;
    private final Yaml yaml;

    public PipelineYamlParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(0);
        options.setNestingDepthLimit(50);
        options.setCodePointLimit(MAX_CONFIGURATION_BYTES);
        this.yaml = new Yaml(new SafeConstructor(options));
    }

    public PipelineDefinition parse(String yamlText) {
        if (yamlText == null || yamlText.isBlank()) {
            throw invalid("$", "Configuration must not be empty");
        }
        if (yamlText.getBytes(StandardCharsets.UTF_8).length > MAX_CONFIGURATION_BYTES) {
            throw invalid("$", "Configuration exceeds 256 KiB");
        }

        final Object parsed;
        try {
            parsed = yaml.load(yamlText);
        } catch (RuntimeException ex) {
            throw invalid("$", "Malformed or unsafe YAML: " + safeMessage(ex));
        }

        if (!(parsed instanceof Map<?, ?>)) {
            throw invalid("$", "Configuration root must be a YAML mapping");
        }

        try {
            return objectMapper.convertValue(parsed, PipelineDefinition.class);
        } catch (IllegalArgumentException ex) {
            throw invalid("$", "Configuration has invalid field types or unknown fields: " + safeMessage(ex));
        }
    }

    private PipelineValidationException invalid(String path, String message) {
        return new PipelineValidationException(
                java.util.List.of(new PipelineValidationError(path, message)));
    }

    private static String safeMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.replaceAll("(?i)(password|token|secret|key)\\s*[:=]\\s*[^,;\\s]+", "$1=***");
    }
}