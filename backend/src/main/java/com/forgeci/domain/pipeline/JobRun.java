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

    /** Phase 17.2 — attempt number (1-based). */
    @Column(name="attempt_number", nullable=false)
    private int attemptNumber = 1;

    @Column(name="attempt_id", nullable=false)
    private UUID attemptId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name="last_failure_type", length=40)
    private FailureType lastFailureType;

    @Column(name="cancellation_requested", nullable=false)
    private boolean cancellationRequested = false;

    @Column(name="max_retries", nullable=false)
    private int maxRetries = 0;

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
        this.attemptNumber = 1;
        this.attemptId = UUID.randomUUID();
        this.maxRetries = 0;
    }

    @PrePersist void onCreate() { createdAt = Instant.now(); }

    public void queue() { transition(JobRunStatus.QUEUED); }
    public void start() { transition(JobRunStatus.RUNNING); startedAt = Instant.now(); }
    public void succeed() { transition(JobRunStatus.SUCCEEDED); finishedAt = Instant.now(); clearLease(); }
    public void fail() { transition(JobRunStatus.FAILED); finishedAt = Instant.now(); clearLease(); }
    public void cancel() { transition(JobRunStatus.CANCELLED); finishedAt = Instant.now(); clearLease(); }
    public void timeOut() { transition(JobRunStatus.TIMED_OUT); finishedAt = Instant.now(); clearLease(); }
    public void skip() { transition(JobRunStatus.SKIPPED); finishedAt = Instant.now(); clearLease(); }

    public void requestCancellation() { this.cancellationRequested = true; }

    public void recordFailure(FailureType type) { this.lastFailureType = type; }

    public void setMaxRetries(int maxRetries) { this.maxRetries = Math.max(0, maxRetries); }

    /**
     * Phase 17.2/17.3 — requeue a failed job for another attempt when policy allows.
     * Returns true if a new attempt was started.
     */
    public boolean scheduleRetryIfEligible() {
        if (status != JobRunStatus.FAILED && status != JobRunStatus.TIMED_OUT) {
            return false;
        }
        if (cancellationRequested) {
            return false;
        }
        if (lastFailureType != null && !lastFailureType.isRetryable()) {
            return false;
        }
        if (attemptNumber > maxRetries) {
            return false;
        }
        // attemptNumber is current finished attempt; next attempt = attemptNumber + 1
        // allowed when attemptNumber <= maxRetries (e.g. maxRetries=2 allows attempts 1,2,3)
        this.attemptNumber = attemptNumber + 1;
        this.attemptId = UUID.randomUUID();
        this.startedAt = null;
        this.finishedAt = null;
        clearLease();
        this.status = JobRunStatus.QUEUED;
        return true;
    }

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
        this.lastFailureType = FailureType.WORKER_FAILURE;
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
    public String getArtifactPaths(){return artifactPaths;}
    public void setArtifactPaths(String artifactPaths){this.artifactPaths=artifactPaths==null?"[]":artifactPaths;}\n    public String getCacheKey(){return cacheKey;}\n    public void setCacheKey(String cacheKey){this.cacheKey=cacheKey;}\n    public String getCachePaths(){return cachePaths;}\n    public void setCachePaths(String cachePaths){this.cachePaths=cachePaths==null?"[]":cachePaths;}\n    public String getSecretNames(){return secretNames;}\n    public void setSecretNames(String secretNames){this.secretNames=secretNames==null?"[]":secretNames;}
    public String getPriority(){return priority;}
    public JobRunStatus getStatus(){return status;}
    public int getAttemptNumber(){return attemptNumber;}
    public UUID getAttemptId(){return attemptId;}
    public FailureType getLastFailureType(){return lastFailureType;}
    public boolean isCancellationRequested(){return cancellationRequested;}
    public int getMaxRetries(){return maxRetries;}
    public UUID getLeaseId(){return leaseId;}
    public UUID getLeaseWorkerId(){return leaseWorkerId;}
    public Instant getLeaseExpiresAt(){return leaseExpiresAt;}
    public Instant getCreatedAt(){return createdAt;}
    public Instant getStartedAt(){return startedAt;}
    public Instant getFinishedAt(){return finishedAt;}
}
