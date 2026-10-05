package com.forgeci.application.test;

import static org.junit.jupiter.api.Assertions.*;

import com.forgeci.domain.test.TestExecution;
import com.forgeci.domain.test.TestStatus;
import com.forgeci.infrastructure.test.TestExecutionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FlakyTestServiceTest {

    @Test
    void persistentFailuresScoreHigh() {
        var repo = Mockito.mock(TestExecutionRepository.class);
        UUID j = UUID.randomUUID();
        UUID r = UUID.randomUUID();
        List<TestExecution> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rows.add(new TestExecution(
                    "t", j, r, "s", "C", "x", "junit", TestStatus.FAILED, 100,
                    null, null, null, "abc", "main", Instant.now().minusSeconds(i)));
        }
        Mockito.when(repo.findTop1000ByTestIdOrderByExecutedAtDesc("t")).thenReturn(rows);
        var a = new FlakyTestService(repo).assess("t");
        assertEquals(FlakinessClassification.PERSISTENT_FAILURE, a.classification());
        assertTrue(a.score() > 50);
    }

    @Test
    void mixedOutcomesAreNotStable() {
        var repo = Mockito.mock(TestExecutionRepository.class);
        UUID j = UUID.randomUUID();
        UUID r = UUID.randomUUID();
        List<TestExecution> rows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            rows.add(new TestExecution(
                    "t", j, r, "s", "C", "x", "junit",
                    i % 2 == 0 ? TestStatus.FAILED : TestStatus.PASSED, 100,
                    null, null, null, "abc", "main", Instant.now().minusSeconds(i)));
        }
        Mockito.when(repo.findTop1000ByTestIdOrderByExecutedAtDesc("t")).thenReturn(rows);
        var a = new FlakyTestService(repo).assess("t");
        assertNotEquals(FlakinessClassification.STABLE, a.classification());
        assertTrue(a.score() > 0);
    }

    @Test
    void emptyHistoryIsStable() {
        var repo = Mockito.mock(TestExecutionRepository.class);
        Mockito.when(repo.findTop1000ByTestIdOrderByExecutedAtDesc("empty")).thenReturn(List.of());
        var a = new FlakyTestService(repo).assess("empty");
        assertEquals(FlakinessClassification.STABLE, a.classification());
        assertEquals(0, a.sampleSize());
    }
}
