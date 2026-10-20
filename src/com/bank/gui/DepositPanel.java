package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DepositPanel extends JPanel {
    private final BankingService bankingService;

    private JTextField accNumField;
    private JTextField amountField;
    private JTextField descField;
    private JLabel previewHolderLabel;
    private JLabel previewTypeLabel;
    private JLabel previewBalanceLabel;

    public DepositPanel(BankingService bankingService) {
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

        JLabel titleLabel = new JLabel("Deposit Funds");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Deposit cash or funds directly into any registered bank account.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(25));

        // Card Container
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(800, 420));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        accNumField = new JTextField(20);
        amountField = new JTextField(20);
        descField = new JTextField("Cash Deposit", 20);

        JButton verifyBtn = new JButton("Check Account");
        verifyBtn.setBackground(new Color(226, 232, 240));
        verifyBtn.setFocusPainted(false);
        verifyBtn.addActionListener(e -> checkAccount());

        JPanel accNumRow = new JPanel(new BorderLayout(8, 0));
        accNumRow.setBackground(Color.WHITE);
        accNumRow.add(accNumField, BorderLayout.CENTER);
        accNumRow.add(verifyBtn, BorderLayout.EAST);

        // Account Preview Box
        JPanel previewBox = new JPanel(new GridLayout(3, 1, 4, 4));
        previewBox.setBackground(new Color(248, 250, 252));
        previewBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        previewHolderLabel = new JLabel("Account Holder: --");
        previewTypeLabel = new JLabel("Account Type: --");
        previewBalanceLabel = new JLabel("Current Balance: --");

        previewHolderLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewBalanceLabel.setForeground(new Color(22, 101, 52));

        previewBox.add(previewHolderLabel);
        previewBox.add(previewTypeLabel);
        previewBox.add(previewBalanceLabel);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(new JLabel("Account Number *:"), gbc);
        gbc.gridx = 1;
        card.add(accNumRow, gbc);
        row++;

        gbc.gridx = 1; gbc.gridy = row;
        card.add(previewBox, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        card.add(new JLabel("Deposit Amount (₹) *:"), gbc);
        gbc.gridx = 1;
        card.add(amountField, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        card.add(new JLabel("Description / Remark:"), gbc);
        gbc.gridx = 1;
        card.add(descField, gbc);
        row++;

        // Deposit Button
        JButton depositBtn = new JButton("Confirm Deposit");
        depositBtn.setBackground(new Color(16, 185, 129));
        depositBtn.setForeground(Color.WHITE);
        depositBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositBtn.setFocusPainted(false);
        depositBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        depositBtn.addActionListener(e -> handleDeposit());

        gbc.gridx = 1; gbc.gridy = row;
        card.add(depositBtn, gbc);

        content.add(card);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void checkAccount() {
        String accNum = accNumField.getText().trim();
        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Account Number first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Account acc = bankingService.getAccountInformation(accNum, null);
            previewHolderLabel.setText("Account Holder: " + acc.getHolderName());
            previewTypeLabel.setText("Account Type: " + acc.getAccountType() + " | Status: " + acc.getStatus());
            previewBalanceLabel.setText(String.format("Current Balance: ₹%.2f", acc.getBalance()));
        } catch (BankingException ex) {
            previewHolderLabel.setText("Account Holder: [Not Found]");
            previewTypeLabel.setText("Account Type: --");
            previewBalanceLabel.setText("Current Balance: --");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Account Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeposit() {
        String accNum = accNumField.getText().trim();
        String amountStr = amountField.getText().trim();
        String desc = descField.getText().trim();

        if (accNum.isEmpty() || amountStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide Account Number and Deposit Amount.", "Input Required", JOptionPane.WARNING_MESSAGE);
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
            Account updated = bankingService.deposit(accNum, amount, desc);
            String successMsg = String.format(
                    "Deposit Successful!\n\n" +
                    "Account Number: %s\n" +
                    "Holder Name:    %s\n" +
                    "Amount Added:   ₹%.2f\n" +
                    "Updated Balance: ₹%.2f\n\n" +
                    "Transaction has been safely logged in the database ledger.",
                    updated.getAccountNumber(), updated.getHolderName(), amount, updated.getBalance());

            JOptionPane.showMessageDialog(this, successMsg, "Deposit Receipt", JOptionPane.INFORMATION_MESSAGE);

            // Refresh preview
            checkAccount();
            amountField.setText("");
        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Deposit Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
