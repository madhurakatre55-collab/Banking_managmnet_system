package com.bank.exception;

public class InsufficientBalanceException extends BankingException {
    private final double currentBalance;
    private final double attemptedAmount;

    public InsufficientBalanceException(String message, double currentBalance, double attemptedAmount) {
        super(message);
        this.currentBalance = currentBalance;
        this.attemptedAmount = attemptedAmount;
    }

    public InsufficientBalanceException(double currentBalance, double attemptedAmount) {
        super(String.format("Insufficient funds! Available balance: ₹%.2f, Requested withdrawal: ₹%.2f",
                currentBalance, attemptedAmount));
        this.currentBalance = currentBalance;
        this.attemptedAmount = attemptedAmount;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }
}
