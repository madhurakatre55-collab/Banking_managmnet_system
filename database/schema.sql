-- ============================================================
-- Banking Management System - Database Schema
-- Subject: Object Oriented Programming in Java (BTech Mini Project)
-- ============================================================

CREATE DATABASE IF NOT EXISTS banking_db;
USE banking_db;

-- 1. Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(30) PRIMARY KEY,
    holder_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    pin VARCHAR(20) NOT NULL,
    balance DOUBLE NOT NULL,
    daily_withdrawal_limit DOUBLE NOT NULL,
    daily_withdrawn_amount DOUBLE NOT NULL DEFAULT 0.0,
    last_withdrawal_date VARCHAR(20),
    account_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at VARCHAR(30)
);

-- 2. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(30) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DOUBLE NOT NULL,
    balance_after DOUBLE NOT NULL,
    timestamp VARCHAR(30) NOT NULL,
    description VARCHAR(255),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- Sample Initial Data (Optional for testing)
INSERT INTO accounts (account_number, holder_name, email, phone_number, pin, balance, daily_withdrawal_limit, daily_withdrawn_amount, last_withdrawal_date, account_type, status, created_at)
VALUES 
('1001001', 'Rahul Sharma', 'rahul.sharma@example.com', '9876543210', '1234', 25000.00, 25000.00, 0.0, CURRENT_DATE(), 'SAVINGS', 'ACTIVE', NOW()),
('1001002', 'Priya Patel', 'priya.patel@example.com', '9811223344', '4321', 75000.00, 100000.00, 0.0, CURRENT_DATE(), 'CURRENT', 'ACTIVE', NOW());
