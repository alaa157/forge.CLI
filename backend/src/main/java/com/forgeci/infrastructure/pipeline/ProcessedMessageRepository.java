package com.forgeci.infrastructure.pipeline;

import com.forgeci.domain.pipeline.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
    boolean existsByMessageIdAndConsumer(UUID messageId, String consumer);
}
