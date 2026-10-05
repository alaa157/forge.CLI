package com.forgeci.domain.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessedMessageTest {
    @Test
    void requiresMessageIdAndConsumer() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProcessedMessage(null, "pipeline-worker"));
        assertThrows(IllegalArgumentException.class,
                () -> new ProcessedMessage(UUID.randomUUID(), ""));
    }

    @Test
    void storesProcessingMetadata() {
        UUID id = UUID.randomUUID();
        ProcessedMessage message = new ProcessedMessage(id, "pipeline-worker");

        assertEquals(id, message.getMessageId());
        assertEquals("pipeline-worker", message.getConsumer());
        assertNotNull(message.getProcessedAt());
    }
}
