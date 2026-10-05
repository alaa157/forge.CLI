package com.forgeci.application.pipeline;

import com.forgeci.application.github.GitHubConnectionService;
import com.forgeci.config.ForgeCiRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.domain.repository.RepositoryConnection;
import com.forgeci.infrastructure.pipeline.*;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Phase 9/10: idempotent consumer for forgeci.jobs using DockerJobExecutor. */
@Service
public class JobConsumer {
    private static final String CONSUMER = "job-worker";
    private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);

    private final ProcessedMessageRepository processedMessages;
    private final JobRunRepository jobs;
    private final StepRunRepository steps;
    private final PipelineRunRepository runs;
    private final RepositoryConnectionRepository repositories;
    private final GitHubConnectionService connections;
    private final JobExecutor executor;
    private final Duration stepTimeout;
    private final int cpuLimit;
    private final long memoryBytes;
    private final long pidsLimit;

    public JobConsumer(
            ProcessedMessageRepository processedMessages,
            JobRunRepository jobs,
            StepRunRepository steps,
            PipelineRunRepository runs,
            RepositoryConnectionRepository repositories,
            GitHubConnectionService connections,
            JobExecutor executor,
            @Value("${forgeci.worker.step-timeout-seconds:1800}") long timeoutSeconds,
            @Value("${forgeci.worker.cpu-limit:1}") int cpuLimit,
            @Value("${forgeci.worker.memory-bytes:1073741824}") long memoryBytes,
            @Value("${forgeci.worker.pids-limit:256}") long pidsLimit) {
        this.processedMessages = processedMessages;
        this.jobs = jobs;
        this.steps = steps;
        this.runs = runs;
        this.repositories = repositories;
        this.connections = connections;
        this.executor = executor;
        this.stepTimeout = Duration.ofSeconds(timeoutSeconds);
        this.cpuLimit = cpuLimit;
        this.memoryBytes = memoryBytes;
        this.pidsLimit = pidsLimit;
    }

    @RabbitListener(queues = ForgeCiRabbitConfig.JOBS_QUEUE)
    @Transactional
    public void consume(JobMessage message) {
        if (processedMessages.claim(UUID.randomUUID(), message.messageId(), CONSUMER) != 1) {
            return;
        }

        JobRun job = jobs.findById(message.jobId()).orElse(null);
        if (job == null) {
            return;
        }
        if (job.getStatus() != JobRunStatus.QUEUED && job.getStatus() != JobRunStatus.RUNNING) {
            return;
        }

        PipelineRun run = runs.findById(job.getPipelineRunId()).orElse(null);
        if (run == null || PipelineRun.isTerminal(run.getStatus())) {
            return;
        }

        if (run.getStatus() == PipelineRunStatus.QUEUED) {
            run.start();
            runs.save(run);
        }

        RepositoryConnection repo = repositories.findById(run.getRepositoryId()).orElse(null);
        if (repo == null) {
            job.fail();
            jobs.save(job);
            return;
        }

        String token;
        try {
            token = connections.webhookToken(repo.getOrganization().getId());
        } catch (RuntimeException e) {
            log.warn("No GitHub token for org {}: {}", repo.getOrganization().getId(), e.getMessage());
            job.fail();
            jobs.save(job);
            return;
        }

        if (job.getStatus() == JobRunStatus.QUEUED) {
            job.start();
            jobs.save(job);
        }

        boolean jobFailed = false;
        for (StepRun step : steps.findAllByJobRunIdOrderByPositionAsc(job.getId())) {
            if (jobFailed) {
                if (step.getStatus() == StepRunStatus.PENDING) {
                    step.skip();
                    steps.save(step);
                }
                continue;
            }

            step.start();
            steps.save(step);

            JobExecutionRequest request = new JobExecutionRequest(
                    job.getId(),
                    run.getRepositoryId(),
                    repo.getCloneUrl(),
                    token,
                    run.getCommitSha(),
                    job.getImage(),
                    step.getCommand(),
                    Map.of(),
                    stepTimeout,
                    cpuLimit,
                    memoryBytes,
                    pidsLimit);

            ExecutionResult result = executor.execute(request);
            if (result.timedOut()) {
                step.timeOut();
                jobFailed = true;
            } else if (result.exitCode() == 0) {
                step.succeed();
            } else {
                step.fail();
                jobFailed = true;
            }
            steps.save(step);
        }

        if (jobFailed) {
            job.fail();
        } else {
            job.succeed();
        }
        jobs.save(job);

        List<JobRun> all = jobs.findAllByPipelineRunIdOrderByNameAsc(run.getId());
        boolean allTerminal = all.stream().allMatch(j ->
                j.getStatus() == JobRunStatus.SUCCEEDED
                        || j.getStatus() == JobRunStatus.FAILED
                        || j.getStatus() == JobRunStatus.SKIPPED
                        || j.getStatus() == JobRunStatus.CANCELLED
                        || j.getStatus() == JobRunStatus.TIMED_OUT);
        if (allTerminal) {
            boolean anyFailed = all.stream().anyMatch(j ->
                    j.getStatus() == JobRunStatus.FAILED
                            || j.getStatus() == JobRunStatus.TIMED_OUT
                            || j.getStatus() == JobRunStatus.CANCELLED);
            if (anyFailed) {
                run.fail();
            } else {
                run.succeed();
            }
            runs.save(run);
        }
    }
}
