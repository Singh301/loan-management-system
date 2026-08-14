ALTER TABLE documents
    ADD COLUMN verification_status VARCHAR(20) DEFAULT 'PENDING',
    ADD COLUMN verified_by BIGINT NULL,
    ADD COLUMN verified_at DATETIME NULL,
    ADD COLUMN rejection_reason VARCHAR(500) NULL;