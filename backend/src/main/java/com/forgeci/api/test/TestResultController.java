package com.forgeci.api.test;
import com.forgeci.application.test.TestResultService;import com.forgeci.domain.test.TestExecution;import java.io.*;import java.util.*;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class TestResultController{
 private final TestResultService service;
 public TestResultController(TestResultService service){this.service=service;}
 @GetMapping("/repositories/{repositoryId}/tests") public List<TestExecution> list(@PathVariable UUID repositoryId,@RequestParam(required=false)String testId){return service.list(repositoryId,testId);}
 @GetMapping("/repositories/{repositoryId}/tests/analytics") public Map<String,Object> analytics(@PathVariable UUID repositoryId,@RequestParam String testId){return service.analytics(repositoryId,testId);}
}