package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AccountInfoPanel extends JPanel {
    private final BankingService bankingService;

    private JTextField accNumField;
    private JPasswordField pinField;

    private JPanel infoContainer;
    private JLabel nameLabel;
    private JLabel accNumLabel;
    private JLabel typeLabel;
    private JLabel phoneLabel;
    private JLabel emailLabel;
    private JLabel balanceLabel;
    private JLabel dailyLimitLabel;
    private JLabel withdrawnTodayLabel;
    private JLabel remainingLimitLabel;
    private JLabel createdLabel;

    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public AccountInfoPanel(BankingService bankingService) {
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

        JLabel titleLabel = new JLabel("Account Information & Statement");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Comprehensive profile overview and transaction passbook statement.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(20));

        // Search Form Card
        JPanel searchCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        searchCard.setBackground(Color.WHITE);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.setMaximumSize(new Dimension(1000, 70));

        accNumField = new JTextField(15);
        pinField = new JPasswordField(10);

        JButton viewBtn = new JButton("Fetch Information");
        viewBtn.setBackground(new Color(37, 99, 235));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> handleFetchInfo());

        searchCard.add(new JLabel("Account Number:"));
        searchCard.add(accNumField);
        searchCard.add(new JLabel("PIN:"));
        searchCard.add(pinField);
        searchCard.add(viewBtn);

        content.add(searchCard);
        content.add(Box.createVerticalStrut(20));

        // Profile Details Card
        infoContainer = new JPanel(new GridLayout(5, 2, 20, 8));
        infoContainer.setBackground(Color.WHITE);
        infoContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        " Account Profile Information "
                ),
                new EmptyBorder(15, 20, 15, 20)
        ));
        infoContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoContainer.setMaximumSize(new Dimension(1000, 200));

        nameLabel = new JLabel("Holder Name: --");
        accNumLabel = new JLabel("Account Number: --");
        typeLabel = new JLabel("Account Type: --");
        balanceLabel = new JLabel("Balance: --");
        phoneLabel = new JLabel("Phone: --");
        emailLabel = new JLabel("Email: --");
        dailyLimitLabel = new JLabel("Daily Limit: --");
        withdrawnTodayLabel = new JLabel("Withdrawn Today: --");
        remainingLimitLabel = new JLabel("Remaining Today: --");
        createdLabel = new JLabel("Created Date: --");

        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        balanceLabel.setForeground(new Color(22, 101, 52));
        remainingLimitLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        remainingLimitLabel.setForeground(new Color(180, 83, 9));

        infoContainer.add(nameLabel);
        infoContainer.add(accNumLabel);
        infoContainer.add(typeLabel);
        infoContainer.add(balanceLabel);
        infoContainer.add(phoneLabel);
        infoContainer.add(emailLabel);
        infoContainer.add(dailyLimitLabel);
        infoContainer.add(withdrawnTodayLabel);
        infoContainer.add(remainingLimitLabel);
        infoContainer.add(createdLabel);

        content.add(infoContainer);
        content.add(Box.createVerticalStrut(20));

        // Transactions Table Card
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        " Account Passbook / Transaction History "
                ),
                new EmptyBorder(10, 15, 15, 15)
        ));
        tableCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableCard.setMaximumSize(new Dimension(1000, 320));

        String[] columns = {"ID", "Date & Time", "Type", "Amount (₹)", "Balance (₹)", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(26);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(new Color(241, 245, 249));

        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        transactionTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(transactionTable);
        tableScroll.setPreferredSize(new Dimension(950, 220));
        tableCard.add(tableScroll, BorderLayout.CENTER);

        content.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);
    }

    private void handleFetchInfo() {
        String accNum = accNumField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (accNum.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Account Number.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Account acc = bankingService.getAccountInformation(accNum, pin.isEmpty() ? null : pin);
            LocalDate today = LocalDate.now();

            nameLabel.setText("Holder Name: " + acc.getHolderName());
            accNumLabel.setText("Account Number: " + acc.getAccountNumber());
            typeLabel.setText("Account Type: " + acc.getAccountType() + " (" + acc.getStatus() + ")");
            balanceLabel.setText(String.format("Balance: ₹%,.2f", acc.getBalance()));
            phoneLabel.setText("Phone: " + (acc.getPhoneNumber().isEmpty() ? "N/A" : acc.getPhoneNumber()));
            emailLabel.setText("Email: " + (acc.getEmail().isEmpty() ? "N/A" : acc.getEmail()));
            dailyLimitLabel.setText(String.format("Daily Limit: ₹%,.2f", acc.getDailyWithdrawalLimit()));
            withdrawnTodayLabel.setText(String.format("Withdrawn Today: ₹%,.2f", acc.getDailyWithdrawnAmount()));
            remainingLimitLabel.setText(String.format("Remaining Today: ₹%,.2f", acc.getRemainingDailyLimit(today)));
            createdLabel.setText("Registered: " + (acc.getCreatedAt() != null ? acc.getCreatedAt().toString().replace("T", " ") : "N/A"));

            // Load transactions
            tableModel.setRowCount(0);
            List<Transaction> transactions = bankingService.getTransactionHistory(accNum, pin.isEmpty() ? null : pin);
            for (Transaction tx : transactions) {
                tableModel.addRow(new Object[]{
                        tx.getTransactionId(),
                        tx.getFormattedTimestamp(),
                        tx.getTransactionType(),
                        String.format("₹%,.2f", tx.getAmount()),
                        String.format("₹%,.2f", tx.getBalanceAfter()),
                        tx.getDescription()
                });
            }

        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fetch Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
