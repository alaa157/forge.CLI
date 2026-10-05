package com.forgeci.infrastructure.worker;

import com.forgeci.domain.worker.Worker;
import com.forgeci.domain.worker.WorkerStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkerRepository extends JpaRepository<Worker, UUID> {
    List<Worker> findByStatus(WorkerStatus status);

    @Query("select w from Worker w where w.lastHeartbeatAt < :cutoff and w.status <> com.forgeci.domain.worker.WorkerStatus.OFFLINE")
    List<Worker> findStale(@Param("cutoff") Instant cutoff);
}
