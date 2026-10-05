package com.forgeci.application.pipeline;

import com.forgeci.domain.pipeline.LogChunk;
import com.forgeci.infrastructure.pipeline.LogChunkRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Phase 11: chunked durable log storage with live STOMP delivery.
 * Limits: 16 KiB per chunk, 10 MiB per job. Once the per-job limit is reached,
 * further chunks are dropped, a truncation marker chunk is persisted and
 * published, and the dropped chunks are counted in the
 * {@code forgeci.logs.dropped_chunks} metric.
 */
@Service
public class LogChunkService {
    private static final int MAX_CHUNK = 16 * 1024;
    private static final long MAX_LOG = 10L * 1024 * 1024;
    static final String TRUNCATION_MARKER =
            "[forgeci] log truncated: per-job limit of 10 MiB reached, further output was dropped";

    private final LogChunkRepository repository;
    private final SimpMessagingTemplate messaging;
    private final Counter droppedChunks;
    private final Set<UUID> truncationMarked = ConcurrentHashMap.newKeySet();

    public LogChunkService(LogChunkRepository repository, SimpMessagingTemplate messaging,
                           MeterRegistry meterRegistry) {
        this.repository = repository;
        this.messaging = messaging;
        this.droppedChunks = Counter.builder("forgeci.logs.dropped_chunks")
                .description("Log chunks dropped because the per-job log size limit was reached")
                .register(meterRegistry);
    }

    public long nextSequence(UUID jobRunId) {
        return repository.nextSequence(jobRunId);
    }

    /**
     * Appends log content as one or more chunks. Callers block on the database
     * write, which provides natural backpressure towards the producing stream.
     */
    @Transactional
    public synchronized void append(UUID jobRunId, String stream, long sequence, String content) {
        if (content == null || content.isEmpty()) return;
        String remaining = content;
        long next = sequence;
        long dropped = 0;
        while (!remaining.isEmpty()) {
            long stored = repository.totalBytes(jobRunId);
            if (stored >= MAX_LOG) {
                dropped++;
                remaining = remaining.substring(Math.min(MAX_CHUNK, remaining.length()));
                continue;
            }
            String chunk = remaining.substring(0, Math.min(MAX_CHUNK, remaining.length()));
            if (stored + chunk.length() > MAX_LOG) chunk = chunk.substring(0, (int) (MAX_LOG - stored));
            if (chunk.isEmpty()) {
                dropped++;
                break;
            }
            LogChunk saved = repository.save(new LogChunk(jobRunId, next++, stream, chunk, Instant.now()));
            publish(jobRunId, saved);
            remaining = remaining.substring(chunk.length());
        }
        if (dropped > 0) {
            droppedChunks.increment(dropped);
            markTruncated(jobRunId);
        }
    }

    private void markTruncated(UUID jobRunId) {
        if (!truncationMarked.add(jobRunId)) return;
        LogChunk marker = new LogChunk(jobRunId, repository.nextSequence(jobRunId), "stderr",
                TRUNCATION_MARKER, Instant.now());
        repository.save(marker);
        publish(jobRunId, marker);
    }

    private void publish(UUID jobRunId, LogChunk chunk) {
        messaging.convertAndSend("/topic/jobs/" + jobRunId + "/logs", (Object) new LogChunkMessage(
                chunk.getSequence(), chunk.getStream(), chunk.getContent(), chunk.getCreatedAt()));
    }

    @Transactional(readOnly = true)
    public List<LogChunk> list(UUID jobRunId, int tail, Long after, Instant since) {
        if (tail <= 0 || tail > 5000) throw new IllegalArgumentException("tail must be between 1 and 5000");
        if (after != null) return repository.findAfter(jobRunId, after, PageRequest.of(0, tail));
        if (since != null) return repository.findSince(jobRunId, since, PageRequest.of(0, tail));
        return repository.findTail(jobRunId, tail);
    }

    /** Live log payload published to /topic/jobs/{jobId}/logs. */
    public record LogChunkMessage(long sequence, String stream, String content, Instant timestamp) {}
}
