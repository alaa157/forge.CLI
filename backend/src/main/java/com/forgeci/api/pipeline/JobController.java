package com.forgeci.api.pipeline;
import com.forgeci.domain.pipeline.JobRun;import com.forgeci.infrastructure.pipeline.JobRunRepository;import com.forgeci.application.artifact.ArtifactService;import com.forgeci.domain.artifact.Artifact;
import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/jobs")
public class JobController{
 private final JobRunRepository jobs;private final ArtifactService artifacts;
 public JobController(JobRunRepository jobs,ArtifactService artifacts){this.jobs=jobs;this.artifacts=artifacts;}
 @GetMapping("/{id}") public JobRun get(@PathVariable UUID id){return jobs.findById(id).orElseThrow(()->new NoSuchElementException("Job not found"));}
 @GetMapping("/{id}/artifacts") public List<Artifact> artifacts(@PathVariable UUID id){return artifacts.list(id);}
}