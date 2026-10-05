package com.forgeci.application.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JobExecutionRequestTest {
    @Test
    void rejectsInvalidImageAndCommit() {
        UUID id=UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,()->new JobExecutionRequest(id,id,"https://github.com/a/b.git","token","not-sha","bad;image","echo hi",Map.of(),Duration.ofSeconds(10),1,1024,10,List.of()));
    }

    @Test
    void acceptsBoundedExecutionRequest() {
        UUID id=UUID.randomUUID();
        var request=new JobExecutionRequest(id,id,"https://github.com/a/b.git","token","0123456789abcdef","alpine:3.20","echo hi",Map.of(),Duration.ofSeconds(30),2,1024*1024,64,List.of("target/*.jar"));
        assertEquals(2,request.cpuLimit());
        assertEquals(List.of("target/*.jar"),request.artifactPaths());
    }
}
