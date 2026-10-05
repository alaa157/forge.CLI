package com.forgeci.application.pipeline;

import com.forgeci.config.PipelineDispatchRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.infrastructure.pipeline.*;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transitional in-process executor (see ADR 0002). Prefer Go forge-runner in production.
 * Jobs must already be QUEUED by JobScheduler after dependency resolution.
 */
@Service
public class PipelineWorker {
    static final String CONSUMER = "pipeline-worker";

    private final PipelineRunRepository runs;
    private final JobRunRepository jobs;
    private final StepRunRepository steps;
    private final ProcessedMessageRepository processedMessages;
    private final ContainerExecutor executor;

    public PipelineWorker(
            PipelineRunRepository runs,
            JobRunRepository jobs,
            StepRunRepository steps,
            ProcessedMessageRepository processedMessages,
            ContainerExecutor executor) {
        this.runs = runs;
        this.jobs = jobs;
        this.steps = steps;
        this.processedMessages = processedMessages;
        this.executor = executor;
    }

    @RabbitListener(queues = PipelineDispatchRabbitConfig.QUEUE)
    @Transactional
    public void consume(PipelineDispatchMessage message) {
        if (processedMessages.claim(UUID.randomUUID(), message.messageId(), CONSUMER) != 1) {
            return;
        }

        PipelineRun run = runs.findById(message.pipelineRunId()).orElse(null);
        if (run == null || PipelineRun.isTerminal(run.getStatus())) {
            return;
        }
        if (run.getStatus() != PipelineRunStatus.QUEUED && run.getStatus() != PipelineRunStatus.RUNNING) {
            return;
        }

        if (run.getStatus() == PipelineRunStatus.QUEUED) {
            run.transitionTo(PipelineRunStatus.RUNNING);
            runs.save(run);
        }

        try {
            List<JobRun> jobRuns = jobs.findAllByPipelineRunIdOrderByNameAsc(run.getId());
            boolean anyFailed = false;

            for (JobRun job : jobRuns) {
                if (job.getStatus() == JobRunStatus.SKIPPED) {
                    continue;
                }
                if (job.getStatus() == JobRunStatus.PENDING) {
                    continue;
                }
                if (job.getStatus() == JobRunStatus.SUCCEEDED
                        || job.getStatus() == JobRunStatus.FAILED
                        || job.getStatus() == JobRunStatus.CANCELLED
                        || job.getStatus() == JobRunStatus.TIMED_OUT) {
                    if (job.getStatus() == JobRunStatus.FAILED
                            || job.getStatus() == JobRunStatus.TIMED_OUT
                            || job.getStatus() == JobRunStatus.CANCELLED) {
                        anyFailed = true;
                    }
                    continue;
                }
                if (job.getStatus() != JobRunStatus.QUEUED && job.getStatus() != JobRunStatus.RUNNING) {
                    continue;
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

                    int exitCode = executor.execute(
                            job.getImage(),
                            step.getCommand(),
                            job.getId().toString(),
                            run.getCommitSha());

                    if (exitCode == 124) {
                        step.timeOut();
                    } else if (exitCode == 0) {
                        step.succeed();
                    } else {
                        step.fail();
                    }
                    steps.save(step);
                    jobFailed = exitCode != 0;
                }

                if (jobFailed) {
                    job.fail();
                    anyFailed = true;
                } else {
                    job.succeed();
                }
                jobs.save(job);
            }

            boolean allTerminal = jobRuns.stream().allMatch(j ->
                    j.getStatus() == JobRunStatus.SUCCEEDED
                            || j.getStatus() == JobRunStatus.FAILED
                            || j.getStatus() == JobRunStatus.SKIPPED
                            || j.getStatus() == JobRunStatus.CANCELLED
                            || j.getStatus() == JobRunStatus.TIMED_OUT);
            if (allTerminal) {
                run.transitionTo(anyFailed ? PipelineRunStatus.FAILED : PipelineRunStatus.SUCCEEDED);
            }
        } catch (RuntimeException e) {
            if (!PipelineRun.isTerminal(run.getStatus())) {
                run.transitionTo(PipelineRunStatus.FAILED);
            }
        }

        runs.save(run);
    }
}
