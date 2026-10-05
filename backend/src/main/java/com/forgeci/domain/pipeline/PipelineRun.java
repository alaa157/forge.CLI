package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pipeline_runs", schema = "forgeci",
       indexes = {
           @Index(name = "idx_pipeline_runs_repository", columnList = "repository_id"),
           @Index(name = "idx_pipeline_runs_status", columnList = "status"),
           @Index(name = "idx_pipeline_runs_created_at", columnList = "created_at")
       })
public class PipelineRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="repository_id", nullable=false)
    private UUID repositoryId;

    @Column(name="commit_sha", nullable=false, length=64)
    private String commitSha;

    @Column(nullable=false, length=255)
    private String branch;

    @Column(nullable=false, length=50)
    private String trigger;

    @Column(name="pipeline_yaml", nullable=false, columnDefinition="text")
    private String pipelineYaml;

    @Column(name="resolved_pipeline", nullable=false, columnDefinition="text")
    private String resolvedPipeline;

    @Column(name="job_graph", nullable=false, columnDefinition="text")
    private String jobGraph;

    @Column(name="forgeci_version", nullable=false, length=64)
    private String forgeciVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PipelineRunStatus status;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @Column(name="started_at")
    private Instant startedAt;

    @Column(name="finished_at")
    private Instant finishedAt;

    @Version
    private long version;

    protected PipelineRun() {}

    public PipelineRun(UUID repositoryId, String commitSha, String branch, String trigger,
                       String pipelineYaml, String resolvedPipeline, String jobGraph,
                       String forgeciVersion) {
        this.repositoryId = require(repositoryId, "repositoryId");
        this.commitSha = requireText(commitSha, "commitSha", 64);
        this.branch = requireText(branch, "branch", 255);
        this.trigger = requireText(trigger, "trigger", 50);
        this.pipelineYaml = requireText(pipelineYaml, "pipelineYaml", Integer.MAX_VALUE);
        this.resolvedPipeline = requireText(resolvedPipeline, "resolvedPipeline", Integer.MAX_VALUE);
        this.jobGraph = requireText(jobGraph, "jobGraph", Integer.MAX_VALUE);
        this.forgeciVersion = requireText(forgeciVersion, "forgeciVersion", 64);
        this.status = PipelineRunStatus.CREATED;
    }

    @PrePersist void onCreate() { createdAt = Instant.now(); }

    public void queue() { transition(PipelineRunStatus.QUEUED); }
    public void start() { transition(PipelineRunStatus.RUNNING); startedAt = Instant.now(); }
    public void succeed() { transition(PipelineRunStatus.SUCCEEDED); finishedAt = Instant.now(); }
    public void fail() { transition(PipelineRunStatus.FAILED); finishedAt = Instant.now(); }
    public void cancel() { transition(PipelineRunStatus.CANCELLED); finishedAt = Instant.now(); }
    public void timeOut() { transition(PipelineRunStatus.TIMED_OUT); finishedAt = Instant.now(); }

    private void transition(PipelineRunStatus target) {
        if (!isAllowed(status, target)) {
            throw new IllegalStateException("Invalid pipeline run transition: " + status + " -> " + target);
        }
        status = target;
    }

    private static boolean isAllowed(PipelineRunStatus from, PipelineRunStatus to) {
        return switch (from) {
            case CREATED -> to == PipelineRunStatus.QUEUED;
            case QUEUED -> to == PipelineRunStatus.RUNNING || to == PipelineRunStatus.CANCELLED;
            case RUNNING -> to == PipelineRunStatus.SUCCEEDED || to == PipelineRunStatus.FAILED
                    || to == PipelineRunStatus.CANCELLED || to == PipelineRunStatus.TIMED_OUT;
            default -> false;
        };
    }

    private static <T> T require(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " is required");
        return value;
    }

    private static String requireText(String value, String name, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        if (value.length() > max) throw new IllegalArgumentException(name + " is too long");
        return value;
    }

    public UUID getId() { return id; }
    public UUID getRepositoryId() { return repositoryId; }
    public String getCommitSha() { return commitSha; }
    public String getBranch() { return branch; }
    public String getTrigger() { return trigger; }
    public String getPipelineYaml() { return pipelineYaml; }
    public String getResolvedPipeline() { return resolvedPipeline; }
    public String getJobGraph() { return jobGraph; }
    public String getForgeciVersion() { return forgeciVersion; }
    public PipelineRunStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
}