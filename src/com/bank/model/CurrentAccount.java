package com.bank.model;

/**
 * Concrete Current Account class designed for commercial/business operations.
 * Demonstrates Inheritance, Polymorphic rules for overdrafts and higher limits.
 */
public class CurrentAccount extends Account {
    public static final double MINIMUM_BALANCE = 5000.00;
    public static final double OVERDRAFT_LIMIT = 25000.00;
    public static final double DEFAULT_DAILY_LIMIT = 100000.00;

    public CurrentAccount() {
        super();
        setDailyWithdrawalLimit(DEFAULT_DAILY_LIMIT);
    }

    public CurrentAccount(String accountNumber, String holderName, String email,
                          String phoneNumber, String pin, double balance, double dailyWithdrawalLimit) {
        super(accountNumber, holderName, email, phoneNumber, pin, balance,
                dailyWithdrawalLimit > 0 ? dailyWithdrawalLimit : DEFAULT_DAILY_LIMIT);
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    @Override
    public double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    @Override
    public double getOverdraftLimit() {
        return OVERDRAFT_LIMIT;
    }

    @Override
    public double getDefaultDailyLimit() {
        return DEFAULT_DAILY_LIMIT;
    }
}
