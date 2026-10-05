package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.StepRun;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRunRepository extends JpaRepository<StepRun, UUID> {
    List<StepRun> findAllByJobRunIdOrderByPositionAsc(UUID jobRunId);
}