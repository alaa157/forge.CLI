package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.JobRun;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
    List<JobRun> findAllByPipelineRunIdOrderByNameAsc(UUID pipelineRunId);
}