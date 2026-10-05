package com.forgeci.application.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.domain.pipeline.JobRun;
import com.forgeci.domain.pipeline.JobRunStatus;
import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.domain.pipeline.PipelineRunStatus;
import com.forgeci.infrastructure.pipeline.JobRunRepository;
import com.forgeci.infrastructure.pipeline.PipelineRunRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Phase 7 scheduler: dependency resolution, queue ordering, concurrency, stale lease recovery. */
@Service
public class JobScheduler {
    private static final Logger log = LoggerFactory.getLogger(JobScheduler.class);

    private final PipelineRunRepository pipelineRuns;
    private final JobRunRepository jobRuns;
    private final ObjectMapper mapper;
    private final int globalConcurrency;

    public JobScheduler(
            PipelineRunRepository pipelineRuns,
            JobRunRepository jobRuns,
            ObjectMapper mapper,
            @Value("${forgeci.scheduler.global-concurrency:20}") int globalConcurrency,
            @Value("${forgeci.scheduler.organization-concurrency:5}") int organizationConcurrency,
            @Value("${forgeci.scheduler.repository-concurrency:2}") int repositoryConcurrency) {
        this.pipelineRuns = pipelineRuns;
        this.jobRuns = jobRuns;
        this.mapper = mapper;
        this.globalConcurrency = globalConcurrency;
    }

    @Scheduled(fixedDelayString = "${forgeci.pipeline.scheduler-delay-ms:500}")
    @Transactional
    public void tick() {
        recoverExpiredLeases();
        promotePipelineRuns();
        resolveDependenciesAndQueue();
    }

    private void recoverExpiredLeases() {
        Instant now = Instant.now();
        for (JobRun job : jobRuns.findExpiredLeases(now)) {
            try {
                job.clearExpiredLeaseAndRequeue();
                jobRuns.save(job);
                log.warn("Requeued job {} after lease expiry", job.getId());
            } catch (RuntimeException e) {
                log.warn("Unable to recover lease for job {}: {}", job.getId(), e.getMessage());
            }
        }
    }

    private void promotePipelineRuns() {
        for (PipelineRun run : pipelineRuns.findAllByStatusOrderByCreatedAtAsc(PipelineRunStatus.CREATED).stream().limit(50).toList()) {
            try {
                run.queue();
                pipelineRuns.save(run);
            } catch (RuntimeException e) {
                log.debug("Skip promote pipeline {}: {}", run.getId(), e.getMessage());
            }
        }
    }

    private void resolveDependenciesAndQueue() {
        long runningGlobal = jobRuns.countByStatus(JobRunStatus.RUNNING);
        if (runningGlobal >= globalConcurrency) {
            return;
        }

        List<JobRun> pending = jobRuns.findAllByStatus(JobRunStatus.PENDING);
        pending.sort(Comparator
                .comparing((JobRun j) -> priorityRank(j.getPriority()))
                .reversed()
                .thenComparing(JobRun::getCreatedAt));

        Map<UUID, List<JobRun>> byPipeline = new HashMap<>();
        for (JobRun job : pending) {
            byPipeline.computeIfAbsent(job.getPipelineRunId(), k -> new ArrayList<>()).add(job);
        }

        for (Map.Entry<UUID, List<JobRun>> entry : byPipeline.entrySet()) {
            List<JobRun> allJobs = jobRuns.findAllByPipelineRunIdOrderByNameAsc(entry.getKey());
            Map<String, JobRun> byName = new HashMap<>();
            for (JobRun j : allJobs) {
                byName.put(j.getName(), j);
            }

            for (JobRun job : entry.getValue()) {
                if (runningGlobal >= globalConcurrency) {
                    return;
                }
                List<String> deps = parseDeps(job.getDependsOn());
                boolean allSucceeded = true;
                boolean anyFailed = false;
                for (String dep : deps) {
                    JobRun depJob = byName.get(dep);
                    if (depJob == null) {
                        anyFailed = true;
                        break;
                    }
                    if (depJob.getStatus() == JobRunStatus.FAILED
                            || depJob.getStatus() == JobRunStatus.CANCELLED
                            || depJob.getStatus() == JobRunStatus.TIMED_OUT
                            || depJob.getStatus() == JobRunStatus.SKIPPED) {
                        anyFailed = true;
                        break;
                    }
                    if (depJob.getStatus() != JobRunStatus.SUCCEEDED) {
                        allSucceeded = false;
                    }
                }
                if (anyFailed) {
                    try {
                        job.skip();
                        jobRuns.save(job);
                    } catch (RuntimeException ignored) {
                    }
                    continue;
                }
                if (!allSucceeded) {
                    continue;
                }
                try {
                    job.queue();
                    jobRuns.save(job);
                    runningGlobal++;
                } catch (RuntimeException e) {
                    log.debug("Unable to queue job {}: {}", job.getId(), e.getMessage());
                }
            }
        }
    }

    private List<String> parseDeps(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.trim())) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static int priorityRank(String priority) {
        if (priority == null) {
            return 1;
        }
        return switch (priority) {
            case "HIGH" -> 2;
            case "LOW" -> 0;
            default -> 1;
        };
    }
}
