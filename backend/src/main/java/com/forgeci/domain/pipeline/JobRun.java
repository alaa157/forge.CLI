package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_runs", schema = "forgeci",
       indexes = {
           @Index(name = "idx_job_runs_pipeline", columnList = "pipeline_run_id"),
           @Index(name = "idx_job_runs_status", columnList = "status")
       })
public class JobRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="pipeline_run_id", nullable=false)
    private UUID pipelineRunId;

    @Column(nullable=false, length=128)
    private String name;

    @Column(nullable=false, length=512)
    private String image;

    @Column(name="commands", nullable=false, columnDefinition="text")
    private String commands;

    @Column(name="depends_on", nullable=false, columnDefinition="text")
    private String dependsOn;

    @Column(name="artifact_paths", nullable=false, columnDefinition="text")
    private String artifactPaths = "[]";

    @Column(nullable=false, length=10)
    private String priority = "NORMAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private JobRunStatus status;

    @Column(name="lease_id")
    private UUID leaseId;

    @Column(name="lease_worker_id")
    private UUID leaseWorkerId;

    @Column(name="lease_expires_at")
    private Instant leaseExpiresAt;

    @Column(name="created_at",nullable=false,updatable=false)
    private Instant createdAt;

    @Column(name="started_at")
    private Instant startedAt;

    @Column(name="finished_at")
    private Instant finishedAt;

    @Version
    private long version;

    protected JobRun() {}

    public JobRun(UUID pipelineRunId, String name, String image, String commands, String dependsOn) {
        if (pipelineRunId == null) throw new IllegalArgumentException("pipelineRunId is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (image == null || image.isBlank()) throw new IllegalArgumentException("image is required");
        this.pipelineRunId = pipelineRunId;
        this.name = name;
        this.image = image;
        this.commands = commands == null ? "[]" : commands;
        this.dependsOn = dependsOn == null ? "[]" : dependsOn;
        this.artifactPaths = "[]";
        this.priority = "NORMAL";
        this.status = JobRunStatus.PENDING;
    }

    @PrePersist void onCreate() { createdAt = Instant.now(); }

    public void queue() { transition(JobRunStatus.QUEUED); }
    public void start() { transition(JobRunStatus.RUNNING); startedAt = Instant.now(); }
    public void succeed() { transition(JobRunStatus.SUCCEEDED); finishedAt = Instant.now(); clearLease(); }
    public void fail() { transition(JobRunStatus.FAILED); finishedAt = Instant.now(); clearLease(); }
    public void cancel() { transition(JobRunStatus.CANCELLED); finishedAt = Instant.now(); clearLease(); }
    public void timeOut() { transition(JobRunStatus.TIMED_OUT); finishedAt = Instant.now(); clearLease(); }
    public void skip() { transition(JobRunStatus.SKIPPED); finishedAt = Instant.now(); clearLease(); }

    public void acquireLease(UUID leaseId, UUID workerId, Instant expiresAt) {
        this.leaseId = leaseId;
        this.leaseWorkerId = workerId;
        this.leaseExpiresAt = expiresAt;
    }

    public void renewLease(Instant expiresAt) {
        this.leaseExpiresAt = expiresAt;
    }

    public void clearExpiredLeaseAndRequeue() {
        if (status != JobRunStatus.RUNNING) {
            throw new IllegalStateException("Only RUNNING jobs can be requeued after lease expiry");
        }
        clearLease();
        this.status = JobRunStatus.QUEUED;
        this.startedAt = null;
    }

    private void clearLease() {
        this.leaseId = null;
        this.leaseWorkerId = null;
        this.leaseExpiresAt = null;
    }

    private void transition(JobRunStatus target) {
        if (!allowed(status, target)) throw new IllegalStateException("Invalid job run transition: " + status + " -> " + target);
        status = target;
    }

    private boolean allowed(JobRunStatus from, JobRunStatus to) {
        return switch (from) {
            case PENDING -> to == JobRunStatus.QUEUED || to == JobRunStatus.SKIPPED || to == JobRunStatus.CANCELLED;
            case QUEUED -> to == JobRunStatus.RUNNING || to == JobRunStatus.CANCELLED || to == JobRunStatus.SKIPPED;
            case RUNNING -> to == JobRunStatus.SUCCEEDED || to == JobRunStatus.FAILED
                    || to == JobRunStatus.CANCELLED || to == JobRunStatus.TIMED_OUT;
            default -> false;
        };
    }

    public UUID getId(){return id;}
    public UUID getPipelineRunId(){return pipelineRunId;}
    public String getName(){return name;}
    public String getImage(){return image;}
    public String getCommands(){return commands;}
    public String getDependsOn(){return dependsOn;}
    /** JSON-encoded list of artifact glob patterns from the pipeline definition. */
    public String getArtifactPaths(){return artifactPaths;}
    public void setArtifactPaths(String artifactPaths){this.artifactPaths=artifactPaths==null?"[]":artifactPaths;}
    public String getPriority(){return priority;}
    public JobRunStatus getStatus(){return status;}
    public UUID getLeaseId(){return leaseId;}
    public UUID getLeaseWorkerId(){return leaseWorkerId;}
    public Instant getLeaseExpiresAt(){return leaseExpiresAt;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getStartedAt(){return startedAt;}
    public Instant getFinishedAt(){return finishedAt;}
}
