package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.AccountDAOImpl;
import com.bank.dao.TransactionDAO;
import com.bank.dao.TransactionDAOImpl;
import com.bank.exception.*;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Implementation of BankingService business logic.
 * Encapsulates core rules including the daily withdrawal limit enforcement.
 */
public class BankingServiceImpl implements BankingService {

    private final AccountDAO accountDAO;
    private final TransactionDAO transactionDAO;
    private final Random random = new Random();

    public BankingServiceImpl() {
        this.accountDAO = new AccountDAOImpl();
        this.transactionDAO = new TransactionDAOImpl();
    }

    public BankingServiceImpl(AccountDAO accountDAO, TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.transactionDAO = transactionDAO;
    }

    @Override
    public Account createAccount(String holderName, String email, String phoneNumber,
                                 String pin, String accountType, double initialDeposit,
                                 Double customDailyLimit) throws BankingException {

        // Validations
        if (holderName == null || holderName.trim().isEmpty()) {
            throw new BankingException("Account holder name cannot be empty.");
        }
        if (pin == null || pin.trim().length() < 4) {
            throw new BankingException("PIN must be at least 4 digits.");
        }

        String accType = accountType != null ? accountType.trim().toUpperCase() : "SAVINGS";
        Account account;
        double defaultLimit;

        if ("CURRENT".equals(accType)) {
            account = new CurrentAccount();
            defaultLimit = CurrentAccount.DEFAULT_DAILY_LIMIT;
        } else {
            account = new SavingsAccount();
            defaultLimit = SavingsAccount.DEFAULT_DAILY_LIMIT;
        }

        if (initialDeposit < account.getMinimumBalance()) {
            throw new InvalidAmountException(String.format(
                    "Initial deposit must be at least the minimum balance of ₹%.2f for %s account.",
                    account.getMinimumBalance(), account.getAccountType()));
        }

        double limit = (customDailyLimit != null && customDailyLimit > 0) ? customDailyLimit : defaultLimit;

        String accountNumber = generateUniqueAccountNumber();
        account.setAccountNumber(accountNumber);
        account.setHolderName(holderName.trim());
        account.setEmail(email != null ? email.trim() : "");
        account.setPhoneNumber(phoneNumber != null ? phoneNumber.trim() : "");
        account.setPin(pin.trim());
        account.setBalance(initialDeposit);
        account.setDailyWithdrawalLimit(limit);
        account.setDailyWithdrawnAmount(0.0);
        account.setLastWithdrawalDate(LocalDate.now());
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());

        // Save to Database
        accountDAO.save(account);

        // Record Initial Deposit Transaction
        Transaction initialTx = new Transaction(
                accountNumber,
                "DEPOSIT",
                initialDeposit,
                initialDeposit,
                "Account opening initial deposit"
        );
        transactionDAO.logTransaction(initialTx);

        return account;
    }

    @Override
    public Account deposit(String accountNumber, double amount, String description) throws BankingException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than zero.");
        }

        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BankingException("Account " + accountNumber + " is not active (" + account.getStatus() + ").");
        }

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        // Update database
        accountDAO.update(account);

        // Log transaction
        String desc = (description != null && !description.trim().isEmpty()) ? description : "Cash/Online Deposit";
        Transaction tx = new Transaction(accountNumber, "DEPOSIT", amount, newBalance, desc);
        transactionDAO.logTransaction(tx);

        return account;
    }

    @Override
    public Account withdraw(String accountNumber, String pin, double amount, String description) throws BankingException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.");
        }

        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }

        if (!account.getPin().equals(pin)) {
            throw new AuthenticationException("Invalid 4-digit PIN for account " + accountNumber + ".");
        }

        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BankingException("Account " + accountNumber + " is not active (" + account.getStatus() + ").");
        }

        LocalDate today = LocalDate.now();

        // 1. HONOR DAILY WITHDRAWAL LIMIT
        account.resetDailyWithdrawnIfNeeded(today);
        double remainingLimit = account.getRemainingDailyLimit(today);

        if (amount > remainingLimit) {
            throw new DailyLimitExceededException(
                    account.getDailyWithdrawalLimit(),
                    account.getDailyWithdrawnAmount(),
                    amount,
                    remainingLimit
            );
        }

        // 2. CHECK BALANCE CONSTRAINTS (Minimum balance / Overdraft)
        double currentBalance = account.getBalance();
        double minBalance = account.getMinimumBalance();
        double overdraft = account.getOverdraftLimit();

        if (account instanceof SavingsAccount) {
            if ((currentBalance - amount) < minBalance) {
                throw new InsufficientBalanceException(
                        String.format("Withdrawal denied! Maintaining minimum balance of ₹%.2f is required. Maximum withdrawable right now: ₹%.2f",
                                minBalance, Math.max(0, currentBalance - minBalance)),
                        currentBalance, amount);
            }
        } else if (account instanceof CurrentAccount) {
            if ((currentBalance - amount) < -overdraft) {
                throw new InsufficientBalanceException(
                        String.format("Withdrawal denied! Overdraft limit of ₹%.2f exceeded. Current balance: ₹%.2f",
                                overdraft, currentBalance),
                        currentBalance, amount);
            }
        }

        // Execute withdrawal
        double newBalance = currentBalance - amount;
        double newDailyWithdrawn = account.getDailyWithdrawnAmount() + amount;

        account.setBalance(newBalance);
        account.setDailyWithdrawnAmount(newDailyWithdrawn);
        account.setLastWithdrawalDate(today);

        // Update database
        accountDAO.updateBalanceAndDailyWithdrawn(accountNumber, newBalance, newDailyWithdrawn, today);

        // Log transaction
        String desc = (description != null && !description.trim().isEmpty()) ? description : "Cash/ATM Withdrawal";
        Transaction tx = new Transaction(accountNumber, "WITHDRAWAL", amount, newBalance, desc);
        transactionDAO.logTransaction(tx);

        return account;
    }

    @Override
    public double checkBalance(String accountNumber, String pin) throws BankingException {
        Account account = getAccountInformation(accountNumber, pin);
        return account.getBalance();
    }

    @Override
    public Account getAccountInformation(String accountNumber, String pin) throws BankingException {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }

        if (pin != null && !pin.isEmpty() && !account.getPin().equals(pin)) {
            throw new AuthenticationException("Incorrect PIN for account " + accountNumber + ".");
        }

        // Ensure daily limits are calculated relative to today
        account.resetDailyWithdrawnIfNeeded(LocalDate.now());
        return account;
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountNumber, String pin) throws BankingException {
        // Authenticate
        getAccountInformation(accountNumber, pin);
        return transactionDAO.findByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> getAllAccounts() throws DatabaseException {
        List<Account> accounts = accountDAO.findAll();
        LocalDate today = LocalDate.now();
        for (Account acc : accounts) {
            acc.resetDailyWithdrawnIfNeeded(today);
        }
        return accounts;
    }

    @Override
    public boolean authenticate(String accountNumber, String pin) throws BankingException {
        Account account = accountDAO.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account.getPin().equals(pin);
    }

    @Override
    public String generateUniqueAccountNumber() throws DatabaseException {
        String accNum;
        do {
            long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            accNum = String.valueOf(number);
        } while (accountDAO.exists(accNum));
        return accNum;
    }
}
