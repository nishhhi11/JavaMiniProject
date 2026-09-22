import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BankGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // backend system reference
    private BankSystem bank;

    // next account number to auto-assign
    private int nextAccountNo = 1001;

    // card layout and cards container
    private CardLayout cardLayout;
    private JPanel mainContentCards;

    // sidebar navigation buttons
    private Map<String, NavButton> navButtons = new LinkedHashMap<>();

    // dashboard dynamic labels & tables
    private JLabel dashTotalBalanceLabel;
    private JLabel dashTotalAccountsLabel;
    private JLabel dashSavingsCountLabel;
    private JLabel dashCurrentCountLabel;
    private DefaultTableModel dashAccountsTableModel;
    private DefaultTableModel dashRecentTxnTableModel;

    // accounts screen table model & summary
    private DefaultTableModel accountsTableModel;
    private JLabel accountsSummaryLabel;

    // create account dynamic visual card elements
    private JLabel createAccNextNoLabel;
    private DecorativeFinBankCard decorativeCard;

    // search screen dynamic components
    private JLabel searchResAccNo;
    private JLabel searchResName;
    private JLabel searchResPhone;
    private JLabel searchResType;
    private JLabel searchResBalance;
    private JLabel searchResTxnCount;
    private JLabel searchStatusLabel;
    private JPanel searchEmptyStatePanel;
    private JPanel searchDetailsGrid;
    private JPanel searchActionButtons;
    private int currentSearchedAccNo = -1;

    // transactions screen dynamic components
    private JLabel txnCustomerNameLabel;
    private JLabel txnCustomerPhoneLabel;
    private JLabel txnCustomerTypeLabel;
    private JLabel txnCustomerBalanceLabel;
    private JPanel txnCustomerChipPanel;
    private JLabel txnHeaderStatsLabel;
    private JPanel txnEmptyStatePanel;
    private JLabel txnEmptyStateTitle;
    private JLabel txnEmptyStateSubtitle;
    private JScrollPane txnScrollPane;
    private DefaultTableModel txnTableModel;
    ModernTextField txnAccNoField;
    ModernButton txnViewBtn;

    // summary screen dynamic components
    private JLabel summaryTotalAccountsLabel;
    private JLabel summaryTotalBalanceLabel;
    private JLabel summaryAvgBalanceLabel;
    private JLabel summarySavingsCountLabel;
    private JLabel summarySavingsMetricsLabel;
    private JLabel summaryCurrentMetricsLabel;
    private RoundedProgressBar summarySavingsBar;
    private RoundedProgressBar summaryCurrentBar;
    private JLabel summaryTotalTxnsLoggedLabel;
    private JLabel summaryTotalDepositsVolLabel;
    private JLabel summaryTotalWithdrawalsVolLabel;
    private DefaultTableModel summaryRecentActivityTableModel;
    private JScrollPane summaryRecentScrollPane;
    private JPanel summaryRecentEmptyPanel;

    // global design system color palette
    private static final Color BG_CREAM = new Color(247, 245, 240);
    private static final Color SIDEBAR_DARK = new Color(23, 24, 29);
    private static final Color SIDEBAR_BORDER = new Color(38, 40, 48);
    private static final Color SIDEBAR_SECTION_TITLE = new Color(105, 107, 118);
    private static final Color PRIMARY_CORAL = new Color(255, 120, 94);
    private static final Color PRIMARY_CORAL_HOVER = new Color(238, 102, 76);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color CARD_BORDER = new Color(234, 230, 222);
    private static final Color TEXT_DARK = new Color(36, 37, 42);
    private static final Color TEXT_MUTED = new Color(119, 119, 127);
    private static final Color SUCCESS_GREEN = new Color(79, 157, 105);
    private static final Color DANGER_RED = new Color(217, 92, 92);
    private static final Color DANGER_RED_HOVER = new Color(196, 75, 75);

    // soft pastel accents
    private static final Color PEACH_START = new Color(255, 245, 239);
    private static final Color PEACH_END = new Color(255, 233, 222);
    private static final Color PEACH_BORDER = new Color(250, 218, 204);

    private static final Color BLUE_START = new Color(236, 245, 254);
    private static final Color BLUE_END = new Color(220, 236, 249);
    private static final Color BLUE_BORDER = new Color(202, 224, 245);

    private static final Color LAVENDER_START = new Color(246, 240, 254);
    private static final Color LAVENDER_END = new Color(232, 221, 248);
    private static final Color LAVENDER_BORDER = new Color(220, 205, 242);

    private static final Color YELLOW_START = new Color(255, 250, 235);
    private static final Color YELLOW_END = new Color(255, 240, 201);
    private static final Color YELLOW_BORDER = new Color(246, 226, 175);

    private static final Color DANGER_START = new Color(255, 240, 240);
    private static final Color DANGER_END = new Color(254, 228, 228);
    private static final Color DANGER_BORDER = new Color(248, 204, 204);

    // constructor setting up the full application
    public BankGUI() {
        // initialize existing backend with starter accounts
        bank = new BankSystem();
        addSampleAccounts();

        // frame configuration
        setTitle("FinBank - Account Management System");
        setSize(1140, 740);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_CREAM);
        setLayout(new BorderLayout());

        // build sidebar and main content panels
        JPanel sidebar = createSidebar();
        JPanel contentContainer = createContentContainer();

        add(sidebar, BorderLayout.WEST);
        add(contentContainer, BorderLayout.CENTER);

        // initial screen and data refresh
        showScreen("Dashboard");
    }

    // load sample accounts with initial transaction records
    private void addSampleAccounts() {
        Customer c1 = new Customer(1001, "Nishi Sharma", "9876501234");
        Customer c2 = new Customer(1002, "Riya Patel", "9876543210");
        Customer c3 = new Customer(1003, "Aman Verma", "9811223344");

        BankAccount a1 = new BankAccount(1001, c1, "Savings", 15000);
        BankAccount a2 = new BankAccount(1002, c2, "Current", 28500);
        BankAccount a3 = new BankAccount(1003, c3, "Savings", 8200);

        a1.transactions.add(new Transaction("Deposit", 15000));
        a2.transactions.add(new Transaction("Deposit", 28500));
        a3.transactions.add(new Transaction("Deposit", 8200));

        bank.addAccount(a1);
        bank.addAccount(a2);
        bank.addAccount(a3);

        nextAccountNo = 1004;
    }

    // =========================================================================
    // SIDEBAR NAVIGATION PANEL (CONSISTENT ACROSS ALL PAGES)
    // =========================================================================
    private JPanel createSidebar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_CREAM);
        outer.setBorder(new EmptyBorder(16, 16, 16, 8));

        RoundedCard sidebarCard = new RoundedCard(24, SIDEBAR_DARK, SIDEBAR_BORDER);
        sidebarCard.setLayout(new BorderLayout(0, 16));
        sidebarCard.setPreferredSize(new Dimension(230, 0));
        sidebarCard.setBorder(new EmptyBorder(24, 16, 20, 16));

        // brand header
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setOpaque(false);

        JLabel dotLabel = new JLabel("●");
        dotLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        dotLabel.setForeground(PRIMARY_CORAL);

        JLabel brandTitle = new JLabel("FinBank");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSubtitle = new JLabel("MANAGEMENT SYSTEM");
        brandSubtitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandSubtitle.setForeground(SIDEBAR_SECTION_TITLE);

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 1));
        titleStack.setOpaque(false);
        titleStack.add(brandTitle);
        titleStack.add(brandSubtitle);

        brandPanel.add(dotLabel, BorderLayout.WEST);
        brandPanel.add(titleStack, BorderLayout.CENTER);
        sidebarCard.add(brandPanel, BorderLayout.NORTH);

        // grouped navigation panel
        JPanel navMenuPanel = new JPanel();
        navMenuPanel.setOpaque(false);
        navMenuPanel.setLayout(new BoxLayout(navMenuPanel, BoxLayout.Y_AXIS));

        // group 1: overview
        addSidebarSection(navMenuPanel, "OVERVIEW");
        addNavItem(navMenuPanel, "Dashboard", "⊞", "Dashboard");
        addNavItem(navMenuPanel, "Accounts", "👥", "Accounts");

        navMenuPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // group 2: operations
        addSidebarSection(navMenuPanel, "OPERATIONS");
        addNavItem(navMenuPanel, "Create Account", "＋", "Create Account");
        addNavItem(navMenuPanel, "Deposit", "↓", "Deposit");
        addNavItem(navMenuPanel, "Withdraw", "↑", "Withdraw");
        addNavItem(navMenuPanel, "Transactions", "📄", "Transactions");

        navMenuPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // group 3: management
        addSidebarSection(navMenuPanel, "MANAGEMENT");
        addNavItem(navMenuPanel, "Search", "🔍", "Search");
        addNavItem(navMenuPanel, "Update", "✎", "Update");
        addNavItem(navMenuPanel, "Delete", "🗑", "Delete");
        addNavItem(navMenuPanel, "Bank Summary", "📊", "Bank Summary");

        JScrollPane menuScroll = new JScrollPane(navMenuPanel);
        menuScroll.setOpaque(false);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setBorder(null);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarCard.add(menuScroll, BorderLayout.CENTER);

        // footer
        JLabel footerLabel = new JLabel("FinBank Desktop • v1.2");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(SIDEBAR_SECTION_TITLE);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sidebarCard.add(footerLabel, BorderLayout.SOUTH);

        outer.add(sidebarCard, BorderLayout.CENTER);
        return outer;
    }

    private void addSidebarSection(JPanel parent, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLabel.setForeground(SIDEBAR_SECTION_TITLE);
        sectionLabel.setBorder(new EmptyBorder(4, 12, 6, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(sectionLabel);
    }

    private void addNavItem(JPanel parent, String label, String icon, String screenKey) {
        NavButton btn = new NavButton(icon, label);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        navButtons.put(screenKey, btn);

        btn.addActionListener(e -> showScreen(screenKey));

        parent.add(btn);
        parent.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    // create card-layout main content wrapper
    private JPanel createContentContainer() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(BG_CREAM);
        outer.setBorder(new EmptyBorder(16, 8, 16, 16));

        cardLayout = new CardLayout();
        mainContentCards = new JPanel(cardLayout);
        mainContentCards.setOpaque(false);

        // register all 10 functional screens
        mainContentCards.add(createDashboardPanel(), "Dashboard");
        mainContentCards.add(createAccountsPanel(), "Accounts");
        mainContentCards.add(createCreateAccountPanel(), "Create Account");
        mainContentCards.add(createDepositPanel(), "Deposit");
        mainContentCards.add(createWithdrawPanel(), "Withdraw");
        mainContentCards.add(createSearchPanel(), "Search");
        mainContentCards.add(createTransactionsPanel(), "Transactions");
        mainContentCards.add(createUpdatePanel(), "Update");
        mainContentCards.add(createDeletePanel(), "Delete");
        mainContentCards.add(createSummaryPanel(), "Bank Summary");

        outer.add(mainContentCards, BorderLayout.CENTER);
        return outer;
    }

    // switch active card and update navigation highlight
    public void showScreen(String name) {
        cardLayout.show(mainContentCards, name);

        // highlight active nav button
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }

        refreshAll();
    }

    // =========================================================================
    // 1. DASHBOARD SCREEN
    // =========================================================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // top page header
        JPanel headerPanel = createPageHeader(
            "Finance Management Dashboard",
            "Real-time summary of bank accounts and recent activity.",
            "● Live System"
        );
        panel.add(headerPanel, BorderLayout.NORTH);

        // main dashboard scrollable content
        JPanel dashboardContent = new JPanel();
        dashboardContent.setOpaque(false);
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));

        // 1. row of 4 pastel stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        statsRow.setPreferredSize(new Dimension(860, 115));

        dashTotalBalanceLabel = new JLabel("Rs. 0.00");
        statsRow.add(createStatCard("Total Balance", dashTotalBalanceLabel, "Across all active accounts", PEACH_START, PEACH_END, PEACH_BORDER));

        dashTotalAccountsLabel = new JLabel("0");
        statsRow.add(createStatCard("Total Accounts", dashTotalAccountsLabel, "Active customer accounts", BLUE_START, BLUE_END, BLUE_BORDER));

        dashSavingsCountLabel = new JLabel("0");
        statsRow.add(createStatCard("Savings Accounts", dashSavingsCountLabel, "Retail savings portfolio", LAVENDER_START, LAVENDER_END, LAVENDER_BORDER));

        dashCurrentCountLabel = new JLabel("0");
        statsRow.add(createStatCard("Current Accounts", dashCurrentCountLabel, "Commercial portfolio", YELLOW_START, YELLOW_END, YELLOW_BORDER));

        dashboardContent.add(statsRow);
        dashboardContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // 2. middle row: account overview & recent transactions
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setPreferredSize(new Dimension(860, 290));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));

        // left card: account overview preview
        RoundedCard accountsCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        accountsCard.setLayout(new BorderLayout(0, 12));
        accountsCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel accCardHeader = new JPanel(new BorderLayout());
        accCardHeader.setOpaque(false);
        JLabel accTitle = new JLabel("Account Overview");
        accTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        accTitle.setForeground(TEXT_DARK);

        ModernButton viewAllBtn = new ModernButton("View All", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 12);
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllBtn.addActionListener(e -> showScreen("Accounts"));

        accCardHeader.add(accTitle, BorderLayout.WEST);
        accCardHeader.add(viewAllBtn, BorderLayout.EAST);
        accountsCard.add(accCardHeader, BorderLayout.NORTH);

        String[] accCols = {"Acc No.", "Customer Name", "Type", "Balance"};
        dashAccountsTableModel = new DefaultTableModel(accCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable dashAccTable = new JTable(dashAccountsTableModel);
        styleTable(dashAccTable);
        JScrollPane dashAccScroll = new JScrollPane(dashAccTable);
        dashAccScroll.setBorder(BorderFactory.createEmptyBorder());
        dashAccScroll.getViewport().setBackground(CARD_WHITE);
        accountsCard.add(dashAccScroll, BorderLayout.CENTER);

        // right card: recent transactions
        RoundedCard recentTxnCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        recentTxnCard.setLayout(new BorderLayout(0, 12));
        recentTxnCard.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel txnCardHeader = new JPanel(new BorderLayout());
        txnCardHeader.setOpaque(false);
        JLabel txnTitle = new JLabel("Recent Transactions");
        txnTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txnTitle.setForeground(TEXT_DARK);

        ModernButton fullHistoryBtn = new ModernButton("Full Statement", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 12);
        fullHistoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        fullHistoryBtn.addActionListener(e -> showScreen("Transactions"));

        txnCardHeader.add(txnTitle, BorderLayout.WEST);
        txnCardHeader.add(fullHistoryBtn, BorderLayout.EAST);
        recentTxnCard.add(txnCardHeader, BorderLayout.NORTH);

        String[] txnCols = {"Account", "Customer", "Type", "Amount"};
        dashRecentTxnTableModel = new DefaultTableModel(txnCols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable dashTxnTable = new JTable(dashRecentTxnTableModel);
        styleTable(dashTxnTable);
        dashTxnTable.getColumnModel().getColumn(2).setCellRenderer(new TransactionTypeRenderer());
        dashTxnTable.getColumnModel().getColumn(3).setCellRenderer(new AmountColorRenderer());

        JScrollPane dashTxnScroll = new JScrollPane(dashTxnTable);
        dashTxnScroll.setBorder(BorderFactory.createEmptyBorder());
        dashTxnScroll.getViewport().setBackground(CARD_WHITE);
        recentTxnCard.add(dashTxnScroll, BorderLayout.CENTER);

        middleRow.add(accountsCard);
        middleRow.add(recentTxnCard);

        dashboardContent.add(middleRow);
        dashboardContent.add(Box.createRigidArea(new Dimension(0, 16)));

        // 3. bottom row: quick actions card
        RoundedCard quickActionsCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        quickActionsCard.setLayout(new BorderLayout(0, 14));
        quickActionsCard.setBorder(new EmptyBorder(18, 20, 18, 20));
        quickActionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel actionsTitle = new JLabel("Quick Banking Actions");
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        actionsTitle.setForeground(TEXT_DARK);
        quickActionsCard.add(actionsTitle, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        actionsGrid.setOpaque(false);

        ModernButton btnNew = new ModernButton("＋ New Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 16);
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNew.addActionListener(e -> showScreen("Create Account"));

        ModernButton btnDep = new ModernButton("↓ Deposit Money", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 16);
        btnDep.addActionListener(e -> showScreen("Deposit"));

        ModernButton btnWith = new ModernButton("↑ Withdraw Money", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 16);
        btnWith.addActionListener(e -> showScreen("Withdraw"));

        ModernButton btnSrch = new ModernButton("🔍 Search Account", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 16);
        btnSrch.addActionListener(e -> showScreen("Search"));

        ModernButton btnSumm = new ModernButton("📊 Bank Summary", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 16);
        btnSumm.addActionListener(e -> showScreen("Bank Summary"));

        actionsGrid.add(btnNew);
        actionsGrid.add(btnDep);
        actionsGrid.add(btnWith);
        actionsGrid.add(btnSrch);
        actionsGrid.add(btnSumm);

        quickActionsCard.add(actionsGrid, BorderLayout.CENTER);
        dashboardContent.add(quickActionsCard);

        JScrollPane scrollPane = new JScrollPane(dashboardContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 2. ACCOUNTS SCREEN (ALL ACCOUNTS TABLE)
    // =========================================================================
    private JPanel createAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        // header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = createHeaderTitleBlock("All Registered Accounts", "View and manage all customer accounts.");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        ModernButton refreshBtn = new ModernButton("↻ Refresh List", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 14);
        refreshBtn.addActionListener(e -> refreshAccountsTable());

        ModernButton createBtn = new ModernButton("＋ New Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        createBtn.addActionListener(e -> showScreen("Create Account"));

        actions.add(refreshBtn);
        actions.add(createBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // full card for table
        RoundedCard card = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // summary bar inside card
        accountsSummaryLabel = new JLabel("Total Accounts: 0 | Active Portfolio");
        accountsSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        accountsSummaryLabel.setForeground(TEXT_MUTED);
        card.add(accountsSummaryLabel, BorderLayout.NORTH);

        String[] columns = {"Account No.", "Customer Name", "Phone", "Account Type", "Balance (Rs.)"};
        accountsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(accountsTableModel);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new AccountTypeBadgeRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(CARD_WHITE);

        card.add(scroll, BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================================
    // 3. CREATE ACCOUNT SCREEN (TWO-COLUMN APPROVED REFERENCE)
    // =========================================================================
    private JPanel createCreateAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Create New Account", "Add a new customer and create their bank account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Visual & Information Card
        GradientCard leftCard = new GradientCard(22, PEACH_START, PEACH_END, PEACH_BORDER);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, new Color(255, 230, 220), new Color(250, 195, 175));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("CREATE ACCOUNT");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(PRIMARY_CORAL);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Open a FinBank Account");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Open a new FinBank customer account with instant automated ledger registration.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(new Color(110, 105, 102));
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: decorative bank card visual and features list
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        decorativeCard = new DecorativeFinBankCard();
        decorativeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCenterPanel.add(decorativeCard);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Instant Account Allocation", "Sequential account number auto-assigned immediately."));
        featuresPanel.add(createFeatureRow("Multi-Tier Portfolios", "Full support for Retail Savings and Commercial Current."));
        featuresPanel.add(createFeatureRow("Audited Ledger Trail", "Opening deposit automatically recorded in linked statement."));

        leftCenterPanel.add(featuresPanel);
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);

        RoundedCard nextNoBadge = new RoundedCard(14, CARD_WHITE, new Color(245, 215, 202));
        nextNoBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        createAccNextNoLabel = new JLabel("Next Account Number: #" + nextAccountNo);
        createAccNextNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createAccNextNoLabel.setForeground(PRIMARY_CORAL);
        nextNoBadge.add(createAccNextNoLabel);

        leftBottomPanel.add(nextNoBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Account Creation Form Card
        RoundedCard rightCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        rightCard.setLayout(new BorderLayout(0, 16));
        rightCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Customer Particulars");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_DARK);

        JLabel formSub = new JLabel("Provide customer identity details and the initial deposit.");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(formSub);
        rightCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        // 1. Customer Full Name
        formFields.add(createFormLabel("Customer Full Name *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField nameField = new ModernTextField(20);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(nameField);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        // 2. Phone Number
        formFields.add(createFormLabel("Phone Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField phoneField = new ModernTextField(20);
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(phoneField);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        // 3. Account Type
        formFields.add(createFormLabel("Account Type *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        JComboBox<String> typeCombo = new JComboBox<>(bank.accountTypes);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        typeCombo.setPreferredSize(new Dimension(300, 42));
        typeCombo.setBackground(Color.WHITE);
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(typeCombo);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        // 4. Initial Deposit Amount
        formFields.add(createFormLabel("Initial Deposit Amount (Rs.) *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField depositField = new ModernTextField(20);
        depositField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        depositField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(depositField);
        formFields.add(Box.createRigidArea(new Dimension(0, 18)));

        // 5. Submit Button
        ModernButton submitBtn = new ModernButton("Create Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 16);
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        submitBtn.setPreferredSize(new Dimension(300, 44));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(submitBtn);
        formFields.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(statusLabel);

        rightCard.add(formFields, BorderLayout.CENTER);

        submitBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            String depStr = depositField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || depStr.isEmpty()) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please fill in all fields.");
                return;
            }

            try {
                double deposit = Double.parseDouble(depStr);
                if (deposit < 0) {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Initial deposit cannot be negative.");
                    return;
                }

                Customer customer = new Customer(nextAccountNo, name, phone);
                BankAccount account = new BankAccount(nextAccountNo, customer, type, deposit);
                if (deposit > 0) {
                    account.transactions.add(new Transaction("Deposit", deposit));
                }
                bank.addAccount(account);

                statusLabel.setForeground(SUCCESS_GREEN);
                statusLabel.setText("✓ Account created successfully! Assigned Account No: " + nextAccountNo);
                JOptionPane.showMessageDialog(this, "Account created successfully!\nAssigned Account Number: " + nextAccountNo, "Success", JOptionPane.INFORMATION_MESSAGE);

                nextAccountNo++;
                nameField.setText("");
                phoneField.setText("");
                depositField.setText("");
                typeCombo.setSelectedIndex(0);

                if (createAccNextNoLabel != null) {
                    createAccNextNoLabel.setText("Next Account Number: #" + nextAccountNo);
                }
                if (decorativeCard != null) {
                    decorativeCard.repaint();
                }

                refreshAll();
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please enter a valid numeric deposit amount.");
            }
        });

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 4. DEPOSIT SCREEN (TWO-COLUMN FINTECH LAYOUT)
    // =========================================================================
    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Deposit Funds", "Add money to an existing customer account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Information & Visual Card
        GradientCard leftCard = new GradientCard(22, PEACH_START, PEACH_END, PEACH_BORDER);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, new Color(255, 230, 220), new Color(250, 195, 175));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(125, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("DEPOSIT FUNDS");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(PRIMARY_CORAL);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Instant Account Inflow");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Add money to an existing customer account with immediate balance crediting.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(new Color(110, 105, 102));
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: decorative deposit visual + features list
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        DecorativeDepositVisual depositVisual = new DecorativeDepositVisual();
        depositVisual.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCenterPanel.add(depositVisual);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Real-Time Balance Update", "Funds reflect immediately across customer balance and totals."));
        featuresPanel.add(createFeatureRow("Immutable Audit Trail", "Deposits append automatically to the account statement."));
        featuresPanel.add(createFeatureRow("Safe Verification", "Account existence verified prior to balance increment."));

        leftCenterPanel.add(featuresPanel);
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        // bottom tip
        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);
        RoundedCard tipBadge = new RoundedCard(14, CARD_WHITE, new Color(245, 215, 202));
        tipBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        JLabel tipLbl = new JLabel("✓ Minimum valid deposit amount: Rs. 1.00");
        tipLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tipLbl.setForeground(SUCCESS_GREEN);
        tipBadge.add(tipLbl);
        leftBottomPanel.add(tipBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: White Form Card
        RoundedCard rightCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        rightCard.setLayout(new BorderLayout(0, 16));
        rightCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Deposit Particulars");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_DARK);

        JLabel formSub = new JLabel("Specify target account number and amount to deposit.");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(formSub);
        rightCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        formFields.add(createFormLabel("Account Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField accNoField = new ModernTextField(20);
        accNoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        accNoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(accNoField);
        formFields.add(Box.createRigidArea(new Dimension(0, 16)));

        formFields.add(createFormLabel("Deposit Amount (Rs.) *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField amountField = new ModernTextField(20);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(amountField);
        formFields.add(Box.createRigidArea(new Dimension(0, 22)));

        ModernButton depositBtn = new ModernButton("Deposit Money", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 16);
        depositBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        depositBtn.setPreferredSize(new Dimension(300, 44));
        depositBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(depositBtn);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(statusLabel);

        rightCard.add(formFields, BorderLayout.CENTER);

        depositBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                boolean success = bank.deposit(accNo, amount);
                if (success) {
                    BankAccount acc = bank.search(accNo);
                    statusLabel.setForeground(SUCCESS_GREEN);
                    statusLabel.setText(String.format("✓ Deposit of Rs. %,.2f successful. New Balance: Rs. %,.2f", amount, acc.balance));
                    accNoField.setText("");
                    amountField.setText("");
                    refreshAll();
                } else {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Deposit failed. Check account number and ensure amount > 0.");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please enter valid numbers for account number and deposit amount.");
            }
        });

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 5. WITHDRAW SCREEN (TWO-COLUMN FINTECH LAYOUT)
    // =========================================================================
    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Withdraw Funds", "Safely withdraw funds from an existing account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Information & Visual Card
        GradientCard leftCard = new GradientCard(22, BLUE_START, BLUE_END, BLUE_BORDER);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, new Color(218, 234, 250), new Color(190, 218, 245));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("WITHDRAW FUNDS");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(new Color(40, 110, 190));
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Secure Fund Payout");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Disburse funds securely with instant balance validation and overdraft protection.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(new Color(90, 105, 120));
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: decorative visual + features list
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        DecorativeWithdrawVisual withdrawVisual = new DecorativeWithdrawVisual();
        withdrawVisual.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCenterPanel.add(withdrawVisual);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Automated Solvency Check", "Withdrawals exceeding available balance are rejected."));
        featuresPanel.add(createFeatureRow("Audited Disbursement Log", "Withdrawals are permanently recorded in the ledger."));
        featuresPanel.add(createFeatureRow("Real-Time Liquidity Sync", "Bank total balance updates simultaneously upon payout."));

        leftCenterPanel.add(featuresPanel);
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        // bottom tip
        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);
        RoundedCard tipBadge = new RoundedCard(14, CARD_WHITE, new Color(205, 225, 248));
        tipBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        JLabel tipLbl = new JLabel("✓ Balance cannot drop below zero");
        tipLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tipLbl.setForeground(new Color(40, 110, 190));
        tipBadge.add(tipLbl);
        leftBottomPanel.add(tipBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: White Form Card
        RoundedCard rightCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        rightCard.setLayout(new BorderLayout(0, 16));
        rightCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Withdrawal Particulars");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_DARK);

        JLabel formSub = new JLabel("Enter target account number and amount to withdraw.");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(formSub);
        rightCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        formFields.add(createFormLabel("Account Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField accNoField = new ModernTextField(20);
        accNoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        accNoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(accNoField);
        formFields.add(Box.createRigidArea(new Dimension(0, 16)));

        formFields.add(createFormLabel("Withdrawal Amount (Rs.) *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField amountField = new ModernTextField(20);
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(amountField);
        formFields.add(Box.createRigidArea(new Dimension(0, 22)));

        ModernButton withdrawBtn = new ModernButton("Withdraw Money", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 16);
        withdrawBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        withdrawBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        withdrawBtn.setPreferredSize(new Dimension(300, 44));
        withdrawBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(withdrawBtn);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(statusLabel);

        rightCard.add(formFields, BorderLayout.CENTER);

        withdrawBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                boolean success = bank.withdraw(accNo, amount);
                if (success) {
                    BankAccount acc = bank.search(accNo);
                    statusLabel.setForeground(SUCCESS_GREEN);
                    statusLabel.setText(String.format("✓ Withdrawal of Rs. %,.2f successful. Remaining: Rs. %,.2f", amount, acc.balance));
                    accNoField.setText("");
                    amountField.setText("");
                    refreshAll();
                } else {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Withdrawal failed. Check balance, account number, or ensure amount > 0.");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please enter valid numbers for account and amount.");
            }
        });

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 6. SEARCH ACCOUNT SCREEN
    // =========================================================================
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Search Account", "Find customer account information quickly."), BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));

        // top search query bar card
        RoundedCard searchBarCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        searchBarCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 14));
        searchBarCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        ModernTextField searchField = new ModernTextField(18);
        ModernButton searchBtn = new ModernButton("Search Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);

        searchBarCard.add(createFormLabel("Enter Account Number:"));
        searchBarCard.add(searchField);
        searchBarCard.add(searchBtn);

        contentContainer.add(searchBarCard);
        contentContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // account details profile card
        RoundedCard profileCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        profileCard.setLayout(new BorderLayout(0, 18));
        profileCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel profileHeader = new JPanel(new BorderLayout());
        profileHeader.setOpaque(false);

        JLabel profileTitle = new JLabel("Account Profile Details");
        profileTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        profileTitle.setForeground(TEXT_DARK);

        searchStatusLabel = new JLabel("Enter an account number above to view account details.");
        searchStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchStatusLabel.setForeground(TEXT_MUTED);

        profileHeader.add(profileTitle, BorderLayout.WEST);
        profileHeader.add(searchStatusLabel, BorderLayout.EAST);
        profileCard.add(profileHeader, BorderLayout.NORTH);

        // empty state panel
        searchEmptyStatePanel = new JPanel(new GridBagLayout());
        searchEmptyStatePanel.setOpaque(false);
        searchEmptyStatePanel.setPreferredSize(new Dimension(0, 240));

        JPanel emptyContent = new JPanel();
        emptyContent.setOpaque(false);
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));

        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 42));
        searchIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptyText = new JLabel("Enter an account number to view account details.");
        emptyText.setFont(new Font("Segoe UI", Font.BOLD, 15));
        emptyText.setForeground(TEXT_MUTED);
        emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emptySubText = new JLabel("Real-time HashMap lookup will fetch active records.");
        emptySubText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emptySubText.setForeground(new Color(150, 150, 155));
        emptySubText.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyContent.add(searchIcon);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 10)));
        emptyContent.add(emptyText);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 4)));
        emptyContent.add(emptySubText);

        searchEmptyStatePanel.add(emptyContent);

        // active details grid
        searchDetailsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        searchDetailsGrid.setOpaque(false);
        searchDetailsGrid.setVisible(false);

        searchResAccNo = new JLabel("-");
        searchResName = new JLabel("-");
        searchResPhone = new JLabel("-");
        searchResType = new JLabel("-");
        searchResBalance = new JLabel("-");
        searchResTxnCount = new JLabel("-");

        searchDetailsGrid.add(createDetailBlock("Account Number", searchResAccNo));
        searchDetailsGrid.add(createDetailBlock("Customer Name", searchResName));
        searchDetailsGrid.add(createDetailBlock("Phone Number", searchResPhone));
        searchDetailsGrid.add(createDetailBlock("Account Type", searchResType));
        searchDetailsGrid.add(createDetailBlock("Current Balance", searchResBalance));
        searchDetailsGrid.add(createDetailBlock("Total Transactions", searchResTxnCount));

        JPanel centerCardWrapper = new JPanel(new BorderLayout());
        centerCardWrapper.setOpaque(false);
        centerCardWrapper.add(searchEmptyStatePanel, BorderLayout.NORTH);
        centerCardWrapper.add(searchDetailsGrid, BorderLayout.CENTER);

        profileCard.add(centerCardWrapper, BorderLayout.CENTER);

        // action buttons at bottom
        searchActionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        searchActionButtons.setOpaque(false);
        searchActionButtons.setVisible(false);

        ModernButton goDepositBtn = new ModernButton("Deposit Funds", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 14);
        goDepositBtn.addActionListener(e -> {
            if (currentSearchedAccNo > 0) {
                showScreen("Deposit");
            }
        });

        ModernButton goStatementBtn = new ModernButton("View Statement", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        goStatementBtn.addActionListener(e -> {
            if (currentSearchedAccNo > 0) {
                showScreen("Transactions");
            }
        });

        searchActionButtons.add(goDepositBtn);
        searchActionButtons.add(goStatementBtn);
        profileCard.add(searchActionButtons, BorderLayout.SOUTH);

        contentContainer.add(profileCard);

        searchBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(searchField.getText().trim());
                BankAccount acc = bank.search(accNo);

                if (acc != null) {
                    currentSearchedAccNo = acc.accountNo;
                    searchResAccNo.setText(String.valueOf(acc.accountNo));
                    searchResName.setText(acc.customer.name);
                    searchResPhone.setText(acc.customer.phone);
                    searchResType.setText(acc.type);
                    searchResBalance.setText(String.format("Rs. %,.2f", acc.balance));
                    searchResTxnCount.setText(acc.transactions.size() + " records");

                    searchEmptyStatePanel.setVisible(false);
                    searchDetailsGrid.setVisible(true);
                    searchActionButtons.setVisible(true);

                    searchStatusLabel.setForeground(SUCCESS_GREEN);
                    searchStatusLabel.setText("✓ Account found");
                } else {
                    currentSearchedAccNo = -1;
                    searchEmptyStatePanel.setVisible(true);
                    searchDetailsGrid.setVisible(false);
                    searchActionButtons.setVisible(false);

                    searchStatusLabel.setForeground(DANGER_RED);
                    searchStatusLabel.setText("✗ Account " + accNo + " not found");
                }
            } catch (NumberFormatException ex) {
                searchStatusLabel.setForeground(DANGER_RED);
                searchStatusLabel.setText("Please enter a valid numeric account number.");
            }
        });

        panel.add(contentContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailBlock(String title, JLabel valueLabel) {
        RoundedCard block = new RoundedCard(16, new Color(248, 247, 244), CARD_BORDER);
        block.setLayout(new BorderLayout(0, 4));
        block.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(TEXT_DARK);

        block.add(titleLbl, BorderLayout.NORTH);
        block.add(valueLabel, BorderLayout.CENTER);
        return block;
    }

    private JPanel createOverviewMetricBlock(String title, JLabel valueLabel) {
        RoundedCard block = new RoundedCard(14, new Color(248, 247, 244), CARD_BORDER);
        block.setLayout(new BorderLayout(0, 2));
        block.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(TEXT_DARK);

        block.add(titleLbl, BorderLayout.NORTH);
        block.add(valueLabel, BorderLayout.CENTER);
        return block;
    }

    // =========================================================================
    // 7. TRANSACTIONS SCREEN
    // =========================================================================
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Transaction Statement", "View complete transaction history for an account."), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        // top query card with search controls and neat customer info strip
        RoundedCard queryCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        queryCard.setLayout(new BorderLayout(0, 12));
        queryCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        // row 1: search input and action button
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        searchRow.setOpaque(false);

        txnAccNoField = new ModernTextField(16);
        txnViewBtn = new ModernButton("View Statement", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);

        searchRow.add(createFormLabel("Enter Account Number:"));
        searchRow.add(txnAccNoField);
        searchRow.add(txnViewBtn);

        queryCard.add(searchRow, BorderLayout.NORTH);

        // row 2: neat customer info strip displayed beside/below search controls
        txnCustomerChipPanel = new RoundedCard(12, new Color(248, 246, 242), CARD_BORDER);
        txnCustomerChipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 6));
        txnCustomerChipPanel.setBorder(new EmptyBorder(4, 14, 4, 14));
        txnCustomerChipPanel.setVisible(false);

        txnCustomerNameLabel = new JLabel(" ");
        txnCustomerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnCustomerNameLabel.setForeground(TEXT_DARK);

        txnCustomerPhoneLabel = new JLabel(" ");
        txnCustomerPhoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txnCustomerPhoneLabel.setForeground(TEXT_MUTED);

        txnCustomerTypeLabel = new JLabel(" ");
        txnCustomerTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txnCustomerTypeLabel.setForeground(new Color(118, 75, 185));

        txnCustomerBalanceLabel = new JLabel(" ");
        txnCustomerBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnCustomerBalanceLabel.setForeground(PRIMARY_CORAL);

        txnCustomerChipPanel.add(txnCustomerNameLabel);
        txnCustomerChipPanel.add(txnCustomerPhoneLabel);
        txnCustomerChipPanel.add(txnCustomerTypeLabel);
        txnCustomerChipPanel.add(txnCustomerBalanceLabel);

        queryCard.add(txnCustomerChipPanel, BorderLayout.CENTER);
        content.add(queryCard, BorderLayout.NORTH);

        // transaction records card
        RoundedCard tableCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        tableCard.setLayout(new BorderLayout(0, 14));
        tableCard.setBorder(new EmptyBorder(22, 24, 22, 24));

        // card header with title on left and live account summary on right
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel statementTitle = new JLabel("Transaction Records");
        statementTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statementTitle.setForeground(TEXT_DARK);

        txnHeaderStatsLabel = new JLabel("Account Type: -  •  Current Balance: -");
        txnHeaderStatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnHeaderStatsLabel.setForeground(TEXT_MUTED);

        tableHeaderPanel.add(statementTitle, BorderLayout.WEST);
        tableHeaderPanel.add(txnHeaderStatsLabel, BorderLayout.EAST);
        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        // center wrapper for empty state or table using CardLayout
        CardLayout txnCardLayout = new CardLayout();
        JPanel centerCards = new JPanel(txnCardLayout);
        centerCards.setOpaque(false);

        // empty state view
        txnEmptyStatePanel = new JPanel(new GridBagLayout());
        txnEmptyStatePanel.setOpaque(false);

        JPanel emptyContent = new JPanel();
        emptyContent.setOpaque(false);
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));

        JLabel emptyIcon = new JLabel("📄");
        emptyIcon.setFont(new Font("Segoe UI", Font.PLAIN, 38));
        emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        txnEmptyStateTitle = new JLabel("No transactions yet");
        txnEmptyStateTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txnEmptyStateTitle.setForeground(TEXT_MUTED);
        txnEmptyStateTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txnEmptyStateSubtitle = new JLabel("Search for an account above to view its transaction history.");
        txnEmptyStateSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txnEmptyStateSubtitle.setForeground(new Color(150, 150, 155));
        txnEmptyStateSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyContent.add(emptyIcon);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 8)));
        emptyContent.add(txnEmptyStateTitle);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 4)));
        emptyContent.add(txnEmptyStateSubtitle);

        txnEmptyStatePanel.add(emptyContent);

        // transactions JTable
        String[] cols = {"Transaction Type", "Amount (Rs.)"};
        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(txnTableModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setCellRenderer(new TransactionTypeRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new AmountColorRenderer());

        txnScrollPane = new JScrollPane(table);
        txnScrollPane.setBorder(BorderFactory.createEmptyBorder());
        txnScrollPane.getViewport().setBackground(CARD_WHITE);

        centerCards.add(txnEmptyStatePanel, "EMPTY");
        centerCards.add(txnScrollPane, "TABLE");
        txnCardLayout.show(centerCards, "EMPTY");

        tableCard.add(centerCards, BorderLayout.CENTER);
        content.add(tableCard, BorderLayout.CENTER);

        txnViewBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(txnAccNoField.getText().trim());
                BankAccount acc = bank.search(accNo);

                txnTableModel.setRowCount(0);

                if (acc == null) {
                    txnCustomerChipPanel.setVisible(true);
                    txnCustomerNameLabel.setText("Account #" + accNo + " not found.");
                    txnCustomerNameLabel.setForeground(DANGER_RED);
                    txnCustomerPhoneLabel.setText("");
                    txnCustomerTypeLabel.setText("");
                    txnCustomerBalanceLabel.setText("");
                    txnHeaderStatsLabel.setText("");

                    txnEmptyStateTitle.setText("Account Not Found");
                    txnEmptyStateSubtitle.setText("Please check the account number and try again.");
                    txnCardLayout.show(centerCards, "EMPTY");
                    return;
                }

                // display customer information neatly
                txnCustomerChipPanel.setVisible(true);
                txnCustomerNameLabel.setForeground(TEXT_DARK);
                txnCustomerNameLabel.setText("👤 " + acc.customer.name);
                txnCustomerPhoneLabel.setText("📞 " + acc.customer.phone);
                txnCustomerTypeLabel.setText("🏷 " + acc.type);
                txnCustomerBalanceLabel.setText("💰 Rs. " + String.format("%,.2f", acc.balance));

                // update right header
                txnHeaderStatsLabel.setText(String.format("Account Type: %s   •   Current Balance: Rs. %,.2f", acc.type, acc.balance));
                txnHeaderStatsLabel.setForeground(TEXT_DARK);

                if (acc.transactions.isEmpty()) {
                    txnEmptyStateTitle.setText("No transactions yet");
                    txnEmptyStateSubtitle.setText("Transactions for this account will appear here.");
                    txnCardLayout.show(centerCards, "EMPTY");
                } else {
                    for (Transaction t : acc.transactions) {
                        txnTableModel.addRow(new Object[]{t.type, t.amount});
                    }
                    txnCardLayout.show(centerCards, "TABLE");
                }
            } catch (NumberFormatException ex) {
                txnCustomerChipPanel.setVisible(true);
                txnCustomerNameLabel.setText("Please enter a valid numeric account number.");
                txnCustomerNameLabel.setForeground(DANGER_RED);
                txnCustomerPhoneLabel.setText("");
                txnCustomerTypeLabel.setText("");
                txnCustomerBalanceLabel.setText("");
                txnHeaderStatsLabel.setText("");
                txnEmptyStateTitle.setText("Invalid Account Number");
                txnEmptyStateSubtitle.setText("Please enter digits only.");
                txnCardLayout.show(centerCards, "EMPTY");
            }
            txnCustomerChipPanel.revalidate();
            txnCustomerChipPanel.repaint();
            queryCard.revalidate();
            queryCard.repaint();
            tableCard.revalidate();
            tableCard.repaint();
        });

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 8. UPDATE CUSTOMER SCREEN (TWO-COLUMN FINTECH LAYOUT)
    // =========================================================================
    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Update Customer", "Modify customer information for an existing account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Information Card
        GradientCard leftCard = new GradientCard(22, LAVENDER_START, LAVENDER_END, LAVENDER_BORDER);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, new Color(236, 226, 252), new Color(214, 198, 244));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("UPDATE PROFILE");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(new Color(110, 70, 180));
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Maintain Customer Data");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Modify customer name and telephone records while preserving balance and account history.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(new Color(100, 95, 115));
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: decorative update visual + features list
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        DecorativeUpdateVisual updateVisual = new DecorativeUpdateVisual();
        updateVisual.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCenterPanel.add(updateVisual);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Persistent Identity Link", "Account number and financial balance remain intact."));
        featuresPanel.add(createFeatureRow("Multi-Registry Sync", "Customer updates instantly propagate across all views."));
        featuresPanel.add(createFeatureRow("Immediate Refresh", "Overview tables sync data without requiring application reboot."));

        leftCenterPanel.add(featuresPanel);
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        // bottom tip
        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);
        RoundedCard tipBadge = new RoundedCard(14, CARD_WHITE, new Color(225, 212, 248));
        tipBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        JLabel tipLbl = new JLabel("✓ Account balance and type remain untouched");
        tipLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tipLbl.setForeground(new Color(110, 70, 180));
        tipBadge.add(tipLbl);
        leftBottomPanel.add(tipBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: White Form Card
        RoundedCard rightCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        rightCard.setLayout(new BorderLayout(0, 16));
        rightCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Update Particulars");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_DARK);

        JLabel formSub = new JLabel("Enter target account number and updated customer identity.");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(formSub);
        rightCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        formFields.add(createFormLabel("Account Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField accNoField = new ModernTextField(20);
        accNoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        accNoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(accNoField);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        formFields.add(createFormLabel("New Customer Name *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField nameField = new ModernTextField(20);
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(nameField);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        formFields.add(createFormLabel("New Phone Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField phoneField = new ModernTextField(20);
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        phoneField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(phoneField);
        formFields.add(Box.createRigidArea(new Dimension(0, 18)));

        ModernButton updateBtn = new ModernButton("Update Customer", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 16);
        updateBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        updateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        updateBtn.setPreferredSize(new Dimension(300, 44));
        updateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(updateBtn);
        formFields.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(statusLabel);

        rightCard.add(formFields, BorderLayout.CENTER);

        updateBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                String name = nameField.getText().trim();
                String phone = phoneField.getText().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Please enter both name and phone number.");
                    return;
                }

                boolean success = bank.update(accNo, name, phone);
                if (success) {
                    statusLabel.setForeground(SUCCESS_GREEN);
                    statusLabel.setText("✓ Customer details updated successfully for Account No: " + accNo);
                    accNoField.setText("");
                    nameField.setText("");
                    phoneField.setText("");
                    refreshAll();
                } else {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Update failed. Account not found.");
                }
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please enter a valid numeric account number.");
            }
        });

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 9. DELETE ACCOUNT SCREEN (TWO-COLUMN FINTECH LAYOUT)
    // =========================================================================
    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Delete Account", "Remove an existing customer account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Subtle Danger-Themed Information Card
        GradientCard leftCard = new GradientCard(22, DANGER_START, DANGER_END, DANGER_BORDER);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, new Color(254, 218, 218), new Color(245, 185, 185));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(140, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("DESTRUCTIVE ACTION");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(DANGER_RED);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Account Deletion Protocol");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(TEXT_DARK);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Permanently revoke and purge customer records from the core bank registry.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(new Color(125, 95, 95));
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: decorative delete visual + warnings
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        DecorativeDeleteVisual deleteVisual = new DecorativeDeleteVisual();
        deleteVisual.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftCenterPanel.add(deleteVisual);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Irreversible Removal", "Purged records cannot be restored once confirmed."));
        featuresPanel.add(createFeatureRow("Full Ledger Deletion", "Account is deleted from both HashMap and TreeMap."));
        featuresPanel.add(createFeatureRow("Explicit Confirmation", "Requires active dialog confirmation prior to deletion."));

        leftCenterPanel.add(featuresPanel);
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        // bottom tip
        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);
        RoundedCard tipBadge = new RoundedCard(14, CARD_WHITE, new Color(248, 204, 204));
        tipBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        JLabel tipLbl = new JLabel("⚠ Warning: Confirm customer identity prior to purging");
        tipLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tipLbl.setForeground(DANGER_RED);
        tipBadge.add(tipLbl);
        leftBottomPanel.add(tipBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: White Form Card
        RoundedCard rightCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        rightCard.setLayout(new BorderLayout(0, 16));
        rightCard.setBorder(new EmptyBorder(26, 30, 26, 30));

        JPanel formHeader = new JPanel(new GridLayout(2, 1, 0, 3));
        formHeader.setOpaque(false);

        JLabel formTitle = new JLabel("Target Account");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formTitle.setForeground(TEXT_DARK);

        JLabel formSub = new JLabel("Specify the account number targeted for permanent deletion.");
        formSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSub.setForeground(TEXT_MUTED);

        formHeader.add(formTitle);
        formHeader.add(formSub);
        rightCard.add(formHeader, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setOpaque(false);
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));

        formFields.add(createFormLabel("Account Number *"));
        formFields.add(Box.createRigidArea(new Dimension(0, 5)));
        ModernTextField accNoField = new ModernTextField(20);
        accNoField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        accNoField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(accNoField);
        formFields.add(Box.createRigidArea(new Dimension(0, 22)));

        ModernButton deleteBtn = new ModernButton("Delete Account", DANGER_RED, DANGER_RED_HOVER, Color.WHITE, 16);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        deleteBtn.setPreferredSize(new Dimension(300, 44));
        deleteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(deleteBtn);
        formFields.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(statusLabel);

        rightCard.add(formFields, BorderLayout.CENTER);

        deleteBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());

                BankAccount acc = bank.search(accNo);
                if (acc == null) {
                    statusLabel.setForeground(DANGER_RED);
                    statusLabel.setText("Account not found.");
                    return;
                }

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to delete this account?\n\nAccount: " + accNo + " (" + acc.customer.name + ")\nBalance: Rs. " + acc.balance,
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    boolean success = bank.delete(accNo);
                    if (success) {
                        statusLabel.setForeground(SUCCESS_GREEN);
                        statusLabel.setText("✓ Account " + accNo + " deleted successfully.");
                        accNoField.setText("");
                        refreshAll();
                    } else {
                        statusLabel.setForeground(DANGER_RED);
                        statusLabel.setText("Delete failed.");
                    }
                }
            } catch (NumberFormatException ex) {
                statusLabel.setForeground(DANGER_RED);
                statusLabel.setText("Please enter a valid numeric account number.");
            }
        });

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        JScrollPane scroll = new JScrollPane(columnsPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 10. BANK SUMMARY SCREEN
    // =========================================================================
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = createHeaderTitleBlock("Bank Summary", "Overview of the current bank account records.");

        ModernButton refreshBtn = new ModernButton("↻ Refresh Data", new Color(245, 242, 235), new Color(236, 232, 224), TEXT_DARK, 14);
        refreshBtn.addActionListener(e -> refreshSummary());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // top 4 refined analytical summary cards
        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryGrid.setOpaque(false);
        summaryGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        summaryGrid.setPreferredSize(new Dimension(860, 105));

        summaryTotalAccountsLabel = new JLabel("0");
        summaryGrid.add(createStatCard("Total Accounts", summaryTotalAccountsLabel, "Registered active accounts", BLUE_START, BLUE_END, BLUE_BORDER));

        summaryTotalBalanceLabel = new JLabel("Rs. 0.00");
        summaryGrid.add(createStatCard("Total Balance", summaryTotalBalanceLabel, "Cumulative customer funds", PEACH_START, PEACH_END, PEACH_BORDER));

        summaryAvgBalanceLabel = new JLabel("Rs. 0.00");
        summaryGrid.add(createStatCard("Avg Account Balance", summaryAvgBalanceLabel, "Balance per active account", YELLOW_START, YELLOW_END, YELLOW_BORDER));

        summarySavingsCountLabel = new JLabel("0");
        summaryGrid.add(createStatCard("Savings Accounts", summarySavingsCountLabel, "Retail deposit base", LAVENDER_START, LAVENDER_END, LAVENDER_BORDER));

        content.add(summaryGrid);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // middle row: portfolio distribution (with progress bars) & user-facing overview metrics
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setPreferredSize(new Dimension(860, 215));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // LEFT CARD: Portfolio Distribution
        RoundedCard distCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        distCard.setLayout(new BorderLayout(0, 12));
        distCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel distHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        distHeader.setOpaque(false);
        JLabel distTitle = new JLabel("Portfolio Distribution");
        distTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        distTitle.setForeground(TEXT_DARK);
        JLabel distSub = new JLabel("Visual ratio of retail savings versus commercial accounts.");
        distSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        distSub.setForeground(TEXT_MUTED);
        distHeader.add(distTitle);
        distHeader.add(distSub);
        distCard.add(distHeader, BorderLayout.NORTH);

        JPanel barsPanel = new JPanel(new GridLayout(2, 1, 0, 12));
        barsPanel.setOpaque(false);

        // savings bar group
        JPanel savingsGroup = new JPanel();
        savingsGroup.setOpaque(false);
        savingsGroup.setLayout(new BoxLayout(savingsGroup, BoxLayout.Y_AXIS));

        JLabel savingsTitle = new JLabel("Savings Accounts");
        savingsTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        savingsTitle.setForeground(TEXT_DARK);

        summarySavingsBar = new RoundedProgressBar(new Color(142, 101, 211));
        summarySavingsMetricsLabel = new JLabel("0 accounts (0%)");
        summarySavingsMetricsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summarySavingsMetricsLabel.setForeground(TEXT_MUTED);

        savingsGroup.add(savingsTitle);
        savingsGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        savingsGroup.add(summarySavingsBar);
        savingsGroup.add(Box.createRigidArea(new Dimension(0, 4)));
        savingsGroup.add(summarySavingsMetricsLabel);

        // current bar group
        JPanel currentGroup = new JPanel();
        currentGroup.setOpaque(false);
        currentGroup.setLayout(new BoxLayout(currentGroup, BoxLayout.Y_AXIS));

        JLabel currentTitle = new JLabel("Current Accounts");
        currentTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentTitle.setForeground(TEXT_DARK);

        summaryCurrentBar = new RoundedProgressBar(new Color(229, 147, 58));
        summaryCurrentMetricsLabel = new JLabel("0 accounts (0%)");
        summaryCurrentMetricsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryCurrentMetricsLabel.setForeground(TEXT_MUTED);

        currentGroup.add(currentTitle);
        currentGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        currentGroup.add(summaryCurrentBar);
        currentGroup.add(Box.createRigidArea(new Dimension(0, 4)));
        currentGroup.add(summaryCurrentMetricsLabel);

        barsPanel.add(savingsGroup);
        barsPanel.add(currentGroup);
        distCard.add(barsPanel, BorderLayout.CENTER);

        // RIGHT CARD: Account & Transaction Overview (User-Facing Business Metrics)
        RoundedCard overviewCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        overviewCard.setLayout(new BorderLayout(0, 12));
        overviewCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel overviewHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        overviewHeader.setOpaque(false);
        JLabel overviewTitle = new JLabel("Account & Transaction Overview");
        overviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overviewTitle.setForeground(TEXT_DARK);
        JLabel overviewSub = new JLabel("Aggregated liquidity and volume across all accounts.");
        overviewSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        overviewSub.setForeground(TEXT_MUTED);
        overviewHeader.add(overviewTitle);
        overviewHeader.add(overviewSub);
        overviewCard.add(overviewHeader, BorderLayout.NORTH);

        JPanel metricsGrid = new JPanel(new GridLayout(2, 2, 12, 10));
        metricsGrid.setOpaque(false);

        summaryTotalTxnsLoggedLabel = new JLabel("0 Records");
        summaryTotalDepositsVolLabel = new JLabel("Rs. 0.00");
        summaryTotalWithdrawalsVolLabel = new JLabel("Rs. 0.00");
        JLabel netLiquidityLabel = new JLabel("Active");

        metricsGrid.add(createOverviewMetricBlock("Total Transactions", summaryTotalTxnsLoggedLabel));
        metricsGrid.add(createOverviewMetricBlock("Deposit Inflow Vol", summaryTotalDepositsVolLabel));
        metricsGrid.add(createOverviewMetricBlock("Withdrawal Outflow", summaryTotalWithdrawalsVolLabel));
        metricsGrid.add(createOverviewMetricBlock("Audit Trail Status", netLiquidityLabel));

        overviewCard.add(metricsGrid, BorderLayout.CENTER);

        middleRow.setPreferredSize(new Dimension(860, 235));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        middleRow.add(distCard);
        middleRow.add(overviewCard);
        content.add(middleRow);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // LOWER SECTION: Recent Banking Activity Card
        RoundedCard recentActivityCard = new RoundedCard(22, CARD_WHITE, CARD_BORDER);
        recentActivityCard.setLayout(new BorderLayout(0, 10));
        recentActivityCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        recentActivityCard.setPreferredSize(new Dimension(860, 185));

        JPanel recentHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        recentHeader.setOpaque(false);
        JLabel recentTitle = new JLabel("Recent Banking Activity");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentTitle.setForeground(TEXT_DARK);
        JLabel recentSub = new JLabel("Latest transaction events posted across the bank registry.");
        recentSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentSub.setForeground(TEXT_MUTED);
        recentHeader.add(recentTitle);
        recentHeader.add(recentSub);
        recentActivityCard.add(recentHeader, BorderLayout.NORTH);

        String[] cols = {"Transaction Type", "Account", "Customer Name", "Amount (Rs.)"};
        summaryRecentActivityTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable recentTable = new JTable(summaryRecentActivityTableModel);
        styleTable(recentTable);
        recentTable.getColumnModel().getColumn(0).setCellRenderer(new TransactionTypeRenderer());
        recentTable.getColumnModel().getColumn(3).setCellRenderer(new AmountColorRenderer());

        summaryRecentScrollPane = new JScrollPane(recentTable);
        summaryRecentScrollPane.setBorder(BorderFactory.createEmptyBorder());
        summaryRecentScrollPane.getViewport().setBackground(CARD_WHITE);

        summaryRecentEmptyPanel = new JPanel(new GridBagLayout());
        summaryRecentEmptyPanel.setOpaque(false);
        JLabel emptyLabel = new JLabel("No recent activity");
        emptyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        emptyLabel.setForeground(TEXT_MUTED);
        summaryRecentEmptyPanel.add(emptyLabel);
        summaryRecentEmptyPanel.setVisible(false);

        JPanel recentCenterWrapper = new JPanel(new BorderLayout());
        recentCenterWrapper.setOpaque(false);
        recentCenterWrapper.add(summaryRecentScrollPane, BorderLayout.CENTER);
        recentCenterWrapper.add(summaryRecentEmptyPanel, BorderLayout.SOUTH);

        recentActivityCard.add(recentCenterWrapper, BorderLayout.CENTER);
        content.add(recentActivityCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // DATA REFRESH METHODS
    // =========================================================================
    private void refreshAll() {
        refreshDashboard();
        refreshAccountsTable();
        refreshSummary();
        if (createAccNextNoLabel != null) {
            createAccNextNoLabel.setText("Next Account Number: #" + nextAccountNo);
        }
        if (decorativeCard != null) {
            decorativeCard.repaint();
        }
    }

    private void refreshDashboard() {
        if (dashTotalBalanceLabel != null) {
            dashTotalBalanceLabel.setText(String.format("Rs. %,.2f", bank.totalBalance()));
        }
        if (dashTotalAccountsLabel != null) {
            dashTotalAccountsLabel.setText(String.valueOf(bank.totalAccounts()));
        }

        int savings = 0;
        int current = 0;
        for (BankAccount a : bank.accounts.values()) {
            if ("Savings".equalsIgnoreCase(a.type)) savings++;
            else if ("Current".equalsIgnoreCase(a.type)) current++;
        }

        if (dashSavingsCountLabel != null) {
            dashSavingsCountLabel.setText(String.valueOf(savings));
        }
        if (dashCurrentCountLabel != null) {
            dashCurrentCountLabel.setText(String.valueOf(current));
        }

        if (dashAccountsTableModel != null) {
            dashAccountsTableModel.setRowCount(0);
            for (BankAccount a : bank.sortedAccounts.values()) {
                dashAccountsTableModel.addRow(new Object[]{
                    a.accountNo,
                    a.customer.name,
                    a.type,
                    String.format("Rs. %,.2f", a.balance)
                });
            }
        }

        if (dashRecentTxnTableModel != null) {
            dashRecentTxnTableModel.setRowCount(0);
            List<Object[]> allTxns = new ArrayList<>();
            for (BankAccount a : bank.accounts.values()) {
                for (Transaction t : a.transactions) {
                    allTxns.add(new Object[]{
                        a.accountNo,
                        a.customer.name,
                        t.type,
                        t.amount
                    });
                }
            }
            int start = Math.max(0, allTxns.size() - 6);
            for (int i = allTxns.size() - 1; i >= start; i--) {
                dashRecentTxnTableModel.addRow(allTxns.get(i));
            }
        }
    }

    private void refreshAccountsTable() {
        if (accountsTableModel == null) return;
        accountsTableModel.setRowCount(0);

        int savings = 0;
        int current = 0;
        for (BankAccount a : bank.sortedAccounts.values()) {
            if ("Savings".equalsIgnoreCase(a.type)) savings++;
            else if ("Current".equalsIgnoreCase(a.type)) current++;

            accountsTableModel.addRow(new Object[]{
                a.accountNo,
                a.customer.name,
                a.customer.phone,
                a.type,
                String.format("%.2f", a.balance)
            });
        }

        if (accountsSummaryLabel != null) {
            accountsSummaryLabel.setText(String.format(
                "Total: %d Accounts (%d Savings, %d Current)  •  Deposit Pool: Rs. %,.2f",
                bank.totalAccounts(), savings, current, bank.totalBalance()
            ));
        }
    }

    private void refreshSummary() {
        int total = bank.totalAccounts();
        double balance = bank.totalBalance();

        if (summaryTotalAccountsLabel != null) {
            summaryTotalAccountsLabel.setText(String.valueOf(total));
        }
        if (summaryTotalBalanceLabel != null) {
            summaryTotalBalanceLabel.setText(String.format("Rs. %,.2f", balance));
        }

        double avg = total > 0 ? (balance / total) : 0.0;
        if (summaryAvgBalanceLabel != null) {
            summaryAvgBalanceLabel.setText(String.format("Rs. %,.2f", avg));
        }

        int savings = 0;
        double savingsBal = 0.0;
        int current = 0;
        double currentBal = 0.0;
        int totalTxns = 0;
        double depositVol = 0.0;
        double withdrawalVol = 0.0;

        List<Object[]> allTxns = new ArrayList<>();

        for (BankAccount a : bank.accounts.values()) {
            if ("Savings".equalsIgnoreCase(a.type)) {
                savings++;
                savingsBal += a.balance;
            } else if ("Current".equalsIgnoreCase(a.type)) {
                current++;
                currentBal += a.balance;
            }

            for (Transaction t : a.transactions) {
                totalTxns++;
                if (t.type.contains("Deposit")) {
                    depositVol += t.amount;
                } else {
                    withdrawalVol += t.amount;
                }
                allTxns.add(new Object[]{
                    t.type,
                    "#" + a.accountNo,
                    a.customer.name,
                    t.amount
                });
            }
        }

        if (summarySavingsCountLabel != null) {
            summarySavingsCountLabel.setText(String.valueOf(savings));
        }

        double savingsRatio = total > 0 ? ((double) savings / total) : 0.0;
        double currentRatio = total > 0 ? ((double) current / total) : 0.0;

        if (summarySavingsBar != null) {
            summarySavingsBar.setProgress(savingsRatio);
        }
        if (summarySavingsMetricsLabel != null) {
            String sText = String.format("%d %s • Rs. %,.2f (%.1f%%)", savings, (savings == 1 ? "account" : "accounts"), savingsBal, savingsRatio * 100);
            summarySavingsMetricsLabel.setText(sText);
        }

        if (summaryCurrentBar != null) {
            summaryCurrentBar.setProgress(currentRatio);
        }
        if (summaryCurrentMetricsLabel != null) {
            String cText = String.format("%d %s • Rs. %,.2f (%.1f%%)", current, (current == 1 ? "account" : "accounts"), currentBal, currentRatio * 100);
            summaryCurrentMetricsLabel.setText(cText);
        }

        if (summaryTotalTxnsLoggedLabel != null) {
            summaryTotalTxnsLoggedLabel.setText(totalTxns + " Records");
        }
        if (summaryTotalDepositsVolLabel != null) {
            summaryTotalDepositsVolLabel.setText(String.format("Rs. %,.2f", depositVol));
        }
        if (summaryTotalWithdrawalsVolLabel != null) {
            summaryTotalWithdrawalsVolLabel.setText(String.format("Rs. %,.2f", withdrawalVol));
        }

        if (summaryRecentActivityTableModel != null) {
            summaryRecentActivityTableModel.setRowCount(0);
            if (allTxns.isEmpty()) {
                if (summaryRecentEmptyPanel != null) summaryRecentEmptyPanel.setVisible(true);
                if (summaryRecentScrollPane != null) summaryRecentScrollPane.setVisible(false);
            } else {
                if (summaryRecentEmptyPanel != null) summaryRecentEmptyPanel.setVisible(false);
                if (summaryRecentScrollPane != null) summaryRecentScrollPane.setVisible(true);
                int start = Math.max(0, allTxns.size() - 6);
                for (int i = allTxns.size() - 1; i >= start; i--) {
                    summaryRecentActivityTableModel.addRow(allTxns.get(i));
                }
            }
        }
    }

    // =========================================================================
    // UI BUILDER HELPERS
    // =========================================================================

    private JPanel createPageHeader(String title, String subtitle, String statusBadge) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBlock = createHeaderTitleBlock(title, subtitle);
        header.add(titleBlock, BorderLayout.WEST);

        if (statusBadge != null) {
            JPanel badge = new RoundedCard(14, new Color(236, 248, 240), new Color(195, 235, 206));
            badge.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 6));
            JLabel badgeText = new JLabel(statusBadge);
            badgeText.setFont(new Font("Segoe UI", Font.BOLD, 12));
            badgeText.setForeground(SUCCESS_GREEN);
            badge.add(badgeText);
            header.add(badge, BorderLayout.EAST);
        }

        return header;
    }

    private JPanel createHeaderTitleBlock(String title, String subtitle) {
        JPanel block = new JPanel(new GridLayout(2, 1, 0, 3));
        block.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLbl.setForeground(TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(TEXT_MUTED);

        block.add(titleLbl);
        block.add(subLbl);
        return block;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color start, Color end, Color border) {
        GradientCard card = new GradientCard(20, start, end, border);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(90, 88, 82));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(TEXT_DARK);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(new Color(125, 120, 115));

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(subLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(55, 57, 65));
        return label;
    }

    private static JPanel createFeatureRow(String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel check = new JLabel("✓");
        check.setFont(new Font("Segoe UI", Font.BOLD, 13));
        check.setForeground(PRIMARY_CORAL);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        textPanel.setOpaque(false);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLbl.setForeground(TEXT_DARK);

        JLabel dLbl = new JLabel(desc);
        dLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dLbl.setForeground(TEXT_MUTED);

        textPanel.add(tLbl);
        textPanel.add(dLbl);

        row.add(check, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(242, 239, 233));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(255, 238, 232));
        table.setSelectionForeground(TEXT_DARK);

        // header styling
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(248, 246, 240));
        table.getTableHeader().setForeground(new Color(110, 110, 118));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 226, 218)));

        // cell padding renderer
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBorder(new EmptyBorder(0, 14, 0, 14));
        table.setDefaultRenderer(Object.class, renderer);
    }

    // =========================================================================
    // CUSTOM SWING COMPONENTS (VIVA EXPLAINABLE)
    // =========================================================================

    // decorative fintech card drawn purely using standard Java Swing Graphics2D
    class DecorativeFinBankCard extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeFinBankCard() {
            setPreferredSize(new Dimension(320, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(34, 36, 44), getWidth(), getHeight(), new Color(18, 19, 24));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(64, 67, 80));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(new Color(255, 120, 94, 35));
            g2.fillOval(getWidth() - 95, -25, 120, 120);
            g2.setColor(new Color(255, 210, 190, 20));
            g2.fillOval(getWidth() - 65, 15, 80, 80);

            g2.setColor(new Color(225, 195, 120));
            g2.fillRoundRect(22, 22, 34, 24, 6, 6);
            g2.setColor(new Color(185, 155, 85));
            g2.drawRoundRect(22, 22, 34, 24, 6, 6);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString("FinBank", getWidth() - 80, 38);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g2.setColor(new Color(235, 235, 240));
            g2.drawString("••••   ••••   ••••   " + nextAccountNo, 22, 90);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(155, 158, 168));
            g2.drawString("CARDHOLDER", 22, 120);
            g2.drawString("STATUS", getWidth() - 80, 120);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(Color.WHITE);
            g2.drawString("NEW CUSTOMER", 22, 138);

            g2.setColor(new Color(90, 210, 130));
            g2.drawString("● READY", getWidth() - 80, 138);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // decorative deposit visual
    static class DecorativeDepositVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeDepositVisual() {
            setPreferredSize(new Dimension(320, 155));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(34, 38, 36), getWidth(), getHeight(), new Color(20, 24, 22));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(55, 75, 65));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            // subtle glow
            g2.setColor(new Color(79, 157, 105, 40));
            g2.fillOval(getWidth() - 90, -20, 110, 110);

            // arrow circle
            g2.setColor(new Color(79, 157, 105, 50));
            g2.fillOval(24, 26, 48, 48);
            g2.setColor(new Color(79, 157, 105));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString("↓", 40, 58);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("Deposit Engine", 86, 45);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(170, 185, 175));
            g2.drawString("Instant credit to customer balance", 86, 65);

            // bottom meter badge
            g2.setColor(new Color(40, 48, 44));
            g2.fillRoundRect(22, 94, getWidth() - 44, 40, 10, 10);
            g2.setColor(new Color(79, 157, 105));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("● AUDITED TRANSACTION LOGGING ACTIVE", 36, 119);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // decorative withdraw visual
    static class DecorativeWithdrawVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeWithdrawVisual() {
            setPreferredSize(new Dimension(320, 155));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(30, 36, 46), getWidth(), getHeight(), new Color(18, 22, 30));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(55, 68, 88));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(new Color(50, 120, 210, 35));
            g2.fillOval(getWidth() - 90, -20, 110, 110);

            // arrow circle
            g2.setColor(new Color(50, 120, 210, 50));
            g2.fillOval(24, 26, 48, 48);
            g2.setColor(new Color(80, 150, 240));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
            g2.drawString("↑", 40, 58);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("Disbursement Gateway", 86, 45);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(170, 185, 205));
            g2.drawString("Strict solvency & balance verification", 86, 65);

            g2.setColor(new Color(36, 44, 58));
            g2.fillRoundRect(22, 94, getWidth() - 44, 40, 10, 10);
            g2.setColor(new Color(90, 165, 255));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("● OVERDRAFT SHIELD VERIFIED", 36, 119);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // decorative update visual
    static class DecorativeUpdateVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeUpdateVisual() {
            setPreferredSize(new Dimension(320, 155));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(36, 32, 44), getWidth(), getHeight(), new Color(22, 19, 28));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(75, 65, 95));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(new Color(150, 110, 230, 35));
            g2.fillOval(getWidth() - 90, -20, 110, 110);

            g2.setColor(new Color(150, 110, 230, 50));
            g2.fillOval(24, 26, 48, 48);
            g2.setColor(new Color(185, 145, 255));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("✎", 38, 58);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("Profile Sync Registry", 86, 45);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(195, 185, 215));
            g2.drawString("Hot-reload customer telephone and name", 86, 65);

            g2.setColor(new Color(45, 40, 56));
            g2.fillRoundRect(22, 94, getWidth() - 44, 40, 10, 10);
            g2.setColor(new Color(190, 155, 255));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("● RE-INDEXING PROPAGATION ACTIVE", 36, 119);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // decorative delete visual
    static class DecorativeDeleteVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeDeleteVisual() {
            setPreferredSize(new Dimension(320, 155));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, new Color(44, 30, 30), getWidth(), getHeight(), new Color(26, 18, 18));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(95, 55, 55));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(new Color(217, 92, 92, 35));
            g2.fillOval(getWidth() - 90, -20, 110, 110);

            g2.setColor(new Color(217, 92, 92, 50));
            g2.fillOval(24, 26, 48, 48);
            g2.setColor(new Color(255, 120, 120));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            g2.drawString("⚠", 38, 58);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g2.drawString("Purge Controller", 86, 45);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            g2.setColor(new Color(215, 180, 180));
            g2.drawString("Irreversible removal from all registries", 86, 65);

            g2.setColor(new Color(56, 36, 36));
            g2.fillRoundRect(22, 94, getWidth() - 44, 40, 10, 10);
            g2.setColor(new Color(255, 120, 120));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("● PERMANENT PURGE RESTRICTION ACTIVE", 36, 119);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // rounded card panel with custom paint and border
    static class RoundedCard extends JPanel {
        private static final long serialVersionUID = 1L;
        private int radius;
        private Color bgColor;
        private Color borderColor;

        public RoundedCard(int radius, Color bgColor, Color borderColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // gradient stat card
    static class GradientCard extends JPanel {
        private static final long serialVersionUID = 1L;
        private int radius;
        private Color startColor;
        private Color endColor;
        private Color borderColor;

        public GradientCard(int radius, Color startColor, Color endColor, Color borderColor) {
            this.radius = radius;
            this.startColor = startColor;
            this.endColor = endColor;
            this.borderColor = borderColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // rounded modern button with hover reaction
    static class ModernButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color normalColor;
        private Color hoverColor;
        private int radius;
        private boolean isHovered = false;

        public ModernButton(String text, Color normalColor, Color hoverColor, Color textColor, int radius) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.radius = radius;
            setForeground(textColor);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isHovered ? hoverColor : normalColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // sidebar pill navigation button with glowing coral active highlight
    static class NavButton extends JButton {
        private static final long serialVersionUID = 1L;
        private boolean active = false;
        private boolean hovered = false;

        public NavButton(String icon, String text) {
            super(icon + "   " + text);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setForeground(new Color(175, 175, 185));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setHorizontalAlignment(SwingConstants.LEFT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setPreferredSize(new Dimension(198, 40));
            setBorder(new EmptyBorder(8, 16, 8, 14));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            if (active) {
                setForeground(Color.WHITE);
                setFont(new Font("Segoe UI", Font.BOLD, 13));
            } else {
                setForeground(new Color(175, 175, 185));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (active) {
                g2.setColor(PRIMARY_CORAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            } else if (hovered) {
                g2.setColor(new Color(36, 38, 46));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // custom text field with focus border highlight
    static class ModernTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private boolean isFocused = false;

        public ModernTextField(int columns) {
            super(columns);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(getPreferredSize().width, 42));
            updateBorder();

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    updateBorder();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    updateBorder();
                }
            });
        }

        private void updateBorder() {
            Color borderColor = isFocused ? PRIMARY_CORAL : new Color(225, 220, 212);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
            ));
        }
    }

    // custom cell renderer for account type badges
    static class AccountTypeBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!isSelected) {
                String val = value != null ? value.toString() : "";
                if ("Savings".equalsIgnoreCase(val)) {
                    label.setForeground(new Color(118, 75, 185));
                } else {
                    label.setForeground(new Color(185, 120, 30));
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return label;
        }
    }

    // custom cell renderer for deposit/withdraw transaction types
    static class TransactionTypeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!isSelected) {
                String val = value != null ? value.toString() : "";
                if ("Deposit".equalsIgnoreCase(val)) {
                    label.setText("↓ Deposit");
                    label.setForeground(SUCCESS_GREEN);
                } else {
                    label.setText("↑ Withdrawal");
                    label.setForeground(DANGER_RED);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            }
            return label;
        }
    }

    // custom cell renderer for transaction amounts with + / -
    static class AmountColorRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!isSelected && value != null) {
                double amt = 0;
                try {
                    amt = Double.parseDouble(value.toString().replace("Rs.", "").replace(",", "").trim());
                } catch (Exception ignored) {}

                Object typeObj = table.getValueAt(row, 0);
                String type = typeObj != null ? typeObj.toString() : "";

                if (type.contains("Deposit") || type.contains("↓")) {
                    label.setText(String.format("+ Rs. %,.2f", amt));
                    label.setForeground(SUCCESS_GREEN);
                } else {
                    label.setText(String.format("- Rs. %,.2f", amt));
                    label.setForeground(DANGER_RED);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            }
            return label;
        }
    }

    // modern rounded horizontal progress bar drawn with standard Swing Graphics2D
    static class RoundedProgressBar extends JPanel {
        private static final long serialVersionUID = 1L;
        private double progress = 0.0;
        private final Color barColor;
        private final Color trackColor = new Color(240, 237, 232);

        public RoundedProgressBar(Color barColor) {
            this.barColor = barColor;
            setOpaque(false);
            setPreferredSize(new Dimension(200, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
            setMinimumSize(new Dimension(60, 10));
        }

        public void setProgress(double progress) {
            this.progress = Math.max(0.0, Math.min(1.0, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int arc = height;

            // track
            g2.setColor(trackColor);
            g2.fillRoundRect(0, 0, width, height, arc, arc);

            // progress fill
            int fillWidth = (int) Math.round(width * progress);
            if (fillWidth > 0) {
                fillWidth = Math.max(fillWidth, height);
                fillWidth = Math.min(fillWidth, width);
                g2.setColor(barColor);
                g2.fillRoundRect(0, 0, fillWidth, height, arc, arc);
            }
            g2.dispose();
        }
    }

    // main launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankGUI gui = new BankGUI();
            gui.setVisible(true);
        });
    }
}
