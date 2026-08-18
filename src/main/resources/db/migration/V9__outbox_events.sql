-- Transactional Outbox for reliable event publishing
CREATE TABLE IF NOT EXISTS outbox_events (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(150) NOT NULL,
    payload         JSON NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    DATETIME     NULL,
    retry_count     INT          NOT NULL DEFAULT 0,
    last_error      VARCHAR(1000) NULL,
    INDEX idx_outbox_status_created (status, created_at)
);
