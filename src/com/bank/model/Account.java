package com.bank.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Abstract Base Class representing a Bank Account.
 * Demonstrates Abstraction and Encapsulation OOP principles.
 */
public abstract class Account {
    private String accountNumber;
    private String holderName;
    private String email;
    private String phoneNumber;
    private String pin; // 4-digit PIN for authentication
    private double balance;
    private double dailyWithdrawalLimit;
    private double dailyWithdrawnAmount;
    private LocalDate lastWithdrawalDate;
    private String status; // ACTIVE, INACTIVE, BLOCKED
    private LocalDateTime createdAt;

    public Account() {
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.dailyWithdrawnAmount = 0.0;
        this.lastWithdrawalDate = LocalDate.now();
    }

    public Account(String accountNumber, String holderName, String email, String phoneNumber,
                   String pin, double balance, double dailyWithdrawalLimit) {
        this();
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.pin = pin;
        this.balance = balance;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    // Abstract methods demonstrating Polymorphism
    public abstract String getAccountType();
    public abstract double getMinimumBalance();
    public abstract double getOverdraftLimit();
    public abstract double getDefaultDailyLimit();

    /**
     * Resets the daily withdrawn counter if a new calendar day has started.
     */
    public void resetDailyWithdrawnIfNeeded(LocalDate today) {
        if (this.lastWithdrawalDate == null || !this.lastWithdrawalDate.equals(today)) {
            this.dailyWithdrawnAmount = 0.0;
            this.lastWithdrawalDate = today;
        }
    }

    /**
     * Calculates the remaining withdrawal limit available for today.
     */
    public double getRemainingDailyLimit(LocalDate today) {
        if (this.lastWithdrawalDate == null || !this.lastWithdrawalDate.equals(today)) {
            return this.dailyWithdrawalLimit;
        }
        return Math.max(0.0, this.dailyWithdrawalLimit - this.dailyWithdrawnAmount);
    }

    /**
     * Validates if the requested withdrawal amount is permissible under daily limit rules.
     */
    public boolean checkDailyLimit(double amount, LocalDate today) {
        double remaining = getRemainingDailyLimit(today);
        return amount <= remaining;
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public void setDailyWithdrawalLimit(double dailyWithdrawalLimit) {
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

    public double getDailyWithdrawnAmount() {
        return dailyWithdrawnAmount;
    }

    public void setDailyWithdrawnAmount(double dailyWithdrawnAmount) {
        this.dailyWithdrawnAmount = dailyWithdrawnAmount;
    }

    public LocalDate getLastWithdrawalDate() {
        return lastWithdrawalDate;
    }

    public void setLastWithdrawalDate(LocalDate lastWithdrawalDate) {
        this.lastWithdrawalDate = lastWithdrawalDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Account [" +
                "accountNumber='" + accountNumber + '\'' +
                ", holderName='" + holderName + '\'' +
                ", type='" + getAccountType() + '\'' +
                ", balance=" + balance +
                ", dailyLimit=" + dailyWithdrawalLimit +
                ", remainingToday=" + getRemainingDailyLimit(LocalDate.now()) +
                ']';
    }
}
