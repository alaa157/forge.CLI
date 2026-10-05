package com.forgeci.application.pipeline;

import com.forgeci.application.github.GitHubConnectionService;
import com.forgeci.config.PipelineDispatchRabbitConfig;
import com.forgeci.domain.pipeline.*;
import com.forgeci.domain.repository.RepositoryConnection;
import com.forgeci.infrastructure.pipeline.*;
import com.forgeci.infrastructure.repository.RepositoryConnectionRepository;
import java.time.Duration;
import java.util.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineWorker {
    static final String CONSUMER="pipeline-worker";
    private final PipelineRunRepository runs; private final JobRunRepository jobs; private final StepRunRepository steps;
    private final ProcessedMessageRepository processedMessages; private final RepositoryConnectionRepository repositories;
    private final GitHubConnectionService github; private final JobExecutor executor;
    private final long timeoutSeconds; private final int cpuLimit; private final long memoryBytes; private final long pidsLimit;

    public PipelineWorker(PipelineRunRepository runs,JobRunRepository jobs,StepRunRepository steps,ProcessedMessageRepository processedMessages,
                          RepositoryConnectionRepository repositories,GitHubConnectionService github,JobExecutor executor,
                          @Value("${forgeci.worker.step-timeout-seconds:1800}") long timeoutSeconds,
                          @Value("${forgeci.worker.cpu-limit:2}") int cpuLimit,
                          @Value("${forgeci.worker.memory-bytes:2147483648}") long memoryBytes,
                          @Value("${forgeci.worker.pids-limit:256}") long pidsLimit){
        this.runs=runs;this.jobs=jobs;this.steps=steps;this.processedMessages=processedMessages;this.repositories=repositories;this.github=github;this.executor=executor;
        this.timeoutSeconds=timeoutSeconds;this.cpuLimit=cpuLimit;this.memoryBytes=memoryBytes;this.pidsLimit=pidsLimit;
    }

    @RabbitListener(queues=PipelineDispatchRabbitConfig.QUEUE)
    @Transactional
    public void consume(PipelineDispatchMessage message){
        if(processedMessages.claim(UUID.randomUUID(),message.messageId(),CONSUMER)!=1)return;
        PipelineRun run=runs.findById(message.pipelineRunId()).orElse(null);
        if(run==null||PipelineRun.isTerminal(run.getStatus())||run.getStatus()!=PipelineRunStatus.QUEUED)return;
        RepositoryConnection repository=repositories.findById(run.getRepositoryId()).orElseThrow(()->new IllegalStateException("Repository not found"));
        String token=github.webhookToken(repository.getOrganization().getId());
        run.transitionTo(PipelineRunStatus.RUNNING);runs.save(run);
        try{
            boolean failed=false;
            for(JobRun job:jobs.findByPipelineRunIdOrderByJobNameAsc(run.getId())){
                if(failed){job.skip();jobs.save(job);continue;}
                job.queue();job.start();jobs.save(job);boolean jobFailed=false;
                for(StepRun step:steps.findByJobRunIdOrderByStepIndexAsc(job.getId())){
                    if(jobFailed){step.skip();steps.save(step);continue;}
                    step.start();steps.save(step);
                    var result=executor.execute(new JobExecutionRequest(job.getId(),run.getRepositoryId(),repository.getCloneUrl(),token,run.getCommitSha(),
                            job.getImage(),step.getCommand(),Map.of(),Duration.ofSeconds(timeoutSeconds),cpuLimit,memoryBytes,pidsLimit));
                    if(result.timedOut())step.timeOut();else if(result.exitCode()==0)step.succeed();else step.fail();
                    steps.save(step);jobFailed=result.timedOut()||result.exitCode()!=0;
                }
                if(jobFailed)job.fail();else job.succeed();jobs.save(job);failed=jobFailed;
            }
            run.transitionTo(failed?PipelineRunStatus.FAILED:PipelineRunStatus.SUCCEEDED);
        }catch(RuntimeException e){if(!PipelineRun.isTerminal(run.getStatus()))run.transitionTo(PipelineRunStatus.FAILED);}
        runs.save(run);
    }
}
