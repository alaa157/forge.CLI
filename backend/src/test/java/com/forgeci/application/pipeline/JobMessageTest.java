package com.forgeci.application.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobMessageTest {
    @Test
    void createsContractWithStableIdentifiers() {
        UUID jobId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID repositoryId = UUID.randomUUID();
        UUID pipelineRunId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UUID traceId = UUID.randomUUID();

        JobMessage message = JobMessage.create(jobId, attemptId, repositoryId, pipelineRunId, organizationId, traceId);

        assertNotNull(message.messageId());
        assertEquals(jobId, message.jobId());
        assertEquals(attemptId, message.attemptId());
        assertEquals(repositoryId, message.repositoryId());
        assertEquals(pipelineRunId, message.pipelineRunId());
        assertEquals(organizationId, message.organizationId());
        assertEquals(traceId, message.traceId());
        assertNotNull(message.createdAt());
    }

    @Test
    void rejectsMissingIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> new JobMessage(
                null, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now()));
    }
}
