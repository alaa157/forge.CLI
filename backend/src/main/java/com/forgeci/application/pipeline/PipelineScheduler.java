package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.domain.pipeline.PipelineRunStatus;
import com.forgeci.infrastructure.pipeline.*;
import java.util.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PipelineScheduler {
 private final PipelineRunRepository pipelineRuns;
 private final PipelineDispatchRepository dispatches;
 public PipelineScheduler(PipelineRunRepository pipelineRuns,PipelineDispatchRepository dispatches){this.pipelineRuns=pipelineRuns;this.dispatches=dispatches;}
 @Scheduled(fixedDelayString="${forgeci.pipeline.scheduler-delay-ms:1000}")
 @Transactional
 public void scheduleCreatedRuns(){
  List<PipelineRun> candidates=pipelineRuns.findAllByStatusOrderByCreatedAtAsc(PipelineRunStatus.CREATED);
  for(PipelineRun run:candidates.stream().limit(50).toList()){
   UUID dispatchId=dispatches.claimDispatch(UUID.randomUUID(),run.getId());
   if(dispatchId==null)continue;
   run.queue();
   pipelineRuns.save(run);
  }
 }
}
