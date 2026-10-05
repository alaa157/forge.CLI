package com.forgeci.application.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class SecretMaskerTest {

    @Test
    void masksRawSecretValues() {
        String out = SecretMasker.mask("token is abc123secret", List.of("abc123secret"));
        assertEquals("token is ********", out);
    }

    @Test
    void masksAssignmentForm() {
        String out = SecretMasker.mask("MY_TOKEN=abc123", List.of());
        assertEquals("MY_TOKEN=********", out);
    }

    @Test
    void leavesHarmlessLines() {
        String line = "Tests run: 42, Failures: 0";
        assertEquals(line, SecretMasker.mask(line, List.of()));
    }
}
