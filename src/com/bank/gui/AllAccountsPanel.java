package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AllAccountsPanel extends JPanel {
    private final BankingService bankingService;

    private JTable accountsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel totalAccountsLabel;
    private JLabel totalDepositsLabel;

    public AllAccountsPanel(BankingService bankingService) {
        this.bankingService = bankingService;
        initUI();
        loadAccounts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBorder(new EmptyBorder(30, 40, 30, 40));
        content.setBackground(new Color(245, 247, 250));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("Bank Accounts Directory (DBMS Ledger)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));

        JLabel subtitleLabel = new JLabel("Live database view of all registered accounts, balance reserves, and daily limits.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));

        titleBox.add(titleLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitleLabel);

        // Stats Summary Box
        JPanel statsBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsBox.setBackground(new Color(245, 247, 250));

        totalAccountsLabel = new JLabel("Total Accounts: 0");
        totalAccountsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalAccountsLabel.setForeground(new Color(30, 64, 175));

        totalDepositsLabel = new JLabel("Total Bank Reserves: ₹0.00");
        totalDepositsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        totalDepositsLabel.setForeground(new Color(22, 101, 52));

        statsBox.add(totalAccountsLabel);
        statsBox.add(new JSeparator(SwingConstants.VERTICAL));
        statsBox.add(totalDepositsLabel);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(statsBox, BorderLayout.EAST);

        content.add(headerPanel, BorderLayout.NORTH);

        // Table Panel Card
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Search & Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBox.setBackground(Color.WHITE);
        searchBox.add(new JLabel("Quick Search:"));
        searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, number, type...");
        searchBox.add(searchField);

        JButton refreshBtn = new JButton("Refresh Data");
        refreshBtn.setBackground(new Color(241, 245, 249));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadAccounts());

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);

        card.add(toolbar, BorderLayout.NORTH);

        // Accounts Table
        String[] columns = {
                "Account Number", "Holder Name", "Type", "Balance (₹)",
                "Daily Limit (₹)", "Used Today (₹)", "Remaining Today (₹)", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        accountsTable = new JTable(tableModel);
        accountsTable.setRowHeight(28);
        accountsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        accountsTable.getTableHeader().setBackground(new Color(241, 245, 249));

        sorter = new TableRowSorter<>(tableModel);
        accountsTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        accountsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        accountsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        accountsTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        JScrollPane tableScroll = new JScrollPane(accountsTable);
        card.add(tableScroll, BorderLayout.CENTER);

        content.add(card, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    public void loadAccounts() {
        try {
            List<Account> accounts = bankingService.getAllAccounts();
            tableModel.setRowCount(0);

            double totalBalance = 0.0;
            LocalDate today = LocalDate.now();

            for (Account acc : accounts) {
                totalBalance += acc.getBalance();
                tableModel.addRow(new Object[]{
                        acc.getAccountNumber(),
                        acc.getHolderName(),
                        acc.getAccountType(),
                        String.format("₹%,.2f", acc.getBalance()),
                        String.format("₹%,.2f", acc.getDailyWithdrawalLimit()),
                        String.format("₹%,.2f", acc.getDailyWithdrawnAmount()),
                        String.format("₹%,.2f", acc.getRemainingDailyLimit(today)),
                        acc.getStatus()
                });
            }

            totalAccountsLabel.setText("Total Accounts: " + accounts.size());
            totalDepositsLabel.setText(String.format("Total Bank Reserves: ₹%,.2f", totalBalance));

        } catch (BankingException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load accounts: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
