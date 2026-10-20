package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreateAccountPanel extends JPanel {
    private final BankingService bankingService;

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField pinField;
    private JComboBox<String> typeCombo;
    private JTextField depositField;
    private JTextField dailyLimitField;
    private JLabel minBalanceLabel;
    private JLabel limitHintLabel;

    public CreateAccountPanel(BankingService bankingService) {
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

        // Header Title
        JLabel titleLabel = new JLabel("Open a New Bank Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Enter applicant details to register a new Savings or Current account.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(25));

        // Form Card Panel
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(800, 520));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        nameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);
        pinField = new JPasswordField(20);
        typeCombo = new JComboBox<>(new String[]{"SAVINGS", "CURRENT"});
        depositField = new JTextField("1000.00", 20);
        dailyLimitField = new JTextField("25000.00", 20);

        minBalanceLabel = new JLabel("Minimum initial deposit for Savings: ₹1,000.00");
        minBalanceLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        minBalanceLabel.setForeground(new Color(74, 85, 104));

        limitHintLabel = new JLabel("Default daily withdrawal limit: ₹25,000.00");
        limitHintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        limitHintLabel.setForeground(new Color(74, 85, 104));

        typeCombo.addActionListener(e -> {
            String selected = (String) typeCombo.getSelectedItem();
            if ("CURRENT".equals(selected)) {
                minBalanceLabel.setText("Minimum initial deposit for Current: ₹5,000.00 (Overdraft up to ₹25,000)");
                limitHintLabel.setText("Default daily withdrawal limit: ₹100,000.00");
                if (depositField.getText().trim().equals("1000.00") || depositField.getText().trim().isEmpty()) {
                    depositField.setText("5000.00");
                }
                dailyLimitField.setText("100000.00");
            } else {
                minBalanceLabel.setText("Minimum initial deposit for Savings: ₹1,000.00");
                limitHintLabel.setText("Default daily withdrawal limit: ₹25,000.00");
                if (depositField.getText().trim().equals("5000.00") || depositField.getText().trim().isEmpty()) {
                    depositField.setText("1000.00");
                }
                dailyLimitField.setText("25000.00");
            }
        });

        int row = 0;
        addFormRow(card, gbc, row++, "Full Customer Name *:", nameField);
        addFormRow(card, gbc, row++, "Email Address:", emailField);
        addFormRow(card, gbc, row++, "Phone Number:", phoneField);
        addFormRow(card, gbc, row++, "4-Digit Security PIN *:", pinField);
        addFormRow(card, gbc, row++, "Account Type *:", typeCombo);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        card.add(new JLabel("Initial Deposit Amount (₹) *:"), gbc);
        gbc.gridx = 1;
        card.add(depositField, gbc);
        row++;

        gbc.gridx = 1; gbc.gridy = row;
        card.add(minBalanceLabel, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        card.add(new JLabel("Daily Withdrawal Limit (₹) *:"), gbc);
        gbc.gridx = 1;
        card.add(dailyLimitField, gbc);
        row++;

        gbc.gridx = 1; gbc.gridy = row;
        card.add(limitHintLabel, gbc);
        row++;

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(Color.WHITE);

        JButton clearBtn = new JButton("Reset Form");
        clearBtn.setBackground(new Color(237, 242, 247));
        clearBtn.setForeground(new Color(45, 55, 72));
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> resetForm());

        JButton submitBtn = new JButton("Create Account");
        submitBtn.setBackground(new Color(37, 99, 235));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.addActionListener(e -> handleCreateAccount());

        btnPanel.add(clearBtn);
        btnPanel.add(submitBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        card.add(btnPanel, gbc);

        content.add(card);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void handleCreateAccount() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();
        String type = (String) typeCombo.getSelectedItem();
        String depositStr = depositField.getText().trim();
        String limitStr = dailyLimitField.getText().trim();

        if (name.isEmpty() || pin.isEmpty() || depositStr.isEmpty() || limitStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields marked with *",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pin.length() < 4) {
            JOptionPane.showMessageDialog(this, "PIN must be at least 4 digits.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double deposit;
        double dailyLimit;
        try {
            deposit = Double.parseDouble(depositStr);
            dailyLimit = Double.parseDouble(limitStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for Deposit and Daily Limit.",
                    "Invalid Number", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Account account = bankingService.createAccount(name, email, phone, pin, type, deposit, dailyLimit);

            String message = String.format(
                    "Account Created Successfully!\n\n" +
                    "Account Number: %s\n" +
                    "Holder Name:    %s\n" +
                    "Account Type:   %s\n" +
                    "Balance:        ₹%.2f\n" +
                    "Daily Limit:    ₹%.2f\n\n" +
                    "Please record the Account Number and PIN securely for future operations.",
                    account.getAccountNumber(), account.getHolderName(), account.getAccountType(),
                    account.getBalance(), account.getDailyWithdrawalLimit());

            JOptionPane.showMessageDialog(this, message, "Account Opened Successfully", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Account Creation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        pinField.setText("");
        typeCombo.setSelectedIndex(0);
        depositField.setText("1000.00");
        dailyLimitField.setText("25000.00");
    }
}
