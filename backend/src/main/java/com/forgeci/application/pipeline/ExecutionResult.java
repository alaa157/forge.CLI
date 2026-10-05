package com.forgeci.application.pipeline;

public record ExecutionResult(int exitCode, boolean timedOut) {}
