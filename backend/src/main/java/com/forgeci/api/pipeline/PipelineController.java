package com.forgeci.api.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.infrastructure.pipeline.PipelineRunRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 15 — formalize /pipelines resource.
 * Pipeline definitions are snapshotted on each run; this API exposes the latest
 * definition per repository (and detail by run id that holds the snapshot).
 */
@RestController
@RequestMapping("/api/v1/pipelines")
public class PipelineController {
    private final PipelineRunRepository runs;
    private final ObjectMapper mapper;

    public PipelineController(PipelineRunRepository runs, ObjectMapper mapper) {
        this.runs = runs;
        this.mapper = mapper;
    }

    @GetMapping
    public List<PipelineSummary> list(@RequestParam UUID repositoryId) {
        List<PipelineRun> all = runs.findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId);
        Map<String, PipelineSummary> byName = new LinkedHashMap<>();
        for (PipelineRun run : all) {
            String name = extractName(run);
            byName.putIfAbsent(name, PipelineSummary.from(run, name));
        }
        return new ArrayList<>(byName.values());
    }

    @GetMapping("/{id}")
    public PipelineDetail get(@PathVariable UUID id) {
        PipelineRun run = runs.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pipeline not found"));
        return PipelineDetail.from(run, extractName(run));
    }

    private String extractName(PipelineRun run) {
        try {
            JsonNode root = mapper.readTree(run.getResolvedPipeline());
            JsonNode name = root.path("pipeline").path("name");
            if (name.isTextual() && !name.asText().isBlank()) {
                return name.asText();
            }
        } catch (Exception ignored) {
            // fall through
        }
        return "pipeline";
    }

    public record PipelineSummary(
            UUID id,
            UUID repositoryId,
            String name,
            String forgeciVersion,
            Instant lastRunAt) {
        static PipelineSummary from(PipelineRun run, String name) {
            return new PipelineSummary(
                    run.getId(), run.getRepositoryId(), name, run.getForgeciVersion(), run.getCreatedAt());
        }
    }

    public record PipelineDetail(
            UUID id,
            UUID repositoryId,
            String name,
            String pipelineYaml,
            String resolvedPipeline,
            String jobGraph,
            String forgeciVersion,
            Instant lastRunAt) {
        static PipelineDetail from(PipelineRun run, String name) {
            return new PipelineDetail(
                    run.getId(),
                    run.getRepositoryId(),
                    name,
                    run.getPipelineYaml(),
                    run.getResolvedPipeline(),
                    run.getJobGraph(),
                    run.getForgeciVersion(),
                    run.getCreatedAt());
        }
    }
}
