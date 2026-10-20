package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model representing an audit trail / ledger entry for a banking transaction.
 */
public class Transaction {
    private long transactionId;
    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAWAL
    private double amount;
    private double balanceAfter;
    private LocalDateTime timestamp;
    private String description;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(long transactionId, String accountNumber, String transactionType,
                       double amount, double balanceAfter, LocalDateTime timestamp, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.description = description;
    }

    public Transaction(String accountNumber, String transactionType, double amount,
                       double balanceAfter, String description) {
        this(0, accountNumber, transactionType, amount, balanceAfter, LocalDateTime.now(), description);
    }

    // Getters and Setters
    public long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(long transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp != null ? timestamp.format(FORMATTER) : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-10s | Amount: ₹%.2f | Balance: ₹%.2f | %s",
                getFormattedTimestamp(), transactionType, amount, balanceAfter, description);
    }
}
