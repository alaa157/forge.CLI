package com.forgeci.application.pipeline;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgeci.application.github.GitHubConnectionService;
import com.forgeci.application.secret.SecretService;
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

/** Phase 9/10/17: idempotent consumer with secrets, cache, cancel, and auto-retry. */
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
    private final ObjectMapper objectMapper;
    private final SecretService secrets;
    private final FailureClassifier failureClassifier;
    private final JobPublisher jobPublisher;
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
            SecretService secrets,
            JobExecutor executor,
            ObjectMapper objectMapper,
            FailureClassifier failureClassifier,
            JobPublisher jobPublisher,
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
        this.secrets = secrets;
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.failureClassifier = failureClassifier;
        this.jobPublisher = jobPublisher;
        this.stepTimeout = Duration.ofSeconds(timeoutSeconds);
        this.cpuLimit = cpuLimit;
        this.memoryBytes = memoryBytes;
        this.pidsLimit = pidsLimit;
    }

    private List<String> listOf(String json) {
        try {
            List<String> v = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return v == null ? List.of() : v;
        } catch (Exception e) {
            throw new IllegalStateException("Invalid job metadata", e);
        }
    }

    private List<String> artifactPathsOf(JobRun job) {
        try {
            List<String> paths = objectMapper.readValue(job.getArtifactPaths(), new TypeReference<List<String>>() {});
            return paths == null ? List.of() : paths;
        } catch (Exception e) {
            log.warn("Ignoring unreadable artifact paths for job {}: {}", job.getId(), e.getMessage());
            return List.of();
        }
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
        if (job.isCancellationRequested()) {
            job.cancel();
            jobs.save(job);
            finalizePipeline(job.getPipelineRunId());
            return;
        }

        PipelineRun run = runs.findById(job.getPipelineRunId()).orElse(null);
        if (run == null || PipelineRun.isTerminal(run.getStatus())) {
            return;
        }
        if (run.isCancellationRequested()) {
            job.requestCancellation();
            job.cancel();
            jobs.save(job);
            finalizePipeline(run.getId());
            return;
        }

        if (run.getStatus() == PipelineRunStatus.QUEUED) {
            run.start();
            runs.save(run);
        }

        RepositoryConnection repo = repositories.findById(run.getRepositoryId()).orElse(null);
        if (repo == null) {
            job.recordFailure(FailureType.CONFIGURATION_FAILURE);
            job.fail();
            jobs.save(job);
            finalizePipeline(run.getId());
            return;
        }

        String token;
        try {
            token = connections.webhookToken(repo.getOrganization().getId());
        } catch (RuntimeException e) {
            log.warn("No GitHub token for org {}: {}", repo.getOrganization().getId(), e.getMessage());
            job.recordFailure(FailureType.CONFIGURATION_FAILURE);
            job.fail();
            jobs.save(job);
            finalizePipeline(run.getId());
            return;
        }

        if (job.getStatus() == JobRunStatus.QUEUED) {
            job.start();
            jobs.save(job);
        }

        boolean jobFailed = false;
        boolean timedOut = false;
        FailureType failureType = null;
        for (StepRun step : steps.findAllByJobRunIdOrderByPositionAsc(job.getId())) {
            JobRun fresh = jobs.findById(job.getId()).orElse(job);
            if (fresh.isCancellationRequested() || Thread.currentThread().isInterrupted()) {
                if (step.getStatus() == StepRunStatus.PENDING || step.getStatus() == StepRunStatus.RUNNING) {
                    step.cancel();
                    steps.save(step);
                }
                job.cancel();
                jobs.save(job);
                finalizePipeline(run.getId());
                return;
            }

            if (jobFailed) {
                if (step.getStatus() == StepRunStatus.PENDING) {
                    step.skip();
                    steps.save(step);
                }
                continue;
            }

            step.start();
            steps.save(step);

            Map<String, String> environment = new java.util.HashMap<>();
            environment.putAll(secrets.resolve(
                    repo.getOrganization().getId(), repo.getId(), listOf(job.getSecretNames())));

            JobExecutionRequest request = new JobExecutionRequest(
                    job.getId(),
                    run.getRepositoryId(),
                    repo.getCloneUrl(),
                    token,
                    run.getCommitSha(),
                    job.getImage(),
                    step.getCommand(),
                    environment,
                    stepTimeout,
                    cpuLimit,
                    memoryBytes,
                    pidsLimit,
                    artifactPathsOf(job),
                    job.getCacheKey(),
                    listOf(job.getCachePaths()));

            try {
                ExecutionResult result = executor.execute(request);
                if (result.timedOut()) {
                    step.timeOut();
                    jobFailed = true;
                    timedOut = true;
                    failureType = FailureType.TIMEOUT;
                } else if (result.exitCode() == 0) {
                    step.succeed();
                } else {
                    step.fail();
                    jobFailed = true;
                    failureType = failureClassifier.classify(result, null);
                }
            } catch (RuntimeException e) {
                step.fail();
                jobFailed = true;
                failureType = failureClassifier.classify(null, e);
                log.warn("Step failed for job {}: {}", job.getId(), e.getMessage());
            }
            steps.save(step);
        }

        if (jobFailed) {
            if (timedOut) {
                job.timeOut();
            } else {
                job.fail();
            }
            if (failureType != null) {
                job.recordFailure(failureType);
            }
            jobs.save(job);

            if (job.scheduleRetryIfEligible()) {
                for (StepRun step : steps.findAllByJobRunIdOrderByPositionAsc(job.getId())) {
                    step.resetForRetry();
                    steps.save(step);
                }
                jobs.save(job);
                log.info("Scheduling retry attempt {} for job {}", job.getAttemptNumber(), job.getId());
                jobPublisher.republish(job);
                return;
            }
        } else {
            job.succeed();
            jobs.save(job);
        }

        finalizePipeline(run.getId());
    }

    private void finalizePipeline(UUID runId) {
        PipelineRun run = runs.findById(runId).orElse(null);
        if (run == null || PipelineRun.isTerminal(run.getStatus())) {
            return;
        }
        List<JobRun> all = jobs.findAllByPipelineRunIdOrderByNameAsc(runId);
        boolean allTerminal = all.stream().allMatch(j ->
                j.getStatus() == JobRunStatus.SUCCEEDED
                        || j.getStatus() == JobRunStatus.FAILED
                        || j.getStatus() == JobRunStatus.SKIPPED
                        || j.getStatus() == JobRunStatus.CANCELLED
                        || j.getStatus() == JobRunStatus.TIMED_OUT);
        if (!allTerminal) {
            return;
        }
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
