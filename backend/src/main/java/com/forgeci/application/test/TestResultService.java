package com.forgeci.application.test;

import com.forgeci.domain.test.TestExecution;
import com.forgeci.domain.test.TestStatus;
import com.forgeci.infrastructure.test.TestExecutionRepository;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestResultService {
    private final JUnitXmlParser parser;
    private final TestExecutionRepository repository;

    public TestResultService(JUnitXmlParser parser, TestExecutionRepository repository) {
        this.parser = parser;
        this.repository = repository;
    }

    @Transactional
    public List<TestExecution> ingest(
            UUID jobRunId, UUID repositoryId, String commitSha, String branch, InputStream xml) {
        List<TestExecution> rows = new ArrayList<>();
        Instant now = Instant.now();
        for (JUnitTestCase t : parser.parse(xml)) {
            String id = TestIdentity.canonical(repositoryId, "junit", t.className(), t.name());
            rows.add(repository.save(new TestExecution(
                    id,
                    jobRunId,
                    repositoryId,
                    t.suite(),
                    t.className(),
                    t.name(),
                    "junit",
                    t.status(),
                    t.durationMs(),
                    t.failureMessage(),
                    t.stdout(),
                    t.stderr(),
                    commitSha,
                    branch,
                    now)));
        }
        return rows;
    }

    @Transactional(readOnly = true)
    public List<TestExecution> history(String testId) {
        return repository.findTop1000ByTestIdOrderByExecutedAtDesc(testId);
    }

    /** Phase 14.3 — last 10 / 30 / 100 runs for a test. */
    @Transactional(readOnly = true)
    public Map<String, List<TestExecution>> historyWindows(String testId) {
        List<TestExecution> all = repository.findTop1000ByTestIdOrderByExecutedAtDesc(testId);
        Map<String, List<TestExecution>> windows = new LinkedHashMap<>();
        windows.put("last10", all.stream().limit(10).toList());
        windows.put("last30", all.stream().limit(30).toList());
        windows.put("last100", all.stream().limit(100).toList());
        return windows;
    }

    @Transactional(readOnly = true)
    public List<TestExecution> list(UUID repositoryId, String testId) {
        return testId == null
                ? repository.findTop1000ByRepositoryIdOrderByExecutedAtDesc(repositoryId)
                : repository.findTop1000ByRepositoryIdAndTestIdOrderByExecutedAtDesc(repositoryId, testId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> analytics(UUID repositoryId, String testId) {
        List<TestExecution> rows =
                repository.findTop1000ByRepositoryIdAndTestIdOrderByExecutedAtDesc(repositoryId, testId);
        if (rows.isEmpty()) {
            return Map.of("testId", testId, "executionCount", 0);
        }
        long passed = rows.stream().filter(x -> x.getStatus() == TestStatus.PASSED).count();
        long failed = rows.stream().filter(x -> x.getStatus() == TestStatus.FAILED).count();
        long errors = rows.stream().filter(x -> x.getStatus() == TestStatus.ERROR).count();
        long skipped = rows.stream().filter(x -> x.getStatus() == TestStatus.SKIPPED).count();
        double avg = rows.stream().mapToLong(TestExecution::getDurationMs).average().orElse(0);
        List<Long> d = rows.stream().map(TestExecution::getDurationMs).sorted().toList();
        long p95 = d.get((int) Math.min(d.size() - 1, Math.ceil(d.size() * 0.95) - 1));
        int consecutive = 0;
        for (TestExecution x : rows) {
            if (x.getStatus() == TestStatus.FAILED || x.getStatus() == TestStatus.ERROR) {
                consecutive++;
            } else {
                break;
            }
        }
        int recentWindow = Math.min(20, rows.size());
        double recent = rows.stream()
                        .limit(recentWindow)
                        .filter(x -> x.getStatus() == TestStatus.FAILED || x.getStatus() == TestStatus.ERROR)
                        .count()
                / (double) recentWindow;
        return Map.of(
                "testId", testId,
                "executionCount", rows.size(),
                "passRate", passed / (double) rows.size(),
                "failureRate", (failed + errors) / (double) rows.size(),
                "averageDurationMs", avg,
                "p95DurationMs", p95,
                "recentFailureRate", recent,
                "consecutiveFailures", consecutive,
                "passed", passed,
                "failed", failed,
                "errors", errors,
                "skipped", skipped);
    }
}
