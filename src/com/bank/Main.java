package com.bank;

import com.bank.dao.DBConnection;
import com.bank.gui.MainFrame;
import com.bank.model.Account;
import com.bank.service.BankingService;
import com.bank.service.BankingServiceImpl;
import com.bank.web.BankingWebServer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Main {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  BANKING MANAGEMENT SYSTEM - BTECH OOP IN JAVA MINI PROJECT");
        System.out.println("==================================================================");

        try {
            // 1. Initialize JDBC Database
            System.out.println("-> Initializing Database (" + DBConnection.getDbType().toUpperCase() + " via JDBC)...");
            DBConnection.initializeDatabase();

            // 2. Instantiate Service Layer
            BankingService bankingService = new BankingServiceImpl();

            // 3. Seed Sample Data if database is brand new
            seedSampleAccountsIfEmpty(bankingService);

            // 4. Start Embedded Web Server
            int port = DEFAULT_PORT;
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {}
            }

            BankingWebServer webServer = new BankingWebServer(port, bankingService);
            webServer.start();

            System.out.println("\n-> Web Prototype Link: http://localhost:" + port);
            System.out.println("   Open the link in any web browser to interact with the system!\n");

            // 5. Launch Swing Desktop GUI (if not headless)
            boolean isHeadless = GraphicsEnvironment.isHeadless();
            boolean skipGui = args.length > 1 && "--no-gui".equalsIgnoreCase(args[1]);

            if (!isHeadless && !skipGui) {
                try {
                    // Try to apply FlatLaf Look and Feel
                    UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
                } catch (Exception e) {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception ignored) {}
                }

                SwingUtilities.invokeLater(() -> {
                    try {
                        MainFrame frame = new MainFrame(bankingService);
                        frame.setVisible(true);
                        System.out.println("-> Desktop Swing GUI window launched successfully.");
                    } catch (Exception e) {
                        System.err.println("Note: Swing GUI initialization encountered: " + e.getMessage());
                    }
                });
            } else {
                System.out.println("-> Running in Web / Server mode. Press Ctrl+C to terminate.");
            }

        } catch (Exception e) {
            System.err.println("Fatal error starting Banking Management System: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedSampleAccountsIfEmpty(BankingService service) {
        try {
            List<Account> accounts = service.getAllAccounts();
            if (accounts.isEmpty()) {
                System.out.println("-> Creating demo accounts for initial testing...");

                // Account 1: Rahul Sharma (Savings)
                Account acc1 = service.createAccount(
                        "Rahul Sharma",
                        "rahul.sharma@example.com",
                        "9876543210",
                        "1234",
                        "SAVINGS",
                        25000.00,
                        25000.00
                );

                // Account 2: Priya Patel (Current)
                Account acc2 = service.createAccount(
                        "Priya Patel",
                        "priya.patel@example.com",
                        "9811223344",
                        "4321",
                        "CURRENT",
                        75000.00,
                        100000.00
                );

                // Add a sample deposit and withdrawal to show transaction history
                service.deposit(acc1.getAccountNumber(), 5000.00, "Online Salary Credit");
                service.withdraw(acc1.getAccountNumber(), "1234", 3000.00, "ATM Cash Withdrawal");

                System.out.println("-> Seeded Demo Account 1: Number = " + acc1.getAccountNumber() + " | PIN = 1234");
                System.out.println("-> Seeded Demo Account 2: Number = " + acc2.getAccountNumber() + " | PIN = 4321");
            }
        } catch (Exception e) {
            System.err.println("Notice while checking/seeding demo data: " + e.getMessage());
        }
    }
}
