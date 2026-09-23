import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BankGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // backend system reference
    BankSystem bank;

    // currently logged-in account
    private BankAccount loggedInAccount;

    // next account number to auto-assign
    private int nextAccountNo = 1001;

    // card layout and cards container
    private CardLayout cardLayout;
    private JPanel mainContentCards;

    // theme state & current screen
    private boolean isDarkMode = false;
    private String currentActiveScreen = "Dashboard";

    // sidebar navigation buttons
    private Map<String, NavButton> navButtons = new LinkedHashMap<>();

    // dashboard dynamic labels & tables
    private JLabel dashTotalBalanceLabel;
    private JLabel dashTotalAccountsLabel;
    private JLabel dashSavingsCountLabel;
    private JLabel dashCurrentCountLabel;
    private DefaultTableModel dashAccountsTableModel;
    private DefaultTableModel dashRecentTxnTableModel;
    private JPanel dashAccEmptyPanel;
    private JScrollPane dashAccScrollPane;
    private JPanel dashTxnEmptyPanel;
    private JScrollPane dashTxnScrollPane;

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

    // analytics screen dynamic components
    private JLabel analyticsTotalAccountsLabel;
    private JLabel analyticsTotalBalanceLabel;
    private JLabel analyticsTotalTransactionsLabel;
    private JLabel analyticsDepositVolumeLabel;
    private JLabel analyticsWithdrawalVolumeLabel;
    private JLabel analyticsTransferVolumeLabel;
    private JLabel analyticsSavingsLabel;
    private JLabel analyticsCurrentLabel;
    private JLabel analyticsLargestTransactionLabel;
    private JLabel analyticsAverageTransactionLabel;

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

    // =========================================================================
    // DYNAMIC DESIGN SYSTEM PALETTE (LIGHT & DARK MODE)
    // =========================================================================
    private Color bgMain;
    private Color cardBg;
    private Color cardBorder;
    private Color textMain;
    private Color textMuted;
    private Color sidebarBg;
    private Color sidebarBorder;
    private Color sidebarSectionTitle;
    private Color inputBg;
    private Color inputBorder;
    private Color tableHeaderBg;
    private Color tableHeaderBorder;
    private Color tableRowAlt;
    private Color tableRowHover;
    private Color secondaryBtnBg;
    private Color secondaryBtnHover;
    private Color secondaryBtnBorder;
    private Color secondaryBtnText;

    // Pastel / accent card gradients (Light vs Dark)
    private Color peachStart, peachEnd, peachBorder;
    private Color blueStart, blueEnd, blueBorder;
    private Color lavenderStart, lavenderEnd, lavenderBorder;
    private Color yellowStart, yellowEnd, yellowBorder;
    private Color dangerStart, dangerEnd, dangerBorder;
    private Color successStart, successEnd, successBorder;

    // Static brand accents (consistent in both themes)
    private static final Color PRIMARY_CORAL = new Color(0xFF, 0x75, 0x5F); // #FF755F
    private static final Color PRIMARY_CORAL_HOVER = new Color(240, 100, 78);
    private static final Color SUCCESS_GREEN = new Color(60, 168, 100);
    private static final Color DANGER_RED = new Color(225, 80, 80);
    private static final Color DANGER_RED_HOVER = new Color(205, 65, 65);

    // =========================================================================
    // CONSTRUCTOR & INITIALIZATION
    // =========================================================================
    public BankGUI() {
        applyThemeColors();

        // load saved bank data
        bank = DataManager.load();

        // create starter data on first run
        if (bank == null) {
            bank = new BankSystem();
            addSampleAccounts();
            DataManager.save(bank);
        }

        // frame configuration
        setTitle("FinBank - Account Management System");
        setSize(1140, 740);
        setMinimumSize(new Dimension(1020, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(bgMain);
        setLayout(new BorderLayout());

        // build sidebar and main content panels
        buildUI();

        // initial screen and data refresh
        showScreen("Dashboard");
    }

    private void buildUI() {
        getContentPane().removeAll();
        navButtons.clear();

        JPanel sidebar = createSidebar();
        JPanel contentContainer = createContentContainer();

        add(sidebar, BorderLayout.WEST);
        add(contentContainer, BorderLayout.CENTER);
        getContentPane().setBackground(bgMain);
    }

    // load sample accounts with initial transaction records
    private void addSampleAccounts() {
        Customer c1 = new Customer(1001, "Nishi Sharma", "9876501234", "1234");
        Customer c2 = new Customer(1002, "Riya Patel", "9876543210", "2345");
        Customer c3 = new Customer(1003, "Aman Verma", "9811223344", "3456");

        BankAccount a1 = new BankAccount(1001, c1, "Savings", 15000);
        BankAccount a2 = new BankAccount(1002, c2, "Current", 28500);
        BankAccount a3 = new BankAccount(1003, c3, "Savings", 8200);

        a1.transactions.add(new Transaction(
                bank.transactionId(), "Deposit", 15000,
                "External", "1001", "SUCCESS"
        ));
        a2.transactions.add(new Transaction(
                bank.transactionId(), "Deposit", 28500,
                "External", "1002", "SUCCESS"
        ));
        a3.transactions.add(new Transaction(
                bank.transactionId(), "Deposit", 8200,
                "External", "1003", "SUCCESS"
        ));

        bank.addAccount(a1);
        bank.addAccount(a2);
        bank.addAccount(a3);

        nextAccountNo = 1004;
    }

    // =========================================================================
    // THEME MANAGEMENT (LIGHT & DARK MODE)
    // =========================================================================
    private void applyThemeColors() {
        if (!isDarkMode) {
            // LIGHT THEME (User-specified palette)
            bgMain = new Color(0xF7, 0xF5, 0xF1);
            cardBg = new Color(0xFF, 0xFF, 0xFF);
            sidebarBg = new Color(0x17, 0x18, 0x1D);
            cardBorder = new Color(0xE5, 0xE1, 0xDA);
            textMain = new Color(0x25, 0x26, 0x2B);
            textMuted = new Color(0x77, 0x79, 0x84);
            sidebarBorder = new Color(38, 40, 48);
            sidebarSectionTitle = new Color(105, 107, 118);

            inputBg = Color.WHITE;
            inputBorder = new Color(226, 222, 214);

            tableHeaderBg = new Color(248, 246, 240);
            tableHeaderBorder = new Color(230, 226, 218);
            tableRowAlt = new Color(252, 251, 248);
            tableRowHover = new Color(255, 243, 238);

            secondaryBtnBg = new Color(246, 243, 238);
            secondaryBtnHover = new Color(238, 234, 227);
            secondaryBtnBorder = new Color(228, 224, 216);
            secondaryBtnText = new Color(37, 38, 43);

            peachStart = new Color(255, 245, 239);
            peachEnd = new Color(255, 233, 222);
            peachBorder = new Color(250, 218, 204);

            blueStart = new Color(236, 245, 254);
            blueEnd = new Color(220, 236, 249);
            blueBorder = new Color(202, 224, 245);

            lavenderStart = new Color(246, 240, 254);
            lavenderEnd = new Color(232, 221, 248);
            lavenderBorder = new Color(220, 205, 242);

            yellowStart = new Color(255, 250, 235);
            yellowEnd = new Color(255, 240, 201);
            yellowBorder = new Color(246, 226, 175);

            dangerStart = new Color(255, 240, 240);
            dangerEnd = new Color(254, 228, 228);
            dangerBorder = new Color(248, 204, 204);

            successStart = new Color(240, 249, 244);
            successEnd = new Color(224, 244, 232);
            successBorder = new Color(196, 234, 210);
        } else {
            // DARK THEME (User-specified palette)
            bgMain = new Color(0x11, 0x12, 0x16);
            cardBg = new Color(0x1A, 0x1C, 0x22);
            sidebarBg = new Color(0x0B, 0x0C, 0x10);
            cardBorder = new Color(0x2B, 0x2D, 0x34);
            textMain = new Color(0xF4, 0xF4, 0xF5);
            textMuted = new Color(0x99, 0x9B, 0xA5);
            sidebarBorder = new Color(26, 28, 36);
            sidebarSectionTitle = new Color(130, 133, 145);

            inputBg = new Color(26, 28, 34);
            inputBorder = new Color(48, 52, 62);

            tableHeaderBg = new Color(20, 22, 28);
            tableHeaderBorder = new Color(43, 45, 52);
            tableRowAlt = new Color(22, 24, 30);
            tableRowHover = new Color(38, 32, 34);

            secondaryBtnBg = new Color(32, 35, 43);
            secondaryBtnHover = new Color(42, 46, 56);
            secondaryBtnBorder = new Color(50, 54, 66);
            secondaryBtnText = new Color(244, 244, 245);

            // Refined dark tint accents
            peachStart = new Color(42, 30, 28);
            peachEnd = new Color(32, 24, 24);
            peachBorder = new Color(68, 48, 44);

            blueStart = new Color(24, 34, 48);
            blueEnd = new Color(18, 26, 38);
            blueBorder = new Color(40, 58, 80);

            lavenderStart = new Color(34, 28, 48);
            lavenderEnd = new Color(26, 22, 38);
            lavenderBorder = new Color(58, 48, 80);

            yellowStart = new Color(42, 36, 24);
            yellowEnd = new Color(32, 28, 20);
            yellowBorder = new Color(70, 60, 40);

            dangerStart = new Color(46, 24, 24);
            dangerEnd = new Color(34, 18, 18);
            dangerBorder = new Color(80, 40, 40);

            successStart = new Color(22, 38, 28);
            successEnd = new Color(16, 28, 22);
            successBorder = new Color(36, 68, 48);
        }

        UIManager.put("OptionPane.background", cardBg);
        UIManager.put("Panel.background", cardBg);
        UIManager.put("OptionPane.messageForeground", textMain);
    }

    public void setTheme(boolean dark) {
        if (this.isDarkMode == dark) return;
        this.isDarkMode = dark;
        applyThemeColors();

        String active = currentActiveScreen != null ? currentActiveScreen : "Dashboard";
        buildUI();
        revalidate();
        repaint();

        showScreen(active);
    }

    // =========================================================================
    // SIDEBAR NAVIGATION PANEL (CONSISTENT ACROSS ALL PAGES)
    // =========================================================================
    private JPanel createSidebar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(bgMain);
        outer.setBorder(new EmptyBorder(16, 16, 16, 8));

        RoundedCard sidebarCard = new RoundedCard(24, sidebarBg, sidebarBorder);
        sidebarCard.setLayout(new BorderLayout(0, 14));
        sidebarCard.setPreferredSize(new Dimension(230, 0));
        sidebarCard.setBorder(new EmptyBorder(22, 16, 18, 16));

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
        brandSubtitle.setForeground(sidebarSectionTitle);

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

        addSidebarSeparator(navMenuPanel);

        // group 2: operations
        addSidebarSection(navMenuPanel, "OPERATIONS");
        addNavItem(navMenuPanel, "Create Account", "＋", "Create Account");
        addNavItem(navMenuPanel, "Deposit", "↓", "Deposit");
        addNavItem(navMenuPanel, "Withdraw", "↑", "Withdraw");
        addNavItem(navMenuPanel, "Transfer", "⇄", "Transfer");
        addNavItem(navMenuPanel, "Transactions", "📄", "Transactions");

        addSidebarSeparator(navMenuPanel);

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

        // footer with Theme Toggle & Version text
        JPanel footerBox = new JPanel();
        footerBox.setOpaque(false);
        footerBox.setLayout(new BoxLayout(footerBox, BoxLayout.Y_AXIS));

        JPanel themeToggle = createThemeToggle();
        themeToggle.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerBox.add(themeToggle);
        footerBox.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel footerLabel = new JLabel("FinBank Desktop • v1.2");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(sidebarSectionTitle);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerBox.add(footerLabel);

        sidebarCard.add(footerBox, BorderLayout.SOUTH);

        outer.add(sidebarCard, BorderLayout.CENTER);
        return outer;
    }

    private JPanel createThemeToggle() {
        RoundedCard toggleCard = new RoundedCard(16, isDarkMode ? new Color(18, 20, 26) : new Color(30, 32, 40), isDarkMode ? new Color(38, 42, 54) : new Color(48, 50, 60));
        toggleCard.setLayout(new GridLayout(1, 2, 4, 0));
        toggleCard.setPreferredSize(new Dimension(198, 34));
        toggleCard.setMaximumSize(new Dimension(198, 34));
        toggleCard.setBorder(new EmptyBorder(3, 3, 3, 3));

        JButton lightBtn = new JButton("☀ Light");
        lightBtn.setFont(new Font("Segoe UI", isDarkMode ? Font.PLAIN : Font.BOLD, 12));
        lightBtn.setFocusPainted(false);
        lightBtn.setBorderPainted(false);
        lightBtn.setContentAreaFilled(false);
        lightBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton darkBtn = new JButton("◐ Dark");
        darkBtn.setFont(new Font("Segoe UI", isDarkMode ? Font.BOLD : Font.PLAIN, 12));
        darkBtn.setFocusPainted(false);
        darkBtn.setBorderPainted(false);
        darkBtn.setContentAreaFilled(false);
        darkBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (!isDarkMode) {
            lightBtn.setOpaque(true);
            lightBtn.setBackground(Color.WHITE);
            lightBtn.setForeground(new Color(25, 26, 30));
            darkBtn.setOpaque(false);
            darkBtn.setForeground(new Color(145, 148, 160));
        } else {
            lightBtn.setOpaque(false);
            lightBtn.setForeground(new Color(145, 148, 160));
            darkBtn.setOpaque(true);
            darkBtn.setBackground(new Color(42, 45, 56));
            darkBtn.setForeground(Color.WHITE);
        }

        lightBtn.addActionListener(e -> setTheme(false));
        darkBtn.addActionListener(e -> setTheme(true));

        toggleCard.add(lightBtn);
        toggleCard.add(darkBtn);
        return toggleCard;
    }

    private void addSidebarSection(JPanel parent, String title) {
        JLabel sectionLabel = new JLabel(title);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLabel.setForeground(sidebarSectionTitle);
        sectionLabel.setBorder(new EmptyBorder(4, 12, 6, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        parent.add(sectionLabel);
    }

    private void addSidebarSeparator(JPanel parent) {
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(sidebarBorder);
        sep.setBackground(sidebarBorder);
        parent.add(sep);
        parent.add(Box.createRigidArea(new Dimension(0, 8)));
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
        outer.setBackground(bgMain);
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
        mainContentCards.add(createTransferPanel(), "Transfer");
        mainContentCards.add(createSearchPanel(), "Search");
        mainContentCards.add(createTransactionsPanel(), "Transactions");
        mainContentCards.add(createUpdatePanel(), "Update");
        mainContentCards.add(createDeletePanel(), "Delete");
        mainContentCards.add(createSummaryPanel(), "Bank Summary");
        mainContentCards.add(createAnalyticsPanel(), "Statistics");

        outer.add(mainContentCards, BorderLayout.CENTER);
        return outer;
    }

    // switch active card and update navigation highlight
    public void showScreen(String name) {
        currentActiveScreen = name;
        cardLayout.show(mainContentCards, name);

        // highlight active nav button
        for (Map.Entry<String, NavButton> entry : navButtons.entrySet()) {
            entry.getValue().setActive(entry.getKey().equals(name));
        }

        refreshAll();
    }

    // =========================================================================
    // 1. DASHBOARD SCREEN (WITH PERSONALITY & CONTEXTUAL HEADER)
    // =========================================================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // top page header
        JPanel headerPanel = createPageHeader(
                "Finance Management Dashboard",
                "Real-time overview of customer portfolios and registry activity.",
                null
        );
        panel.add(headerPanel, BorderLayout.NORTH);

        // main dashboard scrollable content
        JPanel dashboardContent = new JPanel();
        dashboardContent.setOpaque(false);
        dashboardContent.setLayout(new BoxLayout(dashboardContent, BoxLayout.Y_AXIS));

        // Contextual greeting header banner with current date and live system badge
        JPanel contextBanner = createContextualHeader();
        dashboardContent.add(contextBanner);
        dashboardContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // 1. row of 4 pastel stat cards with icons and sparklines
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));
        statsRow.setPreferredSize(new Dimension(860, 115));

        dashTotalBalanceLabel = new JLabel("Rs. 0.00");
        statsRow.add(createStatCard("Total Balance", dashTotalBalanceLabel, "Across all active accounts", peachStart, peachEnd, peachBorder, "💳", 0));

        dashTotalAccountsLabel = new JLabel("0");
        statsRow.add(createStatCard("Total Accounts", dashTotalAccountsLabel, "Active customer accounts", blueStart, blueEnd, blueBorder, "👥", 1));

        dashSavingsCountLabel = new JLabel("0");
        statsRow.add(createStatCard("Savings Accounts", dashSavingsCountLabel, "Retail savings portfolio", lavenderStart, lavenderEnd, lavenderBorder, "🛡", 2));

        dashCurrentCountLabel = new JLabel("0");
        statsRow.add(createStatCard("Current Accounts", dashCurrentCountLabel, "Commercial portfolio", yellowStart, yellowEnd, yellowBorder, "🏛", 3));

        dashboardContent.add(statsRow);
        dashboardContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // 2. middle row: account overview & recent transactions
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setPreferredSize(new Dimension(860, 275));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));

        // left card: account overview preview
        RoundedCard accountsCard = new RoundedCard(22, cardBg, cardBorder);
        accountsCard.setLayout(new BorderLayout(0, 12));
        accountsCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel accCardHeader = new JPanel(new BorderLayout());
        accCardHeader.setOpaque(false);
        JLabel accTitle = new JLabel("Account Overview");
        accTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        accTitle.setForeground(textMain);

        ModernButton viewAllBtn = new ModernButton("View All", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 12);
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
        dashAccTable.getColumnModel().getColumn(2).setCellRenderer(new AccountTypeBadgeRenderer());

        dashAccScrollPane = new JScrollPane(dashAccTable);
        dashAccScrollPane.setBorder(BorderFactory.createEmptyBorder());
        dashAccScrollPane.getViewport().setBackground(cardBg);

        dashAccEmptyPanel = createEmptyState("📄", "No accounts yet", "Add a new account to see it here.");
        dashAccEmptyPanel.setVisible(false);

        JPanel accCenterWrapper = new JPanel(new BorderLayout());
        accCenterWrapper.setOpaque(false);
        accCenterWrapper.add(dashAccScrollPane, BorderLayout.CENTER);
        accCenterWrapper.add(dashAccEmptyPanel, BorderLayout.SOUTH);
        accountsCard.add(accCenterWrapper, BorderLayout.CENTER);

        // right card: recent transactions
        RoundedCard recentTxnCard = new RoundedCard(22, cardBg, cardBorder);
        recentTxnCard.setLayout(new BorderLayout(0, 12));
        recentTxnCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel txnCardHeader = new JPanel(new BorderLayout());
        txnCardHeader.setOpaque(false);
        JLabel txnTitle = new JLabel("Recent Transactions");
        txnTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txnTitle.setForeground(textMain);

        ModernButton fullHistoryBtn = new ModernButton("Full Statement", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 12);
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

        dashTxnScrollPane = new JScrollPane(dashTxnTable);
        dashTxnScrollPane.setBorder(BorderFactory.createEmptyBorder());
        dashTxnScrollPane.getViewport().setBackground(cardBg);

        dashTxnEmptyPanel = createEmptyState("💳", "No recent activity", "Customer transactions will appear here.");
        dashTxnEmptyPanel.setVisible(false);

        JPanel txnCenterWrapper = new JPanel(new BorderLayout());
        txnCenterWrapper.setOpaque(false);
        txnCenterWrapper.add(dashTxnScrollPane, BorderLayout.CENTER);
        txnCenterWrapper.add(dashTxnEmptyPanel, BorderLayout.SOUTH);
        recentTxnCard.add(txnCenterWrapper, BorderLayout.CENTER);

        middleRow.add(accountsCard);
        middleRow.add(recentTxnCard);

        dashboardContent.add(middleRow);
        dashboardContent.add(Box.createRigidArea(new Dimension(0, 14)));

        // 3. bottom row: quick actions card
        RoundedCard quickActionsCard = new RoundedCard(22, cardBg, cardBorder);
        quickActionsCard.setLayout(new BorderLayout(0, 12));
        quickActionsCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        quickActionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JLabel actionsTitle = new JLabel("Quick Banking Actions");
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        actionsTitle.setForeground(textMain);
        quickActionsCard.add(actionsTitle, BorderLayout.NORTH);

        JPanel actionsGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionsGrid.setOpaque(false);

        ModernButton btnNew = new ModernButton("＋  New Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        btnNew.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNew.addActionListener(e -> showScreen("Create Account"));

        ModernButton btnDeposit = new ModernButton("↓  Deposit Money", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        btnDeposit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDeposit.addActionListener(e -> showScreen("Deposit"));

        ModernButton btnWithdraw = new ModernButton("↑  Withdraw Money", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        btnWithdraw.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnWithdraw.addActionListener(e -> showScreen("Withdraw"));

        ModernButton btnTransfer = new ModernButton("⇄  Transfer Money", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        btnTransfer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTransfer.addActionListener(e -> showScreen("Transfer"));

        ModernButton btnSearch = new ModernButton("🔍  Search Account", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSearch.addActionListener(e -> showScreen("Search"));

        ModernButton btnSummary = new ModernButton("📊  Bank Summary", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        btnSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSummary.addActionListener(e -> showScreen("Bank Summary"));

        actionsGrid.add(btnNew);
        actionsGrid.add(btnDeposit);
        actionsGrid.add(btnWithdraw);
        actionsGrid.add(btnTransfer);
        actionsGrid.add(btnSearch);
        actionsGrid.add(btnSummary);

        quickActionsCard.add(actionsGrid, BorderLayout.CENTER);
        dashboardContent.add(quickActionsCard);

        JScrollPane dashScroll = new JScrollPane(dashboardContent);
        dashScroll.setOpaque(false);
        dashScroll.getViewport().setOpaque(false);
        dashScroll.setBorder(null);
        dashScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(dashScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createContextualHeader() {
        JPanel banner = new RoundedCard(18, cardBg, cardBorder);
        banner.setLayout(new BorderLayout(16, 0));
        banner.setBorder(new EmptyBorder(12, 18, 12, 18));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        banner.setPreferredSize(new Dimension(860, 58));

        int hour = LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";

        JPanel leftStack = new JPanel(new GridLayout(2, 1, 0, 2));
        leftStack.setOpaque(false);

        JLabel greetLabel = new JLabel(greeting + ", welcome to FinBank");
        greetLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        greetLabel.setForeground(textMain);

        JLabel subLabel = new JLabel("Here's your current banking overview and transaction pulse.");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(textMuted);

        leftStack.add(greetLabel);
        leftStack.add(subLabel);

        JPanel rightStack = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        rightStack.setOpaque(false);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(textMuted);

        JPanel statusBadge = new RoundedCard(12, isDarkMode ? new Color(20, 42, 28) : new Color(236, 248, 240), isDarkMode ? new Color(34, 76, 48) : new Color(195, 235, 206));
        statusBadge.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        JLabel statusText = new JLabel("● Live System");
        statusText.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusText.setForeground(SUCCESS_GREEN);
        statusBadge.add(statusText);

        rightStack.add(dateLabel);
        rightStack.add(statusBadge);

        banner.add(leftStack, BorderLayout.WEST);
        banner.add(rightStack, BorderLayout.EAST);
        return banner;
    }

    // =========================================================================
    // 2. ACCOUNTS SCREEN (WITH SUMMARY BADGE & REFINED BADGES)
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

        ModernButton refreshBtn = new ModernButton("↻ Refresh List", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        refreshBtn.addActionListener(e -> refreshAccountsTable());

        ModernButton createBtn = new ModernButton("＋ New Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        createBtn.addActionListener(e -> showScreen("Create Account"));

        actions.add(refreshBtn);
        actions.add(createBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(actions, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        // full card for table
        RoundedCard card = new RoundedCard(22, cardBg, cardBorder);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // summary bar inside card
        accountsSummaryLabel = new JLabel("Total Accounts: 0 | Active Portfolio");
        accountsSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        accountsSummaryLabel.setForeground(textMuted);
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
        scroll.getViewport().setBackground(cardBg);

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
        GradientCard leftCard = new GradientCard(22, peachStart, peachEnd, peachBorder);
        leftCard.setLayout(new BorderLayout(0, 16));
        leftCard.setBorder(new EmptyBorder(26, 26, 26, 26));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, isDarkMode ? new Color(55, 35, 30) : new Color(255, 230, 220), isDarkMode ? new Color(85, 45, 40) : new Color(250, 195, 175));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("CREATE ACCOUNT");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(PRIMARY_CORAL);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Open a New Account");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Register customer details and initialize a new banking account.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // center: compact horizontal credit card visual and features list
        JPanel leftCenterPanel = new JPanel();
        leftCenterPanel.setOpaque(false);
        leftCenterPanel.setLayout(new BoxLayout(leftCenterPanel, BoxLayout.Y_AXIS));

        decorativeCard = new DecorativeFinBankCard();
        decorativeCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftCenterPanel.add(decorativeCard);
        leftCenterPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel featuresPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        featuresPanel.setOpaque(false);
        featuresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        featuresPanel.add(createFeatureRow("Instant Account Allocation", "Sequential account number auto-assigned immediately."));
        featuresPanel.add(createFeatureRow("Multi-Tier Portfolios", "Support for Savings and Current accounts."));
        featuresPanel.add(createFeatureRow("Audited Ledger Trail", "Opening deposit recorded in transaction history."));

        leftCenterPanel.add(featuresPanel);
        leftCenterPanel.add(Box.createVerticalGlue());
        leftCard.add(leftCenterPanel, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.setOpaque(false);

        RoundedCard nextNoBadge = new RoundedCard(14, isDarkMode ? new Color(32, 28, 28) : Color.WHITE, peachBorder);
        nextNoBadge.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 8));
        createAccNextNoLabel = new JLabel("Next Account Number: #" + nextAccountNo);
        createAccNextNoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createAccNextNoLabel.setForeground(PRIMARY_CORAL);
        nextNoBadge.add(createAccNextNoLabel);

        leftBottomPanel.add(nextNoBadge, BorderLayout.WEST);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Clean Account Form
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(26, 28, 26, 28));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);
        JLabel formHeading = new JLabel("Account Details");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);
        JLabel formSubheading = new JLabel("Please enter customer information accurately.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);
        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField nameField = new ModernTextField(20);
        ModernTextField phoneField = new ModernTextField(20);
        ModernTextField pinField = new ModernTextField(20);

        String[] accountTypes = {"Savings", "Current"};
        JComboBox<String> typeCombo = new JComboBox<>(accountTypes);
        typeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeCombo.setBackground(inputBg);
        typeCombo.setForeground(textMain);
        typeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        typeCombo.setPreferredSize(new Dimension(200, 42));
        typeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inputBorder, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        typeCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                javax.swing.plaf.basic.BasicArrowButton btn = new javax.swing.plaf.basic.BasicArrowButton(
                        SwingConstants.SOUTH, inputBg, inputBorder, textMain, inputBg
                );
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
                return btn;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(inputBg);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        typeCombo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBorder(new EmptyBorder(6, 12, 6, 12));
                l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (isSelected) {
                    l.setBackground(isDarkMode ? new Color(55, 45, 40) : new Color(255, 235, 228));
                    l.setForeground(PRIMARY_CORAL);
                } else {
                    l.setBackground(isDarkMode ? cardBg : Color.WHITE);
                    l.setForeground(textMain);
                }
                return l;
            }
        });

        ModernTextField depositField = new ModernTextField(20);

        formFieldsPanel.add(createFormLabel("Customer Full Name"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(nameField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Phone Number"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(phoneField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        formFieldsPanel.add(createFormLabel("4-Digit PIN"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(pinField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Account Type"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(typeCombo);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Initial Deposit Amount (Rs.)"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(depositField);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        // bottom action buttons
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton createAccountBtn = new ModernButton("Create Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        createAccountBtn.setPreferredSize(new Dimension(170, 44));

        createAccountBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String pin = pinField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();
            String depStr = depositField.getText().trim();

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty() || depStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all customer fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pin.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(
                        this,
                        "PIN must contain exactly 4 digits.",
                        "Invalid PIN",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try {
                double initialDeposit = Double.parseDouble(depStr);
                if (initialDeposit < 0) {
                    JOptionPane.showMessageDialog(this, "Initial deposit cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int assignedAccNo = nextAccountNo++;
                Customer customer = new Customer(assignedAccNo, name, phone, pin);
                BankAccount newAccount = new BankAccount(assignedAccNo, customer, type, initialDeposit);

                if (initialDeposit > 0) {
                    newAccount.transactions.add(new Transaction(
                            bank.transactionId(),
                            "Deposit",
                            initialDeposit,
                            "External",
                            String.valueOf(assignedAccNo),
                            "SUCCESS"
                    ));
                }

                bank.addAccount(newAccount);

                JOptionPane.showMessageDialog(
                        this,
                        String.format("Account Created Successfully!\n\nAccount No: #%d\nCustomer: %s\nType: %s\nInitial Balance: Rs. %,.2f",
                                assignedAccNo, name, type, initialDeposit),
                        "Account Activated",
                        JOptionPane.INFORMATION_MESSAGE
                );

                nameField.setText("");
                phoneField.setText("");
                pinField.setText("");
                depositField.setText("");
                typeCombo.setSelectedIndex(0);

                refreshAll();
                showScreen("Accounts");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric deposit amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(createAccountBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 4. DEPOSIT SCREEN (TWO-COLUMN FINTECH LAYOUT WITH LIVE PREVIEW)
    // =========================================================================
    private JPanel createDepositPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Deposit Funds", "Credit balance instantly to an active customer bank account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Functional Deposit Preview Card with soft green identity
        GradientCard leftCard = new GradientCard(22, successStart, successEnd, successBorder);
        leftCard.setLayout(new BorderLayout(0, 14));
        leftCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, isDarkMode ? new Color(20, 48, 30) : new Color(225, 248, 235), isDarkMode ? new Color(36, 80, 50) : new Color(185, 235, 205));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(145, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("INFLOW OPERATION");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(SUCCESS_GREEN);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Deposit Preview");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Live balance projection and ledger verification for incoming funds.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // Center Live Calculation Preview Card
        RoundedCard previewCard = new RoundedCard(16, isDarkMode ? new Color(24, 26, 32) : Color.WHITE, successBorder);
        previewCard.setLayout(new BorderLayout(0, 12));
        previewCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Account Header Row
        JPanel accountRow = new JPanel(new BorderLayout(8, 0));
        accountRow.setOpaque(false);

        JLabel previewAccLabel = new JLabel("Account: # ----");
        previewAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewAccLabel.setForeground(textMain);

        JLabel previewVerifyBadge = new JLabel("Awaiting Account");
        previewVerifyBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewVerifyBadge.setForeground(textMuted);

        accountRow.add(previewAccLabel, BorderLayout.WEST);
        accountRow.add(previewVerifyBadge, BorderLayout.EAST);

        JLabel previewNameLabel = new JLabel("Customer: Not selected");
        previewNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewNameLabel.setForeground(textMuted);

        JPanel accountBlock = new JPanel();
        accountBlock.setOpaque(false);
        accountBlock.setLayout(new BoxLayout(accountBlock, BoxLayout.Y_AXIS));
        accountBlock.add(accountRow);
        accountBlock.add(Box.createRigidArea(new Dimension(0, 3)));
        accountBlock.add(previewNameLabel);

        previewCard.add(accountBlock, BorderLayout.NORTH);

        // Balance Calculation Grid
        JPanel calcPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        calcPanel.setOpaque(false);

        JLabel curBalVal = new JLabel("Rs. 0.00");
        curBalVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        curBalVal.setForeground(textMain);
        JPanel curBalRow = new JPanel(new BorderLayout());
        curBalRow.setOpaque(false);
        JLabel curBalLbl = new JLabel("Current Balance:");
        curBalLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        curBalLbl.setForeground(textMuted);
        curBalRow.add(curBalLbl, BorderLayout.WEST);
        curBalRow.add(curBalVal, BorderLayout.EAST);

        JLabel depAmtVal = new JLabel("+ Rs. 0.00");
        depAmtVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depAmtVal.setForeground(SUCCESS_GREEN);
        JPanel depAmtRow = new JPanel(new BorderLayout());
        depAmtRow.setOpaque(false);
        JLabel depAmtLbl = new JLabel("Deposit Inflow:");
        depAmtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        depAmtLbl.setForeground(textMuted);
        depAmtRow.add(depAmtLbl, BorderLayout.WEST);
        depAmtRow.add(depAmtVal, BorderLayout.EAST);

        JLabel newBalVal = new JLabel("Rs. 0.00");
        newBalVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        newBalVal.setForeground(textMain);
        JPanel newBalRow = new JPanel(new BorderLayout());
        newBalRow.setOpaque(false);
        JLabel newBalLbl = new JLabel("Projected Balance:");
        newBalLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        newBalLbl.setForeground(textMain);
        newBalRow.add(newBalLbl, BorderLayout.WEST);
        newBalRow.add(newBalVal, BorderLayout.EAST);

        calcPanel.add(curBalRow);
        calcPanel.add(depAmtRow);
        calcPanel.add(newBalRow);

        previewCard.add(calcPanel, BorderLayout.CENTER);

        // Status Feedback Pill inside preview
        JPanel statusBox = new RoundedCard(10, isDarkMode ? new Color(18, 30, 24) : new Color(236, 248, 240), successBorder);
        statusBox.setLayout(new BorderLayout());
        statusBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel statusText = new JLabel("Enter an account number to verify customer and preview new balance.");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusText.setForeground(textMuted);
        statusBox.add(statusText, BorderLayout.CENTER);

        previewCard.add(statusBox, BorderLayout.SOUTH);
        leftCard.add(previewCard, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Instant Ledger Reflection");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setForeground(textMain);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoDesc = new JLabel("Funds reflect immediately on customer statement and dashboard.");
        infoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoDesc.setForeground(textMuted);
        infoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBottomPanel.add(infoTitle);
        leftBottomPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        leftBottomPanel.add(infoDesc);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Form Card
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);
        JLabel formHeading = new JLabel("Deposit Transaction");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);
        JLabel formSubheading = new JLabel("Enter the recipient account and transaction amount.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);
        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField accNoField = new ModernTextField(20, "e.g. 1001");
        JLabel accHint = new JLabel(" ");
        accHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accHint.setForeground(textMuted);
        accHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernTextField amountField = new ModernTextField(20, "e.g. 5000");

        formFieldsPanel.add(createFormLabel("Account Number"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(accNoField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        formFieldsPanel.add(accHint);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        formFieldsPanel.add(createFormLabel("Quick Select Amount"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        JPanel quickChips = createQuickAmountChips(
                new String[]{"+ ₹500", "+ ₹1,000", "+ ₹2,000", "+ ₹5,000"},
                new double[]{500, 1000, 2000, 5000},
                val -> {
                    double cur = 0;
                    try {
                        String t = amountField.getText().trim();
                        if (!t.isEmpty()) cur = Double.parseDouble(t);
                    } catch (Exception ignored) {}
                    amountField.setText(String.format("%.0f", cur + val));
                }
        );
        formFieldsPanel.add(quickChips);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formFieldsPanel.add(createFormLabel("Deposit Amount (Rs.)"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(amountField);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        // Real-time Preview Synchronizer
        Runnable syncPreview = () -> {
            int accNo = -1;
            BankAccount acc = null;
            try {
                String accStr = accNoField.getText().trim();
                if (!accStr.isEmpty()) {
                    accNo = Integer.parseInt(accStr);
                    acc = bank.search(accNo);
                }
            } catch (Exception ignored) {}

            double amount = 0;
            try {
                String amtStr = amountField.getText().trim();
                if (!amtStr.isEmpty()) amount = Double.parseDouble(amtStr);
            } catch (Exception ignored) {}

            if (acc != null) {
                previewAccLabel.setText("Account #" + acc.accountNo + " (" + acc.type + ")");
                previewNameLabel.setText("Customer: " + acc.customer.name);
                previewVerifyBadge.setText("✓ Verified Active");
                previewVerifyBadge.setForeground(SUCCESS_GREEN);
                accHint.setText("✓ Verified: " + acc.customer.name + " (" + acc.type + ")");
                accHint.setForeground(SUCCESS_GREEN);

                curBalVal.setText(String.format("Rs. %,.2f", acc.balance));
                depAmtVal.setText(String.format("+ Rs. %,.2f", amount > 0 ? amount : 0));
                newBalVal.setText(String.format("Rs. %,.2f", acc.balance + (amount > 0 ? amount : 0)));

                if (amount > 0) {
                    statusText.setText("✓ Ready to deposit Rs. " + String.format("%,.2f", amount) + " to #" + acc.accountNo + ".");
                    statusText.setForeground(SUCCESS_GREEN);
                } else {
                    statusText.setText("Account verified. Enter deposit amount on the right.");
                    statusText.setForeground(textMuted);
                }
            } else if (!accNoField.getText().trim().isEmpty()) {
                previewAccLabel.setText("Account: # ----");
                previewNameLabel.setText("Customer: Not found");
                previewVerifyBadge.setText("Account Not Found");
                previewVerifyBadge.setForeground(DANGER_RED);
                accHint.setText("No account found matching #" + accNoField.getText().trim());
                accHint.setForeground(DANGER_RED);

                curBalVal.setText("Rs. 0.00");
                depAmtVal.setText("+ Rs. 0.00");
                newBalVal.setText("Rs. 0.00");
                statusText.setText("Invalid account number. Please check records.");
                statusText.setForeground(DANGER_RED);
            } else {
                previewAccLabel.setText("Account: # ----");
                previewNameLabel.setText("Customer: Not selected");
                previewVerifyBadge.setText("Awaiting Account");
                previewVerifyBadge.setForeground(textMuted);
                accHint.setText(" ");

                curBalVal.setText("Rs. 0.00");
                depAmtVal.setText(String.format("+ Rs. %,.2f", amount > 0 ? amount : 0));
                newBalVal.setText("Rs. 0.00");
                statusText.setText("Enter an account number to verify customer and preview new balance.");
                statusText.setForeground(textMuted);
            }
        };

        attachDocListener(accNoField, syncPreview);
        attachDocListener(amountField, syncPreview);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton depositBtn = new ModernButton("Confirm Deposit", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        depositBtn.setPreferredSize(new Dimension(170, 44));

        depositBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Deposit amount must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BankAccount acc = bank.search(accNo);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this, "Account #" + accNo + " does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                bank.deposit(accNo, amount);

                JOptionPane.showMessageDialog(
                        this,
                        String.format("Deposit Successful!\n\nAccount: #%d (%s)\nDeposited: Rs. %,.2f\nUpdated Balance: Rs. %,.2f",
                                accNo, acc.customer.name, amount, acc.balance),
                        "Transaction Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                );

                accNoField.setText("");
                amountField.setText("");
                refreshAll();
                showScreen("Transactions");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric inputs.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(depositBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 5. WITHDRAW SCREEN (TWO-COLUMN FINTECH LAYOUT WITH LIVE PREVIEW)
    // =========================================================================
    private JPanel createWithdrawPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Withdraw Funds", "Process authorized debit requests from customer accounts."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Functional Withdrawal Preview Card with soft coral identity
        GradientCard leftCard = new GradientCard(22, peachStart, peachEnd, peachBorder);
        leftCard.setLayout(new BorderLayout(0, 14));
        leftCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, isDarkMode ? new Color(55, 35, 30) : new Color(255, 230, 220), isDarkMode ? new Color(85, 45, 40) : new Color(250, 195, 175));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(145, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("OUTFLOW OPERATION");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(PRIMARY_CORAL);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Withdrawal Preview");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Live balance deduction, liquidity guard verification, and audit tracking.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // Center Live Calculation Preview Card
        RoundedCard previewCard = new RoundedCard(16, isDarkMode ? new Color(24, 26, 32) : Color.WHITE, peachBorder);
        previewCard.setLayout(new BorderLayout(0, 12));
        previewCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        // Account Header Row
        JPanel accountRow = new JPanel(new BorderLayout(8, 0));
        accountRow.setOpaque(false);

        JLabel previewAccLabel = new JLabel("Account: # ----");
        previewAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewAccLabel.setForeground(textMain);

        JLabel previewVerifyBadge = new JLabel("Awaiting Account");
        previewVerifyBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewVerifyBadge.setForeground(textMuted);

        accountRow.add(previewAccLabel, BorderLayout.WEST);
        accountRow.add(previewVerifyBadge, BorderLayout.EAST);

        JLabel previewNameLabel = new JLabel("Customer: Not selected");
        previewNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewNameLabel.setForeground(textMuted);

        JPanel accountBlock = new JPanel();
        accountBlock.setOpaque(false);
        accountBlock.setLayout(new BoxLayout(accountBlock, BoxLayout.Y_AXIS));
        accountBlock.add(accountRow);
        accountBlock.add(Box.createRigidArea(new Dimension(0, 3)));
        accountBlock.add(previewNameLabel);

        previewCard.add(accountBlock, BorderLayout.NORTH);

        // Balance Calculation Grid
        JPanel calcPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        calcPanel.setOpaque(false);

        JLabel curBalVal = new JLabel("Rs. 0.00");
        curBalVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        curBalVal.setForeground(textMain);
        JPanel curBalRow = new JPanel(new BorderLayout());
        curBalRow.setOpaque(false);
        JLabel curBalLbl = new JLabel("Current Balance:");
        curBalLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        curBalLbl.setForeground(textMuted);
        curBalRow.add(curBalLbl, BorderLayout.WEST);
        curBalRow.add(curBalVal, BorderLayout.EAST);

        JLabel wthAmtVal = new JLabel("- Rs. 0.00");
        wthAmtVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wthAmtVal.setForeground(PRIMARY_CORAL);
        JPanel wthAmtRow = new JPanel(new BorderLayout());
        wthAmtRow.setOpaque(false);
        JLabel wthAmtLbl = new JLabel("Withdrawal Outflow:");
        wthAmtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        wthAmtLbl.setForeground(textMuted);
        wthAmtRow.add(wthAmtLbl, BorderLayout.WEST);
        wthAmtRow.add(wthAmtVal, BorderLayout.EAST);

        JLabel remBalVal = new JLabel("Rs. 0.00");
        remBalVal.setFont(new Font("Segoe UI", Font.BOLD, 17));
        remBalVal.setForeground(textMain);
        JPanel remBalRow = new JPanel(new BorderLayout());
        remBalRow.setOpaque(false);
        JLabel remBalLbl = new JLabel("Remaining Balance:");
        remBalLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        remBalLbl.setForeground(textMain);
        remBalRow.add(remBalLbl, BorderLayout.WEST);
        remBalRow.add(remBalVal, BorderLayout.EAST);

        calcPanel.add(curBalRow);
        calcPanel.add(wthAmtRow);
        calcPanel.add(remBalRow);

        previewCard.add(calcPanel, BorderLayout.CENTER);

        // Status Feedback Pill inside preview
        JPanel statusBox = new RoundedCard(10, isDarkMode ? new Color(32, 24, 24) : new Color(255, 244, 240), peachBorder);
        statusBox.setLayout(new BorderLayout());
        statusBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel statusText = new JLabel("Enter an account number on the right to check liquidity.");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusText.setForeground(textMuted);
        statusBox.add(statusText, BorderLayout.CENTER);

        previewCard.add(statusBox, BorderLayout.SOUTH);
        leftCard.add(previewCard, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Liquidity Guard Active");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setForeground(textMain);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoDesc = new JLabel("Prevents overdrafts and maintains real-time ledger consistency.");
        infoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoDesc.setForeground(textMuted);
        infoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBottomPanel.add(infoTitle);
        leftBottomPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        leftBottomPanel.add(infoDesc);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Form Card
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);
        JLabel formHeading = new JLabel("Withdrawal Request");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);
        JLabel formSubheading = new JLabel("Enter the source account number and withdrawal amount.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);
        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField accNoField = new ModernTextField(20, "e.g. 1001");
        JLabel accHint = new JLabel(" ");
        accHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accHint.setForeground(textMuted);
        accHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernTextField amountField = new ModernTextField(20, "e.g. 2000");

        formFieldsPanel.add(createFormLabel("Account Number"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(accNoField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        formFieldsPanel.add(accHint);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        formFieldsPanel.add(createFormLabel("Quick Select Amount"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        JPanel quickChips = createQuickAmountChips(
                new String[]{"₹500", "₹1,000", "₹2,000", "₹5,000"},
                new double[]{500, 1000, 2000, 5000},
                val -> amountField.setText(String.format("%.0f", val))
        );
        formFieldsPanel.add(quickChips);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formFieldsPanel.add(createFormLabel("Withdrawal Amount (Rs.)"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(amountField);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        // Real-time Preview Synchronizer
        Runnable syncPreview = () -> {
            int accNo = -1;
            BankAccount acc = null;
            try {
                String accStr = accNoField.getText().trim();
                if (!accStr.isEmpty()) {
                    accNo = Integer.parseInt(accStr);
                    acc = bank.search(accNo);
                }
            } catch (Exception ignored) {}

            double amount = 0;
            try {
                String amtStr = amountField.getText().trim();
                if (!amtStr.isEmpty()) amount = Double.parseDouble(amtStr);
            } catch (Exception ignored) {}

            if (acc != null) {
                previewAccLabel.setText("Account #" + acc.accountNo + " (" + acc.type + ")");
                previewNameLabel.setText("Customer: " + acc.customer.name);
                previewVerifyBadge.setText("✓ Verified Active");
                previewVerifyBadge.setForeground(SUCCESS_GREEN);
                accHint.setText("✓ Verified: " + acc.customer.name + " | Available: Rs. " + String.format("%,.2f", acc.balance));
                accHint.setForeground(SUCCESS_GREEN);

                curBalVal.setText(String.format("Rs. %,.2f", acc.balance));
                wthAmtVal.setText(String.format("- Rs. %,.2f", amount > 0 ? amount : 0));

                if (amount <= 0) {
                    remBalVal.setText(String.format("Rs. %,.2f", acc.balance));
                    remBalVal.setForeground(textMain);
                    statusText.setText("Account verified. Enter withdrawal amount to verify liquidity.");
                    statusText.setForeground(textMuted);
                } else if (amount <= acc.balance) {
                    double remaining = acc.balance - amount;
                    remBalVal.setText(String.format("Rs. %,.2f", remaining));
                    remBalVal.setForeground(SUCCESS_GREEN);
                    statusText.setText("✓ Liquidity Approved: Sufficient balance for disbursement.");
                    statusText.setForeground(SUCCESS_GREEN);
                } else {
                    double shortfall = amount - acc.balance;
                    remBalVal.setText(String.format("Rs. %,.2f", acc.balance - amount));
                    remBalVal.setForeground(DANGER_RED);
                    statusText.setText("⚠ Insufficient Balance! Shortfall of Rs. " + String.format("%,.2f", shortfall) + ".");
                    statusText.setForeground(DANGER_RED);
                }
            } else if (!accNoField.getText().trim().isEmpty()) {
                previewAccLabel.setText("Account: # ----");
                previewNameLabel.setText("Customer: Not found");
                previewVerifyBadge.setText("Account Not Found");
                previewVerifyBadge.setForeground(DANGER_RED);
                accHint.setText("No account found matching #" + accNoField.getText().trim());
                accHint.setForeground(DANGER_RED);

                curBalVal.setText("Rs. 0.00");
                wthAmtVal.setText("- Rs. 0.00");
                remBalVal.setText("Rs. 0.00");
                remBalVal.setForeground(textMain);
                statusText.setText("Invalid account number. Please check records.");
                statusText.setForeground(DANGER_RED);
            } else {
                previewAccLabel.setText("Account: # ----");
                previewNameLabel.setText("Customer: Not selected");
                previewVerifyBadge.setText("Awaiting Account");
                previewVerifyBadge.setForeground(textMuted);
                accHint.setText(" ");

                curBalVal.setText("Rs. 0.00");
                wthAmtVal.setText(String.format("- Rs. %,.2f", amount > 0 ? amount : 0));
                remBalVal.setText("Rs. 0.00");
                remBalVal.setForeground(textMain);
                statusText.setText("Enter an account number on the right to check liquidity.");
                statusText.setForeground(textMuted);
            }
        };

        attachDocListener(accNoField, syncPreview);
        attachDocListener(amountField, syncPreview);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton withdrawBtn = new ModernButton("Confirm Withdrawal", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        withdrawBtn.setPreferredSize(new Dimension(180, 44));

        withdrawBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Withdrawal amount must be positive.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                BankAccount acc = bank.search(accNo);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this, "Account #" + accNo + " does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (acc.balance < amount) {
                    JOptionPane.showMessageDialog(
                            this,
                            String.format("Insufficient Funds!\n\nCurrent Balance: Rs. %,.2f\nRequested: Rs. %,.2f", acc.balance, amount),
                            "Transaction Denied",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                bank.withdraw(accNo, amount);

                JOptionPane.showMessageDialog(
                        this,
                        String.format("Withdrawal Successful!\n\nAccount: #%d (%s)\nDebited: Rs. %,.2f\nRemaining Balance: Rs. %,.2f",
                                accNo, acc.customer.name, amount, acc.balance),
                        "Transaction Confirmed",
                        JOptionPane.INFORMATION_MESSAGE
                );

                accNoField.setText("");
                amountField.setText("");
                refreshAll();
                showScreen("Transactions");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric inputs.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(withdrawBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 6. TRANSFER MONEY SCREEN (TWO-COLUMN FINTECH LAYOUT WITH LIVE PREVIEW)
    // =========================================================================
    private JPanel createTransferPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock(
                "Transfer Money",
                "Move funds between FinBank accounts or simulate an external bank transfer."
        ), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Functional Transfer Preview Card
        GradientCard leftCard = new GradientCard(22, blueStart, blueEnd, blueBorder);
        leftCard.setLayout(new BorderLayout(0, 14));
        leftCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(
                12,
                isDarkMode ? new Color(25, 40, 55) : new Color(225, 239, 253),
                isDarkMode ? new Color(45, 65, 88) : new Color(195, 220, 244)
        );
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(150, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel badgeLbl = new JLabel("FUND TRANSFER");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(new Color(65, 125, 190));
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Transfer Preview");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel(
                "<html>Real-time routing verification and balance projection.</html>"
        );
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // Center Live Transfer Preview Card
        RoundedCard previewCard = new RoundedCard(16, isDarkMode ? new Color(24, 26, 32) : Color.WHITE, blueBorder);
        previewCard.setLayout(new BorderLayout(0, 10));
        previewCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel transferFlowPanel = new JPanel();
        transferFlowPanel.setOpaque(false);
        transferFlowPanel.setLayout(new BoxLayout(transferFlowPanel, BoxLayout.Y_AXIS));

        // 1. Sender (From) Box
        JPanel senderBox = new RoundedCard(12, isDarkMode ? new Color(20, 26, 36) : new Color(245, 249, 255), blueBorder);
        senderBox.setLayout(new BorderLayout(8, 2));
        senderBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel fromTitleLbl = new JLabel("FROM (SENDER)");
        fromTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        fromTitleLbl.setForeground(new Color(65, 125, 190));

        JLabel senderVerifyBadge = new JLabel("Awaiting Source");
        senderVerifyBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        senderVerifyBadge.setForeground(textMuted);

        JPanel senderTopRow = new JPanel(new BorderLayout());
        senderTopRow.setOpaque(false);
        senderTopRow.add(fromTitleLbl, BorderLayout.WEST);
        senderTopRow.add(senderVerifyBadge, BorderLayout.EAST);

        JLabel senderAccLabel = new JLabel("Account: # ----");
        senderAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        senderAccLabel.setForeground(textMain);

        JLabel senderBalLabel = new JLabel("Available Balance: Rs. 0.00");
        senderBalLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        senderBalLabel.setForeground(textMuted);

        senderBox.add(senderTopRow, BorderLayout.NORTH);
        senderBox.add(senderAccLabel, BorderLayout.CENTER);
        senderBox.add(senderBalLabel, BorderLayout.SOUTH);
        transferFlowPanel.add(senderBox);

        transferFlowPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 2. Transfer Amount Pill in between
        JPanel arrowPill = new RoundedCard(10, isDarkMode ? new Color(30, 42, 58) : new Color(230, 240, 252), blueBorder);
        arrowPill.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 3));
        arrowPill.setMaximumSize(new Dimension(240, 24));
        JLabel arrowAmountLabel = new JLabel("↓ Transfer: Rs. 0.00");
        arrowAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        arrowAmountLabel.setForeground(new Color(65, 125, 190));
        arrowPill.add(arrowAmountLabel);
        transferFlowPanel.add(arrowPill);

        transferFlowPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        // 3. Recipient (To) Box
        JPanel receiverBox = new RoundedCard(12, isDarkMode ? new Color(20, 26, 36) : new Color(245, 249, 255), blueBorder);
        receiverBox.setLayout(new BorderLayout(8, 2));
        receiverBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel toTitleLbl = new JLabel("TO (RECIPIENT)");
        toTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        toTitleLbl.setForeground(new Color(65, 125, 190));

        JLabel receiverVerifyBadge = new JLabel("Pending");
        receiverVerifyBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        receiverVerifyBadge.setForeground(textMuted);

        JPanel receiverTopRow = new JPanel(new BorderLayout());
        receiverTopRow.setOpaque(false);
        receiverTopRow.add(toTitleLbl, BorderLayout.WEST);
        receiverTopRow.add(receiverVerifyBadge, BorderLayout.EAST);

        JLabel receiverAccLabel = new JLabel("Destination: Not specified");
        receiverAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        receiverAccLabel.setForeground(textMain);

        JLabel receiverSubLabel = new JLabel("Internal or External routing");
        receiverSubLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        receiverSubLabel.setForeground(textMuted);

        receiverBox.add(receiverTopRow, BorderLayout.NORTH);
        receiverBox.add(receiverAccLabel, BorderLayout.CENTER);
        receiverBox.add(receiverSubLabel, BorderLayout.SOUTH);
        transferFlowPanel.add(receiverBox);

        transferFlowPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        // 4. Post-transfer remaining balance line
        JPanel remBalRow = new JPanel(new BorderLayout());
        remBalRow.setOpaque(false);
        JLabel remBalLbl = new JLabel("Sender Remaining Balance:");
        remBalLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        remBalLbl.setForeground(textMuted);
        JLabel remBalVal = new JLabel("Rs. 0.00");
        remBalVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        remBalVal.setForeground(textMain);
        remBalRow.add(remBalLbl, BorderLayout.WEST);
        remBalRow.add(remBalVal, BorderLayout.EAST);
        transferFlowPanel.add(remBalRow);

        previewCard.add(transferFlowPanel, BorderLayout.CENTER);

        // Status Feedback Pill inside preview
        JPanel statusBox = new RoundedCard(10, isDarkMode ? new Color(18, 28, 40) : new Color(236, 245, 255), blueBorder);
        statusBox.setLayout(new BorderLayout());
        statusBox.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel statusText = new JLabel("Enter source and destination accounts to simulate transfer.");
        statusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusText.setForeground(textMuted);
        statusBox.add(statusText, BorderLayout.CENTER);

        previewCard.add(statusBox, BorderLayout.SOUTH);
        leftCard.add(previewCard, BorderLayout.CENTER);

        JPanel leftBottom = new RoundedCard(
                14,
                isDarkMode ? new Color(20, 30, 40) : Color.WHITE,
                blueBorder
        );
        leftBottom.setLayout(new BorderLayout());
        leftBottom.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel guard = new JLabel("Transfer Guard Active");
        guard.setFont(new Font("Segoe UI", Font.BOLD, 12));
        guard.setForeground(textMain);

        JLabel guardDesc = new JLabel("Dual-ledger validation prevents overdrafts and unverified routing.");
        guardDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        guardDesc.setForeground(textMuted);

        JPanel guardText = new JPanel(new GridLayout(2, 1, 0, 2));
        guardText.setOpaque(false);
        guardText.add(guard);
        guardText.add(guardDesc);
        leftBottom.add(guardText, BorderLayout.CENTER);
        leftCard.add(leftBottom, BorderLayout.SOUTH);

        // RIGHT COLUMN: transfer form
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 12));
        rightCard.setBorder(new EmptyBorder(22, 26, 22, 26));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);

        JLabel formHeading = new JLabel("Transfer Details");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);

        JLabel formSubheading = new JLabel("Enter the required transfer details below.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);

        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField fromField = new ModernTextField(20, "e.g. 1001");
        JLabel fromBalHelper = new JLabel("Available Balance: --");
        fromBalHelper.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fromBalHelper.setForeground(textMuted);
        fromBalHelper.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernTextField toField = new ModernTextField(20, "e.g. 1002");
        JLabel toAccHelper = new JLabel(" ");
        toAccHelper.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toAccHelper.setForeground(textMuted);
        toAccHelper.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernTextField amountField = new ModernTextField(20, "e.g. 5000");
        ModernTextField beneficiaryNameField = new ModernTextField(20, "e.g. John Doe");
        ModernTextField bankNameField = new ModernTextField(20, "e.g. HDFC Bank");
        ModernTextField externalAccountField = new ModernTextField(20, "e.g. 50100234567890");
        ModernTextField ifscField = new ModernTextField(20, "e.g. HDFC0001234");

        JComboBox<String> transferType = new JComboBox<>(new String[]{
                "Internal Transfer",
                "External Bank Transfer"
        });
        transferType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        transferType.setBackground(inputBg);
        transferType.setForeground(textMain);
        transferType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        transferType.setPreferredSize(new Dimension(200, 40));
        transferType.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferType.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(inputBorder, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        formFieldsPanel.add(createFormLabel("Transfer Type"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(transferType);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        formFieldsPanel.add(createFormLabel("From Account"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(fromField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formFieldsPanel.add(fromBalHelper);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel toInternalPanel = new JPanel();
        toInternalPanel.setOpaque(false);
        toInternalPanel.setLayout(new BoxLayout(toInternalPanel, BoxLayout.Y_AXIS));
        toInternalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        toInternalPanel.add(createFormLabel("To Account"));
        toInternalPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        toInternalPanel.add(toField);
        toInternalPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        toInternalPanel.add(toAccHelper);

        formFieldsPanel.add(toInternalPanel);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel externalFields = new JPanel();
        externalFields.setOpaque(false);
        externalFields.setLayout(new BoxLayout(externalFields, BoxLayout.Y_AXIS));
        externalFields.setAlignmentX(Component.LEFT_ALIGNMENT);

        externalFields.add(createFormLabel("Beneficiary Name"));
        externalFields.add(Box.createRigidArea(new Dimension(0, 4)));
        externalFields.add(beneficiaryNameField);
        externalFields.add(Box.createRigidArea(new Dimension(0, 6)));

        externalFields.add(createFormLabel("Bank Name"));
        externalFields.add(Box.createRigidArea(new Dimension(0, 4)));
        externalFields.add(bankNameField);
        externalFields.add(Box.createRigidArea(new Dimension(0, 6)));

        externalFields.add(createFormLabel("Account Number"));
        externalFields.add(Box.createRigidArea(new Dimension(0, 4)));
        externalFields.add(externalAccountField);
        externalFields.add(Box.createRigidArea(new Dimension(0, 6)));

        externalFields.add(createFormLabel("IFSC Code"));
        externalFields.add(Box.createRigidArea(new Dimension(0, 4)));
        externalFields.add(ifscField);

        externalFields.setVisible(false);
        formFieldsPanel.add(externalFields);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 6)));

        formFieldsPanel.add(createFormLabel("Quick Select Amount"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        JPanel quickChips = createQuickAmountChips(
                new String[]{"₹500", "₹1,000", "₹5,000", "₹10,000"},
                new double[]{500, 1000, 5000, 10000},
                val -> amountField.setText(String.format("%.0f", val))
        );
        formFieldsPanel.add(quickChips);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        formFieldsPanel.add(createFormLabel("Amount (Rs.)"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(amountField);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        // Real-time Transfer Synchronizer
        Runnable syncTransferPreview = () -> {
            boolean isExternal = transferType.getSelectedIndex() == 1;

            int fromNo = -1;
            BankAccount senderAcc = null;
            try {
                String fStr = fromField.getText().trim();
                if (!fStr.isEmpty()) {
                    fromNo = Integer.parseInt(fStr);
                    senderAcc = bank.search(fromNo);
                }
            } catch (Exception ignored) {}

            int toNo = -1;
            BankAccount receiverAcc = null;
            try {
                String tStr = toField.getText().trim();
                if (!tStr.isEmpty()) {
                    toNo = Integer.parseInt(tStr);
                    receiverAcc = bank.search(toNo);
                }
            } catch (Exception ignored) {}

            double amount = 0;
            try {
                String amtStr = amountField.getText().trim();
                if (!amtStr.isEmpty()) amount = Double.parseDouble(amtStr);
            } catch (Exception ignored) {}

            // Update sender card
            if (senderAcc != null) {
                senderAccLabel.setText("Account #" + senderAcc.accountNo + " (" + senderAcc.customer.name + ")");
                senderBalLabel.setText("Available Balance: Rs. " + String.format("%,.2f", senderAcc.balance));
                senderVerifyBadge.setText("✓ Verified Sender");
                senderVerifyBadge.setForeground(SUCCESS_GREEN);
                fromBalHelper.setText("✓ Available Balance: Rs. " + String.format("%,.2f", senderAcc.balance));
                fromBalHelper.setForeground(SUCCESS_GREEN);
            } else if (!fromField.getText().trim().isEmpty()) {
                senderAccLabel.setText("Account: # ----");
                senderBalLabel.setText("Available Balance: Rs. 0.00");
                senderVerifyBadge.setText("Account Not Found");
                senderVerifyBadge.setForeground(DANGER_RED);
                fromBalHelper.setText("Source account not found in records");
                fromBalHelper.setForeground(DANGER_RED);
            } else {
                senderAccLabel.setText("Account: # ----");
                senderBalLabel.setText("Available Balance: Rs. 0.00");
                senderVerifyBadge.setText("Awaiting Source");
                senderVerifyBadge.setForeground(textMuted);
                fromBalHelper.setText("Available Balance: --");
                fromBalHelper.setForeground(textMuted);
            }

            // Update amount arrow
            arrowAmountLabel.setText("↓ Transfer: Rs. " + String.format("%,.2f", amount > 0 ? amount : 0));

            // Update recipient card
            if (!isExternal) {
                if (receiverAcc != null) {
                    receiverAccLabel.setText("Account #" + receiverAcc.accountNo + " (" + receiverAcc.customer.name + ")");
                    receiverSubLabel.setText("Internal FinBank Account (" + receiverAcc.type + ")");
                    receiverVerifyBadge.setText("✓ Verified Recipient");
                    receiverVerifyBadge.setForeground(SUCCESS_GREEN);
                    toAccHelper.setText("✓ Recipient: " + receiverAcc.customer.name + " (" + receiverAcc.type + ")");
                    toAccHelper.setForeground(SUCCESS_GREEN);
                } else if (!toField.getText().trim().isEmpty()) {
                    receiverAccLabel.setText("Destination: # ----");
                    receiverSubLabel.setText("Recipient account not found");
                    receiverVerifyBadge.setText("Account Not Found");
                    receiverVerifyBadge.setForeground(DANGER_RED);
                    toAccHelper.setText("Recipient account not found");
                    toAccHelper.setForeground(DANGER_RED);
                } else {
                    receiverAccLabel.setText("Destination: Not specified");
                    receiverSubLabel.setText("Enter destination account number");
                    receiverVerifyBadge.setText("Pending");
                    receiverVerifyBadge.setForeground(textMuted);
                    toAccHelper.setText(" ");
                }
            } else {
                String bName = beneficiaryNameField.getText().trim();
                String bBank = bankNameField.getText().trim();
                String bAcc = externalAccountField.getText().trim();
                String bIfsc = ifscField.getText().trim();

                if (!bName.isEmpty() && !bAcc.isEmpty()) {
                    receiverAccLabel.setText(bName + " (#" + bAcc + ")");
                    receiverSubLabel.setText((bBank.isEmpty() ? "External Bank" : bBank) + (bIfsc.isEmpty() ? "" : " | " + bIfsc));
                    receiverVerifyBadge.setText("External Routing");
                    receiverVerifyBadge.setForeground(new Color(65, 125, 190));
                } else {
                    receiverAccLabel.setText("External Beneficiary Pending");
                    receiverSubLabel.setText("Fill beneficiary details on right");
                    receiverVerifyBadge.setText("Incomplete");
                    receiverVerifyBadge.setForeground(textMuted);
                }
            }

            // Update remaining balance & status message
            if (senderAcc != null) {
                if (amount <= 0) {
                    remBalVal.setText(String.format("Rs. %,.2f", senderAcc.balance));
                    remBalVal.setForeground(textMain);
                    statusText.setText("Sender verified. Enter transfer amount.");
                    statusText.setForeground(textMuted);
                } else if (amount > senderAcc.balance) {
                    double shortfall = amount - senderAcc.balance;
                    remBalVal.setText(String.format("Rs. %,.2f", senderAcc.balance - amount));
                    remBalVal.setForeground(DANGER_RED);
                    statusText.setText("⚠ Insufficient Balance! Shortfall of Rs. " + String.format("%,.2f", shortfall) + ".");
                    statusText.setForeground(DANGER_RED);
                } else if (!isExternal && fromNo != -1 && fromNo == toNo) {
                    remBalVal.setText(String.format("Rs. %,.2f", senderAcc.balance));
                    remBalVal.setForeground(textMain);
                    statusText.setText("⚠ Source and destination accounts cannot be the same.");
                    statusText.setForeground(DANGER_RED);
                } else {
                    remBalVal.setText(String.format("Rs. %,.2f", senderAcc.balance - amount));
                    remBalVal.setForeground(SUCCESS_GREEN);
                    statusText.setText("✓ Ready for Transfer: Sufficient liquidity for disbursement.");
                    statusText.setForeground(SUCCESS_GREEN);
                }
            } else {
                remBalVal.setText("Rs. 0.00");
                remBalVal.setForeground(textMain);
                statusText.setText("Enter source and destination accounts to simulate transfer.");
                statusText.setForeground(textMuted);
            }
        };

        transferType.addActionListener(e -> {
            boolean external = transferType.getSelectedIndex() == 1;
            toInternalPanel.setVisible(!external);
            externalFields.setVisible(external);
            syncTransferPreview.run();
            rightCard.revalidate();
            rightCard.repaint();
        });

        attachDocListener(fromField, syncTransferPreview);
        attachDocListener(toField, syncTransferPreview);
        attachDocListener(amountField, syncTransferPreview);
        attachDocListener(beneficiaryNameField, syncTransferPreview);
        attachDocListener(bankNameField, syncTransferPreview);
        attachDocListener(externalAccountField, syncTransferPreview);
        attachDocListener(ifscField, syncTransferPreview);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton(
                "Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14
        );
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton transferBtn = new ModernButton(
                "Confirm Transfer", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14
        );
        transferBtn.setPreferredSize(new Dimension(180, 44));

        transferBtn.addActionListener(e -> {
            try {
                int from = Integer.parseInt(fromField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Transfer amount must be positive.",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                BankAccount sender = bank.search(from);
                if (sender == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Source account #" + from + " does not exist.",
                            "Account Not Found",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                if (loggedInAccount != null && from != loggedInAccount.accountNo) {
                    JOptionPane.showMessageDialog(
                            this,
                            "You can only transfer money from your logged-in account.",
                            "Access Denied",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (!sender.canWithdraw(amount)) {
                    JOptionPane.showMessageDialog(
                            this,
                            String.format(
                                    "Insufficient Funds!\n\nAvailable: Rs. %,.2f\nRequested: Rs. %,.2f",
                                    sender.balance,
                                    amount
                            ),
                            "Transaction Denied",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (transferType.getSelectedIndex() == 0) {
                    int to = Integer.parseInt(toField.getText().trim());

                    if (from == to) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Source and destination accounts cannot be the same.",
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    BankAccount receiver = bank.search(to);
                    if (receiver == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Destination account #" + to + " does not exist.",
                                "Account Not Found",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    if (!verifyTransactionPin()) {
                        return;
                    }

                    if (!bank.transfer(from, to, amount)) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Transfer could not be completed.",
                                "Transaction Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    String id = sender.transactions.getLast().id;

                    JOptionPane.showMessageDialog(
                            this,
                            String.format(
                                    "Transfer Successful!\n\nTransaction ID: %s\nFrom: #%d\nTo: #%d\nAmount: Rs. %,.2f\nRemaining Balance: Rs. %,.2f",
                                    id,
                                    from,
                                    to,
                                    amount,
                                    sender.balance
                            ),
                            "Transaction Confirmed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    String beneficiaryName = beneficiaryNameField.getText().trim();
                    String bankName = bankNameField.getText().trim();
                    String externalAccount = externalAccountField.getText().trim();
                    String ifsc = ifscField.getText().trim();

                    if (beneficiaryName.isEmpty() || bankName.isEmpty()
                            || externalAccount.isEmpty() || ifsc.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please enter all beneficiary details.",
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    Beneficiary beneficiary = new Beneficiary(
                            beneficiaryName,
                            bankName,
                            externalAccount,
                            ifsc
                    );

                    if (!verifyTransactionPin()) {
                        return;
                    }

                    if (!bank.externalTransfer(from, beneficiary, amount)) {
                        JOptionPane.showMessageDialog(
                                this,
                                "External transfer could not be completed.",
                                "Transaction Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    bank.addBeneficiary(beneficiary);
                    String id = sender.transactions.getLast().id;

                    JOptionPane.showMessageDialog(
                            this,
                            String.format(
                                    "External Transfer Successful!\n\nTransaction ID: %s\nBeneficiary: %s\nBank: %s\nAmount: Rs. %,.2f\nRemaining Balance: Rs. %,.2f",
                                    id,
                                    beneficiaryName,
                                    bankName,
                                    amount,
                                    sender.balance
                            ),
                            "Transaction Confirmed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }

                fromField.setText("");
                toField.setText("");
                amountField.setText("");
                beneficiaryNameField.setText("");
                bankNameField.setText("");
                externalAccountField.setText("");
                ifscField.setText("");

                refreshAll();
                showScreen("Transactions");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter valid numeric account numbers and amount.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(transferBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
        return panel;
    }

    private boolean verifyTransactionPin() {

        if (loggedInAccount == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please login before making a transaction.",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }

        JDialog pinDialog = new JDialog(this, "Transaction PIN", true);
        pinDialog.setSize(360, 220);
        pinDialog.setLocationRelativeTo(this);
        pinDialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(24, 28, 24, 28));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);

        JLabel title = new JLabel("Confirm Transaction");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(textMain);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your 4-digit transaction PIN");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(textMuted);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField pinField = new JPasswordField();
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        ModernButton confirmBtn = new ModernButton(
                "Confirm",
                PRIMARY_CORAL,
                PRIMARY_CORAL_HOVER,
                Color.WHITE,
                13
        );
        confirmBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        final boolean[] verified = {false};

        confirmBtn.addActionListener(e -> {
            String pin = new String(pinField.getPassword());

            if (!pin.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(
                        pinDialog,
                        "PIN must contain exactly 4 digits.",
                        "Invalid PIN",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (loggedInAccount.customer.verifyPin(pin)) {
                verified[0] = true;
                pinDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(
                        pinDialog,
                        "Incorrect transaction PIN.",
                        "Verification Failed",
                        JOptionPane.ERROR_MESSAGE
                );
                pinField.setText("");
            }
        });

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));
        panel.add(pinField);
        panel.add(Box.createRigidArea(new Dimension(0, 14)));
        panel.add(confirmBtn);

        pinDialog.add(panel);
        pinDialog.setVisible(true);

        return verified[0];
    }

    // =========================================================================
    // 6. SEARCH ACCOUNT SCREEN
    // =========================================================================
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Search Customer Account", "Look up account profiles, balances, and customer info."), BorderLayout.NORTH);

        JPanel contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));

        // query card
        RoundedCard queryCard = new RoundedCard(22, cardBg, cardBorder);
        queryCard.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));
        queryCard.setBorder(new EmptyBorder(6, 16, 6, 16));
        queryCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        ModernTextField searchField = new ModernTextField(18);
        ModernButton searchBtn = new ModernButton("Search Account", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);

        queryCard.add(createFormLabel("Enter Account Number:"));
        queryCard.add(searchField);
        queryCard.add(searchBtn);

        contentContainer.add(queryCard);
        contentContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // result display card
        RoundedCard detailsCard = new RoundedCard(22, cardBg, cardBorder);
        detailsCard.setLayout(new BorderLayout(0, 16));
        detailsCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel detailsHeader = new JPanel(new BorderLayout());
        detailsHeader.setOpaque(false);
        JLabel detailsTitle = new JLabel("Account Profile Details");
        detailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        detailsTitle.setForeground(textMain);

        searchStatusLabel = new JLabel("Enter an account number above to begin");
        searchStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchStatusLabel.setForeground(textMuted);

        detailsHeader.add(detailsTitle, BorderLayout.WEST);
        detailsHeader.add(searchStatusLabel, BorderLayout.EAST);
        detailsCard.add(detailsHeader, BorderLayout.NORTH);

        // empty state view
        searchEmptyStatePanel = createEmptyState("🔍", "No account loaded", "Enter an account number above and click Search Account.");
        searchEmptyStatePanel.setPreferredSize(new Dimension(0, 260));

        // grid for details when found
        searchDetailsGrid = new JPanel(new GridLayout(3, 2, 16, 16));
        searchDetailsGrid.setOpaque(false);

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

        // contextual actions bar placed directly beneath the details grid
        searchActionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        searchActionButtons.setOpaque(false);

        ModernButton statementBtn = new ModernButton("View Statement", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        statementBtn.addActionListener(e -> {
            if (currentSearchedAccNo != -1) {
                showScreen("Transactions");
                if (txnAccNoField != null) {
                    txnAccNoField.setText(String.valueOf(currentSearchedAccNo));
                    if (txnViewBtn != null) txnViewBtn.doClick();
                }
            }
        });

        ModernButton depositQuickBtn = new ModernButton("Deposit Funds", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        depositQuickBtn.addActionListener(e -> showScreen("Deposit"));

        searchActionButtons.add(statementBtn);
        searchActionButtons.add(depositQuickBtn);

        // Found results container anchored at the top
        JPanel foundWrapper = new JPanel(new BorderLayout());
        foundWrapper.setOpaque(false);

        JPanel foundContent = new JPanel(new BorderLayout(0, 20));
        foundContent.setOpaque(false);
        foundContent.add(searchDetailsGrid, BorderLayout.NORTH);
        foundContent.add(searchActionButtons, BorderLayout.SOUTH);

        foundWrapper.add(foundContent, BorderLayout.NORTH);

        CardLayout searchCardLayout = new CardLayout();
        JPanel centerWrapper = new JPanel(searchCardLayout);
        centerWrapper.setOpaque(false);
        centerWrapper.add(searchEmptyStatePanel, "EMPTY");
        centerWrapper.add(foundWrapper, "FOUND");
        searchCardLayout.show(centerWrapper, "EMPTY");

        detailsCard.add(centerWrapper, BorderLayout.CENTER);

        contentContainer.add(detailsCard);

        searchField.addActionListener(e -> searchBtn.doClick());

        searchBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(searchField.getText().trim());
                BankAccount acc = bank.search(accNo);

                if (acc != null) {
                    currentSearchedAccNo = accNo;
                    searchResAccNo.setText("#" + acc.accountNo);
                    searchResName.setText(acc.customer.name);
                    searchResPhone.setText(acc.customer.phone);
                    searchResType.setText(acc.type);
                    searchResBalance.setText(String.format("Rs. %,.2f", acc.balance));
                    searchResTxnCount.setText(acc.transactions.size() + " Records");

                    searchCardLayout.show(centerWrapper, "FOUND");
                    searchStatusLabel.setText("Account Verified Active");
                    searchStatusLabel.setForeground(SUCCESS_GREEN);
                } else {
                    currentSearchedAccNo = -1;
                    searchCardLayout.show(centerWrapper, "EMPTY");
                    searchStatusLabel.setText("Account #" + accNo + " Not Found");
                    searchStatusLabel.setForeground(DANGER_RED);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric account number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(contentContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailBlock(String title, JLabel valueLabel) {
        RoundedCard block = new RoundedCard(16, isDarkMode ? new Color(26, 28, 36) : new Color(248, 247, 244), cardBorder);
        block.setLayout(new BorderLayout(0, 4));
        block.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(textMuted);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valueLabel.setForeground(textMain);

        block.add(titleLbl, BorderLayout.NORTH);
        block.add(valueLabel, BorderLayout.CENTER);
        return block;
    }

    private JPanel createOverviewMetricBlock(String title, JLabel valueLabel) {
        RoundedCard block = new RoundedCard(14, isDarkMode ? new Color(26, 28, 36) : new Color(248, 247, 244), cardBorder);
        block.setLayout(new BorderLayout(0, 2));
        block.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(textMuted);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(textMain);

        block.add(titleLbl, BorderLayout.NORTH);
        block.add(valueLabel, BorderLayout.CENTER);
        return block;
    }

    // =========================================================================
    // 7. TRANSACTIONS SCREEN (POLISHED STATEMENT WITH EMPTY STATES)
    // =========================================================================
    private JPanel createTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(
                createHeaderTitleBlock(
                        "Account Statement",
                        "View the complete transaction history of the selected account."
                ),
                BorderLayout.NORTH
        );

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);

        // account selection card
        RoundedCard queryCard = new RoundedCard(22, cardBg, cardBorder);
        queryCard.setLayout(new BorderLayout(0, 10));
        queryCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        searchRow.setOpaque(false);

        txnAccNoField = new ModernTextField(16);
        txnViewBtn = new ModernButton(
                "View Statement",
                PRIMARY_CORAL,
                PRIMARY_CORAL_HOVER,
                Color.WHITE,
                14
        );

        searchRow.add(createFormLabel("Account Number:"));
        searchRow.add(txnAccNoField);
        searchRow.add(txnViewBtn);

        queryCard.add(searchRow, BorderLayout.NORTH);

        // customer information strip
        txnCustomerChipPanel = new RoundedCard(
                12,
                isDarkMode ? new Color(26, 28, 36) : new Color(248, 246, 242),
                cardBorder
        );
        txnCustomerChipPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 6));
        txnCustomerChipPanel.setBorder(new EmptyBorder(4, 14, 4, 14));
        txnCustomerChipPanel.setVisible(false);

        txnCustomerNameLabel = new JLabel(" ");
        txnCustomerNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnCustomerNameLabel.setForeground(textMain);

        txnCustomerPhoneLabel = new JLabel(" ");
        txnCustomerPhoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txnCustomerPhoneLabel.setForeground(textMuted);

        txnCustomerTypeLabel = new JLabel(" ");
        txnCustomerTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txnCustomerTypeLabel.setForeground(new Color(145, 110, 225));

        txnCustomerBalanceLabel = new JLabel(" ");
        txnCustomerBalanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnCustomerBalanceLabel.setForeground(PRIMARY_CORAL);

        txnCustomerChipPanel.add(txnCustomerNameLabel);
        txnCustomerChipPanel.add(txnCustomerPhoneLabel);
        txnCustomerChipPanel.add(txnCustomerTypeLabel);
        txnCustomerChipPanel.add(txnCustomerBalanceLabel);

        queryCard.add(txnCustomerChipPanel, BorderLayout.CENTER);
        content.add(queryCard, BorderLayout.NORTH);

        // statement table card
        RoundedCard tableCard = new RoundedCard(22, cardBg, cardBorder);
        tableCard.setLayout(new BorderLayout(0, 14));
        tableCard.setBorder(new EmptyBorder(22, 24, 18, 24));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        JLabel statementTitle = new JLabel("Transaction Records");
        statementTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statementTitle.setForeground(textMain);

        txnHeaderStatsLabel = new JLabel("Account Type: -  •  Current Balance: -");
        txnHeaderStatsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txnHeaderStatsLabel.setForeground(textMuted);

        tableHeaderPanel.add(statementTitle, BorderLayout.WEST);
        tableHeaderPanel.add(txnHeaderStatsLabel, BorderLayout.EAST);
        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);

        CardLayout txnCardLayout = new CardLayout();
        JPanel centerCards = new JPanel(txnCardLayout);
        centerCards.setOpaque(false);

        // empty state
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
        txnEmptyStateTitle.setForeground(textMuted);
        txnEmptyStateTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        txnEmptyStateSubtitle = new JLabel(
                "Search for an account above to view its transaction history."
        );
        txnEmptyStateSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txnEmptyStateSubtitle.setForeground(textMuted);
        txnEmptyStateSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyContent.add(emptyIcon);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 8)));
        emptyContent.add(txnEmptyStateTitle);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 4)));
        emptyContent.add(txnEmptyStateSubtitle);

        txnEmptyStatePanel.add(emptyContent);

        // full statement columns
        String[] cols = {
                "Transaction ID",
                "Type",
                "Amount (Rs.)",
                "From",
                "To",
                "Status",
                "Date & Time"
        };

        txnTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        JTable table = new JTable(txnTableModel);
        styleTable(table);

        table.getColumnModel().getColumn(1)
                .setCellRenderer(new TransactionTypeRenderer());

        table.getColumnModel().getColumn(2)
                .setCellRenderer(new AmountColorRenderer());

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {105, 135, 105, 100, 100, 90, 165};

        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        txnScrollPane = new JScrollPane(table);
        txnScrollPane.setBorder(BorderFactory.createEmptyBorder());
        txnScrollPane.getViewport().setBackground(cardBg);

        centerCards.add(txnEmptyStatePanel, "EMPTY");
        centerCards.add(txnScrollPane, "TABLE");

        txnCardLayout.show(centerCards, "EMPTY");

        tableCard.add(centerCards, BorderLayout.CENTER);

        // receipt button
        JPanel receiptPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        receiptPanel.setOpaque(false);

        ModernButton receiptButton = new ModernButton(
                "View Receipt",
                new Color(120, 92, 190),
                new Color(100, 76, 165),
                Color.WHITE,
                13
        );

        receiptPanel.add(receiptButton);
        tableCard.add(receiptPanel, BorderLayout.SOUTH);

        content.add(tableCard, BorderLayout.CENTER);

        // view statement button
        txnViewBtn.addActionListener(e -> {

            try {

                int accNo;

                // use logged-in account automatically when available
                if (txnAccNoField.getText().trim().isEmpty()
                        && loggedInAccount != null) {

                    accNo = loggedInAccount.accountNo;
                    txnAccNoField.setText(String.valueOf(accNo));

                } else {

                    accNo = Integer.parseInt(
                            txnAccNoField.getText().trim()
                    );
                }

                BankAccount acc = bank.search(accNo);

                txnTableModel.setRowCount(0);

                if (acc == null) {

                    txnCustomerChipPanel.setVisible(true);

                    txnCustomerNameLabel.setText(
                            "Account #" + accNo + " not found."
                    );
                    txnCustomerNameLabel.setForeground(DANGER_RED);

                    txnCustomerPhoneLabel.setText("");
                    txnCustomerTypeLabel.setText("");
                    txnCustomerBalanceLabel.setText("");
                    txnHeaderStatsLabel.setText("");

                    txnEmptyStateTitle.setText("Account Not Found");
                    txnEmptyStateSubtitle.setText(
                            "Please check the account number and try again."
                    );

                    txnCardLayout.show(centerCards, "EMPTY");
                    return;
                }

                // display customer information
                txnCustomerChipPanel.setVisible(true);
                txnCustomerNameLabel.setForeground(textMain);

                txnCustomerNameLabel.setText(
                        "👤 " + acc.customer.name
                );

                txnCustomerPhoneLabel.setText(
                        "📞 " + acc.customer.phone
                );

                txnCustomerTypeLabel.setText(
                        "🏷 " + acc.type
                );

                txnCustomerBalanceLabel.setText(
                        "💰 Rs. " + String.format(
                                "%,.2f",
                                acc.balance
                        )
                );

                txnHeaderStatsLabel.setText(
                        String.format(
                                "Account Type: %s   •   Current Balance: Rs. %,.2f",
                                acc.type,
                                acc.balance
                        )
                );

                txnHeaderStatsLabel.setForeground(textMain);

                if (acc.transactions.isEmpty()) {

                    txnEmptyStateTitle.setText(
                            "No transactions yet"
                    );

                    txnEmptyStateSubtitle.setText(
                            "Transactions for this account will appear here."
                    );

                    txnCardLayout.show(centerCards, "EMPTY");

                } else {

                    for (Transaction t : acc.transactions) {

                        String dateTime = t.dateTime == null
                                ? "-"
                                : t.dateTime.format(
                                DateTimeFormatter.ofPattern(
                                        "dd-MM-yyyy HH:mm"
                                )
                        );

                        txnTableModel.addRow(
                                new Object[]{
                                        t.id,
                                        t.type,
                                        t.amount,
                                        t.from,
                                        t.to,
                                        t.status,
                                        dateTime
                                }
                        );
                    }

                    txnCardLayout.show(centerCards, "TABLE");
                }

            } catch (NumberFormatException ex) {

                txnCustomerChipPanel.setVisible(true);

                txnCustomerNameLabel.setText(
                        "Please enter a valid numeric account number."
                );
                txnCustomerNameLabel.setForeground(DANGER_RED);

                txnCustomerPhoneLabel.setText("");
                txnCustomerTypeLabel.setText("");
                txnCustomerBalanceLabel.setText("");
                txnHeaderStatsLabel.setText("");

                txnEmptyStateTitle.setText(
                        "Invalid Account Number"
                );

                txnEmptyStateSubtitle.setText(
                        "Please enter digits only."
                );

                txnCardLayout.show(centerCards, "EMPTY");
            }

            txnCustomerChipPanel.revalidate();
            txnCustomerChipPanel.repaint();

            queryCard.revalidate();
            queryCard.repaint();

            tableCard.revalidate();
            tableCard.repaint();
        });

        // receipt action
        receiptButton.addActionListener(e -> {

            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please select a transaction first.",
                        "No Transaction Selected",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {

                int accNo = Integer.parseInt(
                        txnAccNoField.getText().trim()
                );

                BankAccount acc = bank.search(accNo);

                if (acc == null || selectedRow >= acc.transactions.size()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Transaction details could not be loaded.",
                            "Receipt Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                Transaction transaction = acc.transactions.get(selectedRow);

                showTransactionReceipt(acc, transaction);

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Please view a valid account statement first.",
                        "Receipt Error",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        // open statement directly for the logged-in account
        if (loggedInAccount != null) {

            txnAccNoField.setText(
                    String.valueOf(loggedInAccount.accountNo)
            );

            SwingUtilities.invokeLater(
                    () -> txnViewBtn.doClick()
            );
        }

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void showTransactionReceipt(
            BankAccount account,
            Transaction transaction
    ) {

        JDialog receiptDialog = new JDialog(
                this,
                "Transaction Receipt",
                true
        );

        receiptDialog.setSize(540, 710);
        receiptDialog.setLocationRelativeTo(this);
        receiptDialog.setResizable(false);
        receiptDialog.getContentPane().setBackground(bgMain);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(bgMain);
        mainPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // 1. RECEIPT MAIN CARD
        Color receiptCardBg = isDarkMode ? new Color(0x1A, 0x1C, 0x22) : Color.WHITE;
        RoundedCard receiptCard = new RoundedCard(18, receiptCardBg, cardBorder);
        receiptCard.setLayout(new BoxLayout(receiptCard, BoxLayout.Y_AXIS));
        receiptCard.setBorder(new EmptyBorder(16, 20, 16, 20));

        // TOP HEADER
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);
        topHeader.setMaximumSize(new Dimension(Short.MAX_VALUE, 28));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        headerLeft.setOpaque(false);
        MiniReceiptIcon receiptIcon = new MiniReceiptIcon(PRIMARY_CORAL);
        JLabel modalTitle = new JLabel("Transaction Receipt");
        modalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modalTitle.setForeground(textMain);
        headerLeft.add(receiptIcon);
        headerLeft.add(modalTitle);

        JButton closeXBtn = new JButton("✕");
        closeXBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeXBtn.setForeground(textMuted);
        closeXBtn.setContentAreaFilled(false);
        closeXBtn.setBorderPainted(false);
        closeXBtn.setFocusPainted(false);
        closeXBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeXBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeXBtn.setForeground(PRIMARY_CORAL);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeXBtn.setForeground(textMuted);
            }
        });
        closeXBtn.addActionListener(e -> receiptDialog.dispose());

        topHeader.add(headerLeft, BorderLayout.WEST);
        topHeader.add(closeXBtn, BorderLayout.EAST);
        receiptCard.add(topHeader);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // FINBANK BRANDING
        JLabel brandName = new JLabel("FINBANK");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandName.setForeground(PRIMARY_CORAL);
        brandName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("MANAGEMENT SYSTEM");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 9));
        brandSub.setForeground(textMuted);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiptCard.add(brandName);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 2)));
        receiptCard.add(brandSub);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // SUCCESS SECTION
        boolean isSuccess = transaction.status != null && transaction.status.equalsIgnoreCase("SUCCESS");
        Color checkCircleColor = isSuccess
                ? (isDarkMode ? new Color(36, 110, 65) : SUCCESS_GREEN)
                : (isDarkMode ? new Color(110, 36, 36) : DANGER_RED);

        CircularCheckIcon checkIcon = new CircularCheckIcon(42, checkCircleColor, Color.WHITE);
        checkIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusHeader = new JLabel(isSuccess ? "Transaction Successful" : ("Transaction " + transaction.status));
        statusHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusHeader.setForeground(isSuccess ? (isDarkMode ? new Color(130, 225, 160) : SUCCESS_GREEN) : DANGER_RED);
        statusHeader.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusSub = new JLabel(isSuccess
                ? "Your transaction has been completed successfully."
                : ("Transaction status: " + transaction.status));
        statusSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusSub.setForeground(textMuted);
        statusSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiptCard.add(checkIcon);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 6)));
        receiptCard.add(statusHeader);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 3)));
        receiptCard.add(statusSub);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // AMOUNT HERO
        JLabel amtTag = new JLabel("AMOUNT");
        amtTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        amtTag.setForeground(textMuted);
        amtTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amtVal = new JLabel("Rs. " + String.format("%,.2f", transaction.amount));
        amtVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        amtVal.setForeground(PRIMARY_CORAL);
        amtVal.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle row: Type • [TXN ID Chip]
        JPanel heroSubRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        heroSubRow.setOpaque(false);
        heroSubRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 24));

        JLabel heroTypeLbl = new JLabel(transaction.type);
        heroTypeLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heroTypeLbl.setForeground(textMain);

        JLabel heroDotLbl = new JLabel("•");
        heroDotLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        heroDotLbl.setForeground(textMuted);

        RoundedCard txnChip = new RoundedCard(
                8,
                isDarkMode ? new Color(34, 38, 48) : new Color(242, 240, 236),
                isDarkMode ? new Color(52, 56, 70) : new Color(225, 221, 214)
        );
        txnChip.setBorder(new EmptyBorder(2, 7, 2, 7));
        JLabel chipLbl = new JLabel(transaction.id);
        chipLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        chipLbl.setForeground(PRIMARY_CORAL);
        txnChip.add(chipLbl);

        heroSubRow.add(heroTypeLbl);
        heroSubRow.add(heroDotLbl);
        heroSubRow.add(txnChip);
        heroSubRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        String heroDateTime = transaction.dateTime != null
                ? transaction.dateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy  |  HH:mm"))
                : "-";
        JLabel dateHeroLbl = new JLabel(heroDateTime);
        dateHeroLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateHeroLbl.setForeground(textMuted);
        dateHeroLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiptCard.add(amtTag);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 2)));
        receiptCard.add(amtVal);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 4)));
        receiptCard.add(heroSubRow);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 4)));
        receiptCard.add(dateHeroLbl);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 14)));

        // INFORMATION CARDS
        // Card 1: TRANSACTION DETAILS
        String formattedDateTime = transaction.dateTime != null
                ? transaction.dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
                : "-";

        RoundedCard statusPill = new RoundedCard(
                10,
                isSuccess
                        ? (isDarkMode ? new Color(22, 46, 30) : new Color(236, 249, 241))
                        : (isDarkMode ? new Color(48, 24, 24) : new Color(254, 240, 240)),
                isSuccess
                        ? (isDarkMode ? new Color(38, 76, 50) : new Color(196, 234, 210))
                        : (isDarkMode ? new Color(80, 38, 38) : new Color(248, 204, 204))
        );
        statusPill.setBorder(new EmptyBorder(2, 7, 2, 7));
        JLabel pillText = new JLabel((isSuccess ? "✓ " : "✕ ") + transaction.status);
        pillText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        pillText.setForeground(isSuccess
                ? (isDarkMode ? new Color(130, 225, 160) : SUCCESS_GREEN)
                : DANGER_RED);
        statusPill.add(pillText);

        JPanel card1Grid = new JPanel(new GridLayout(1, 3, 10, 0));
        card1Grid.setOpaque(false);
        card1Grid.add(createReceiptDetailItem("Transaction ID", transaction.id, null));
        card1Grid.add(createReceiptDetailItem("Date & Time", formattedDateTime, null));
        card1Grid.add(createReceiptDetailItem("Status", null, statusPill));

        RoundedCard card1 = createReceiptInfoCard("TRANSACTION DETAILS", card1Grid);
        receiptCard.add(card1);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Card 2: TRANSFER DETAILS
        JPanel card2Grid = new JPanel(new GridLayout(1, 3, 10, 0));
        card2Grid.setOpaque(false);
        String fromVal = transaction.from != null && !transaction.from.isEmpty() ? transaction.from : "-";
        String toVal = transaction.to != null && !transaction.to.isEmpty() ? transaction.to : "-";
        card2Grid.add(createReceiptDetailItem("From", fromVal, null));
        card2Grid.add(createReceiptDetailItem("To", toVal, null));
        card2Grid.add(createReceiptDetailItem("Transaction Type", transaction.type, null));

        RoundedCard card2 = createReceiptInfoCard("TRANSFER DETAILS", card2Grid);
        receiptCard.add(card2);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 8)));

        // Card 3: ACCOUNT INFORMATION
        JPanel card3Grid = new JPanel(new GridLayout(1, 3, 10, 0));
        card3Grid.setOpaque(false);
        card3Grid.add(createReceiptDetailItem("Account Number", "#" + account.accountNo, null));
        card3Grid.add(createReceiptDetailItem("Account Type", account.type, null));
        card3Grid.add(createReceiptDetailItem("Current Balance", "Rs. " + String.format("%,.2f", account.balance), null));

        RoundedCard card3 = createReceiptInfoCard("ACCOUNT INFORMATION", card3Grid);
        receiptCard.add(card3);
        receiptCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // SYSTEM GENERATED RECEIPT NOTICE
        RoundedCard systemBox = new RoundedCard(
                12,
                isDarkMode ? new Color(18, 32, 24) : new Color(240, 249, 244),
                isDarkMode ? new Color(32, 58, 42) : new Color(205, 238, 218)
        );
        systemBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        systemBox.setLayout(new BorderLayout(8, 0));
        systemBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));

        JPanel sysLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sysLeft.setOpaque(false);
        ShieldIcon shield = new ShieldIcon(isDarkMode ? new Color(130, 225, 160) : SUCCESS_GREEN);
        JPanel sysTexts = new JPanel();
        sysTexts.setOpaque(false);
        sysTexts.setLayout(new BoxLayout(sysTexts, BoxLayout.Y_AXIS));

        JLabel sysL1 = new JLabel("This is a system generated receipt.");
        sysL1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sysL1.setForeground(isDarkMode ? new Color(170, 225, 185) : new Color(35, 115, 65));

        JLabel sysL2 = new JLabel("Keep this receipt for your records.");
        sysL2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sysL2.setForeground(isDarkMode ? new Color(130, 175, 145) : new Color(75, 130, 95));

        sysTexts.add(sysL1);
        sysTexts.add(sysL2);
        sysLeft.add(shield);
        sysLeft.add(sysTexts);

        JPanel sysRight = new JPanel();
        sysRight.setOpaque(false);
        sysRight.setLayout(new BoxLayout(sysRight, BoxLayout.Y_AXIS));

        JLabel brandL1 = new JLabel("FinBank");
        brandL1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        brandL1.setForeground(isDarkMode ? new Color(170, 225, 185) : new Color(35, 115, 65));
        brandL1.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel brandL2 = new JLabel("Secure Banking. Trusted Always.");
        brandL2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        brandL2.setForeground(isDarkMode ? new Color(130, 175, 145) : new Color(75, 130, 95));
        brandL2.setAlignmentX(Component.RIGHT_ALIGNMENT);

        sysRight.add(brandL1);
        sysRight.add(brandL2);

        systemBox.add(sysLeft, BorderLayout.WEST);
        systemBox.add(sysRight, BorderLayout.EAST);
        receiptCard.add(systemBox);

        // Put receipt card in a clean borderless scrollpane for responsiveness
        JScrollPane scrollPane = new JScrollPane(receiptCard);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // BOTTOM ACTIONS
        JPanel bottomActionPanel = new JPanel();
        bottomActionPanel.setOpaque(false);
        bottomActionPanel.setLayout(new BoxLayout(bottomActionPanel, BoxLayout.Y_AXIS));

        JLabel copyFeedbackLabel = new JLabel(" ");
        copyFeedbackLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        copyFeedbackLabel.setForeground(isDarkMode ? new Color(130, 225, 160) : SUCCESS_GREEN);
        copyFeedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        ModernButton printBtn = new ModernButton(
                "Print Receipt",
                secondaryBtnBg,
                secondaryBtnHover,
                secondaryBtnText,
                10
        );
        printBtn.setPreferredSize(new Dimension(130, 36));
        printBtn.addActionListener(e -> {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("FinBank_Receipt_" + transaction.id);
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) {
                        return Printable.NO_SUCH_PAGE;
                    }
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    double scaleX = pageFormat.getImageableWidth() / receiptCard.getWidth();
                    double scaleY = pageFormat.getImageableHeight() / receiptCard.getHeight();
                    double scale = Math.min(1.0, Math.min(scaleX, scaleY));
                    g2d.scale(scale, scale);
                    receiptCard.paint(g2d);
                    return Printable.PAGE_EXISTS;
                });
                if (job.printDialog()) {
                    job.print();
                    copyFeedbackLabel.setText("✓ Sent to printer successfully!");
                }
            } catch (Exception ex) {
                copyFeedbackLabel.setText("Print error: " + ex.getMessage());
            }
        });

        ModernButton copyBtn = new ModernButton(
                "Copy Transaction ID",
                secondaryBtnBg,
                secondaryBtnHover,
                secondaryBtnText,
                10
        );
        copyBtn.setPreferredSize(new Dimension(165, 36));
        copyBtn.addActionListener(e -> {
            try {
                StringSelection selection = new StringSelection(transaction.id);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                copyFeedbackLabel.setText("✓ Transaction ID copied: " + transaction.id);
            } catch (Exception ex) {
                copyFeedbackLabel.setText("ID: " + transaction.id);
            }
        });

        ModernButton closeBtn = new ModernButton(
                "Close",
                PRIMARY_CORAL,
                PRIMARY_CORAL_HOVER,
                Color.WHITE,
                10
        );
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(e -> receiptDialog.dispose());

        btnRow.add(printBtn);
        btnRow.add(copyBtn);
        btnRow.add(closeBtn);

        bottomActionPanel.add(copyFeedbackLabel);
        bottomActionPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        bottomActionPanel.add(btnRow);

        mainPanel.add(bottomActionPanel, BorderLayout.SOUTH);

        receiptDialog.add(mainPanel);
        receiptDialog.setVisible(true);
    }

    private RoundedCard createReceiptInfoCard(String title, JPanel contentPanel) {
        Color infoCardBg = isDarkMode ? new Color(0x20, 0x23, 0x2B) : new Color(0xFA, 0xFA, 0xFA);
        RoundedCard card = new RoundedCard(12, infoCardBg, cardBorder);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(8, 12, 8, 12));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 66));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        titleLbl.setForeground(textMuted);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createReceiptDetailItem(String label, String value, Component customValue) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(textMuted);
        item.add(l);
        item.add(Box.createRigidArea(new Dimension(0, 2)));

        if (customValue != null) {
            item.add(customValue);
        } else {
            JLabel v = new JLabel(value != null ? value : "-");
            v.setFont(new Font("Segoe UI", Font.BOLD, 12));
            v.setForeground(textMain);
            item.add(v);
        }
        return item;
    }

    static class CircularCheckIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private int size;
        private Color circleColor;
        private Color checkColor;

        public CircularCheckIcon(int size, Color circleColor, Color checkColor) {
            this.size = size;
            this.circleColor = circleColor;
            this.checkColor = checkColor;
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(circleColor);
            g2.fillOval(1, 1, size - 2, size - 2);

            g2.setColor(checkColor);
            g2.setStroke(new BasicStroke(Math.max(2.2f, size * 0.08f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = size / 2;
            int cy = size / 2;
            int x1 = cx - size / 5;
            int y1 = cy;
            int x2 = cx - size / 16;
            int y2 = cy + size / 6;
            int x3 = cx + size / 4;
            int y3 = cy - size / 6;

            g2.drawLine(x1, y1, x2, y2);
            g2.drawLine(x2, y2, x3, y3);
            g2.dispose();
        }
    }

    static class MiniReceiptIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Color iconColor;

        public MiniReceiptIcon(Color iconColor) {
            this.iconColor = iconColor;
            setPreferredSize(new Dimension(18, 18));
            setMaximumSize(new Dimension(18, 18));
            setMinimumSize(new Dimension(18, 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(1.4f));
            g2.drawRoundRect(2, 1, 13, 15, 3, 3);
            g2.drawLine(5, 5, 11, 5);
            g2.drawLine(5, 8, 11, 8);
            g2.drawLine(5, 11, 9, 11);
            g2.dispose();
        }
    }

    static class ShieldIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Color color;

        public ShieldIcon(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(20, 22));
            setMaximumSize(new Dimension(20, 22));
            setMinimumSize(new Dimension(20, 22));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            int w = getWidth();
            int h = getHeight();

            GeneralPath path = new GeneralPath();
            path.moveTo(w / 2.0, 2);
            path.lineTo(w - 3.0, 5);
            path.quadTo(w - 3.0, h * 0.65, w / 2.0, h - 2);
            path.quadTo(3.0, h * 0.65, 3.0, 5);
            path.closePath();

            g2.setStroke(new BasicStroke(1.6f));
            g2.draw(path);

            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(w / 2 - 3, h / 2, w / 2 - 1, h / 2 + 3);
            g2.drawLine(w / 2 - 1, h / 2 + 3, w / 2 + 4, h / 2 - 3);

            g2.dispose();
        }
    }

    // =========================================================================
    // 8. UPDATE CUSTOMER SCREEN (TWO-COLUMN FINTECH LAYOUT WITH WORKFLOW PREVIEW)
    // =========================================================================
    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Update Customer", "Modify customer information for an existing account."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Workflow Guide & Customer Record Card with soft lavender identity
        GradientCard leftCard = new GradientCard(22, lavenderStart, lavenderEnd, lavenderBorder);
        leftCard.setLayout(new BorderLayout(0, 14));
        leftCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, isDarkMode ? new Color(42, 34, 58) : new Color(240, 230, 255), isDarkMode ? new Color(70, 52, 95) : new Color(215, 195, 245));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("PROFILE EDIT");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(new Color(145, 110, 225));
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Customer Record");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Verified customer identity view and protected ledger record.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // Center Content Container
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 1. Workflow Step Guide Card
        RoundedCard stepBox = new RoundedCard(12, isDarkMode ? new Color(28, 22, 38) : new Color(248, 244, 255), lavenderBorder);
        stepBox.setLayout(new BorderLayout(0, 4));
        stepBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        stepBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        JLabel stepTitleLbl = new JLabel("WORKFLOW STEPS");
        stepTitleLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        stepTitleLbl.setForeground(new Color(145, 110, 225));

        JLabel stepStatusLabel = new JLabel("Step 1: Enter Account #  →  Step 2: Load Info  →  Step 4: Edit Details  →  Step 5: Save");
        stepStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        stepStatusLabel.setForeground(textMuted);

        stepBox.add(stepTitleLbl, BorderLayout.NORTH);
        stepBox.add(stepStatusLabel, BorderLayout.CENTER);
        centerPanel.add(stepBox);

        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // 2. Customer Profile Card
        RoundedCard profileCard = new RoundedCard(16, isDarkMode ? new Color(24, 26, 32) : Color.WHITE, lavenderBorder);
        profileCard.setLayout(new BorderLayout(0, 12));
        profileCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel profileTop = new JPanel(new BorderLayout(8, 0));
        profileTop.setOpaque(false);

        JLabel previewAccLabel = new JLabel("Account: # ----");
        previewAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewAccLabel.setForeground(textMain);

        JLabel previewVerifyBadge = new JLabel("Awaiting Load");
        previewVerifyBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewVerifyBadge.setForeground(textMuted);

        profileTop.add(previewAccLabel, BorderLayout.WEST);
        profileTop.add(previewVerifyBadge, BorderLayout.EAST);

        JPanel detailsGrid = new JPanel(new GridLayout(4, 1, 0, 6));
        detailsGrid.setOpaque(false);

        JLabel previewNameRow = new JLabel("Registered Name: --");
        previewNameRow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewNameRow.setForeground(textMain);

        JLabel previewPhoneRow = new JLabel("Registered Phone: --");
        previewPhoneRow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewPhoneRow.setForeground(textMain);

        JLabel previewTypeRow = new JLabel("Account Type: --");
        previewTypeRow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewTypeRow.setForeground(textMain);

        JLabel previewBalRow = new JLabel("Ledger Balance: -- (🔒 Protected)");
        previewBalRow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        previewBalRow.setForeground(new Color(145, 110, 225));

        detailsGrid.add(previewNameRow);
        detailsGrid.add(previewPhoneRow);
        detailsGrid.add(previewTypeRow);
        detailsGrid.add(previewBalRow);

        profileCard.add(profileTop, BorderLayout.NORTH);
        profileCard.add(detailsGrid, BorderLayout.CENTER);

        JPanel guardBox = new RoundedCard(10, isDarkMode ? new Color(24, 18, 34) : new Color(246, 240, 255), lavenderBorder);
        guardBox.setLayout(new BorderLayout());
        guardBox.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel guardNotice = new JLabel("<html>Account number and ledger balance remain protected & immutable.</html>");
        guardNotice.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        guardNotice.setForeground(textMuted);
        guardBox.add(guardNotice, BorderLayout.CENTER);
        profileCard.add(guardBox, BorderLayout.SOUTH);

        centerPanel.add(profileCard);
        leftCard.add(centerPanel, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Immutable Ledger Protected");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setForeground(textMain);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoDesc = new JLabel("Customer profiles update without affecting historic transaction logs.");
        infoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoDesc.setForeground(textMuted);
        infoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBottomPanel.add(infoTitle);
        leftBottomPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        leftBottomPanel.add(infoDesc);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Form Card
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);
        JLabel formHeading = new JLabel("Customer Information");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);
        JLabel formSubheading = new JLabel("Load an existing account to update customer details.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);
        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField accNoField = new ModernTextField(20, "e.g. 1001");
        ModernTextField nameField = new ModernTextField(20, "Enter updated full name");
        ModernTextField phoneField = new ModernTextField(20, "Enter updated 10-digit phone");

        JLabel loadStatusLabel = new JLabel(" ");
        loadStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        loadStatusLabel.setForeground(textMuted);
        loadStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel loadRow = new JPanel(new BorderLayout(10, 0));
        loadRow.setOpaque(false);
        loadRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loadRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadRow.add(accNoField, BorderLayout.CENTER);

        ModernButton loadBtn = new ModernButton("Load Info", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        loadBtn.setPreferredSize(new Dimension(110, 42));
        loadRow.add(loadBtn, BorderLayout.EAST);

        formFieldsPanel.add(createFormLabel("Step 1: Account Number to Update"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(loadRow);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        formFieldsPanel.add(loadStatusLabel);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Step 4: New Customer Name"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(nameField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Step 4: New Phone Number"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(phoneField);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        accNoField.addActionListener(e -> loadBtn.doClick());

        loadBtn.addActionListener(e -> {
            try {
                String raw = accNoField.getText().trim();
                if (raw.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter an account number.", "Input Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int accNo = Integer.parseInt(raw);
                BankAccount acc = bank.search(accNo);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this, "Account #" + accNo + " does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
                    previewAccLabel.setText("Account: # " + accNo);
                    previewVerifyBadge.setText("Not Found");
                    previewVerifyBadge.setForeground(DANGER_RED);
                    previewNameRow.setText("Registered Name: --");
                    previewPhoneRow.setText("Registered Phone: --");
                    previewTypeRow.setText("Account Type: --");
                    previewBalRow.setText("Ledger Balance: --");
                    loadStatusLabel.setText("No account record found for #" + accNo);
                    loadStatusLabel.setForeground(DANGER_RED);
                    return;
                }

                nameField.setText(acc.customer.name);
                phoneField.setText(acc.customer.phone);

                previewAccLabel.setText("Account #" + acc.accountNo);
                previewVerifyBadge.setText("✓ Verified Active");
                previewVerifyBadge.setForeground(SUCCESS_GREEN);
                previewNameRow.setText("Registered Name: " + acc.customer.name);
                previewPhoneRow.setText("Registered Phone: " + acc.customer.phone);
                previewTypeRow.setText("Account Type: " + acc.type);
                previewBalRow.setText(String.format("Ledger Balance: Rs. %,.2f (🔒 Protected)", acc.balance));

                loadStatusLabel.setText("✓ Record loaded for " + acc.customer.name + ". Edit fields and Save.");
                loadStatusLabel.setForeground(SUCCESS_GREEN);
                stepStatusLabel.setText("Status: Step 3 & 4 active — Review details, edit fields, and Save Changes.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric account number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton saveBtn = new ModernButton("Save Changes", PRIMARY_CORAL, PRIMARY_CORAL_HOVER, Color.WHITE, 14);
        saveBtn.setPreferredSize(new Dimension(170, 44));

        saveBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                String newName = nameField.getText().trim();
                String newPhone = phoneField.getText().trim();

                if (newName.isEmpty() || newPhone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name and phone cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                BankAccount acc = bank.search(accNo);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this, "Account #" + accNo + " does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                bank.update(accNo, newName, newPhone);

                JOptionPane.showMessageDialog(
                        this,
                        String.format("Customer Details Updated!\n\nAccount: #%d\nName: %s\nPhone: %s", accNo, newName, newPhone),
                        "Profile Updated",
                        JOptionPane.INFORMATION_MESSAGE
                );

                accNoField.setText("");
                nameField.setText("");
                phoneField.setText("");
                refreshAll();
                showScreen("Accounts");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric account number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(saveBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 9. DELETE ACCOUNT SCREEN (TWO-COLUMN FINTECH LAYOUT)
    // =========================================================================
    // =========================================================================
    // 9. DELETE ACCOUNT SCREEN (TWO-COLUMN FINTECH LAYOUT WITH DANGER SAFEGUARD)
    // =========================================================================
    private JPanel createDeletePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(createHeaderTitleBlock("Close / Delete Account", "Permanently remove an account record from the registry."), BorderLayout.NORTH);

        JPanel columnsPanel = new JPanel(new GridLayout(1, 2, 22, 0));
        columnsPanel.setOpaque(false);

        // LEFT COLUMN: Termination Summary & Critical Warning Card
        GradientCard leftCard = new GradientCard(22, dangerStart, dangerEnd, dangerBorder);
        leftCard.setLayout(new BorderLayout(0, 14));
        leftCard.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel leftTopPanel = new JPanel();
        leftTopPanel.setOpaque(false);
        leftTopPanel.setLayout(new BoxLayout(leftTopPanel, BoxLayout.Y_AXIS));

        JPanel badgePill = new RoundedCard(12, isDarkMode ? new Color(54, 28, 28) : new Color(255, 230, 230), isDarkMode ? new Color(90, 42, 42) : new Color(250, 195, 195));
        badgePill.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
        badgePill.setMaximumSize(new Dimension(135, 26));
        badgePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel badgeLbl = new JLabel("CLOSURE ACTION");
        badgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badgeLbl.setForeground(DANGER_RED);
        badgePill.add(badgeLbl);

        JLabel leftTitle = new JLabel("Account Termination");
        leftTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        leftTitle.setForeground(textMain);
        leftTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel leftDesc = new JLabel("<html>Permanent deregistration, ledger unindexing, and balance payout.</html>");
        leftDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        leftDesc.setForeground(textMuted);
        leftDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftTopPanel.add(badgePill);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftTopPanel.add(leftTitle);
        leftTopPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        leftTopPanel.add(leftDesc);

        leftCard.add(leftTopPanel, BorderLayout.NORTH);

        // Center Termination Preview Card
        RoundedCard previewCard = new RoundedCard(16, isDarkMode ? new Color(28, 22, 24) : Color.WHITE, dangerBorder);
        previewCard.setLayout(new BorderLayout(0, 12));
        previewCard.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Account Header Row
        JPanel accountRow = new JPanel(new BorderLayout(8, 0));
        accountRow.setOpaque(false);

        JLabel previewAccLabel = new JLabel("Account: # ----");
        previewAccLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        previewAccLabel.setForeground(textMain);

        JLabel previewStatusBadge = new JLabel("Awaiting Input");
        previewStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        previewStatusBadge.setForeground(textMuted);

        accountRow.add(previewAccLabel, BorderLayout.WEST);
        accountRow.add(previewStatusBadge, BorderLayout.EAST);

        JLabel previewNameLabel = new JLabel("Customer: Not selected");
        previewNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewNameLabel.setForeground(textMuted);

        JLabel previewTypeLabel = new JLabel("Account Type: --");
        previewTypeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        previewTypeLabel.setForeground(textMuted);

        JPanel accountBlock = new JPanel();
        accountBlock.setOpaque(false);
        accountBlock.setLayout(new BoxLayout(accountBlock, BoxLayout.Y_AXIS));
        accountBlock.add(accountRow);
        accountBlock.add(Box.createRigidArea(new Dimension(0, 2)));
        accountBlock.add(previewNameLabel);
        accountBlock.add(Box.createRigidArea(new Dimension(0, 2)));
        accountBlock.add(previewTypeLabel);

        previewCard.add(accountBlock, BorderLayout.NORTH);

        // Balance Disbursement Callout Box
        JPanel disburseBox = new RoundedCard(12, isDarkMode ? new Color(40, 22, 22) : new Color(255, 238, 238), dangerBorder);
        disburseBox.setLayout(new BorderLayout(0, 4));
        disburseBox.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel disburseTitle = new JLabel("BALANCE DUE FOR DISBURSEMENT");
        disburseTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        disburseTitle.setForeground(DANGER_RED);

        JLabel disburseAmount = new JLabel("Rs. 0.00");
        disburseAmount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        disburseAmount.setForeground(DANGER_RED);

        JLabel disburseNote = new JLabel("Ensure total balance is paid out to customer before closing.");
        disburseNote.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        disburseNote.setForeground(textMuted);

        disburseBox.add(disburseTitle, BorderLayout.NORTH);
        disburseBox.add(disburseAmount, BorderLayout.CENTER);
        disburseBox.add(disburseNote, BorderLayout.SOUTH);

        previewCard.add(disburseBox, BorderLayout.CENTER);

        // Warning Alert Box
        JPanel warningBox = new RoundedCard(10, isDarkMode ? new Color(34, 18, 20) : new Color(254, 242, 242), dangerBorder);
        warningBox.setLayout(new BorderLayout(0, 3));
        warningBox.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel warnHeading = new JLabel("⚠ CRITICAL: Irreversible Action");
        warnHeading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        warnHeading.setForeground(DANGER_RED);

        JLabel warnDetails = new JLabel("<html>Deregistered accounts cannot be recovered. All future transfers to this account will be permanently rejected.</html>");
        warnDetails.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        warnDetails.setForeground(textMuted);

        warningBox.add(warnHeading, BorderLayout.NORTH);
        warningBox.add(warnDetails, BorderLayout.CENTER);
        previewCard.add(warningBox, BorderLayout.SOUTH);

        leftCard.add(previewCard, BorderLayout.CENTER);

        JPanel leftBottomPanel = new JPanel();
        leftBottomPanel.setOpaque(false);
        leftBottomPanel.setLayout(new BoxLayout(leftBottomPanel, BoxLayout.Y_AXIS));

        JLabel infoTitle = new JLabel("Two-Step Confirmation Enforced");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        infoTitle.setForeground(textMain);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel infoDesc = new JLabel("Account number re-entry ensures no account is closed accidentally.");
        infoDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoDesc.setForeground(textMuted);
        infoDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftBottomPanel.add(infoTitle);
        leftBottomPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        leftBottomPanel.add(infoDesc);
        leftCard.add(leftBottomPanel, BorderLayout.SOUTH);

        // RIGHT COLUMN: Form Card
        RoundedCard rightCard = new RoundedCard(22, cardBg, cardBorder);
        rightCard.setLayout(new BorderLayout(0, 14));
        rightCard.setBorder(new EmptyBorder(24, 26, 24, 26));

        JPanel formTitleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        formTitleBlock.setOpaque(false);
        JLabel formHeading = new JLabel("Confirm Deletion");
        formHeading.setFont(new Font("Segoe UI", Font.BOLD, 18));
        formHeading.setForeground(textMain);
        JLabel formSubheading = new JLabel("Verify and confirm the account number to permanently close.");
        formSubheading.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        formSubheading.setForeground(textMuted);
        formTitleBlock.add(formHeading);
        formTitleBlock.add(formSubheading);
        rightCard.add(formTitleBlock, BorderLayout.NORTH);

        JPanel formFieldsPanel = new JPanel();
        formFieldsPanel.setOpaque(false);
        formFieldsPanel.setLayout(new BoxLayout(formFieldsPanel, BoxLayout.Y_AXIS));

        ModernTextField accNoField = new ModernTextField(20, "e.g. 1001");
        JLabel accStatusLabel = new JLabel(" ");
        accStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accStatusLabel.setForeground(textMuted);
        accStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        ModernTextField confirmAccNoField = new ModernTextField(20, "Re-type account number to unlock button");
        JLabel confirmStatusLabel = new JLabel("Type account number again to unlock deletion.");
        confirmStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        confirmStatusLabel.setForeground(textMuted);
        confirmStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel lockNoticeBox = new RoundedCard(12, isDarkMode ? new Color(24, 26, 32) : new Color(248, 247, 244), cardBorder);
        lockNoticeBox.setLayout(new BorderLayout(0, 4));
        lockNoticeBox.setBorder(new EmptyBorder(10, 12, 10, 12));
        lockNoticeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        lockNoticeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lockTitle = new JLabel("Security Confirmation Lock");
        lockTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lockTitle.setForeground(textMain);

        JLabel lockDesc = new JLabel("<html>The 'Delete Account' button will remain locked until both account numbers match an active record.</html>");
        lockDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lockDesc.setForeground(textMuted);

        lockNoticeBox.add(lockTitle, BorderLayout.NORTH);
        lockNoticeBox.add(lockDesc, BorderLayout.CENTER);

        formFieldsPanel.add(createFormLabel("Step 1: Account Number to Delete"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(accNoField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formFieldsPanel.add(accStatusLabel);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        formFieldsPanel.add(createFormLabel("Step 2: Re-enter Account Number to Confirm"));
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formFieldsPanel.add(confirmAccNoField);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        formFieldsPanel.add(confirmStatusLabel);
        formFieldsPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        formFieldsPanel.add(lockNoticeBox);
        formFieldsPanel.add(Box.createVerticalGlue());

        rightCard.add(formFieldsPanel, BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionsPanel.setOpaque(false);

        ModernButton cancelBtn = new ModernButton("Cancel", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        cancelBtn.addActionListener(e -> showScreen("Dashboard"));

        ModernButton deleteBtn = new ModernButton("Delete Account", DANGER_RED, DANGER_RED_HOVER, Color.WHITE, 14);
        deleteBtn.setPreferredSize(new Dimension(170, 44));
        deleteBtn.setEnabled(false);

        // Real-time Synchronizer & Safeguard Lock
        Runnable syncDeletePreview = () -> {
            int accNo = -1;
            BankAccount acc = null;
            try {
                String accStr = accNoField.getText().trim();
                if (!accStr.isEmpty()) {
                    accNo = Integer.parseInt(accStr);
                    acc = bank.search(accNo);
                }
            } catch (Exception ignored) {}

            String confirmStr = confirmAccNoField.getText().trim();

            if (acc != null) {
                previewAccLabel.setText("Account #" + acc.accountNo);
                previewStatusBadge.setText("Active Record");
                previewStatusBadge.setForeground(DANGER_RED);
                previewNameLabel.setText("Customer: " + acc.customer.name);
                previewTypeLabel.setText("Account Type: " + acc.type);
                disburseAmount.setText(String.format("Rs. %,.2f", acc.balance));
                accStatusLabel.setText("✓ Account found: " + acc.customer.name + " (" + acc.type + ")");
                accStatusLabel.setForeground(SUCCESS_GREEN);

                if (confirmStr.isEmpty()) {
                    deleteBtn.setEnabled(false);
                    confirmStatusLabel.setText("Type account number #" + acc.accountNo + " again to unlock deletion.");
                    confirmStatusLabel.setForeground(textMuted);
                } else if (confirmStr.equals(String.valueOf(acc.accountNo))) {
                    deleteBtn.setEnabled(true);
                    confirmStatusLabel.setText("✓ Confirmation matches #" + acc.accountNo + ". Deletion unlocked.");
                    confirmStatusLabel.setForeground(SUCCESS_GREEN);
                } else {
                    deleteBtn.setEnabled(false);
                    confirmStatusLabel.setText("Account numbers do not match.");
                    confirmStatusLabel.setForeground(DANGER_RED);
                }
            } else if (!accNoField.getText().trim().isEmpty()) {
                previewAccLabel.setText("Account: # ----");
                previewStatusBadge.setText("Account Not Found");
                previewStatusBadge.setForeground(DANGER_RED);
                previewNameLabel.setText("Customer: Not found");
                previewTypeLabel.setText("Account Type: --");
                disburseAmount.setText("Rs. 0.00");
                accStatusLabel.setText("No account found matching #" + accNoField.getText().trim());
                accStatusLabel.setForeground(DANGER_RED);
                confirmStatusLabel.setText("Please enter a valid source account first.");
                confirmStatusLabel.setForeground(textMuted);
                deleteBtn.setEnabled(false);
            } else {
                previewAccLabel.setText("Account: # ----");
                previewStatusBadge.setText("Awaiting Input");
                previewStatusBadge.setForeground(textMuted);
                previewNameLabel.setText("Customer: Not selected");
                previewTypeLabel.setText("Account Type: --");
                disburseAmount.setText("Rs. 0.00");
                accStatusLabel.setText(" ");
                confirmStatusLabel.setText("Type account number again to unlock deletion.");
                confirmStatusLabel.setForeground(textMuted);
                deleteBtn.setEnabled(false);
            }
        };

        attachDocListener(accNoField, syncDeletePreview);
        attachDocListener(confirmAccNoField, syncDeletePreview);

        deleteBtn.addActionListener(e -> {
            try {
                int accNo = Integer.parseInt(accNoField.getText().trim());
                BankAccount acc = bank.search(accNo);

                if (acc == null) {
                    JOptionPane.showMessageDialog(this, "Account #" + accNo + " does not exist.", "Account Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String confirmStr = confirmAccNoField.getText().trim();
                if (!confirmStr.equals(String.valueOf(accNo))) {
                    JOptionPane.showMessageDialog(this, "Confirmation account number does not match.", "Confirmation Failed", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(
                        this,
                        String.format("Are you sure you want to permanently delete this account?\n\nAccount: #%d\nCustomer: %s\nBalance to Disburse: Rs. %,.2f",
                                accNo, acc.customer.name, acc.balance),
                        "Confirm Account Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    bank.delete(accNo);

                    JOptionPane.showMessageDialog(
                            this,
                            "Account #" + accNo + " has been permanently removed.",
                            "Account Deleted",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    accNoField.setText("");
                    confirmAccNoField.setText("");
                    refreshAll();
                    showScreen("Accounts");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric account number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(cancelBtn);
        actionsPanel.add(deleteBtn);
        rightCard.add(actionsPanel, BorderLayout.SOUTH);

        columnsPanel.add(leftCard);
        columnsPanel.add(rightCard);

        panel.add(columnsPanel, BorderLayout.CENTER);
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

        JPanel titleBlock = createHeaderTitleBlock("Bank Summary", "Overview of current bank account records and portfolio metrics.");

        ModernButton refreshBtn = new ModernButton("↻ Refresh Data", secondaryBtnBg, secondaryBtnHover, secondaryBtnText, 14);
        refreshBtn.addActionListener(e -> refreshSummary());

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // top 4 refined analytical summary cards with icons and sparklines
        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 14, 0));
        summaryGrid.setOpaque(false);
        summaryGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        summaryGrid.setPreferredSize(new Dimension(860, 105));

        summaryTotalAccountsLabel = new JLabel("0");
        summaryGrid.add(createStatCard("Total Accounts", summaryTotalAccountsLabel, "Registered active accounts", blueStart, blueEnd, blueBorder, "👥", 1));

        summaryTotalBalanceLabel = new JLabel("Rs. 0.00");
        summaryGrid.add(createStatCard("Total Balance", summaryTotalBalanceLabel, "Cumulative customer funds", peachStart, peachEnd, peachBorder, "💳", 0));

        summaryAvgBalanceLabel = new JLabel("Rs. 0.00");
        summaryGrid.add(createStatCard("Avg Account Balance", summaryAvgBalanceLabel, "Balance per active account", yellowStart, yellowEnd, yellowBorder, "📊", 3));

        summarySavingsCountLabel = new JLabel("0");
        summaryGrid.add(createStatCard("Savings Accounts", summarySavingsCountLabel, "Retail deposit base", lavenderStart, lavenderEnd, lavenderBorder, "🛡", 2));

        content.add(summaryGrid);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // middle row: portfolio distribution (with progress bars) & user-facing overview metrics
        JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        middleRow.setOpaque(false);
        middleRow.setPreferredSize(new Dimension(860, 230));
        middleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 235));

        // LEFT CARD: Portfolio Distribution
        RoundedCard distCard = new RoundedCard(22, cardBg, cardBorder);
        distCard.setLayout(new BorderLayout(0, 12));
        distCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel distHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        distHeader.setOpaque(false);
        JLabel distTitle = new JLabel("Portfolio Distribution");
        distTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        distTitle.setForeground(textMain);
        JLabel distSub = new JLabel("Visual ratio of retail savings versus commercial accounts.");
        distSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        distSub.setForeground(textMuted);
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
        savingsTitle.setForeground(textMain);

        Color trackCol = isDarkMode ? new Color(38, 42, 52) : new Color(240, 237, 232);
        summarySavingsBar = new RoundedProgressBar(new Color(142, 101, 211), trackCol);
        summarySavingsMetricsLabel = new JLabel("0 accounts (0%)");
        summarySavingsMetricsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summarySavingsMetricsLabel.setForeground(textMuted);

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
        currentTitle.setForeground(textMain);

        summaryCurrentBar = new RoundedProgressBar(new Color(229, 147, 58), trackCol);
        summaryCurrentMetricsLabel = new JLabel("0 accounts (0%)");
        summaryCurrentMetricsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryCurrentMetricsLabel.setForeground(textMuted);

        currentGroup.add(currentTitle);
        currentGroup.add(Box.createRigidArea(new Dimension(0, 6)));
        currentGroup.add(summaryCurrentBar);
        currentGroup.add(Box.createRigidArea(new Dimension(0, 4)));
        currentGroup.add(summaryCurrentMetricsLabel);

        barsPanel.add(savingsGroup);
        barsPanel.add(currentGroup);
        distCard.add(barsPanel, BorderLayout.CENTER);

        // RIGHT CARD: Account & Transaction Overview (User-Facing Business Metrics)
        RoundedCard overviewCard = new RoundedCard(22, cardBg, cardBorder);
        overviewCard.setLayout(new BorderLayout(0, 12));
        overviewCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel overviewHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        overviewHeader.setOpaque(false);
        JLabel overviewTitle = new JLabel("Account & Transaction Overview");
        overviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        overviewTitle.setForeground(textMain);
        JLabel overviewSub = new JLabel("Aggregated liquidity and volume across all accounts.");
        overviewSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        overviewSub.setForeground(textMuted);
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

        middleRow.add(distCard);
        middleRow.add(overviewCard);
        content.add(middleRow);
        content.add(Box.createRigidArea(new Dimension(0, 14)));

        // LOWER SECTION: Recent Banking Activity Card
        RoundedCard recentActivityCard = new RoundedCard(22, cardBg, cardBorder);
        recentActivityCard.setLayout(new BorderLayout(0, 10));
        recentActivityCard.setBorder(new EmptyBorder(16, 20, 16, 20));
        recentActivityCard.setPreferredSize(new Dimension(860, 185));

        JPanel recentHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        recentHeader.setOpaque(false);
        JLabel recentTitle = new JLabel("Recent Banking Activity");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        recentTitle.setForeground(textMain);
        JLabel recentSub = new JLabel("Latest transaction events posted across the bank registry.");
        recentSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentSub.setForeground(textMuted);
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
        summaryRecentScrollPane.getViewport().setBackground(cardBg);

        summaryRecentEmptyPanel = createEmptyState("💳", "No recent activity", "Transactions recorded on any account will show here.");
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
    // 8. STATISTICS & ANALYTICS SCREEN
    // =========================================================================
    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);

        panel.add(
                createHeaderTitleBlock(
                        "Statistics & Analytics",
                        "View overall banking activity and account statistics."
                ),
                BorderLayout.NORTH
        );

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JPanel topRow = new JPanel(new GridLayout(1, 3, 14, 0));
        topRow.setOpaque(false);

        analyticsTotalAccountsLabel = new JLabel("0");
        analyticsTotalBalanceLabel = new JLabel("Rs. 0.00");
        analyticsTotalTransactionsLabel = new JLabel("0");

        topRow.add(createOverviewMetricBlock(
                "Total Accounts", analyticsTotalAccountsLabel
        ));
        topRow.add(createOverviewMetricBlock(
                "Total Bank Balance", analyticsTotalBalanceLabel
        ));
        topRow.add(createOverviewMetricBlock(
                "Total Transactions", analyticsTotalTransactionsLabel
        ));

        JPanel secondRow = new JPanel(new GridLayout(1, 3, 14, 0));
        secondRow.setOpaque(false);

        analyticsDepositVolumeLabel = new JLabel("Rs. 0.00");
        analyticsWithdrawalVolumeLabel = new JLabel("Rs. 0.00");
        analyticsTransferVolumeLabel = new JLabel("Rs. 0.00");

        secondRow.add(createOverviewMetricBlock(
                "Deposit Volume", analyticsDepositVolumeLabel
        ));
        secondRow.add(createOverviewMetricBlock(
                "Withdrawal Volume", analyticsWithdrawalVolumeLabel
        ));
        secondRow.add(createOverviewMetricBlock(
                "Transfer Volume", analyticsTransferVolumeLabel
        ));

        RoundedCard accountCard = new RoundedCard(22, cardBg, cardBorder);
        accountCard.setLayout(new BorderLayout(0, 12));
        accountCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel accountHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        accountHeader.setOpaque(false);

        JLabel accountTitle = new JLabel("Account Distribution");
        accountTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        accountTitle.setForeground(textMain);

        JLabel accountSub = new JLabel(
                "Current distribution of Savings and Current accounts."
        );
        accountSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        accountSub.setForeground(textMuted);

        accountHeader.add(accountTitle);
        accountHeader.add(accountSub);
        accountCard.add(accountHeader, BorderLayout.NORTH);

        JPanel accountGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        accountGrid.setOpaque(false);

        analyticsSavingsLabel = new JLabel("0");
        analyticsCurrentLabel = new JLabel("0");

        accountGrid.add(createOverviewMetricBlock(
                "Savings Accounts", analyticsSavingsLabel
        ));
        accountGrid.add(createOverviewMetricBlock(
                "Current Accounts", analyticsCurrentLabel
        ));

        accountCard.add(accountGrid, BorderLayout.CENTER);

        RoundedCard activityCard = new RoundedCard(22, cardBg, cardBorder);
        activityCard.setLayout(new BorderLayout(0, 12));
        activityCard.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel activityHeader = new JPanel(new GridLayout(2, 1, 0, 2));
        activityHeader.setOpaque(false);

        JLabel activityTitle = new JLabel("Transaction Insights");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        activityTitle.setForeground(textMain);

        JLabel activitySub = new JLabel(
                "Simple transaction statistics from the banking ledger."
        );
        activitySub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        activitySub.setForeground(textMuted);

        activityHeader.add(activityTitle);
        activityHeader.add(activitySub);
        activityCard.add(activityHeader, BorderLayout.NORTH);

        JPanel activityGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        activityGrid.setOpaque(false);

        analyticsLargestTransactionLabel = new JLabel("Rs. 0.00");
        analyticsAverageTransactionLabel = new JLabel("Rs. 0.00");

        activityGrid.add(createOverviewMetricBlock(
                "Largest Transaction", analyticsLargestTransactionLabel
        ));
        activityGrid.add(createOverviewMetricBlock(
                "Average Transaction", analyticsAverageTransactionLabel
        ));

        activityCard.add(activityGrid, BorderLayout.CENTER);

        content.add(topRow);
        content.add(Box.createRigidArea(new Dimension(0, 14)));
        content.add(secondRow);
        content.add(Box.createRigidArea(new Dimension(0, 14)));
        content.add(accountCard);
        content.add(Box.createRigidArea(new Dimension(0, 14)));
        content.add(activityCard);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // DATA REFRESH METHODS
    // =========================================================================
    void refreshAll() {
        refreshDashboard();
        refreshAccountsTable();
        refreshSummary();
        refreshAnalytics();
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
            boolean empty = dashAccountsTableModel.getRowCount() == 0;
            if (dashAccEmptyPanel != null) dashAccEmptyPanel.setVisible(empty);
            if (dashAccScrollPane != null) dashAccScrollPane.setVisible(!empty);
        }

        if (dashRecentTxnTableModel != null) {
            dashRecentTxnTableModel.setRowCount(0);
            List<Object[]> allTxns = new ArrayList<>();
            for (BankAccount a : bank.accounts.values()) {
                for (Transaction t : a.transactions) {
                    allTxns.add(new Object[]{
                            "#" + a.accountNo,
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
            boolean empty = dashRecentTxnTableModel.getRowCount() == 0;
            if (dashTxnEmptyPanel != null) dashTxnEmptyPanel.setVisible(empty);
            if (dashTxnScrollPane != null) dashTxnScrollPane.setVisible(!empty);
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
                    String.format("Rs. %,.2f", a.balance)
            });
        }

        if (accountsSummaryLabel != null) {
            accountsSummaryLabel.setText(String.format(
                    "Total: %d Accounts  •  %d Savings, %d Current  •  Active Registry",
                    bank.totalAccounts(), savings, current
            ));
        }
    }

    private void refreshAnalytics() {
        int totalAccounts = bank.totalAccounts();
        double totalBalance = bank.totalBalance();

        int savings = 0;
        int current = 0;
        int totalTransactions = 0;
        double depositVolume = 0.0;
        double withdrawalVolume = 0.0;
        double transferVolume = 0.0;
        double largestTransaction = 0.0;
        double totalTransactionAmount = 0.0;

        for (BankAccount account : bank.accounts.values()) {
            if ("Savings".equalsIgnoreCase(account.type)) {
                savings++;
            } else if ("Current".equalsIgnoreCase(account.type)) {
                current++;
            }

            for (Transaction t : account.transactions) {
                totalTransactions++;
                totalTransactionAmount += t.amount;

                if (t.amount > largestTransaction) {
                    largestTransaction = t.amount;
                }

                if (t.type.contains("Deposit")) {
                    depositVolume += t.amount;
                } else if (t.type.contains("Transfer")) {
                    transferVolume += t.amount;
                    withdrawalVolume += t.amount;
                } else {
                    withdrawalVolume += t.amount;
                }
            }
        }

        double averageTransaction =
                totalTransactions > 0
                        ? totalTransactionAmount / totalTransactions
                        : 0.0;

        if (analyticsTotalAccountsLabel != null) {
            analyticsTotalAccountsLabel.setText(String.valueOf(totalAccounts));
        }

        if (analyticsTotalBalanceLabel != null) {
            analyticsTotalBalanceLabel.setText(
                    String.format("Rs. %,.2f", totalBalance)
            );
        }

        if (analyticsTotalTransactionsLabel != null) {
            analyticsTotalTransactionsLabel.setText(
                    String.valueOf(totalTransactions)
            );
        }

        if (analyticsDepositVolumeLabel != null) {
            analyticsDepositVolumeLabel.setText(
                    String.format("Rs. %,.2f", depositVolume)
            );
        }

        if (analyticsWithdrawalVolumeLabel != null) {
            analyticsWithdrawalVolumeLabel.setText(
                    String.format("Rs. %,.2f", withdrawalVolume)
            );
        }

        if (analyticsTransferVolumeLabel != null) {
            analyticsTransferVolumeLabel.setText(
                    String.format("Rs. %,.2f", transferVolume)
            );
        }

        if (analyticsSavingsLabel != null) {
            analyticsSavingsLabel.setText(String.valueOf(savings));
        }

        if (analyticsCurrentLabel != null) {
            analyticsCurrentLabel.setText(String.valueOf(current));
        }

        if (analyticsLargestTransactionLabel != null) {
            analyticsLargestTransactionLabel.setText(
                    String.format("Rs. %,.2f", largestTransaction)
            );
        }

        if (analyticsAverageTransactionLabel != null) {
            analyticsAverageTransactionLabel.setText(
                    String.format("Rs. %,.2f", averageTransaction)
            );
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
            JPanel badge = new RoundedCard(14, isDarkMode ? new Color(20, 42, 28) : new Color(236, 248, 240), isDarkMode ? new Color(34, 76, 48) : new Color(195, 235, 206));
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
        titleLbl.setForeground(textMain);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLbl.setForeground(textMuted);

        block.add(titleLbl);
        block.add(subLbl);
        return block;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle, Color start, Color end, Color border, String icon, int sparklineType) {
        GradientCard card = new GradientCard(20, start, end, border);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(isDarkMode ? textMuted : new Color(90, 88, 82));

        JPanel visualPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        visualPanel.setOpaque(false);

        SparklineVisual sparkline = new SparklineVisual(sparklineType, isDarkMode);
        visualPanel.add(sparkline);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        visualPanel.add(iconLabel);

        topBar.add(titleLbl, BorderLayout.WEST);
        topBar.add(visualPanel, BorderLayout.EAST);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(textMain);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(textMuted);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        centerPanel.setOpaque(false);
        centerPanel.add(valueLabel);
        centerPanel.add(subLbl);

        card.add(topBar, BorderLayout.NORTH);
        card.add(centerPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEmptyState(String iconSymbol, String title, String subtitle) {
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setOpaque(false);

        JPanel emptyContent = new JPanel();
        emptyContent.setOpaque(false);
        emptyContent.setLayout(new BoxLayout(emptyContent, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(iconSymbol);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tLbl.setForeground(textMuted);
        tLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sLbl = new JLabel(subtitle);
        sLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sLbl.setForeground(textMuted);
        sLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyContent.add(icon);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 6)));
        emptyContent.add(tLbl);
        emptyContent.add(Box.createRigidArea(new Dimension(0, 4)));
        emptyContent.add(sLbl);

        emptyPanel.add(emptyContent);
        return emptyPanel;
    }

    private JPanel createFeatureRow(String title, String desc) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);

        JLabel check = new JLabel("✓");
        check.setFont(new Font("Segoe UI", Font.BOLD, 13));
        check.setForeground(PRIMARY_CORAL);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        textPanel.setOpaque(false);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tLbl.setForeground(textMain);

        JLabel dLbl = new JLabel(desc);
        dLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dLbl.setForeground(textMuted);

        textPanel.add(tLbl);
        textPanel.add(dLbl);

        row.add(check, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);
        return row;
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(textMain);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void attachDocListener(JTextField field, Runnable callback) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { callback.run(); }
            @Override
            public void removeUpdate(DocumentEvent e) { callback.run(); }
            @Override
            public void changedUpdate(DocumentEvent e) { callback.run(); }
        });
    }

    private JPanel createQuickAmountChips(String[] labels, double[] values, Consumer<Double> onSelected) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < labels.length; i++) {
            final double val = values[i];
            JButton chip = new JButton(labels[i]);
            chip.setFont(new Font("Segoe UI", Font.BOLD, 11));
            chip.setBackground(secondaryBtnBg);
            chip.setForeground(secondaryBtnText);
            chip.setFocusPainted(false);
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(secondaryBtnBorder, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            chip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            chip.addActionListener(e -> onSelected.accept(val));
            panel.add(chip);
        }
        return panel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(cardBorder);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(isDarkMode ? new Color(60, 40, 36) : new Color(255, 238, 232));
        table.setSelectionForeground(isDarkMode ? Color.WHITE : textMain);

        // header styling with custom renderer for high contrast across themes
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(tableHeaderBg);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, tableHeaderBorder));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setBackground(tableHeaderBg);
                l.setForeground(isDarkMode ? new Color(210, 212, 222) : textMuted);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                return l;
            }
        };
        table.getTableHeader().setDefaultRenderer(headerRenderer);

        // cell padding & alternating row hover renderer
        final int[] hoveredRow = {-1};
        ThemedTableCellRenderer renderer = new ThemedTableCellRenderer(hoveredRow);
        table.setDefaultRenderer(Object.class, renderer);

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow[0]) {
                    hoveredRow[0] = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow[0] = -1;
                table.repaint();
            }
        });
    }

    // =========================================================================
    // CUSTOM SWING COMPONENTS (VIVA EXPLAINABLE)
    // =========================================================================

    // decorative fintech card drawn purely using standard Java Swing Graphics2D
    class DecorativeFinBankCard extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeFinBankCard() {
            setPreferredSize(new Dimension(320, 180));
            setMaximumSize(new Dimension(350, 190));
            setMinimumSize(new Dimension(280, 160));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int availableW = getWidth();
            int availableH = getHeight();
            int cardW = Math.min(availableW - 8, 320);
            int cardH = Math.min(availableH - 8, (int) (cardW * 0.5625)); // realistic 16:9 aspect ratio
            if (cardH > 180) {
                cardH = 180;
                cardW = (int) (cardH / 0.5625);
            }
            int cardX = 0;
            int cardY = (availableH - cardH) / 2;

            // 1. Subtle card drop shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(cardX + 2, cardY + 5, cardW, cardH, 16, 16);
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fillRoundRect(cardX + 1, cardY + 2, cardW, cardH, 16, 16);

            // 2. Realistic dark charcoal/black card surface with subtle gradient depth
            GradientPaint gp = new GradientPaint(
                    cardX, cardY,
                    new Color(36, 38, 46),
                    cardX + cardW, cardY + cardH,
                    new Color(18, 19, 24)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);

            // Crisp border
            g2.setColor(new Color(68, 72, 85));
            g2.drawRoundRect(cardX, cardY, cardW - 1, cardH - 1, 16, 16);

            // 3. Subtle background decorative glow circles in the corner
            g2.setColor(new Color(255, 117, 95, 25));
            g2.fillOval(cardX + cardW - 100, cardY - 25, 120, 120);
            g2.setColor(new Color(255, 200, 150, 15));
            g2.fillOval(cardX + cardW - 70, cardY + 18, 80, 80);

            // 4. Gold EMV chip (top left)
            int chipX = cardX + 20;
            int chipY = cardY + 18;
            g2.setColor(new Color(228, 192, 108));
            g2.fillRoundRect(chipX, chipY, 34, 25, 6, 6);
            g2.setColor(new Color(175, 142, 68));
            g2.drawRoundRect(chipX, chipY, 34, 25, 6, 6);
            g2.drawLine(chipX, chipY + 12, chipX + 34, chipY + 12);
            g2.drawLine(chipX + 12, chipY, chipX + 12, chipY + 25);
            g2.drawLine(chipX + 22, chipY, chipX + 22, chipY + 25);

            // Wireless wave symbol next to chip
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawArc(chipX + 40, chipY + 4, 14, 16, -45, 90);
            g2.drawArc(chipX + 44, chipY + 2, 20, 20, -45, 90);

            // 5. FinBank branding (top right)
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String brand = "FinBank";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(brand, cardX + cardW - fm.stringWidth(brand) - 20, cardY + 34);

            // 6. Card number (centered vertically in card)
            g2.setFont(new Font("Consolas", Font.BOLD, 15));
            g2.setColor(new Color(240, 242, 248));
            g2.drawString("••••   ••••   ••••   " + nextAccountNo, cardX + 20, cardY + (int) (cardH * 0.58));

            // 7. Bottom labels and values
            int bottomLabelY = cardY + cardH - 32;
            int bottomValueY = cardY + cardH - 16;

            // Left side: CARDHOLDER / NEW CUSTOMER
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(150, 154, 168));
            g2.drawString("CARDHOLDER", cardX + 20, bottomLabelY);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.setColor(Color.WHITE);
            g2.drawString("NEW CUSTOMER", cardX + 20, bottomValueY);

            // Right side: STATUS / ● READY
            String statusLabel = "STATUS";
            String statusVal = "● READY";
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            int statusX = cardX + cardW - g2.getFontMetrics().stringWidth(statusLabel) - 20;
            g2.setColor(new Color(150, 154, 168));
            g2.drawString(statusLabel, statusX, bottomLabelY);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.setColor(new Color(80, 215, 130)); // green status
            int statusValX = cardX + cardW - g2.getFontMetrics().stringWidth(statusVal) - 20;
            g2.drawString(statusVal, statusValX, bottomValueY);

            // Small decorative network overlapping circles in corner
            g2.setColor(new Color(255, 117, 95, 190));
            g2.fillOval(cardX + cardW - 52, cardY + cardH - 44, 18, 18);
            g2.setColor(new Color(255, 195, 90, 170));
            g2.fillOval(cardX + cardW - 40, cardY + cardH - 44, 18, 18);

            g2.dispose();
        }
    }

    // decorative visual for deposit screen
    class DecorativeDepositVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeDepositVisual() {
            setPreferredSize(new Dimension(320, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0,
                    isDarkMode ? new Color(20, 36, 26) : new Color(230, 246, 236),
                    getWidth(), getHeight(),
                    isDarkMode ? new Color(14, 24, 18) : new Color(210, 240, 222)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(SUCCESS_GREEN);
            g2.fillOval(getWidth() / 2 - 28, 24, 56, 56);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            g2.drawString("↓", getWidth() / 2 - 8, 62);

            g2.setColor(textMain);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String msg = "Direct Account Inflow";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - w) / 2, 108);

            g2.setColor(textMuted);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String sub = "Instant credit to customer balance";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, (getWidth() - sw) / 2, 128);

            g2.dispose();
        }
    }

    // decorative visual for withdrawal screen
    class DecorativeWithdrawVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeWithdrawVisual() {
            setPreferredSize(new Dimension(320, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0,
                    isDarkMode ? new Color(42, 28, 26) : new Color(255, 240, 235),
                    getWidth(), getHeight(),
                    isDarkMode ? new Color(28, 20, 18) : new Color(255, 226, 218)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(PRIMARY_CORAL);
            g2.fillOval(getWidth() / 2 - 28, 24, 56, 56);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
            g2.drawString("↑", getWidth() / 2 - 8, 62);

            g2.setColor(textMain);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String msg = "Secure Outflow Process";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - w) / 2, 108);

            g2.setColor(textMuted);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String sub = "Overdraft checks & real-time debit";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, (getWidth() - sw) / 2, 128);

            g2.dispose();
        }
    }

    // decorative visual for update screen
    class DecorativeUpdateVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeUpdateVisual() {
            setPreferredSize(new Dimension(320, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0,
                    isDarkMode ? new Color(34, 28, 48) : new Color(246, 238, 255),
                    getWidth(), getHeight(),
                    isDarkMode ? new Color(24, 20, 36) : new Color(236, 224, 252)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(145, 110, 225));
            g2.fillOval(getWidth() / 2 - 28, 24, 56, 56);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            g2.drawString("✎", getWidth() / 2 - 9, 60);

            g2.setColor(textMain);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String msg = "Synchronized Profile Sync";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - w) / 2, 108);

            g2.setColor(textMuted);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String sub = "Instant updates across database maps";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, (getWidth() - sw) / 2, 128);

            g2.dispose();
        }
    }

    // decorative visual for delete screen
    class DecorativeDeleteVisual extends JPanel {
        private static final long serialVersionUID = 1L;

        public DecorativeDeleteVisual() {
            setPreferredSize(new Dimension(320, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(
                    0, 0,
                    isDarkMode ? new Color(46, 24, 24) : new Color(255, 238, 238),
                    getWidth(), getHeight(),
                    isDarkMode ? new Color(32, 18, 18) : new Color(254, 224, 224)
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(DANGER_RED);
            g2.fillOval(getWidth() / 2 - 28, 24, 56, 56);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
            g2.drawString("🗑", getWidth() / 2 - 10, 60);

            g2.setColor(textMain);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
            String msg = "Permanent Registry Removal";
            int w = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, (getWidth() - w) / 2, 108);

            g2.setColor(textMuted);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String sub = "Deregisters account index & sorted tree";
            int sw = g2.getFontMetrics().stringWidth(sub);
            g2.drawString(sub, (getWidth() - sw) / 2, 128);

            g2.dispose();
        }
    }

    // rounded card with antialiased borders and subtle fill
    static class RoundedCard extends JPanel {
        private static final long serialVersionUID = 1L;
        private int cornerRadius;
        private Color bgColor;
        private Color borderColor;

        public RoundedCard(int radius, Color bg, Color border) {
            this.cornerRadius = radius;
            this.bgColor = bg;
            this.borderColor = border;
            setOpaque(false);
        }

        public void setColors(Color bg, Color border) {
            this.bgColor = bg;
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (bgColor != null) {
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }
            g2.dispose();
        }
    }

    // gradient card with soft pastel/tint transitions
    static class GradientCard extends JPanel {
        private static final long serialVersionUID = 1L;
        private int cornerRadius;
        private Color startColor;
        private Color endColor;
        private Color borderColor;

        public GradientCard(int radius, Color start, Color end, Color border) {
            this.cornerRadius = radius;
            this.startColor = start;
            this.endColor = end;
            this.borderColor = border;
            setOpaque(false);
        }

        public void setColors(Color start, Color end, Color border) {
            this.startColor = start;
            this.endColor = end;
            this.borderColor = border;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint gp = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
            }
            g2.dispose();
        }
    }

    // rounded modern button with hover and pressed tactile reaction
    static class ModernButton extends JButton {
        private static final long serialVersionUID = 1L;
        private Color normalColor;
        private Color hoverColor;
        private int radius;
        private boolean isHovered = false;
        private boolean isPressed = false;

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
                    isPressed = false;
                    repaint();
                }
                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = isPressed ? hoverColor.darker() : (isHovered ? hoverColor : normalColor);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // sidebar navigation button with active coral highlight and hover transition
    class NavButton extends JButton {
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
                g2.setColor(isDarkMode ? new Color(26, 28, 36) : new Color(36, 38, 46));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // custom text field with focus ring highlight and subtle placeholder support
    class ModernTextField extends JTextField {
        private static final long serialVersionUID = 1L;
        private boolean isFocused = false;
        private String placeholder = "";

        public ModernTextField(int columns) {
            this(columns, "");
        }

        public ModernTextField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder != null ? placeholder : "";
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBackground(inputBg);
            setForeground(textMain);
            setCaretColor(textMain);
            setPreferredSize(new Dimension(getPreferredSize().width, 42));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            updateBorder();

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    updateBorder();
                    repaint();
                }
                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    updateBorder();
                    repaint();
                }
            });
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder != null ? placeholder : "";
            repaint();
        }

        private void updateBorder() {
            Color borderColor = isFocused ? PRIMARY_CORAL : inputBorder;
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, isFocused ? 2 : 1, true),
                    BorderFactory.createEmptyBorder(isFocused ? 7 : 8, 14, isFocused ? 7 : 8, 14)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && !isFocused && placeholder != null && !placeholder.isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(textMuted);
                Insets insets = getInsets();
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, insets.left, y);
                g2.dispose();
            }
        }
    }

    // themed table cell renderer with alternating row and hover highlight
    class ThemedTableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final int[] hoveredRow;

        public ThemedTableCellRenderer(int[] hoveredRow) {
            this.hoveredRow = hoveredRow;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (isSelected) {
                label.setBackground(table.getSelectionBackground());
                label.setForeground(table.getSelectionForeground());
            } else if (hoveredRow != null && row == hoveredRow[0]) {
                label.setBackground(tableRowHover);
                label.setForeground(textMain);
            } else {
                label.setBackground(row % 2 == 0 ? cardBg : tableRowAlt);
                label.setForeground(textMain);
            }
            return label;
        }
    }

    // custom cell renderer for account type badges with colored pills
    class AccountTypeBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));

            if (!isSelected) {
                String val = value != null ? value.toString() : "";
                if ("Savings".equalsIgnoreCase(val)) {
                    label.setText("●  Savings");
                    label.setForeground(isDarkMode ? new Color(195, 165, 255) : new Color(118, 75, 185));
                } else {
                    label.setText("●  Current");
                    label.setForeground(isDarkMode ? new Color(255, 195, 95) : new Color(185, 120, 30));
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(row % 2 == 0 ? cardBg : tableRowAlt);
            }
            return label;
        }
    }

    // custom cell renderer for deposit/withdraw transaction types
    class TransactionTypeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!isSelected) {
                String val = value != null ? value.toString() : "";
                if (val.contains("Deposit") || "Deposit".equalsIgnoreCase(val)) {
                    label.setText("↓  Deposit");
                    label.setForeground(SUCCESS_GREEN);
                } else {
                    label.setText("↑  Withdrawal");
                    label.setForeground(DANGER_RED);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                label.setBackground(row % 2 == 0 ? cardBg : tableRowAlt);
            }
            return label;
        }
    }

    // custom cell renderer for transaction amounts with + / -
    class AmountColorRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setBorder(new EmptyBorder(0, 14, 0, 14));
            if (!isSelected && value != null) {
                double amt = 0;
                String valStr = value.toString().replace("+", "").replace("-", "").replace("Rs.", "").replace(",", "").trim();
                try {
                    amt = Double.parseDouble(valStr);
                } catch (Exception ignored) {}

                String type = "";
                for (int c = 0; c < table.getColumnCount(); c++) {
                    Object cellVal = table.getValueAt(row, c);
                    if (cellVal != null) {
                        String s = cellVal.toString().toLowerCase();
                        if (s.contains("deposit")) {
                            type = "Deposit";
                            break;
                        } else if (s.contains("withdraw")) {
                            type = "Withdrawal";
                            break;
                        }
                    }
                }

                if (type.equalsIgnoreCase("Deposit")) {
                    label.setText(String.format("+ Rs. %,.2f", amt));
                    label.setForeground(SUCCESS_GREEN);
                } else if (type.equalsIgnoreCase("Withdrawal")) {
                    label.setText(String.format("- Rs. %,.2f", amt));
                    label.setForeground(DANGER_RED);
                } else {
                    label.setText(String.format("Rs. %,.2f", amt));
                    label.setForeground(textMain);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBackground(row % 2 == 0 ? cardBg : tableRowAlt);
            }
            return label;
        }
    }

    // modern rounded horizontal progress bar drawn with standard Swing Graphics2D
    static class RoundedProgressBar extends JPanel {
        private static final long serialVersionUID = 1L;
        private double progress = 0.0;
        private final Color barColor;
        private final Color trackColor;

        public RoundedProgressBar(Color barColor, Color trackColor) {
            this.barColor = barColor;
            this.trackColor = trackColor;
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

    // decorative mini sparkline curve for statistic cards (pure Swing 2D graphics)
    static class SparklineVisual extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int type;
        private final boolean dark;

        public SparklineVisual(int type, boolean dark) {
            this.type = type;
            this.dark = dark;
            setPreferredSize(new Dimension(36, 16));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            Color strokeColor;
            if (type == 0) { // Balance
                strokeColor = new Color(255, 120, 94, 200);
            } else if (type == 1) { // Accounts
                strokeColor = new Color(90, 150, 230, 200);
            } else if (type == 2) { // Savings
                strokeColor = new Color(150, 110, 220, 200);
            } else { // Current
                strokeColor = new Color(230, 160, 50, 200);
            }

            GeneralPath path = new GeneralPath();
            path.moveTo(0, h - 3);

            if (type == 0) {
                path.curveTo(w * 0.3, h - 3, w * 0.6, h * 0.4, w - 2, 2);
            } else if (type == 1) {
                path.curveTo(w * 0.25, h * 0.7, w * 0.5, h * 0.2, w - 2, h * 0.35);
            } else if (type == 2) {
                path.curveTo(w * 0.35, h * 0.8, w * 0.7, h * 0.4, w - 2, 3);
            } else {
                path.curveTo(w * 0.3, h * 0.3, w * 0.65, h * 0.8, w - 2, 3);
            }

            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(strokeColor);
            g2.draw(path);

            g2.fillOval(w - 4, 1, 4, 4);
            g2.dispose();
        }
    }

    // login dialog for account access
    private void showLoginScreen() {

        JDialog loginDialog = new JDialog(this, "FinBank Login", true);
        loginDialog.setSize(420, 320);
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(30, 35, 30, 35));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(cardBg);

        JLabel title = new JLabel("Welcome to FinBank");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(textMain);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Login to access your account");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(textMuted);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        ModernTextField accountField = new ModernTextField(20);
        JPasswordField pinField = new JPasswordField();

        accountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        ModernButton loginButton = new ModernButton(
                "Login",
                PRIMARY_CORAL,
                PRIMARY_CORAL_HOVER,
                Color.WHITE,
                14
        );
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        loginButton.addActionListener(e -> {

            try {

                int accountNo = Integer.parseInt(accountField.getText().trim());
                String pin = new String(pinField.getPassword());

                if (!pin.matches("\\d{4}")) {
                    JOptionPane.showMessageDialog(
                            loginDialog,
                            "PIN must contain exactly 4 digits.",
                            "Invalid PIN",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                BankAccount account = bank.login(accountNo, pin);

                if (account == null) {
                    JOptionPane.showMessageDialog(
                            loginDialog,
                            "Invalid account number or PIN.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                    pinField.setText("");
                    return;
                }

                loggedInAccount = account;
                loginDialog.dispose();
                showScreen("Dashboard");

            } catch (NumberFormatException ex) {

                JOptionPane.showMessageDialog(
                        loginDialog,
                        "Enter a valid account number.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(subtitle);
        panel.add(Box.createRigidArea(new Dimension(0, 22)));

        panel.add(createFormLabel("Account Number"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(accountField);

        panel.add(Box.createRigidArea(new Dimension(0, 12)));

        panel.add(createFormLabel("4-Digit PIN"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(pinField);

        panel.add(Box.createRigidArea(new Dimension(0, 18)));
        panel.add(loginButton);

        loginDialog.add(panel);
        loginDialog.setVisible(true);
    }

    // main launcher
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankGUI gui = new BankGUI();
            gui.setVisible(true);
            gui.showLoginScreen();
        });
    }
}
