package com.forgeci.application.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ContainerExecutor {
    private final String dockerBinary;
    private final long timeoutSeconds;

    public ContainerExecutor(
            @Value("${forgeci.worker.docker-binary:docker}") String dockerBinary,
            @Value("${forgeci.worker.step-timeout-seconds:1800}") long timeoutSeconds) {
        this.dockerBinary = dockerBinary;
        this.timeoutSeconds = timeoutSeconds;
    }

    public int execute(String image, String command, String runId, String commitSha) {
        if (image == null || !image.matches("[A-Za-z0-9./:_@-]+")) {
            throw new IllegalArgumentException("Unsafe container image");
        }

        List<String> args = new ArrayList<>(List.of(
                dockerBinary, "run", "--rm",
                "--network", "none",
                "--read-only",
                "--tmpfs", "/tmp:rw,noexec,nosuid,size=64m",
                "--label", "forgeci.run=" + runId,
                "--label", "forgeci.commit=" + commitSha,
                image, "sh", "-lc", command));

        try {
            Process process = new ProcessBuilder(args)
                    .redirectErrorStream(true)
                    .inheritIO()
                    .start();

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return 124;
            }
            return process.exitValue();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start container runtime", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Worker interrupted", e);
        }
    }
}
