CREATE TABLE forgeci.processed_messages (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    consumer VARCHAR(128) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_processed_messages_message_id UNIQUE (message_id)
);

CREATE INDEX idx_processed_messages_processed_at
    ON forgeci.processed_messages (processed_at);
