package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PipelineDispatchRepository extends JpaRepository<PipelineDispatch,UUID>{
 List<PipelineDispatch> findTop50ByStatusOrderByCreatedAtAsc(PipelineDispatchStatus status);
 @Modifying
 @Query(value="INSERT INTO forgeci.pipeline_dispatches (id,pipeline_run_id,status,attempts,created_at) VALUES (:dispatchId,:pipelineRunId,'PENDING',0,CURRENT_TIMESTAMP) ON CONFLICT (pipeline_run_id) DO NOTHING",nativeQuery=true)
 int claimDispatch(@Param("dispatchId") UUID dispatchId,@Param("pipelineRunId") UUID pipelineRunId);
}
