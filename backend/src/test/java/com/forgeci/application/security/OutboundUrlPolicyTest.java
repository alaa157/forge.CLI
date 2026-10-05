package com.forgeci.application.security;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.Test;

class OutboundUrlPolicyTest {

    private final OutboundUrlPolicy policy = new OutboundUrlPolicy();

    @Test
    void allowsPublicHttps() {
        assertDoesNotThrow(() -> policy.validate(URI.create("https://github.com/org/repo.git")));
    }

    @Test
    void blocksLocalhost() {
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("http://localhost/x")));
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("http://127.0.0.1/x")));
    }

    @Test
    void blocksMetadata() {
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.validate(URI.create("http://169.254.169.254/latest/meta-data")));
    }

    @Test
    void blocksNonHttpSchemes() {
        assertThrows(IllegalArgumentException.class, () -> policy.validate(URI.create("file:///etc/passwd")));
    }
}
