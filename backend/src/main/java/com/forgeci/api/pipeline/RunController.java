package com.forgeci.api.pipeline;
import com.forgeci.domain.pipeline.*;import com.forgeci.infrastructure.pipeline.JobRunRepository;import com.forgeci.infrastructure.pipeline.PipelineRunRepository;import com.forgeci.application.pipeline.PipelineRunService;import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/runs")
public class RunController{
 private final PipelineRunRepository runs;private final PipelineRunService service;private final JobRunRepository jobs;public RunController(PipelineRunRepository runs,PipelineRunService service,JobRunRepository jobs){this.runs=runs;this.service=service;this.jobs=jobs;}
 @GetMapping public List<PipelineRun> list(@RequestParam UUID repositoryId){return runs.findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId);}
 @PostMapping("/{id}/cancel") public PipelineRun cancel(@PathVariable UUID id){return service.cancel(id);}
 @PostMapping("/{id}/retry") public PipelineRun retry(@PathVariable UUID id){return service.retry(id);}
 @GetMapping("/{id}/jobs") public List<JobRun> jobs(@PathVariable UUID id){return jobs.findAllByPipelineRunIdOrderByNameAsc(id);}
 @GetMapping("/{id}") public PipelineRun get(@PathVariable UUID id){return runs.findById(id).orElseThrow(()->new NoSuchElementException("Run not found"));}
}