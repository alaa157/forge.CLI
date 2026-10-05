package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.LogChunk;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface LogChunkRepository extends JpaRepository<LogChunk, UUID> {
    @Query("select coalesce(sum(length(l.content)), 0) from LogChunk l where l.jobRunId = :jobRunId")
    long totalBytes(@Param("jobRunId") UUID jobRunId);

    @Query("select coalesce(max(l.sequence), -1) + 1 from LogChunk l where l.jobRunId = :jobRunId")
    long nextSequence(@Param("jobRunId") UUID jobRunId);

    @Query("select l from LogChunk l where l.jobRunId = :jobRunId order by l.sequence desc")
    List<LogChunk> findTailRaw(@Param("jobRunId") UUID jobRunId, Pageable pageable);

    @Query("select l from LogChunk l where l.jobRunId = :jobRunId and l.sequence > :after order by l.sequence asc")
    List<LogChunk> findAfter(@Param("jobRunId") UUID jobRunId,@Param("after") long after,Pageable pageable);

    @Query("select l from LogChunk l where l.jobRunId = :jobRunId and l.createdAt > :since order by l.sequence asc")
    List<LogChunk> findSince(@Param("jobRunId") UUID jobRunId,@Param("since") Instant since,Pageable pageable);

    default List<LogChunk> findTail(UUID jobRunId,int tail) {
        var rows=findTailRaw(jobRunId,Pageable.ofSize(tail));
        Collections.reverse(rows);
        return rows;
    }
}
