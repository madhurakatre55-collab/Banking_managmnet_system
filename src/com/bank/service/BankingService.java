package com.bank.service;

import com.bank.exception.*;
import com.bank.model.Account;
import com.bank.model.Transaction;

import java.util.List;

/**
 * Service Layer Interface defining the core business logic contracts for the Banking System.
 */
public interface BankingService {

    /**
     * Operation 1: Create an account.
     */
    Account createAccount(String holderName, String email, String phoneNumber,
                          String pin, String accountType, double initialDeposit,
                          Double customDailyLimit) throws BankingException;

    /**
     * Operation 2: Deposit money.
     */
    Account deposit(String accountNumber, double amount, String description) throws BankingException;

    /**
     * Operation 3 & 4: Withdraw money while strictly honoring the daily withdrawal limit.
     */
    Account withdraw(String accountNumber, String pin, double amount, String description) throws BankingException;

    /**
     * Operation 5: Check the balance.
     */
    double checkBalance(String accountNumber, String pin) throws BankingException;

    /**
     * Operation 6: Display Account information.
     */
    Account getAccountInformation(String accountNumber, String pin) throws BankingException;

    /**
     * Retrieves transaction history for an authenticated account.
     */
    List<Transaction> getTransactionHistory(String accountNumber, String pin) throws BankingException;

    /**
     * Retrieves all accounts (for banking manager / directory view).
     */
    List<Account> getAllAccounts() throws BankingException;

    /**
     * Authenticates an account with its PIN.
     */
    boolean authenticate(String accountNumber, String pin) throws BankingException;

    /**
     * Generates a unique 10-digit account number.
     */
    String generateUniqueAccountNumber() throws BankingException;
}
