package com.bank.exception;

/**
 * Exception thrown when a customer attempts to withdraw an amount
 * exceeding their configured daily withdrawal limit.
 */
public class DailyLimitExceededException extends BankingException {
    private final double dailyLimit;
    private final double alreadyWithdrawnToday;
    private final double attemptedAmount;
    private final double remainingLimit;

    public DailyLimitExceededException(double dailyLimit, double alreadyWithdrawnToday,
                                       double attemptedAmount, double remainingLimit) {
        super(String.format("Daily withdrawal limit exceeded! Daily Limit: ₹%.2f, Already Withdrawn Today: ₹%.2f, " +
                        "Attempted: ₹%.2f. Available Limit Remaining Today: ₹%.2f",
                dailyLimit, alreadyWithdrawnToday, attemptedAmount, remainingLimit));
        this.dailyLimit = dailyLimit;
        this.alreadyWithdrawnToday = alreadyWithdrawnToday;
        this.attemptedAmount = attemptedAmount;
        this.remainingLimit = remainingLimit;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public double getAlreadyWithdrawnToday() {
        return alreadyWithdrawnToday;
    }

    public double getAttemptedAmount() {
        return attemptedAmount;
    }

    public double getRemainingLimit() {
        return remainingLimit;
    }
}
