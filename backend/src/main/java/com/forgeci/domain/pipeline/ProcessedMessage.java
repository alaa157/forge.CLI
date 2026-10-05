package com.forgeci.domain.pipeline;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_messages", schema = "forgeci",
       uniqueConstraints = @UniqueConstraint(name = "uq_processed_messages_message_id", columnNames = "message_id"))
public class ProcessedMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message_id", nullable = false, updatable = false)
    private UUID messageId;

    @Column(name = "consumer", nullable = false, length = 128, updatable = false)
    private String consumer;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedMessage() {}

    public ProcessedMessage(UUID messageId, String consumer) {
        if (messageId == null) throw new IllegalArgumentException("messageId is required");
        if (consumer == null || consumer.isBlank()) throw new IllegalArgumentException("consumer is required");
        this.messageId = messageId;
        this.consumer = consumer;
        this.processedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getMessageId() { return messageId; }
    public String getConsumer() { return consumer; }
    public Instant getProcessedAt() { return processedAt; }
}
