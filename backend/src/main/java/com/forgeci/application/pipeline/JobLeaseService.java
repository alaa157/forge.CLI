package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.JobRun;
import com.forgeci.domain.pipeline.JobRunStatus;
import com.forgeci.infrastructure.pipeline.JobRunRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobLeaseService {
    private final JobRunRepository jobRuns;
    private final long leaseSeconds;

    public JobLeaseService(
            JobRunRepository jobRuns,
            @Value("${forgeci.worker.lease-seconds:60}") long leaseSeconds) {
        this.jobRuns = jobRuns;
        this.leaseSeconds = leaseSeconds;
    }

    @Transactional
    public Lease acquire(UUID jobRunId, UUID workerId) {
        JobRun job = jobRuns.findById(jobRunId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (job.getStatus() != JobRunStatus.QUEUED) {
            throw new IllegalStateException("Job is not QUEUED");
        }
        UUID leaseId = UUID.randomUUID();
        Instant expires = Instant.now().plus(leaseSeconds, ChronoUnit.SECONDS);
        job.acquireLease(leaseId, workerId, expires);
        job.start();
        jobRuns.save(job);
        return new Lease(leaseId, workerId, expires, jobRunId);
    }

    @Transactional
    public void renew(UUID jobRunId, UUID leaseId, UUID workerId) {
        JobRun job = jobRuns.findById(jobRunId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        if (!leaseId.equals(job.getLeaseId()) || !workerId.equals(job.getLeaseWorkerId())) {
            throw new IllegalStateException("Lease fencing token mismatch");
        }
        Instant expires = Instant.now().plus(leaseSeconds, ChronoUnit.SECONDS);
        job.renewLease(expires);
        jobRuns.save(job);
    }

    public record Lease(UUID leaseId, UUID workerId, Instant expiresAt, UUID jobRunId) {}
}
