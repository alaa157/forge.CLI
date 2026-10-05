package com.forgeci.domain.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LogChunkTest {
    @Test
    void validatesStreamAndSequence() {
        UUID job=UUID.randomUUID();
        var chunk=new LogChunk(job,0,"stdout","hello",null);
        assertEquals(job,chunk.getJobRunId());
        assertThrows(IllegalArgumentException.class,()->new LogChunk(job,-1,"stdout","x",null));
        assertThrows(IllegalArgumentException.class,()->new LogChunk(job,0,"secret","x",null));
    }
}
