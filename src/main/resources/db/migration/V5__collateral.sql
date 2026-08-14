CREATE TABLE IF NOT EXISTS collaterals (
                                           collateral_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           loan_id BIGINT NOT NULL,
                                           collateral_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    estimated_value DECIMAL(15,2),
    ownership_proof VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collateral_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
    );