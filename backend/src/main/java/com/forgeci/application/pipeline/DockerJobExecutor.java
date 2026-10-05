package com.forgeci.application.pipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Phase 10: checkout exact commit, hardened container, stop-on-fail per step. */
@Service
@Primary
public class DockerJobExecutor implements JobExecutor {
    private final String dockerBinary;
    private final WorkspaceManager workspaces;
    private final LogChunkService logs;

    public DockerJobExecutor(
            @Value("${forgeci.worker.docker-binary:docker}") String dockerBinary,
            WorkspaceManager workspaces,
            LogChunkService logs) {
        this.dockerBinary = dockerBinary;
        this.workspaces = workspaces;
        this.logs = logs;
    }

    @Override
    public ExecutionResult execute(JobExecutionRequest request) {
        Path workspace = workspaces.checkout(request.cloneUrl(), request.commitSha(), request.githubToken());
        try {
            List<String> args = new ArrayList<>();
            args.add(dockerBinary);
            args.add("run");
            args.add("--rm");
            args.add("--network");
            args.add("none");
            args.add("--read-only");
            args.add("--cap-drop");
            args.add("ALL");
            args.add("--security-opt");
            args.add("no-new-privileges");
            args.add("--pids-limit");
            args.add(String.valueOf(request.pidsLimit()));
            args.add("--cpus");
            args.add(String.valueOf(request.cpuLimit()));
            args.add("--memory");
            args.add(String.valueOf(request.memoryBytes()));
            args.add("--tmpfs");
            args.add("/tmp:rw,noexec,nosuid,size=64m");
            args.add("-v");
            args.add(workspace.toAbsolutePath() + ":/workspace:rw");
            args.add("-w");
            args.add("/workspace");
            args.add("--label");
            args.add("forgeci.job=" + request.jobId());
            args.add("--label");
            args.add("forgeci.commit=" + request.commitSha());
            for (Map.Entry<String, String> e : request.environment().entrySet()) {
                if (e.getKey() != null && e.getValue() != null
                        && e.getKey().matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    args.add("-e");
                    args.add(e.getKey() + "=" + e.getValue());
                }
            }
            args.add(request.image());
            args.add("sh");
            args.add("-lc");
            args.add(request.command());

            Process process = new ProcessBuilder(args).redirectErrorStream(false).start();
            ExecutorService io = Executors.newFixedThreadPool(2);
            AtomicLong sequence = new AtomicLong(logs.nextSequence(request.jobId()));
            Future<?> out = io.submit(() -> capture(process.getInputStream(), request.jobId(), "stdout", sequence));
            Future<?> err = io.submit(() -> capture(process.getErrorStream(), request.jobId(), "stderr", sequence));
            try {
                if (!process.waitFor(request.timeout().toMillis(), TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();
                    return new ExecutionResult(124, true);
                }
                out.get(5, TimeUnit.SECONDS);
                err.get(5, TimeUnit.SECONDS);
                return new ExecutionResult(process.exitValue(), false);
            } finally {
                io.shutdownNow();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start container runtime", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Worker interrupted", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Unable to capture container output", e);
        } finally {
            workspaces.cleanup(workspace);
        }
    }

    private void capture(InputStream input, UUID jobId, String stream, AtomicLong sequence) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logs.append(jobId, stream, sequence.getAndIncrement(), line + System.lineSeparator());
            }
        } catch (IOException e) {
            logs.append(jobId, stream, sequence.getAndIncrement(), "[log capture failed]" + System.lineSeparator());
        }
    }
}
