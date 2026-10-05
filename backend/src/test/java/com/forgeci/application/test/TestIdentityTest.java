package com.forgeci.application.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TestIdentityTest {

    @Test
    void canonicalIdentityIncludesRepository() {
        UUID repo = UUID.fromString("11111111-1111-1111-1111-111111111111");
        assertEquals(
                "forgeci::11111111-1111-1111-1111-111111111111::junit::com.example.UserTest::createsUser",
                TestIdentity.canonical(repo, "junit", "com.example.UserTest", "createsUser"));
    }

    @Test
    void sha256IsDeterministic() {
        assertEquals(TestIdentity.sha256("x"), TestIdentity.sha256("x"));
        assertNotEquals(TestIdentity.sha256("x"), TestIdentity.sha256("y"));
    }
}
