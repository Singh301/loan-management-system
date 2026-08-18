-- Users table
CREATE TABLE users (
                       user_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                       full_name       VARCHAR(255) NOT NULL,
                       username        VARCHAR(100) NOT NULL UNIQUE,
                       email           VARCHAR(150) NOT NULL UNIQUE,
                       password        VARCHAR(255) NOT NULL,
                       role            VARCHAR(50),
                       enabled         BOOLEAN DEFAULT TRUE,
                       created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Customers table
CREATE TABLE customers (
                           customer_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                           first_name      VARCHAR(100) NOT NULL,
                           last_name       VARCHAR(100) NOT NULL,
                           email           VARCHAR(150) NOT NULL UNIQUE,
                           mobile_number   VARCHAR(20)  NOT NULL UNIQUE,
                           pan_number      VARCHAR(20)  NOT NULL,
                           aadhaar_number  VARCHAR(20)  NOT NULL,
                           address         VARCHAR(255),
                           city            VARCHAR(100),
                           state           VARCHAR(100),
                           pin_code        VARCHAR(20),
                           active          BOOLEAN DEFAULT TRUE,
                           user_id         BIGINT UNIQUE,
                           CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Loans table
CREATE TABLE loans (
                       loan_id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
                       customer_id             BIGINT NOT NULL,
                       loan_type               VARCHAR(50) NOT NULL,
                       loan_amount             DECIMAL(15,2) NOT NULL,
                       interest_rate           DECIMAL(5,2) NOT NULL,
                       tenure_months           INT NOT NULL,
                       emi                     DECIMAL(15,2),
                       loan_status             VARCHAR(50) DEFAULT 'PENDING',
                       application_date        DATE,
                       remarks                 VARCHAR(500),
                       outstanding_principal   DECIMAL(18,2) DEFAULT 0.00,
                       paid_installments       INT DEFAULT 0,
                       remaining_installments  INT DEFAULT 0,
                       CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Loan Repayments table
CREATE TABLE loan_repayments (
                                 repayment_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 loan_id                 BIGINT NOT NULL,
                                 amount_paid             DECIMAL(18,2) NOT NULL,
                                 principal_paid          DECIMAL(18,2) NOT NULL,
                                 interest_paid           DECIMAL(18,2) NOT NULL,
                                 remaining_principal     DECIMAL(18,2) NOT NULL,
                                 payment_date            DATETIME,
                                 payment_mode            VARCHAR(30),
                                 transaction_reference   VARCHAR(100) UNIQUE,
                                 remarks                 VARCHAR(500),
                                 CONSTRAINT fk_repayment_loan FOREIGN KEY (loan_id) REFERENCES loans(loan_id)
);

-- Documents table
CREATE TABLE documents (
                           document_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                           customer_id     BIGINT NOT NULL,
                           document_type   VARCHAR(50) NOT NULL,
                           file_name       VARCHAR(255) NOT NULL,
                           file_type       VARCHAR(100) NOT NULL,
                           file_size       BIGINT NOT NULL,
                           file_path       VARCHAR(500) NOT NULL,
                           uploaded_at     DATETIME NOT NULL,
                           CONSTRAINT fk_document_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Refresh Tokens table
CREATE TABLE refresh_tokens (
                                token_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                                token           VARCHAR(500) NOT NULL UNIQUE,
                                user_id         BIGINT NOT NULL UNIQUE,
                                expiry_date     DATETIME NOT NULL,
                                CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Audit Logs table
CREATE TABLE audit_logs (
                            audit_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                            username        VARCHAR(255) NOT NULL,
                            action          VARCHAR(255) NOT NULL,
                            module          VARCHAR(255) NOT NULL,
                            description     VARCHAR(1000),
                            created_at      DATETIME NOT NULL
);