package com.forgeci.application.pipeline;

public interface JobExecutor {
    ExecutionResult execute(JobExecutionRequest request);
}
