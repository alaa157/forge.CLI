CREATE TABLE forgeci.webhook_deliveries (
 id UUID PRIMARY KEY, provider VARCHAR(30) NOT NULL, delivery_id VARCHAR(128) NOT NULL, event_type VARCHAR(50) NOT NULL,
 received_at TIMESTAMPTZ NOT NULL, processed_at TIMESTAMPTZ, status VARCHAR(20) NOT NULL, payload_hash VARCHAR(64) NOT NULL,
 CONSTRAINT uk_webhook_delivery_provider_id UNIQUE(provider,delivery_id),
 CONSTRAINT ck_webhook_delivery_status CHECK(status IN ('RECEIVED','PROCESSED','FAILED'))
);
CREATE INDEX idx_webhook_deliveries_status ON forgeci.webhook_deliveries(status);
CREATE INDEX idx_webhook_deliveries_received_at ON forgeci.webhook_deliveries(received_at);