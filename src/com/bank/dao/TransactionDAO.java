package com.bank.dao;

import com.bank.exception.DatabaseException;
import com.bank.model.Transaction;

import java.util.List;

/**
 * Data Access Object Interface for recording and querying transaction logs.
 */
public interface TransactionDAO {
    boolean logTransaction(Transaction transaction) throws DatabaseException;
    List<Transaction> findByAccountNumber(String accountNumber) throws DatabaseException;
    List<Transaction> findRecent(int limit) throws DatabaseException;
}
