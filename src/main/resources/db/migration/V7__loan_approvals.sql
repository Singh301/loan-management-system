CREATE TABLE IF NOT EXISTS loan_approvals (
                                              approval_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              loan_id BIGINT NOT NULL,
                                              approver_user_id BIGINT NOT NULL,
                                              level INT NOT NULL,
                                              status VARCHAR(20) NOT NULL,
    remarks VARCHAR(500),
    action_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_approval_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
    );