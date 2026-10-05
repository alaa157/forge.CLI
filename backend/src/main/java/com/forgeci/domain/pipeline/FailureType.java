package com.forgeci.domain.pipeline;

/**
 * Phase 17.3 — classify job failures so only appropriate failures are retried.
 */
public enum FailureType {
    INFRASTRUCTURE_FAILURE,
    TEST_FAILURE,
    CONFIGURATION_FAILURE,
    TIMEOUT,
    WORKER_FAILURE;

    /** Whether this failure type is eligible for automatic retry by default. */
    public boolean isRetryable() {
        return this == INFRASTRUCTURE_FAILURE || this == TIMEOUT || this == WORKER_FAILURE;
    }
}
