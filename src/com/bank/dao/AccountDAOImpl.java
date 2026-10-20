package com.bank.dao;

import com.bank.exception.DatabaseException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of AccountDAO using PreparedStatements.
 */
public class AccountDAOImpl implements AccountDAO {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public boolean save(Account account) throws DatabaseException {
        String sql = "INSERT INTO accounts (account_number, holder_name, email, phone_number, pin, " +
                "balance, daily_withdrawal_limit, daily_withdrawn_amount, last_withdrawal_date, " +
                "account_type, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getHolderName());
            pstmt.setString(3, account.getEmail());
            pstmt.setString(4, account.getPhoneNumber());
            pstmt.setString(5, account.getPin());
            pstmt.setDouble(6, account.getBalance());
            pstmt.setDouble(7, account.getDailyWithdrawalLimit());
            pstmt.setDouble(8, account.getDailyWithdrawnAmount());
            pstmt.setString(9, account.getLastWithdrawalDate() != null ?
                    account.getLastWithdrawalDate().format(DATE_FORMATTER) : LocalDate.now().format(DATE_FORMATTER));
            pstmt.setString(10, account.getAccountType());
            pstmt.setString(11, account.getStatus());
            pstmt.setString(12, account.getCreatedAt() != null ?
                    account.getCreatedAt().format(DATETIME_FORMATTER) : LocalDateTime.now().format(DATETIME_FORMATTER));

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error saving account to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Account findByAccountNumber(String accountNumber) throws DatabaseException {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAccount(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Error querying account by number: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Account account) throws DatabaseException {
        String sql = "UPDATE accounts SET holder_name = ?, email = ?, phone_number = ?, pin = ?, " +
                "balance = ?, daily_withdrawal_limit = ?, daily_withdrawn_amount = ?, " +
                "last_withdrawal_date = ?, status = ? WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, account.getHolderName());
            pstmt.setString(2, account.getEmail());
            pstmt.setString(3, account.getPhoneNumber());
            pstmt.setString(4, account.getPin());
            pstmt.setDouble(5, account.getBalance());
            pstmt.setDouble(6, account.getDailyWithdrawalLimit());
            pstmt.setDouble(7, account.getDailyWithdrawnAmount());
            pstmt.setString(8, account.getLastWithdrawalDate() != null ?
                    account.getLastWithdrawalDate().format(DATE_FORMATTER) : null);
            pstmt.setString(9, account.getStatus());
            pstmt.setString(10, account.getAccountNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating account details: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateBalanceAndDailyWithdrawn(String accountNumber, double newBalance,
                                                 double newDailyWithdrawn, LocalDate lastWithdrawalDate) throws DatabaseException {
        String sql = "UPDATE accounts SET balance = ?, daily_withdrawn_amount = ?, " +
                "last_withdrawal_date = ? WHERE account_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newBalance);
            pstmt.setDouble(2, newDailyWithdrawn);
            pstmt.setString(3, lastWithdrawalDate != null ? lastWithdrawalDate.format(DATE_FORMATTER) : null);
            pstmt.setString(4, accountNumber);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating balance & daily limit: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> findAll() throws DatabaseException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving all accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String accountNumber) throws DatabaseException {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking account existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String accountNumber) throws DatabaseException {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting account: " + e.getMessage(), e);
        }
    }

    /**
     * Maps database row to polymorphic Account instance.
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        Account acc;
        if ("CURRENT".equalsIgnoreCase(type)) {
            acc = new CurrentAccount();
        } else {
            acc = new SavingsAccount();
        }

        acc.setAccountNumber(rs.getString("account_number"));
        acc.setHolderName(rs.getString("holder_name"));
        acc.setEmail(rs.getString("email"));
        acc.setPhoneNumber(rs.getString("phone_number"));
        acc.setPin(rs.getString("pin"));
        acc.setBalance(rs.getDouble("balance"));
        acc.setDailyWithdrawalLimit(rs.getDouble("daily_withdrawal_limit"));
        acc.setDailyWithdrawnAmount(rs.getDouble("daily_withdrawn_amount"));

        String dateStr = rs.getString("last_withdrawal_date");
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                acc.setLastWithdrawalDate(LocalDate.parse(dateStr, DATE_FORMATTER));
            } catch (Exception ignored) {
                acc.setLastWithdrawalDate(LocalDate.now());
            }
        } else {
            acc.setLastWithdrawalDate(LocalDate.now());
        }

        acc.setStatus(rs.getString("status"));

        String createdStr = rs.getString("created_at");
        if (createdStr != null && !createdStr.trim().isEmpty()) {
            try {
                acc.setCreatedAt(LocalDateTime.parse(createdStr, DATETIME_FORMATTER));
            } catch (Exception ignored) {
                acc.setCreatedAt(LocalDateTime.now());
            }
        }

        return acc;
    }
}
