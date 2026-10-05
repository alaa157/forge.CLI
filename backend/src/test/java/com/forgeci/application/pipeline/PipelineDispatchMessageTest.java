package com.forgeci.application.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PipelineDispatchMessageTest {
    @Test
    void carriesMessageAndDispatchIds() {
        UUID m = UUID.randomUUID();
        UUID d = UUID.randomUUID();
        UUID p = UUID.randomUUID();

        var message = new PipelineDispatchMessage(m, d, p);

        assertEquals(m, message.messageId());
        assertEquals(d, message.dispatchId());
        assertEquals(p, message.pipelineRunId());
        assertNotNull(message.messageId());
    }
}
