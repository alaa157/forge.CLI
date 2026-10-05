package com.forgeci.api.test;
import com.forgeci.application.test.*;import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class FlakyTestController{
 private final FlakyTestService service;public FlakyTestController(FlakyTestService service){this.service=service;}
 @GetMapping("/flaky-tests") public List<FlakyTestAssessment> top(@RequestParam UUID repositoryId,@RequestParam(defaultValue="20")int limit){return service.top(repositoryId,limit);}
 @GetMapping("/flaky-tests/assessment") public FlakyTestAssessment assessment(@RequestParam String testId){return service.assess(testId);}
 @GetMapping("/repositories/{repositoryId}/tests/dashboard") public Map<String,Object> dashboard(@PathVariable UUID repositoryId){return service.dashboard(repositoryId);}
}