package com.forgeci.application.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobConsumer {
 private static final String CONSUMER="job-worker";private static final Logger log=LoggerFactory.getLogger(JobConsumer.class);
 private final ProcessedMessageRepository processedMessages;private final JobRunRepository jobs;private final StepRunRepository steps;private final PipelineRunRepository runs;private final RepositoryConnectionRepository repositories;private final GitHubConnectionService connections;private final JobExecutor executor;private final Duration stepTimeout;private final int cpuLimit;private final long memoryBytes;private final long pidsLimit;private final ObjectMapper objectMapper;private final JobCancellationRegistry cancellations;
 public JobConsumer(ProcessedMessageRepository processedMessages,JobRunRepository jobs,StepRunRepository steps,PipelineRunRepository runs,RepositoryConnectionRepository repositories,GitHubConnectionService connections,JobExecutor executor,@Value("${forgeci.worker.step-timeout-seconds:1800}")long timeoutSeconds,@Value("${forgeci.worker.cpu-limit:1}")int cpuLimit,@Value("${forgeci.worker.memory-bytes:1073741824}")long memoryBytes,@Value("${forgeci.worker.pids-limit:256}")long pidsLimit,ObjectMapper objectMapper,JobCancellationRegistry cancellations){
  this.processedMessages=processedMessages;this.jobs=jobs;this.steps=steps;this.runs=runs;this.repositories=repositories;this.connections=connections;this.executor=executor;this.stepTimeout=Duration.ofSeconds(timeoutSeconds);this.cpuLimit=cpuLimit;this.memoryBytes=memoryBytes;this.pidsLimit=pidsLimit;this.objectMapper=objectMapper;this.cancellations=cancellations;
 }
 @RabbitListener(queues=ForgeCiRabbitConfig.JOBS_QUEUE) @Transactional
 public void consume(JobMessage message){
  if(processedMessages.claim(UUID.randomUUID(),message.messageId(),CONSUMER)!=1)return;
  JobRun job=jobs.findById(message.jobId()).orElse(null);if(job==null)return;
  if(!message.attemptId().equals(job.getAttemptId()))return;
  if(job.getStatus()!=JobRunStatus.QUEUED&&job.getStatus()!=JobRunStatus.RUNNING)return;
  PipelineRun run=runs.findById(job.getPipelineRunId()).orElse(null);if(run==null||PipelineRun.isTerminal(run.getStatus()))return;
  if(run.isCancellationRequested()||job.isCancellationRequested()){cancel(job,run);return;}
  if(run.getStatus()==PipelineRunStatus.QUEUED){run.start();runs.save(run);}
  RepositoryConnection repo=repositories.findById(run.getRepositoryId()).orElse(null);if(repo==null){finishFailure(job,run,JobFailureType.INFRASTRUCTURE_FAILURE);return;}
  String token;try{token=connections.webhookToken(repo.getOrganization().getId());}catch(RuntimeException e){log.warn("GitHub token unavailable for org {}",repo.getOrganization().getId());finishFailure(job,run,JobFailureType.INFRASTRUCTURE_FAILURE);return;}
  if(job.getStatus()==JobRunStatus.QUEUED){job.start();jobs.save(job);}
  cancellations.register(job.getId());
  boolean failed=false;JobFailureType failureType=null;
  try{
   for(StepRun step:steps.findAllByJobRunIdOrderByPositionAsc(job.getId())){
    if(run.isCancellationRequested()||job.isCancellationRequested()||Thread.currentThread().isInterrupted()){cancelStep(step);cancel(job,run);return;}
    if(failed){if(step.getStatus()==StepRunStatus.PENDING){step.skip();steps.save(step);}continue;}
    step.start();steps.save(step);
    try{
     JobExecutionRequest request=new JobExecutionRequest(job.getId(),run.getRepositoryId(),repo.getCloneUrl(),token,run.getCommitSha(),job.getImage(),step.getCommand(),Map.of(),stepTimeout,cpuLimit,memoryBytes,pidsLimit);
     ExecutionResult result=executor.execute(request);
     if(job.isCancellationRequested()||run.isCancellationRequested()){cancelStep(step);cancel(job,run);return;}
     if(result.timedOut()){step.timeOut();failureType=JobFailureType.TIMEOUT;failed=true;}
     else if(result.exitCode()==0){step.succeed();}
     else {step.fail();failureType=JobFailureType.TEST_FAILURE;failed=true;}
    }catch(RuntimeException e){
      if(job.isCancellationRequested()||run.isCancellationRequested()){cancelStep(step);cancel(job,run);return;}
      log.warn("Job {} attempt {} execution failed: {}",job.getId(),job.getAttemptNumber(),e.getMessage());if(step.getStatus()==StepRunStatus.RUNNING)step.fail();failureType=JobFailureType.INFRASTRUCTURE_FAILURE;failed=true;
    }
    steps.save(step);
   }
  }finally{cancellations.unregister(job.getId());}
  if(failed){finishFailure(job,run,failureType);return;}
  job.succeed();jobs.save(job);completeRunIfTerminal(run);
 }
 private void finishFailure(JobRun job,PipelineRun run,JobFailureType type){
  if(type==null)type=JobFailureType.WORKER_FAILURE;job.markFailure(type);if(type==JobFailureType.INFRASTRUCTURE_FAILURE||type==JobFailureType.WORKER_FAILURE){int retries=allowedRetries(run,job.getName());if(job.getAttemptNumber()<=retries){for(StepRun s:steps.findAllByJobRunIdOrderByPositionAsc(job.getId()))s.resetForRetry();job.retry();jobs.save(job);return;}}
  if(type==JobFailureType.TIMEOUT)job.timeOut();else job.fail();jobs.save(job);completeRunIfTerminal(run);
 }
 private int allowedRetries(PipelineRun run,String jobName){try{var d=objectMapper.readValue(run.getResolvedPipeline(),PipelineDefinition.class);var j=d.pipeline().jobs().get(jobName);if(j!=null&&j.retries()!=null)return Math.max(0,j.retries());return d.pipeline().defaults()!=null&&d.pipeline().defaults().retries()!=null?Math.max(0,d.pipeline().defaults().retries()):0;}catch(Exception e){return 0;}}
 private void completeRunIfTerminal(PipelineRun run){List<JobRun> all=jobs.findAllByPipelineRunIdOrderByNameAsc(run.getId());if(all.stream().allMatch(j->j.getStatus()==JobRunStatus.SUCCEEDED||j.getStatus()==JobRunStatus.FAILED||j.getStatus()==JobRunStatus.SKIPPED||j.getStatus()==JobRunStatus.CANCELLED||j.getStatus()==JobRunStatus.TIMED_OUT)){if(all.stream().anyMatch(j->j.getStatus()==JobRunStatus.FAILED||j.getStatus()==JobRunStatus.TIMED_OUT||j.getStatus()==JobRunStatus.CANCELLED))run.fail();else run.succeed();runs.save(run);}}
 private void cancelStep(StepRun step){if(step.getStatus()==StepRunStatus.RUNNING||step.getStatus()==StepRunStatus.PENDING){step.cancel();steps.save(step);}}
 private void cancel(JobRun job,PipelineRun run){if(job.getStatus()!=JobRunStatus.CANCELLED&&job.getStatus()!=JobRunStatus.SUCCEEDED&&job.getStatus()!=JobRunStatus.FAILED&&job.getStatus()!=JobRunStatus.TIMED_OUT&&job.getStatus()!=JobRunStatus.SKIPPED)job.cancel();if(!PipelineRun.isTerminal(run.getStatus()))run.cancel();jobs.save(job);runs.save(run);}
}
