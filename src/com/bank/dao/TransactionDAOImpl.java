package com.bank.dao;

import com.bank.exception.DatabaseException;
import com.bank.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of TransactionDAO using PreparedStatements.
 */
public class TransactionDAOImpl implements TransactionDAO {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean logTransaction(Transaction transaction) throws DatabaseException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount, balance_after, timestamp, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transaction.getAccountNumber());
            pstmt.setString(2, transaction.getTransactionType());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setDouble(4, transaction.getBalanceAfter());
            pstmt.setString(5, transaction.getTimestamp() != null ?
                    transaction.getTimestamp().format(DATETIME_FORMATTER) : LocalDateTime.now().format(DATETIME_FORMATTER));
            pstmt.setString(6, transaction.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error logging transaction to database: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) throws DatabaseException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching transactions for account " + accountNumber + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> findRecent(int limit) throws DatabaseException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_id DESC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving recent transactions: " + e.getMessage(), e);
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction tx = new Transaction();
        tx.setTransactionId(rs.getLong("transaction_id"));
        tx.setAccountNumber(rs.getString("account_number"));
        tx.setTransactionType(rs.getString("transaction_type"));
        tx.setAmount(rs.getDouble("amount"));
        tx.setBalanceAfter(rs.getDouble("balance_after"));

        String timeStr = rs.getString("timestamp");
        if (timeStr != null && !timeStr.trim().isEmpty()) {
            try {
                tx.setTimestamp(LocalDateTime.parse(timeStr, DATETIME_FORMATTER));
            } catch (Exception ignored) {
                tx.setTimestamp(LocalDateTime.now());
            }
        }

        tx.setDescription(rs.getString("description"));
        return tx;
    }
}
