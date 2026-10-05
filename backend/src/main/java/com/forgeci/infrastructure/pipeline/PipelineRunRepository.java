package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.PipelineRun;
import com.forgeci.domain.pipeline.PipelineRunStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PipelineRunRepository extends JpaRepository<PipelineRun, UUID> {
    List<PipelineRun> findAllByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);
    List<PipelineRun> findAllByStatusOrderByCreatedAtAsc(PipelineRunStatus status);
}