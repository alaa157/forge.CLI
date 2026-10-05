package com.forgeci.api.pipeline;
import com.forgeci.domain.pipeline.JobRun;import com.forgeci.infrastructure.pipeline.JobRunRepository;
import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/jobs")
public class JobController{
 private final JobRunRepository jobs;
 public JobController(JobRunRepository jobs){this.jobs=jobs;}
 @GetMapping("/{id}") public JobRun get(@PathVariable UUID id){return jobs.findById(id).orElseThrow(()->new NoSuchElementException("Job not found"));}
}
