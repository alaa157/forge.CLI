package com.forgeci.domain.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class StepRunTest {
    @Test
    void followsStepLifecycle() {
        StepRun run = new StepRun(UUID.randomUUID(), 0, "npm test");

        run.start();
        run.succeed();

        assertThat(run.getStatus()).isEqualTo(StepRunStatus.SUCCEEDED);
    }

    @Test
    void rejectsSkippingRunningStep() {
        StepRun run = new StepRun(UUID.randomUUID(), 0, "npm test");
        run.start();

        assertThatThrownBy(run::skip)
                .isInstanceOf(IllegalStateException.class);
    }
}