package com.forgeci.domain.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineRunTest {
    private PipelineRun run() {
        return new PipelineRun(UUID.randomUUID(), "a".repeat(40), "main", "push",
                "version: 1", "{\"version\":1}", "{\"topologicalOrder\":[\"build\"]}", "0.1.0");
    }

    @Test
    void followsOnlyLegalPipelineTransitions() {
        PipelineRun run = run();

        assertThat(run.getStatus()).isEqualTo(PipelineRunStatus.CREATED);
        run.queue();
        run.start();
        run.succeed();

        assertThat(run.getStatus()).isEqualTo(PipelineRunStatus.SUCCEEDED);
        assertThat(run.getStartedAt()).isNotNull();
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void rejectsInvalidPipelineTransition() {
        PipelineRun run = run();

        assertThatThrownBy(run::start)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED -> RUNNING");
    }

    @Test
    void allowsCancellationFromQueuedAndRunningOnly() {
        PipelineRun queued = run();
        queued.queue();
        queued.cancel();

        assertThat(queued.getStatus()).isEqualTo(PipelineRunStatus.CANCELLED);

        PipelineRun completed = run();
        completed.queue();
        completed.start();
        completed.succeed();

        assertThatThrownBy(completed::cancel)
                .isInstanceOf(IllegalStateException.class);
    }
}