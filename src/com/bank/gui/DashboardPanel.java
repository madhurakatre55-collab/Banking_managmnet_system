package com.bank.gui;

import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final BankingService bankingService;

    public DashboardPanel(MainFrame mainFrame, BankingService bankingService) {
        this.mainFrame = mainFrame;
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

        // Hero Welcome Banner
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(new Color(24, 43, 73));
        hero.setBorder(new EmptyBorder(25, 30, 25, 30));
        hero.setMaximumSize(new Dimension(1000, 130));
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel heroTitle = new JLabel("APEX BANKING MANAGEMENT SYSTEM");
        heroTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        heroTitle.setForeground(Color.WHITE);

        JLabel heroSubtitle = new JLabel("Java OOP Mini Project with JDBC Database Integration | BTech Computer Science");
        heroSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heroSubtitle.setForeground(new Color(191, 219, 254));

        hero.add(heroTitle, BorderLayout.NORTH);
        hero.add(Box.createVerticalStrut(6), BorderLayout.CENTER);
        hero.add(heroSubtitle, BorderLayout.SOUTH);

        content.add(hero);
        content.add(Box.createVerticalStrut(25));

        // Quick Operations Grid
        JLabel opTitle = new JLabel("CORE BANKING OPERATIONS");
        opTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        opTitle.setForeground(new Color(71, 85, 105));
        opTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(opTitle);
        content.add(Box.createVerticalStrut(12));

        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setBackground(new Color(245, 247, 250));
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(1000, 240));

        grid.add(createActionCard("Create Account", "Open Savings or Current accounts with custom limits", new Color(37, 99, 235), () -> mainFrame.showPanel("CREATE")));
        grid.add(createActionCard("Deposit Money", "Credit cash or fund transfers directly to accounts", new Color(16, 185, 129), () -> mainFrame.showPanel("DEPOSIT")));
        grid.add(createActionCard("Withdraw Money", "Secure withdrawals with daily limit enforcement", new Color(239, 68, 68), () -> mainFrame.showPanel("WITHDRAW")));
        grid.add(createActionCard("Check Balance", "Quick balance & remaining daily limit inquiry", new Color(245, 158, 11), () -> mainFrame.showPanel("BALANCE")));
        grid.add(createActionCard("Account Info", "Comprehensive profile details & passbook history", new Color(99, 102, 241), () -> mainFrame.showPanel("INFO")));
        grid.add(createActionCard("Accounts Directory", "Live view of all accounts in the database", new Color(14, 165, 233), () -> mainFrame.showPanel("ALL")));

        content.add(grid);
        content.add(Box.createVerticalStrut(30));

        // Academic / OOP Highlights Panel
        JPanel academicCard = new JPanel(new BorderLayout());
        academicCard.setBackground(Color.WHITE);
        academicCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 20, 16, 20)
        ));
        academicCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        academicCard.setMaximumSize(new Dimension(1000, 160));

        JLabel oopTitle = new JLabel("Object-Oriented Programming (OOP) Features Implemented:");
        oopTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        oopTitle.setForeground(new Color(30, 41, 59));

        JLabel oopDetails = new JLabel("<html>" +
                "• <b>Encapsulation:</b> Private state variables, validated accessors, secure PIN authentication.<br>" +
                "• <b>Inheritance:</b> Abstract <code>Account</code> base class extended by <code>SavingsAccount</code> and <code>CurrentAccount</code>.<br>" +
                "• <b>Polymorphism:</b> Overridden limit rules, minimum balance constraints, and dynamic binding.<br>" +
                "• <b>Daily Limit Enforcement:</b> Date-aware tracking honoring per-day maximum withdrawal quota with automated daily rollover.<br>" +
                "• <b>JDBC & DAO:</b> <code>PreparedStatement</code> CRUD queries, transaction ledger, and database resilience (SQLite / MySQL)." +
                "</html>");
        oopDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        oopDetails.setForeground(new Color(71, 85, 105));

        academicCard.add(oopTitle, BorderLayout.NORTH);
        academicCard.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        academicCard.add(oopDetails, BorderLayout.SOUTH);

        content.add(academicCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createActionCard(String title, String desc, Color accentColor, Runnable action) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                        new EmptyBorder(16, 16, 16, 16)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(new Color(30, 41, 59));

        JLabel descLbl = new JLabel("<html>" + desc + "</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(new Color(100, 116, 139));

        JButton btn = new JButton("Open ->");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(accentColor);
        btn.setBackground(new Color(248, 250, 252));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> action.run());

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(descLbl, BorderLayout.CENTER);
        card.add(btn, BorderLayout.SOUTH);

        return card;
    }
}
