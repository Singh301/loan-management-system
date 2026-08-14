-- Phase 1: Optimistic locking + Idempotency support

-- Optimistic locking version columns
ALTER TABLE loans ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE emi_schedules ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE loan_repayments ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE customers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- Idempotency key for disbursement (prevents double disbursement)
ALTER TABLE loans ADD COLUMN disbursement_idempotency_key VARCHAR(100) NULL;
CREATE UNIQUE INDEX uk_loans_disbursement_idempotency ON loans (disbursement_idempotency_key);

-- Soft-delete index for faster filtering
CREATE INDEX idx_loans_deleted ON loans (deleted);
CREATE INDEX idx_customers_deleted ON customers (deleted);
CREATE INDEX idx_users_deleted ON users (deleted);

-- Ensure loan_approvals has proper indexes
CREATE INDEX idx_loan_approvals_loan_id ON loan_approvals (loan_id);
CREATE INDEX idx_loan_approvals_status ON loan_approvals (status);
