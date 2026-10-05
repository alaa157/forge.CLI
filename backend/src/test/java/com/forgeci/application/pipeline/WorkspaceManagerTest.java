package com.forgeci.application.pipeline;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class WorkspaceManagerTest {
    @Test
    void rejectsNonGithubCloneUrls() {
        WorkspaceManager manager=new WorkspaceManager("git");
        assertThrows(IllegalArgumentException.class,()->manager.checkout("https://example.com/repo.git","0123456789abcdef","token"));
    }
}
