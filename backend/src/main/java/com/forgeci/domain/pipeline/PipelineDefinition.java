package com.forgeci.domain.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record PipelineDefinition(
        int version,
        Pipeline pipeline
) {
    public record Pipeline(
            String name,
            List<String> triggers,
            Defaults defaults,
            Map<String, Job> jobs
    ) {}

    public record Defaults(
            Integer timeout,
            Integer retries
    ) {}

    public record Job(
            String image,
            List<String> commands,
            @JsonProperty("depends_on") List<String> dependsOn,
            Map<String, String> environment,
            Integer timeout,
            Integer retries,
            Artifacts artifacts,
            Cache cache,
            @JsonProperty("working_directory") String workingDirectory
    ) {}

    public record Artifacts(
            List<String> paths
    ) {}

    public record Cache(
            String key,
            List<String> paths
    ) {}
}