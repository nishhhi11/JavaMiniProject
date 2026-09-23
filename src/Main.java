import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static BankSystem bank = new BankSystem();
    static int nextAccountNo = 1003;

    public static void main(String[] args) {

        addSampleAccounts();
        welcome();

        while (true) {
            showMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> viewAccounts();
                case 3 -> searchAccount();
                case 4 -> checkBalance();
                case 5 -> deposit();
                case 6 -> withdraw();
                case 7 -> transfer();
                case 8 -> transactions();
                case 9 -> updateCustomer();
                case 10 -> deleteAccount();
                case 11 -> summary();
                case 0 -> exit();
                default -> System.out.println("\n✗ Invalid choice.");
            }
        }
    }

    static void welcome() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("            NEXA BANKING SYSTEM");
        System.out.println("          SECURE • SIMPLE • SMART");
        System.out.println("==================================================");
        System.out.println("System Status : ONLINE");
        System.out.println("Accounts      : " + bank.totalAccounts());
        System.out.println("Transactions  : " + bank.totalTransactions());
        System.out.println("==================================================");
    }

    static void showMenu() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("                 MAIN MENU");
        System.out.println("==================================================");
        System.out.println("  1. Create New Account");
        System.out.println("  2. View All Accounts");
        System.out.println("  3. Search Account");
        System.out.println("  4. Check Balance");
        System.out.println("  5. Deposit Money");
        System.out.println("  6. Withdraw Money");
        System.out.println("  7. Transfer Money");
        System.out.println("  8. Transaction History");
        System.out.println("  9. Update Customer");
        System.out.println(" 10. Delete Account");
        System.out.println(" 11. Bank Summary");
        System.out.println("  0. Exit");
        System.out.println("--------------------------------------------------");
    }

    static void createAccount() {
        System.out.println("\n--------------- CREATE ACCOUNT ----------------");

        String name = readText("Customer name: ");
        String phone = readText("Phone number: ");

        System.out.println("\nAccount Types:");
        System.out.println("1. Savings");
        System.out.println("2. Current");

        int typeChoice = readInt("Choose account type: ");

        if (typeChoice != 1 && typeChoice != 2) {
            System.out.println("✗ Invalid account type.");
            return;
        }

        String type = typeChoice == 1 ? "Savings" : "Current";

        double balance = readDouble("Initial deposit: ");

        if (balance < 0) {
            System.out.println("✗ Initial deposit cannot be negative.");
            return;
        }

        Customer customer =
                new Customer(nextAccountNo, name, phone);

        BankAccount account =
                new BankAccount(nextAccountNo, customer, type, balance);

        bank.addAccount(account);

        System.out.println("\n==================================================");
        System.out.println("          ✓ ACCOUNT CREATED SUCCESSFULLY");
        System.out.println("==================================================");
        System.out.println("Account Number : " + nextAccountNo);
        System.out.println("Customer       : " + name);
        System.out.println("Account Type   : " + type);
        System.out.printf("Balance        : Rs. %.2f%n", balance);
        System.out.println("==================================================");

        nextAccountNo++;
    }

    static void viewAccounts() {
        System.out.println("\n---------------- ALL ACCOUNTS ----------------");
        bank.showAll();
    }

    static void searchAccount() {
        System.out.println("\n---------------- SEARCH ACCOUNT ----------------");
        System.out.println("1. Search by Account Number");
        System.out.println("2. Search by Customer Name");

        int choice = readInt("Choose option: ");
        BankAccount account = null;

        if (choice == 1) {
            int no = readInt("Enter account number: ");
            account = bank.search(no);
        } else if (choice == 2) {
            String name = readText("Enter customer name: ");
            account = bank.searchByName(name);
        } else {
            System.out.println("✗ Invalid option.");
            return;
        }

        if (account == null) {
            System.out.println("\n✗ Account not found.");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("                 ACCOUNT DASHBOARD");
        System.out.println("==================================================");
        account.display();
        System.out.println("==================================================");
    }

    static void checkBalance() {
        int no = readInt("\nEnter account number: ");
        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        System.out.println("\n---------------- BALANCE ----------------");
        account.showBalance();
    }

    static void deposit() {
        System.out.println("\n---------------- DEPOSIT ----------------");

        int no = readInt("Enter account number: ");

        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        double amount = readDouble("Enter deposit amount: ");

        double oldBalance = account.balance;

        if (bank.deposit(no, amount)) {
            System.out.println("\n==================================================");
            System.out.println("           ✓ TRANSACTION SUCCESSFUL");
            System.out.println("==================================================");
            System.out.println("Transaction : Deposit");
            System.out.println("Account No. : " + no);
            System.out.printf("Amount      : Rs. %.2f%n", amount);
            System.out.printf("Old Balance : Rs. %.2f%n", oldBalance);
            System.out.printf("New Balance : Rs. %.2f%n", account.balance);
            System.out.println("==================================================");
        } else {
            System.out.println("✗ Invalid deposit amount.");
        }
    }

    static void withdraw() {
        System.out.println("\n---------------- WITHDRAW ----------------");

        int no = readInt("Enter account number: ");

        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        double amount = readDouble("Enter withdrawal amount: ");

        if (amount <= 0) {
            System.out.println("✗ Amount must be greater than zero.");
            return;
        }

        if (amount > account.balance) {
            System.out.println("\n✗ Insufficient balance.");
            System.out.printf("Available Balance : Rs. %.2f%n",
                    account.balance);
            System.out.printf("Requested Amount   : Rs. %.2f%n",
                    amount);
            return;
        }

        double oldBalance = account.balance;

        bank.withdraw(no, amount);

        System.out.println("\n==================================================");
        System.out.println("           ✓ TRANSACTION SUCCESSFUL");
        System.out.println("==================================================");
        System.out.println("Transaction : Withdrawal");
        System.out.println("Account No. : " + no);
        System.out.printf("Amount      : Rs. %.2f%n", amount);
        System.out.printf("Old Balance : Rs. %.2f%n", oldBalance);
        System.out.printf("New Balance : Rs. %.2f%n", account.balance);
        System.out.println("==================================================");
    }

    static void transfer() {
        System.out.println("\n---------------- TRANSFER MONEY ----------------");

        int from = readInt("From account : ");
        int to = readInt("To account   : ");

        if (from == to) {
            System.out.println("✗ Cannot transfer to the same account.");
            return;
        }

        BankAccount sender = bank.search(from);
        BankAccount receiver = bank.search(to);

        if (sender == null || receiver == null) {
            System.out.println("✗ One or both accounts not found.");
            return;
        }

        double amount = readDouble("Transfer amount: ");

        if (amount <= 0) {
            System.out.println("✗ Amount must be greater than zero.");
            return;
        }

        if (amount > sender.balance) {
            System.out.println("✗ Insufficient balance.");
            return;
        }

        if (bank.transfer(from, to, amount)) {
            System.out.println("\n==================================================");
            System.out.println("             ✓ TRANSFER SUCCESSFUL");
            System.out.println("==================================================");
            System.out.println("From Account : " + from);
            System.out.println("To Account   : " + to);
            System.out.printf("Amount       : Rs. %.2f%n", amount);
            System.out.printf("New Balance  : Rs. %.2f%n",
                    sender.balance);
            System.out.println("==================================================");
        }
    }

    static void transactions() {
        int no = readInt("\nEnter account number: ");
        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("              TRANSACTION HISTORY");
        System.out.println("==================================================");
        System.out.println("Account  : " + account.accountNo);
        System.out.println("Customer : " + account.customer.name);
        System.out.println();

        account.showTransactions();
    }

    static void updateCustomer() {
        System.out.println("\n---------------- UPDATE CUSTOMER ----------------");

        int no = readInt("Enter account number: ");

        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        String name = readText("New customer name: ");
        String phone = readText("New phone number: ");

        bank.update(no, name, phone);

        System.out.println("✓ Customer details updated successfully.");
    }

    static void deleteAccount() {
        System.out.println("\n---------------- DELETE ACCOUNT ----------------");

        int no = readInt("Enter account number: ");

        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("✗ Account not found.");
            return;
        }

        String confirm = readText(
                "Are you sure you want to delete? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            bank.delete(no);
            System.out.println("✓ Account deleted successfully.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    static void summary() {
        System.out.println("\n==================================================");
        System.out.println("                  BANK SUMMARY");
        System.out.println("==================================================");

        System.out.println("Total Accounts     : " + bank.totalAccounts());
        System.out.println("Savings Accounts   : "
                + bank.countType("Savings"));
        System.out.println("Current Accounts   : "
                + bank.countType("Current"));
        System.out.println("Total Transactions : "
                + bank.totalTransactions());

        System.out.printf("Total Bank Balance : Rs. %.2f%n",
                bank.totalBalance());

        System.out.println("==================================================");
    }

    static void addSampleAccounts() {

        Customer c1 =
                new Customer(1001, "Nishi", "9876501234");

        Customer c2 =
                new Customer(1002, "Riya", "9876543210");

        BankAccount a1 =
                new BankAccount(1001, c1, "Savings", 5000);

        BankAccount a2 =
                new BankAccount(1002, c2, "Current", 10000);

        bank.addAccount(a1);
        bank.addAccount(a2);
    }

    static void exit() {
        System.out.println("\n==================================================");
        System.out.println("       Thank you for using NEXA BANKING");
        System.out.println("==================================================");
        sc.close();
        System.exit(0);
    }

    static int readInt(String message) {
        System.out.print(message);
        return sc.nextInt();
    }

    static double readDouble(String message) {
        System.out.print(message);
        return sc.nextDouble();
    }

    static String readText(String message) {
        sc.nextLine();
        System.out.print(message);
        return sc.nextLine();
    }
}