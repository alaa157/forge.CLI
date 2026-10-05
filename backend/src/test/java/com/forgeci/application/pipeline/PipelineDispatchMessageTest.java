package com.forgeci.application.pipeline;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.UUID;
import org.junit.jupiter.api.Test;
class PipelineDispatchMessageTest {
 @Test void carriesDispatchIds(){UUID d=UUID.randomUUID(),p=UUID.randomUUID();var m=new PipelineDispatchMessage(d,p);assertEquals(d,m.dispatchId());assertEquals(p,m.pipelineRunId());}
}
