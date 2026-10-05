package com.forgeci.application.test;

import com.forgeci.domain.test.TestExecution;
import com.forgeci.domain.test.TestStatus;
import com.forgeci.infrastructure.test.TestExecutionRepository;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FlakyTestService {
    private final TestExecutionRepository repo;

    public FlakyTestService(TestExecutionRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public FlakyTestAssessment assess(String testId) {
        List<TestExecution> rows = repo.findTop1000ByTestIdOrderByExecutedAtDesc(testId);
        if (rows.isEmpty()) {
            return new FlakyTestAssessment(
                    testId, FlakinessClassification.STABLE, 0, 0, 0, 0, 0, 0);
        }
        int n = rows.size();
        double failures = rows.stream().filter(this::failed).count() / (double) n;
        double inconsistency = Math.min(1, failures * (1 - failures) * 4);
        int recentN = Math.min(10, n);
        double recent = rows.subList(0, recentN).stream().filter(this::failed).count() / (double) recentN;
        double olderN = n > recentN ? n - recentN : 0;
        double older = olderN == 0
                ? recent
                : rows.subList(recentN, n).stream().filter(this::failed).count() / olderN;
        double recency = Math.min(1, Math.max(0, recent - older) + recent * 0.5);
        double duration = durationInstability(rows);
        double score = Math.min(100, 100 * (0.30 * failures + 0.30 * inconsistency + 0.25 * recency + 0.15 * duration));

        FlakinessClassification cls;
        if (recentN >= 3 && recent == 1.0 && olderN >= 5 && older < 0.5) {
            cls = FlakinessClassification.NEWLY_FLAKY;
        } else if (failures >= 0.8 && n >= 5) {
            cls = FlakinessClassification.PERSISTENT_FAILURE;
        } else if (score >= 60) {
            cls = FlakinessClassification.FLAKY;
        } else if (score >= 30) {
            cls = FlakinessClassification.LIKELY_FLAKY;
        } else {
            cls = FlakinessClassification.STABLE;
        }

        return new FlakyTestAssessment(
                testId, cls, round(score), n, failures, inconsistency, recent, duration);
    }

    @Transactional(readOnly = true)
    public List<FlakyTestAssessment> top(UUID repositoryId, int limit) {
        List<TestExecution> rows = repo.findTop1000ByRepositoryIdOrderByExecutedAtDesc(repositoryId);
        return rows.stream()
                .collect(Collectors.groupingBy(TestExecution::getTestId))
                .keySet()
                .stream()
                .map(this::assess)
                .sorted(Comparator.comparingDouble(FlakyTestAssessment::score).reversed())
                .limit(Math.max(1, Math.min(100, limit)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard(UUID repositoryId) {
        List<TestExecution> rows = repo.findTop1000ByRepositoryIdOrderByExecutedAtDesc(repositoryId);
        Set<String> ids = rows.stream()
                .map(TestExecution::getTestId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<FlakyTestAssessment> all = ids.stream().map(this::assess).toList();
        return Map.of(
                "topFlakyTests",
                all.stream()
                        .sorted(Comparator.comparingDouble(FlakyTestAssessment::score).reversed())
                        .limit(10)
                        .toList(),
                "newFlakyTests",
                all.stream()
                        .filter(x -> x.classification() == FlakinessClassification.NEWLY_FLAKY)
                        .sorted(Comparator.comparingDouble(FlakyTestAssessment::score).reversed())
                        .limit(10)
                        .toList(),
                "testsGettingWorse",
                all.stream()
                        .filter(x -> x.recencyFailureRate() > x.failureFrequency())
                        .sorted(Comparator.comparingDouble(FlakyTestAssessment::recencyFailureRate).reversed())
                        .limit(10)
                        .toList(),
                "slowestTests",
                ids.stream()
                        .map(this::latest)
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingLong(TestExecution::getDurationMs).reversed())
                        .limit(10)
                        .toList(),
                "recentlyFailedTests",
                rows.stream().filter(this::failed).limit(10).toList());
    }

    private TestExecution latest(String id) {
        var x = repo.findTop1000ByTestIdOrderByExecutedAtDesc(id);
        return x.isEmpty() ? null : x.get(0);
    }

    private boolean failed(TestExecution x) {
        return x.getStatus() == TestStatus.FAILED || x.getStatus() == TestStatus.ERROR;
    }

    private double durationInstability(List<TestExecution> r) {
        double avg = r.stream().mapToLong(TestExecution::getDurationMs).average().orElse(0);
        if (avg == 0) {
            return 0;
        }
        double sd = Math.sqrt(
                r.stream().mapToDouble(x -> Math.pow(x.getDurationMs() - avg, 2)).average().orElse(0));
        return Math.min(1, sd / avg);
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
