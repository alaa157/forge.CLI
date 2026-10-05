package com.forgeci.domain.worker;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workers", schema = "forgeci")
public class Worker {
    @Id
    private UUID id;

    @Column(nullable = false, length = 255)
    private String hostname;

    @Column(nullable = false, length = 64)
    private String version;

    @Column(nullable = false, columnDefinition = "text")
    private String capabilities = "[]";

    @Column(name = "max_concurrency", nullable = false)
    private int maxConcurrency = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkerStatus status = WorkerStatus.STARTING;

    @Column(name = "last_heartbeat_at", nullable = false)
    private Instant lastHeartbeatAt;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private Instant registeredAt;

    protected Worker() {}

    public Worker(UUID id, String hostname, String version, String capabilities, int maxConcurrency) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.hostname = hostname;
        this.version = version;
        this.capabilities = capabilities == null ? "[]" : capabilities;
        this.maxConcurrency = maxConcurrency <= 0 ? 1 : maxConcurrency;
        this.status = WorkerStatus.STARTING;
        Instant now = Instant.now();
        this.lastHeartbeatAt = now;
        this.registeredAt = now;
    }

    public void heartbeat() {
        this.lastHeartbeatAt = Instant.now();
        if (this.status == WorkerStatus.STARTING || this.status == WorkerStatus.OFFLINE) {
            this.status = WorkerStatus.READY;
        }
    }

    public void markReady() { this.status = WorkerStatus.READY; }
    public void markBusy() { this.status = WorkerStatus.BUSY; }
    public void markDraining() { this.status = WorkerStatus.DRAINING; }
    public void markOffline() { this.status = WorkerStatus.OFFLINE; }

    public UUID getId() { return id; }
    public String getHostname() { return hostname; }
    public String getVersion() { return version; }
    public String getCapabilities() { return capabilities; }
    public int getMaxConcurrency() { return maxConcurrency; }
    public WorkerStatus getStatus() { return status; }
    public Instant getLastHeartbeatAt() { return lastHeartbeatAt; }
    public Instant getRegisteredAt() { return registeredAt; }
}
