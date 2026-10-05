package com.forgeci.application.pipeline;

import com.forgeci.config.PipelineDispatchRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.infrastructure.pipeline.*;
import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (run == null || PipelineRun.isTerminal(run.getStatus())) return;
        if (run.getStatus() != PipelineRunStatus.QUEUED) return;

        run.transitionTo(PipelineRunStatus.RUNNING);
        runs.save(run);

        try {
            boolean failed = false;
            List<JobRun> jobRuns = jobs.findByPipelineRunIdOrderByJobNameAsc(run.getId());

            for (JobRun job : jobRuns) {
                if (failed) {
                    job.skip();
                    jobs.save(job);
                    continue;
                }

                job.queue();
                job.start();
                jobs.save(job);

                boolean jobFailed = false;
                for (StepRun step : steps.findByJobRunIdOrderByStepIndexAsc(job.getId())) {
                    if (jobFailed) {
                        step.skip();
                        steps.save(step);
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
                } else {
                    job.succeed();
                }
                jobs.save(job);
                failed = jobFailed;
            }

            run.transitionTo(failed ? PipelineRunStatus.FAILED : PipelineRunStatus.SUCCEEDED);
        } catch (RuntimeException e) {
            if (!PipelineRun.isTerminal(run.getStatus())) {
                run.transitionTo(PipelineRunStatus.FAILED);
            }
        }

        runs.save(run);
    }
}
