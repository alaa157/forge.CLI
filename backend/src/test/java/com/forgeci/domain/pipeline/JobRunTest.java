package com.forgeci.domain.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobRunTest {
    @Test
    void supportsPendingQueuedRunningAndSuccess() {
        JobRun run = new JobRun(UUID.randomUUID(), "test", "node:22", "[\"npm test\"]", "[]");

        run.queue();
        run.start();
        run.succeed();

        assertThat(run.getStatus()).isEqualTo(JobRunStatus.SUCCEEDED);
    }

    @Test
    void canSkipPendingJob() {
        JobRun run = new JobRun(UUID.randomUUID(), "test", "node:22", "[\"npm test\"]", "[]");

        run.skip();

        assertThat(run.getStatus()).isEqualTo(JobRunStatus.SKIPPED);
    }

    @Test
    void rejectsTerminalTransition() {
        JobRun run = new JobRun(UUID.randomUUID(), "test", "node:22", "[\"npm test\"]", "[]");
        run.queue();
        run.start();
        run.fail();

        assertThatThrownBy(run::succeed)
                .isInstanceOf(IllegalStateException.class);
    }
}