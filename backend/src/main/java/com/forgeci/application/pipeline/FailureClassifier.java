package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.FailureType;
import org.springframework.stereotype.Component;

/** Maps execution outcomes to {@link FailureType} for retry policy decisions. */
@Component
public class FailureClassifier {

    public FailureType classify(ExecutionResult result, Exception error) {
        if (error != null) {
            String msg = error.getMessage() == null ? "" : error.getMessage().toLowerCase();
            if (msg.contains("interrupt") || msg.contains("worker")) {
                return FailureType.WORKER_FAILURE;
            }
            if (msg.contains("docker") || msg.contains("container") || msg.contains("network")) {
                return FailureType.INFRASTRUCTURE_FAILURE;
            }
            if (msg.contains("config") || msg.contains("yaml") || msg.contains("image")) {
                return FailureType.CONFIGURATION_FAILURE;
            }
            return FailureType.INFRASTRUCTURE_FAILURE;
        }
        if (result == null) {
            return FailureType.WORKER_FAILURE;
        }
        if (result.timedOut()) {
            return FailureType.TIMEOUT;
        }
        // Non-zero exit is treated as test/script failure (not retried by default).
        return FailureType.TEST_FAILURE;
    }
}
