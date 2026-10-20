package com.bank.dao;

import com.bank.exception.DatabaseException;
import com.bank.model.Account;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object Interface for Account management.
 * Demonstrates Abstraction and DAO architectural pattern.
 */
public interface AccountDAO {
    boolean save(Account account) throws DatabaseException;
    Account findByAccountNumber(String accountNumber) throws DatabaseException;
    boolean update(Account account) throws DatabaseException;
    boolean updateBalanceAndDailyWithdrawn(String accountNumber, double newBalance,
                                          double newDailyWithdrawn, LocalDate lastWithdrawalDate) throws DatabaseException;
    List<Account> findAll() throws DatabaseException;
    boolean exists(String accountNumber) throws DatabaseException;
    boolean delete(String accountNumber) throws DatabaseException;
}
