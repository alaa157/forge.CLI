package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.JobRun;
import com.forgeci.domain.pipeline.JobRunStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {
    List<JobRun> findAllByPipelineRunIdOrderByNameAsc(UUID pipelineRunId);

    List<JobRun> findAllByStatus(JobRunStatus status);

    long countByStatus(JobRunStatus status);

    @Query("select j from JobRun j where j.status = com.forgeci.domain.pipeline.JobRunStatus.RUNNING and j.leaseExpiresAt is not null and j.leaseExpiresAt < :now")
    List<JobRun> findExpiredLeases(@Param("now") Instant now);
}
