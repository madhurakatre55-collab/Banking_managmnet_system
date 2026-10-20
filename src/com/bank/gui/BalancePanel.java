package com.bank.gui;

import com.bank.exception.BankingException;
import com.bank.model.Account;
import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

public class BalancePanel extends JPanel {
    private final BankingService bankingService;

    private JTextField accNumField;
    private JPasswordField pinField;

    private JPanel resultCard;
    private JLabel balanceValLabel;
    private JLabel nameValLabel;
    private JLabel accNumValLabel;
    private JLabel typeValLabel;
    private JLabel dailyLimitValLabel;
    private JLabel remainingLimitValLabel;
    private JLabel statusValLabel;

    public BalancePanel(BankingService bankingService) {
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

        JLabel titleLabel = new JLabel("Check Account Balance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 42, 86));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Secure balance and limit inquiry with PIN authentication.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(113, 128, 150));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(titleLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subtitleLabel);
        content.add(Box.createVerticalStrut(25));

        // Search Form Card
        JPanel searchCard = new JPanel(new GridBagLayout());
        searchCard.setBackground(Color.WHITE);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(20, 25, 20, 25)
        ));
        searchCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchCard.setMaximumSize(new Dimension(800, 160));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        accNumField = new JTextField(15);
        pinField = new JPasswordField(10);

        JButton checkBtn = new JButton("Inquire Balance");
        checkBtn.setBackground(new Color(37, 99, 235));
        checkBtn.setForeground(Color.WHITE);
        checkBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        checkBtn.setFocusPainted(false);
        checkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBtn.addActionListener(e -> handleCheckBalance());

        gbc.gridx = 0; gbc.gridy = 0;
        searchCard.add(new JLabel("Account Number:"), gbc);
        gbc.gridx = 1;
        searchCard.add(accNumField, gbc);

        gbc.gridx = 2;
        searchCard.add(new JLabel("PIN:"), gbc);
        gbc.gridx = 3;
        searchCard.add(pinField, gbc);

        gbc.gridx = 4;
        searchCard.add(checkBtn, gbc);

        content.add(searchCard);
        content.add(Box.createVerticalStrut(20));

        // Result Card (Initially hidden or showing placeholders)
        resultCard = new JPanel(new BorderLayout());
        resultCard.setBackground(Color.WHITE);
        resultCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));
        resultCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultCard.setMaximumSize(new Dimension(800, 320));
        resultCard.setVisible(false);

        // Top Balance Banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(239, 246, 255));
        banner.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel bannerTitle = new JLabel("AVAILABLE TOTAL BALANCE");
        bannerTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bannerTitle.setForeground(new Color(30, 64, 175));

        balanceValLabel = new JLabel("₹0.00");
        balanceValLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        balanceValLabel.setForeground(new Color(30, 58, 138));

        banner.add(bannerTitle, BorderLayout.NORTH);
        banner.add(balanceValLabel, BorderLayout.CENTER);

        // Details Grid
        JPanel detailsGrid = new JPanel(new GridLayout(3, 2, 20, 12));
        detailsGrid.setBackground(Color.WHITE);
        detailsGrid.setBorder(new EmptyBorder(20, 10, 10, 10));

        nameValLabel = new JLabel("Holder Name: --");
        accNumValLabel = new JLabel("Account Number: --");
        typeValLabel = new JLabel("Account Type: --");
        dailyLimitValLabel = new JLabel("Configured Daily Limit: --");
        remainingLimitValLabel = new JLabel("Today's Available Limit: --");
        statusValLabel = new JLabel("Account Status: --");

        nameValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        accNumValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        typeValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dailyLimitValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        remainingLimitValLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        remainingLimitValLabel.setForeground(new Color(180, 83, 9));
        statusValLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        detailsGrid.add(nameValLabel);
        detailsGrid.add(accNumValLabel);
        detailsGrid.add(typeValLabel);
        detailsGrid.add(statusValLabel);
        detailsGrid.add(dailyLimitValLabel);
        detailsGrid.add(remainingLimitValLabel);

        resultCard.add(banner, BorderLayout.NORTH);
        resultCard.add(detailsGrid, BorderLayout.CENTER);

        content.add(resultCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void handleCheckBalance() {
        String accNum = accNumField.getText().trim();
        String pin = new String(pinField.getPassword()).trim();

        if (accNum.isEmpty() || pin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Account Number and PIN.", "Credentials Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Account acc = bankingService.getAccountInformation(accNum, pin);
            LocalDate today = LocalDate.now();

            balanceValLabel.setText(String.format("₹%,.2f", acc.getBalance()));
            nameValLabel.setText("Holder Name: " + acc.getHolderName());
            accNumValLabel.setText("Account Number: " + acc.getAccountNumber());
            typeValLabel.setText("Account Type: " + acc.getAccountType());
            statusValLabel.setText("Account Status: " + acc.getStatus());
            dailyLimitValLabel.setText(String.format("Configured Daily Limit: ₹%,.2f", acc.getDailyWithdrawalLimit()));
            remainingLimitValLabel.setText(String.format("Today's Available Limit: ₹%,.2f", acc.getRemainingDailyLimit(today)));

            resultCard.setVisible(true);
            resultCard.revalidate();
            resultCard.repaint();

        } catch (BankingException ex) {
            resultCard.setVisible(false);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Inquiry Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
