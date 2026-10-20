package com.bank.dao;

import com.bank.exception.DatabaseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Singleton / Factory class to manage Database Connections.
 * Uses JDBC to connect to either SQLite (embedded zero-config) or MySQL.
 */
public class DBConnection {
    private static Properties properties = new Properties();
    private static String dbType = "sqlite";
    private static String dbUrl = "jdbc:sqlite:banking.db";
    private static String dbUser = "";
    private static String dbPassword = "";
    private static boolean isInitialized = false;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                properties.load(fis);
                dbType = properties.getProperty("db.type", "sqlite").trim().toLowerCase();
                if ("mysql".equals(dbType)) {
                    dbUrl = properties.getProperty("mysql.url", "jdbc:mysql://localhost:3306/banking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
                    dbUser = properties.getProperty("mysql.user", "root");
                    dbPassword = properties.getProperty("mysql.password", "");
                } else {
                    dbUrl = properties.getProperty("sqlite.url", "jdbc:sqlite:banking.db");
                }
            } catch (IOException e) {
                System.err.println("Notice: Using default SQLite configuration: " + e.getMessage());
            }
        }
    }

    /**
     * Obtains a new JDBC connection.
     */
    public static synchronized Connection getConnection() throws DatabaseException {
        try {
            if ("mysql".equals(dbType)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            } else {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection(dbUrl);
            }
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("JDBC Driver not found for " + dbType + ". Ensure JDBC JAR is in classpath.", e);
        } catch (SQLException e) {
            throw new DatabaseException("Database connection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Automatically ensures database tables and indexes exist.
     */
    public static synchronized void initializeDatabase() throws DatabaseException {
        if (isInitialized) {
            return;
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            if ("mysql".equals(dbType)) {
                stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                        "account_number VARCHAR(30) PRIMARY KEY," +
                        "holder_name VARCHAR(100) NOT NULL," +
                        "email VARCHAR(100)," +
                        "phone_number VARCHAR(20)," +
                        "pin VARCHAR(20) NOT NULL," +
                        "balance DOUBLE NOT NULL," +
                        "daily_withdrawal_limit DOUBLE NOT NULL," +
                        "daily_withdrawn_amount DOUBLE NOT NULL DEFAULT 0.0," +
                        "last_withdrawal_date VARCHAR(20)," +
                        "account_type VARCHAR(20) NOT NULL," +
                        "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
                        "created_at VARCHAR(30)" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                        "transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "account_number VARCHAR(30) NOT NULL," +
                        "transaction_type VARCHAR(20) NOT NULL," +
                        "amount DOUBLE NOT NULL," +
                        "balance_after DOUBLE NOT NULL," +
                        "timestamp VARCHAR(30) NOT NULL," +
                        "description VARCHAR(255)" +
                        ");");
            } else {
                // SQLite syntax
                stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                        "account_number TEXT PRIMARY KEY," +
                        "holder_name TEXT NOT NULL," +
                        "email TEXT," +
                        "phone_number TEXT," +
                        "pin TEXT NOT NULL," +
                        "balance REAL NOT NULL," +
                        "daily_withdrawal_limit REAL NOT NULL," +
                        "daily_withdrawn_amount REAL NOT NULL DEFAULT 0.0," +
                        "last_withdrawal_date TEXT," +
                        "account_type TEXT NOT NULL," +
                        "status TEXT NOT NULL DEFAULT 'ACTIVE'," +
                        "created_at TEXT" +
                        ");");

                stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                        "transaction_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "account_number TEXT NOT NULL," +
                        "transaction_type TEXT NOT NULL," +
                        "amount REAL NOT NULL," +
                        "balance_after REAL NOT NULL," +
                        "timestamp TEXT NOT NULL," +
                        "description TEXT" +
                        ");");
            }

            isInitialized = true;
            System.out.println("Database successfully connected and initialized (" + dbType.toUpperCase() + ")");
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database tables: " + e.getMessage(), e);
        }
    }

    public static String getDbType() {
        return dbType;
    }
}
