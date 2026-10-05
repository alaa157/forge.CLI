package com.forgeci.application.test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Deterministic test identity: repository + framework + classname + test name.
 * Example: {@code forgeci::{repo}::junit::com.example.UserTest::createsUser}
 */
public final class TestIdentity {
    private TestIdentity() {}

    public static String canonical(UUID repositoryId, String framework, String className, String testName) {
        String repo = repositoryId == null ? "unknown" : repositoryId.toString();
        String fw = framework == null || framework.isBlank() ? "junit" : framework.trim();
        String cn = className == null ? "" : className.trim();
        String tn = testName == null ? "" : testName.trim();
        return "forgeci::" + repo + "::" + fw + "::" + cn + "::" + tn;
    }

    /** @deprecated Prefer {@link #canonical(UUID, String, String, String)} with repository. */
    @Deprecated
    public static String canonical(String framework, String className, String testName) {
        return "forgeci::" + framework + "::" + className + "::" + testName;
    }

    public static String sha256(String canonical) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
