package com.bank.exception;

public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found with Account Number: " + accountNumber);
    }
}
