import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static BankSystem bank = new BankSystem();
    static int nextAccountNo = 1001;

    public static void main(String[] args) {

        addSampleAccounts();

        while (true) {
            showMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> viewAccounts();
                case 3 -> searchAccount();
                case 4 -> deposit();
                case 5 -> withdraw();
                case 6 -> transactions();
                case 7 -> updateCustomer();
                case 8 -> deleteAccount();
                case 9 -> summary();
                case 0 -> {
                    System.out.println("\nThank you for using the Bank System!");
                    return;
                }
                default -> System.out.println("\nInvalid choice!");
            }
        }
    }

    static void showMenu() {
        System.out.println("\n==================================================");
        System.out.println("          BANK ACCOUNT MANAGEMENT SYSTEM");
        System.out.println("==================================================");
        System.out.println("  1. Create New Account");
        System.out.println("  2. View All Accounts");
        System.out.println("  3. Search Account");
        System.out.println("  4. Deposit Money");
        System.out.println("  5. Withdraw Money");
        System.out.println("  6. View Transactions");
        System.out.println("  7. Update Customer");
        System.out.println("  8. Delete Account");
        System.out.println("  9. Bank Summary");
        System.out.println("  0. Exit");
        System.out.println("--------------------------------------------------");
    }

    static void createAccount() {
        System.out.println("\n--------------- CREATE ACCOUNT ----------------");

        String name = readText("Customer name: ");
        String phone = readText("Phone number: ");

        System.out.println("1. Savings");
        System.out.println("2. Current");

        int typeChoice = readInt("Choose account type: ");
        String type = typeChoice == 1 ? "Savings" : "Current";

        double balance = readDouble("Initial deposit: ");

        Customer customer =
                new Customer(nextAccountNo, name, phone);

        BankAccount account =
                new BankAccount(nextAccountNo, customer, type, balance);

        bank.addAccount(account);

        System.out.println("\n✓ Account created successfully!");
        System.out.println("Account Number : " + nextAccountNo);

        nextAccountNo++;
    }

    static void viewAccounts() {
        System.out.println("\n---------------- ALL ACCOUNTS ----------------");
        bank.showAll();
    }

    static void searchAccount() {
        int no = readInt("\nEnter account number: ");
        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.println("\n---------------- ACCOUNT DETAILS ----------------");
        account.display();
    }

    static void deposit() {
        int no = readInt("\nEnter account number: ");
        double amount = readDouble("Enter deposit amount: ");

        if (bank.deposit(no, amount))
            System.out.println("✓ Deposit successful.");
        else
            System.out.println("✗ Deposit failed.");
    }

    static void withdraw() {
        int no = readInt("\nEnter account number: ");
        double amount = readDouble("Enter withdrawal amount: ");

        if (bank.withdraw(no, amount))
            System.out.println("✓ Withdrawal successful.");
        else
            System.out.println("✗ Withdrawal failed.");
    }

    static void transactions() {
        int no = readInt("\nEnter account number: ");
        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.println("\n--------------- TRANSACTION HISTORY ---------------");
        System.out.println("Account : " + account.accountNo);
        System.out.println("Customer: " + account.customer.name);
        System.out.println();

        account.showTransactions();
    }

    static void updateCustomer() {
        int no = readInt("\nEnter account number: ");
        BankAccount account = bank.search(no);

        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        String name = readText("New customer name: ");
        String phone = readText("New phone number: ");

        bank.update(no, name, phone);

        System.out.println("✓ Customer details updated.");
    }

    static void deleteAccount() {
        int no = readInt("\nEnter account number: ");

        if (bank.delete(no))
            System.out.println("✓ Account deleted successfully.");
        else
            System.out.println("✗ Account not found.");
    }

    static void summary() {
        System.out.println("\n---------------- BANK SUMMARY ----------------");
        System.out.println("Total Accounts : " + bank.totalAccounts());
        System.out.printf("Total Balance   : Rs. %.2f%n",
                bank.totalBalance());
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

        nextAccountNo = 1003;
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