package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
    boolean existsByMessageIdAndConsumer(UUID messageId, String consumer);

    @Modifying
    @Query(value = """
            INSERT INTO forgeci.processed_messages (id, message_id, consumer, processed_at)
            VALUES (:id, :messageId, :consumer, CURRENT_TIMESTAMP)
            ON CONFLICT (message_id, consumer) DO NOTHING
            """, nativeQuery = true)
    int claim(@Param("id") UUID id, @Param("messageId") UUID messageId, @Param("consumer") String consumer);
}
