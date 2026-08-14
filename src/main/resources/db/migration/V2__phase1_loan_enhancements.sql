-- 1. Create loan_products table
CREATE TABLE IF NOT EXISTS loan_products (
                                             product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    loan_type VARCHAR(30) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    min_tenure_months INT NOT NULL,
    max_tenure_months INT NOT NULL,
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    processing_fee_percent DECIMAL(10,2),
    late_fee_amount DECIMAL(10,2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 2. Create emi_schedules table
CREATE TABLE IF NOT EXISTS emi_schedules (
                                             schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             loan_id BIGINT NOT NULL,
                                             installment_number INT NOT NULL,
                                             due_date DATE NOT NULL,
                                             emi_amount DECIMAL(15,2) NOT NULL,
    principal_component DECIMAL(15,2) NOT NULL,
    interest_component DECIMAL(15,2) NOT NULL,
    outstanding_principal_after DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_date DATE,
    amount_paid DECIMAL(15,2),
    late_fee DECIMAL(10,2),
    CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
    );

-- 3. Alter loans table
ALTER TABLE loans
    ADD COLUMN product_id BIGINT NULL,
    ADD COLUMN disbursement_date DATE NULL,
    ADD COLUMN next_due_date DATE NULL,
    ADD COLUMN total_late_fee DECIMAL(15,2) DEFAULT 0.00;

ALTER TABLE loans
    ADD CONSTRAINT fk_loan_product FOREIGN KEY (product_id) REFERENCES loan_products(product_id);

-- 4. Insert sample Loan Products
INSERT INTO loan_products (product_code, product_name, loan_type, interest_rate, min_tenure_months, max_tenure_months, min_amount, max_amount, processing_fee_percent, late_fee_amount)
VALUES
    ('HOME_STD', 'Standard Home Loan', 'HOME', 8.50, 60, 360, 500000.00, 10000000.00, 1.00, 500.00),
    ('HOME_PREMIUM', 'Premium Home Loan', 'HOME', 8.00, 60, 360, 1000000.00, 20000000.00, 0.75, 500.00),
    ('CAR_STD', 'Standard Car Loan', 'CAR', 9.25, 12, 84, 100000.00, 2000000.00, 1.50, 300.00),
    ('PERSONAL_STD', 'Standard Personal Loan', 'PERSONAL', 12.50, 12, 60, 50000.00, 1000000.00, 2.00, 400.00),
    ('EDUCATION_STD', 'Education Loan', 'EDUCATION', 9.00, 12, 96, 50000.00, 2000000.00, 1.00, 300.00),
    ('BUSINESS_STD', 'Business Loan', 'BUSINESS', 11.00, 12, 84, 200000.00, 5000000.00, 1.75, 500.00),
    ('GOLD_STD', 'Gold Loan', 'GOLD', 10.00, 6, 36, 25000.00, 2000000.00, 1.00, 200.00);