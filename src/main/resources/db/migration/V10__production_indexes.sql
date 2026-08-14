-- Production query performance indexes

CREATE INDEX idx_loans_customer_id ON loans (customer_id);
CREATE INDEX idx_loans_status ON loans (loan_status);
CREATE INDEX idx_loans_type ON loans (loan_type);
CREATE INDEX idx_loans_application_date ON loans (application_date);
CREATE INDEX idx_loans_customer_status ON loans (customer_id, loan_status);

CREATE INDEX idx_emi_loan_id ON emi_schedules (loan_id);
CREATE INDEX idx_emi_status_due ON emi_schedules (status, due_date);

CREATE INDEX idx_repayments_loan_id ON loan_repayments (loan_id);
CREATE INDEX idx_repayments_payment_date ON loan_repayments (payment_date);

CREATE INDEX idx_documents_customer_id ON documents (customer_id);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_audit_created_at ON audit_logs (created_at);
