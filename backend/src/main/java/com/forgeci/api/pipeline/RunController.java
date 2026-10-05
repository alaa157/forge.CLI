package com.forgeci.api.pipeline;
import com.forgeci.domain.pipeline.*;import com.forgeci.infrastructure.pipeline.PipelineRunRepository;import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/runs")
public class RunController{
 private final PipelineRunRepository runs;public RunController(PipelineRunRepository runs){this.runs=runs;}
 @GetMapping public List<PipelineRun> list(@RequestParam UUID repositoryId){return runs.findAllByRepositoryIdOrderByCreatedAtDesc(repositoryId);}
 @GetMapping("/{id}") public PipelineRun get(@PathVariable UUID id){return runs.findById(id).orElseThrow(()->new NoSuchElementException("Run not found"));}
}