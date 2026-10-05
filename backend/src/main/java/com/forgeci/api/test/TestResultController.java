package com.forgeci.api.test;

import com.forgeci.application.test.TestResultService;
import com.forgeci.domain.test.TestExecution;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class TestResultController {
    private final TestResultService service;

    public TestResultController(TestResultService service) {
        this.service = service;
    }

    @PostMapping(value = "/jobs/{jobRunId}/test-results/junit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<TestExecution> ingest(
            @PathVariable UUID jobRunId,
            @RequestParam UUID repositoryId,
            @RequestParam String commitSha,
            @RequestParam String branch,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("JUnit XML file is empty");
        }
        if (file.getSize() > 20L * 1024 * 1024) {
            throw new IllegalArgumentException("JUnit XML file is too large");
        }
        return service.ingest(jobRunId, repositoryId, commitSha, branch, file.getInputStream());
    }

    @GetMapping("/tests/{id}/history")
    public List<TestExecution> history(@PathVariable String id) {
        return service.history(id);
    }

    /** Phase 14.3 historical comparison windows. */
    @GetMapping("/tests/{id}/history/windows")
    public Map<String, List<TestExecution>> historyWindows(@PathVariable String id) {
        return service.historyWindows(id);
    }

    @GetMapping("/repositories/{repositoryId}/tests")
    public List<TestExecution> list(
            @PathVariable UUID repositoryId, @RequestParam(required = false) String testId) {
        return service.list(repositoryId, testId);
    }

    @GetMapping("/repositories/{repositoryId}/tests/analytics")
    public Map<String, Object> analytics(
            @PathVariable UUID repositoryId, @RequestParam String testId) {
        return service.analytics(repositoryId, testId);
    }
}
