package com.forgeci.application.worker;

import com.forgeci.domain.worker.Worker;
import com.forgeci.domain.worker.WorkerStatus;
import com.forgeci.infrastructure.worker.WorkerRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkerService {
    private final WorkerRepository workers;
    private final long unhealthyAfterSeconds;

    public WorkerService(
            WorkerRepository workers,
            @Value("${forgeci.worker.unhealthy-after-seconds:30}") long unhealthyAfterSeconds) {
        this.workers = workers;
        this.unhealthyAfterSeconds = unhealthyAfterSeconds;
    }

    @Transactional
    public Worker register(UUID workerId, String hostname, String version, String capabilities, int maxConcurrency) {
        Worker existing = workerId != null ? workers.findById(workerId).orElse(null) : null;
        if (existing != null) {
            existing.heartbeat();
            existing.markReady();
            return workers.save(existing);
        }
        Worker w = new Worker(workerId, hostname, version, capabilities, maxConcurrency);
        w.markReady();
        return workers.save(w);
    }

    @Transactional
    public Worker heartbeat(UUID workerId) {
        Worker w = workers.findById(workerId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown worker: " + workerId));
        w.heartbeat();
        return workers.save(w);
    }

    @Scheduled(fixedDelayString = "${forgeci.worker.health-check-ms:10000}")
    @Transactional
    public void markUnhealthyWorkersOffline() {
        Instant cutoff = Instant.now().minus(unhealthyAfterSeconds, ChronoUnit.SECONDS);
        for (Worker w : workers.findStale(cutoff)) {
            w.markOffline();
            workers.save(w);
        }
    }

    public List<Worker> readyWorkers() {
        return workers.findByStatus(WorkerStatus.READY);
    }
}
