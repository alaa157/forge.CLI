package com.forgeci.application.pipeline;

import com.forgeci.config.PipelineDispatchRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.infrastructure.pipeline.*;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class PipelineWorker {
    private final PipelineRunRepository runs;
    private final JobRunRepository jobs;
    private final StepRunRepository steps;
    private final ContainerExecutor executor;

    public PipelineWorker(PipelineRunRepository runs, JobRunRepository jobs, StepRunRepository steps, ContainerExecutor executor) {
        this.runs = runs;
        this.jobs = jobs;
        this.steps = steps;
        this.executor = executor;
    }

    @RabbitListener(queues = PipelineDispatchRabbitConfig.QUEUE)
    public void consume(PipelineDispatchMessage message) {
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
                    job.transitionTo(JobRunStatus.SKIPPED);
                    jobs.save(job);
                    continue;
                }

                job.transitionTo(JobRunStatus.QUEUED);
                job.transitionTo(JobRunStatus.RUNNING);
                jobs.save(job);

                boolean jobFailed = false;
                for (StepRun step : steps.findByJobRunIdOrderByStepIndexAsc(job.getId())) {
                    if (jobFailed) {
                        step.transitionTo(StepRunStatus.SKIPPED);
                        steps.save(step);
                        continue;
                    }

                    step.transitionTo(StepRunStatus.RUNNING);
                    steps.save(step);

                    int exitCode = executor.execute(
                            job.getImage(),
                            step.getCommand(),
                            job.getId().toString(),
                            run.getCommitSha());

                    step.complete(exitCode);
                    steps.save(step);
                    jobFailed = exitCode != 0;
                }

                job.complete(jobFailed ? 1 : 0);
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
