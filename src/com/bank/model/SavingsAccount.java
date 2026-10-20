package com.bank.model;

/**
 * Concrete Savings Account class demonstrating Inheritance and Polymorphism.
 */
public class SavingsAccount extends Account {
    public static final double MINIMUM_BALANCE = 1000.00;
    public static final double DEFAULT_DAILY_LIMIT = 25000.00;
    public static final double INTEREST_RATE = 4.0; // 4% per annum

    public SavingsAccount() {
        super();
        setDailyWithdrawalLimit(DEFAULT_DAILY_LIMIT);
    }

    public SavingsAccount(String accountNumber, String holderName, String email,
                          String phoneNumber, String pin, double balance, double dailyWithdrawalLimit) {
        super(accountNumber, holderName, email, phoneNumber, pin, balance,
                dailyWithdrawalLimit > 0 ? dailyWithdrawalLimit : DEFAULT_DAILY_LIMIT);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public double getOverdraftLimit() {
        return 0.00; // Savings accounts do not permit overdraft
    }

    @Override
    public double getDefaultDailyLimit() {
        return DEFAULT_DAILY_LIMIT;
    }

    public double getInterestRate() {
        return INTEREST_RATE;
    }
}
