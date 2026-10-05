package com.forgeci.application.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineDispatchMessageTest {
    @Test
    void carriesStableMessageAndDispatchIds() {
        UUID d = UUID.randomUUID();
        UUID p = UUID.randomUUID();

        var message = new PipelineDispatchMessage(d, d, p);

        assertEquals(d, message.messageId());
        assertEquals(d, message.dispatchId());
        assertEquals(p, message.pipelineRunId());
    }

    @Test
    void derivesMessageIdForLegacyPayload() {
        UUID d = UUID.randomUUID();
        UUID p = UUID.randomUUID();

        var message = new PipelineDispatchMessage(null, d, p);

        assertEquals(d, message.messageId());
    }
}
