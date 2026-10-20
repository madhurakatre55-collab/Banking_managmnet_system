package com.bank.gui;

import com.bank.service.BankingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final BankingService bankingService;
    private final CardLayout cardLayout;
    private final JPanel contentCards;

    private CreateAccountPanel createAccountPanel;
    private DepositPanel depositPanel;
    private WithdrawPanel withdrawPanel;
    private BalancePanel balancePanel;
    private AccountInfoPanel accountInfoPanel;
    private AllAccountsPanel allAccountsPanel;
    private DashboardPanel dashboardPanel;

    private JButton activeNavButton = null;

    public MainFrame(BankingService bankingService) {
        this.bankingService = bankingService;
        this.cardLayout = new CardLayout();
        this.contentCards = new JPanel(cardLayout);

        setTitle("Banking Management System - Java OOP Mini Project");
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 620));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(15, 23, 42)); // Dark Slate
        sidebar.setPreferredSize(new Dimension(240, 720));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        // Brand Logo / Header
        JLabel brandLogo = new JLabel("APEX BANK");
        brandLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandLogo.setForeground(Color.WHITE);
        brandLogo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel brandSub = new JLabel("Core Banking System");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(148, 163, 184));
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(brandLogo);
        sidebar.add(brandSub);
        sidebar.add(Box.createVerticalStrut(25));

        // Navigation Buttons
        JButton navDash = createNavButton("Dashboard", "DASHBOARD");
        JButton navCreate = createNavButton("Create Account", "CREATE");
        JButton navDeposit = createNavButton("Deposit Money", "DEPOSIT");
        JButton navWithdraw = createNavButton("Withdraw Money", "WITHDRAW");
        JButton navBalance = createNavButton("Check Balance", "BALANCE");
        JButton navInfo = createNavButton("Account Info", "INFO");
        JButton navAll = createNavButton("All Accounts", "ALL");

        sidebar.add(navDash);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navCreate);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navDeposit);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navWithdraw);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navBalance);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navInfo);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(navAll);

        sidebar.add(Box.createVerticalGlue());

        // Footer in Sidebar
        JLabel footerLbl = new JLabel("OOP Java Mini Project");
        footerLbl.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerLbl.setForeground(new Color(100, 116, 139));
        footerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerLbl);

        add(sidebar, BorderLayout.WEST);

        // 2. Central Content Cards
        dashboardPanel = new DashboardPanel(this, bankingService);
        createAccountPanel = new CreateAccountPanel(bankingService);
        depositPanel = new DepositPanel(bankingService);
        withdrawPanel = new WithdrawPanel(bankingService);
        balancePanel = new BalancePanel(bankingService);
        accountInfoPanel = new AccountInfoPanel(bankingService);
        allAccountsPanel = new AllAccountsPanel(bankingService);

        contentCards.add(dashboardPanel, "DASHBOARD");
        contentCards.add(createAccountPanel, "CREATE");
        contentCards.add(depositPanel, "DEPOSIT");
        contentCards.add(withdrawPanel, "WITHDRAW");
        contentCards.add(balancePanel, "BALANCE");
        contentCards.add(accountInfoPanel, "INFO");
        contentCards.add(allAccountsPanel, "ALL");

        add(contentCards, BorderLayout.CENTER);

        setActiveNav(navDash);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(15, 23, 42));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            setActiveNav(btn);
            showPanel(cardName);
        });

        return btn;
    }

    private void setActiveNav(JButton btn) {
        if (activeNavButton != null) {
            activeNavButton.setBackground(new Color(15, 23, 42));
            activeNavButton.setForeground(new Color(203, 213, 225));
            activeNavButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        activeNavButton = btn;
        activeNavButton.setBackground(new Color(30, 41, 59));
        activeNavButton.setForeground(Color.WHITE);
        activeNavButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    public void showPanel(String name) {
        if ("ALL".equals(name) && allAccountsPanel != null) {
            allAccountsPanel.loadAccounts();
        }
        cardLayout.show(contentCards, name);
    }
}
