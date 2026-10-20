package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.exception.DailyLimitExceededException;
import com.bank.model.Account;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class WithdrawPanel extends JPanel {
    private final BankingService bankingService;

    private JTextField accNumField;
    private JPasswordField pinField;
    private JTextField amountField;
    private JTextField descField;

    private JLabel holderInfoLabel;
    private JLabel balanceInfoLabel;
    private JLabel dailyLimitLabel;
    private JLabel withdrawnTodayLabel;
    private JLabel remainingLimitLabel;
    private JProgressBar limitProgressBar;

    public WithdrawPanel(BankingService bankingService) {
        this.bankingService = bankingService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));
        content.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Withdraw Funds (Daily Limit Protected)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Cash/ATM withdrawal strictly enforcing the customer's daily limit policy.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(25));

        // Main Card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(850, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        accNumField = new JTextField(15);
        pinField = new JPasswordField(15);
        amountField = new JTextField(15);
        descField = new JTextField("ATM Withdrawal", 15);

        JButton checkLimitBtn = new JButton("Verify & Check Limits");
        checkLimitBtn.setBackground(new Color(226, 232, 240));
        checkLimitBtn.setFocusPainted(false);
        checkLimitBtn.addActionListener(e -> verifyAndDisplayLimits());

        JPanel authRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        authRow.setBackground(Color.WHITE);
        authRow.add(new JLabel("Account No:"));
        authRow.add(accNumField);
        authRow.add(new JLabel("4-Digit PIN:"));
        authRow.add(pinField);
        authRow.add(checkLimitBtn);

        // Daily Limit Monitor Box (Highlights Feature: "Honor Daily Withdrawal Limit")
        JPanel limitBox = new JPanel(new BorderLayout(10, 10));
        limitBox.setBackground(new Color(241, 245, 249));
        limitBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(148, 163, 184)),
                        " Daily Withdrawal Limit Monitor (Automated Reset Daily) "
                ),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 10, 6));
        statsGrid.setBackground(new Color(241, 245, 249));

        holderInfoLabel = new JLabel("Holder: --");
        balanceInfoLabel = new JLabel("Current Balance: --");
        dailyLimitLabel = new JLabel("Daily Limit: --");
        withdrawnTodayLabel = new JLabel("Withdrawn Today: --");
        remainingLimitLabel = new JLabel("Remaining Limit: --");

        balanceInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remainingLimitLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remainingLimitLabel.setForeground(new Color(180, 83, 9)); // Amber

        statsGrid.add(holderInfoLabel);
        statsGrid.add(balanceInfoLabel);
        statsGrid.add(dailyLimitLabel);
        statsGrid.add(withdrawnTodayLabel);
        statsGrid.add(remainingLimitLabel);

        limitProgressBar = new JProgressBar(0, 100);
        limitProgressBar.setValue(0);
        limitProgressBar.setStringPainted(true);
        limitProgressBar.setString("Daily Limit Usage: 0%");
        limitProgressBar.setForeground(new Color(239, 68, 68)); // Red bar for limit consumption

        limitBox.add(statsGrid, BorderLayout.CENTER);
        limitBox.add(limitProgressBar, BorderLayout.SOUTH);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(authRow, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(limitBox, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(new JLabel("Withdrawal Amount (₹) *:"), gbc);
        gbc.gridx = 1;
        card.add(amountField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        card.add(new JLabel("Description / Remark:"), gbc);
        gbc.gridx = 1;
        card.add(descField, gbc);
        row++;

        // Action Button
        JButton withdrawBtn = new JButton("Authorize & Withdraw");
        withdrawBtn.setBackground(new Color(220, 38, 38));
        withdrawBtn.setForeground(Color.WHITE);
        withdrawBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        withdrawBtn.setFocusPainted(false);
        withdrawBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        withdrawBtn.addActionListener(e -> handleWithdraw());

        gbc.gridx = 1; gbc.gridy = row;
        card.add(withdrawBtn, gbc);

        content.add(card);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void verifyAndDisplayLimits() {
        String accNum = accNumField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (accNum.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Account Number and PIN.", "Credentials Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Account acc = bankingService.getAccountInformation(accNum, pin);
            LocalDate today = LocalDate.now();
            double dailyLimit = acc.getDailyWithdrawalLimit();
            double withdrawn = acc.getDailyWithdrawnAmount();
            double remaining = acc.getRemainingDailyLimit(today);
            double pct = (dailyLimit > 0) ? Math.min(100, (withdrawn / dailyLimit) * 100.0) : 0;

            holderInfoLabel.setText("Holder: " + acc.getHolderName() + " (" + acc.getAccountType() + ")");
            balanceInfoLabel.setText(String.format("Current Balance: ₹%.2f", acc.getBalance()));
            dailyLimitLabel.setText(String.format("Daily Limit: ₹%.2f", dailyLimit));
            withdrawnTodayLabel.setText(String.format("Withdrawn Today: ₹%.2f", withdrawn));
            remainingLimitLabel.setText(String.format("Remaining Limit: ₹%.2f", remaining));

            limitProgressBar.setValue((int) pct);
            limitProgressBar.setString(String.format("Daily Limit Usage: %.1f%% (₹%.2f used)", pct, withdrawn));

        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Verification Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleWithdraw() {
        String accNum = accNumField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String amountStr = amountField.getText().trim();
        String desc = descField.getText().trim();

        if (accNum.isEmpty() || pin.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Account Number, PIN, and Withdrawal Amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Account updated = bankingService.withdraw(accNum, pin, amount, desc);

            String receipt = String.format(
                    "Withdrawal Authorized and Completed!\n\n" +
                    "Account Number:    %s\n" +
                    "Holder Name:       %s\n" +
                    "Withdrawn Amount:  ₹%.2f\n" +
                    "New Balance:       ₹%.2f\n" +
                    "Today's Limit Left: ₹%.2f (of ₹%.2f)\n\n" +
                    "The daily withdrawal limit was honored and recorded.",
                    updated.getAccountNumber(), updated.getHolderName(), amount,
                    updated.getBalance(), updated.getRemainingDailyLimit(LocalDate.now()),
                    updated.getDailyWithdrawalLimit());

            JOptionPane.showMessageDialog(this, receipt, "Withdrawal Receipt", JOptionPane.INFORMATION_MESSAGE);

            verifyAndDisplayLimits();
            amountField.setText("");

        } catch (DailyLimitExceededException ex) {
            // Emphasize the daily withdrawal limit rule
            String warningMsg = String.format(
                    "TRANSACTION REJECTED: DAILY LIMIT EXCEEDED!\n\n" +
                    "Configured Daily Limit:     ₹%.2f\n" +
                    "Already Withdrawn Today:    ₹%.2f\n" +
                    "Attempted Withdrawal:       ₹%.2f\n" +
                    "Available Limit Remaining:  ₹%.2f\n\n" +
                    "The system has strictly honored the daily withdrawal limit.",
                    ex.getDailyLimit(), ex.getAlreadyWithdrawnToday(), ex.getAttemptedAmount(), ex.getRemainingLimit());

            JOptionPane.showMessageDialog(this, warningMsg, "Daily Withdrawal Limit Enforced", JOptionPane.WARNING_MESSAGE);

        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Withdrawal Denied", JOptionPane.ERROR_MESSAGE);
        }
    }
}
