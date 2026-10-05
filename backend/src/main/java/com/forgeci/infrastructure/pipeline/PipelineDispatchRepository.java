package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PipelineDispatchRepository extends JpaRepository<PipelineDispatch,UUID>{
 List<PipelineDispatch> findTop50ByStatusOrderByCreatedAtAsc(PipelineDispatchStatus status);
 @Query(value="INSERT INTO forgeci.pipeline_dispatches (id,pipeline_run_id,status,attempts,created_at) VALUES (gen_random_uuid(),:pipelineRunId,'PENDING',0,CURRENT_TIMESTAMP) ON CONFLICT (pipeline_run_id) DO NOTHING RETURNING id",nativeQuery=true)
 UUID claimDispatch(@Param("pipelineRunId") UUID pipelineRunId);
}
