package com.forgeci.application.test;

public record FlakyTestAssessment(
        String testId,
        FlakinessClassification classification,
        double score,
        int sampleSize,
        double failureFrequency,
        double inconsistency,
        double recencyFailureRate,
        double durationInstability) {

    public FlakyTestAssessment {
        if (testId == null || testId.isBlank()) {
            throw new IllegalArgumentException("testId is required");
        }
        if (classification == null) {
            classification = FlakinessClassification.STABLE;
        }
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("score must be 0-100");
        }
    }
}
