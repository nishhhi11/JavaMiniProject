import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static BankSystem bank = new BankSystem();
    static int nextAccountNo = 1003;

    public static void main(String[] args) {

        addSampleAccounts();
        welcome();

        while (true) {
            mainMenu();

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> viewAccounts();
                case 3 -> searchAccount();
                case 4 -> checkBalance();
                case 5 -> deposit();
                case 6 -> withdraw();
                case 7 -> transferMenu();
                case 8 -> transactionHistory();
                case 9 -> beneficiaryMenu();
                case 10 -> updateCustomer();
                case 11 -> deleteAccount();
                case 12 -> bankSummary();
                case 0 -> exit();
                default -> System.out.println("✗ Invalid choice.");
            }
        }
    }

    static void welcome() {

        System.out.println();
        System.out.println("==================================================");
        System.out.println("              NEXA BANKING SYSTEM");
        System.out.println("           DIGITAL BANKING PLATFORM");
        System.out.println("==================================================");
        System.out.println("System Status : ONLINE");
        System.out.println("Accounts      : " + bank.totalAccounts());
        System.out.println("Transactions  : " + bank.totalTransactions());
        System.out.println("==================================================");
    }

    static void mainMenu() {

        System.out.println();
        System.out.println("==================================================");
        System.out.println("                    MAIN MENU");
        System.out.println("==================================================");

        System.out.println("  1. Create New Account");
        System.out.println("  2. View All Accounts");
        System.out.println("  3. Search Account");
        System.out.println("  4. Check Balance");
        System.out.println("  5. Deposit Money");
        System.out.println("  6. Withdraw Money");
        System.out.println("  7. Transfer Money");
        System.out.println("  8. Transaction History");
        System.out.println("  9. Beneficiary Management");
        System.out.println(" 10. Update Customer");
        System.out.println(" 11. Delete Account");
        System.out.println(" 12. Bank Summary");
        System.out.println("  0. Exit");

        System.out.println("--------------------------------------------------");
    }

    static void createAccount() {

        System.out.println("\n--------------- CREATE ACCOUNT ----------------");

        String name = readText("Customer name: ");
        String phone = readText("Phone number: ");

        System.out.println("\n1. Savings");
        System.out.println("2. Current");

        int choice = readInt("Choose account type: ");

        if (choice != 1 && choice != 2) {
            System.out.println("✗ Invalid account type.");
            return;
        }

        String type = choice == 1 ? "Savings" : "Current";

        double amount = readDouble("Initial deposit: ");

        if (amount < 0) {
            System.out.println("✗ Invalid amount.");
            return;
        }

        Customer customer =
                new Customer(nextAccountNo, name, phone);

        BankAccount account =
                new BankAccount(
                        nextAccountNo,
                        customer,
                        type,
                        amount
                );

        bank.addAccount(account);

        System.out.println("\n==================================================");
        System.out.println("          ✓ ACCOUNT CREATED SUCCESSFULLY");
        System.out.println("==================================================");
        System.out.println("Account Number : " + nextAccountNo);
        System.out.println("Customer       : " + name);
        System.out.println("Account Type   : " + type);
        System.out.printf(
                "Balance        : Rs. %.2f%n",
                amount
        );
        System.out.println("==================================================");

        nextAccountNo++;
    }

    static void viewAccounts() {

        System.out.println("\n---------------- ALL ACCOUNTS ----------------");

        bank.showAll();
    }

    static void searchAccount() {

        System.out.println("\n---------------- SEARCH ACCOUNT ----------------");

        System.out.println("1. Account Number");
        System.out.println("2. Customer Name");

        int choice = readInt("Search by: ");

        BankAccount account = null;

        if (choice == 1) {

            int number =
                    readInt("Account number: ");

            account = bank.search(number);

        } else if (choice == 2) {

            String name =
                    readText("Customer name: ");

            account = bank.searchByName(name);

        } else {

            System.out.println("✗ Invalid option.");
            return;
        }

        if (account == null) {

            System.out.println("✗ Account not found.");
            return;
        }

        System.out.println("\n==================================================");
        System.out.println("                ACCOUNT DASHBOARD");
        System.out.println("==================================================");

        account.display();

        System.out.println("==================================================");
    }

    static void checkBalance() {

        int number =
                readInt("\nAccount number: ");

        BankAccount account =
                bank.search(number);

        if (account == null) {

            System.out.println("✗ Account not found.");
            return;
        }

        System.out.println("\n---------------- BALANCE ----------------");

        account.showBalance();
    }

    static void deposit() {

        System.out.println("\n---------------- DEPOSIT ----------------");

        int number =
                readInt("Account number: ");

        BankAccount account =
                bank.search(number);

        if (account == null) {

            System.out.println("✗ Account not found.");
            return;
        }

        double amount =
                readDouble("Deposit amount: ");

        double oldBalance =
                account.balance;

        String id =
                bank.transactionId();

        if (account.deposit(amount, id)) {

            System.out.println("\n==================================================");
            System.out.println("             ✓ DEPOSIT SUCCESSFUL");
            System.out.println("==================================================");

            System.out.println("Transaction ID : " + id);
            System.out.println("Account No.    : " + number);

            System.out.printf(
                    "Amount         : Rs. %.2f%n",
                    amount
            );

            System.out.printf(
                    "Old Balance    : Rs. %.2f%n",
                    oldBalance
            );

            System.out.printf(
                    "New Balance    : Rs. %.2f%n",
                    account.balance
            );

            System.out.println("Status         : SUCCESS");

            System.out.println("==================================================");

        } else {

            System.out.println("✗ Invalid deposit amount.");
        }
    }

    static void withdraw() {

        System.out.println("\n---------------- WITHDRAW ----------------");

        int number =
                readInt("Account number: ");

        BankAccount account =
                bank.search(number);

        if (account == null) {

            System.out.println("✗ Account not found.");
            return;
        }

        double amount =
                readDouble("Withdrawal amount: ");

        if (amount <= 0) {

            System.out.println(
                    "✗ Amount must be greater than zero."
            );

            return;
        }

        if (amount > account.balance) {

            System.out.println("\n✗ TRANSACTION DECLINED");
            System.out.println("Reason: Insufficient balance.");

            System.out.printf(
                    "Available : Rs. %.2f%n",
                    account.balance
            );

            System.out.printf(
                    "Requested : Rs. %.2f%n",
                    amount
            );

            return;
        }

        double oldBalance =
                account.balance;

        String id =
                bank.transactionId();

        account.withdraw(amount, id);

        System.out.println("\n==================================================");
        System.out.println("            ✓ WITHDRAWAL SUCCESSFUL");
        System.out.println("==================================================");

        System.out.println("Transaction ID : " + id);
        System.out.println("Account No.    : " + number);

        System.out.printf(
                "Amount         : Rs. %.2f%n",
                amount
        );

        System.out.printf(
                "Old Balance    : Rs. %.2f%n",
                oldBalance
        );

        System.out.printf(
                "New Balance    : Rs. %.2f%n",
                account.balance
        );

        System.out.println("Status         : SUCCESS");

        System.out.println("==================================================");
    }

    static void transferMenu() {

        System.out.println("\n---------------- TRANSFER MONEY ----------------");

        System.out.println("1. NEXA Bank Account");
        System.out.println("2. Other Bank Account");

        int choice =
                readInt("Transfer type: ");

        if (choice == 1)
            internalTransfer();

        else if (choice == 2)
            externalTransfer();

        else
            System.out.println("✗ Invalid option.");
    }

    static void internalTransfer() {

        int from =
                readInt("\nFrom account: ");

        int to =
                readInt("To account: ");

        if (from == to) {

            System.out.println(
                    "✗ Cannot transfer to same account."
            );

            return;
        }

        double amount =
                readDouble("Transfer amount: ");

        BankAccount sender =
                bank.search(from);

        BankAccount receiver =
                bank.search(to);

        if (sender == null || receiver == null) {

            System.out.println(
                    "✗ Account not found."
            );

            return;
        }

        if (amount > sender.balance) {

            System.out.println(
                    "✗ Insufficient balance."
            );

            return;
        }

        if (bank.transfer(from, to, amount)) {

            System.out.println("\n==================================================");
            System.out.println("             ✓ TRANSFER SUCCESSFUL");
            System.out.println("==================================================");

            System.out.println("From Account : " + from);
            System.out.println("To Account   : " + to);

            System.out.printf(
                    "Amount       : Rs. %.2f%n",
                    amount
            );

            System.out.println("Status       : SUCCESS");

            System.out.println("==================================================");
        }
    }

    static void externalTransfer() {

        System.out.println(
                "\n----------- EXTERNAL BANK TRANSFER -----------"
        );

        int from =
                readInt("Your account number: ");

        BankAccount sender =
                bank.search(from);

        if (sender == null) {

            System.out.println(
                    "✗ Account not found."
            );

            return;
        }

        String name =
                readText("Beneficiary name: ");

        String bankName =
                readText("Bank name: ");

        String accountNumber =
                readText("Bank account number: ");

        String ifsc =
                readText("IFSC code: ");

        double amount =
                readDouble("Transfer amount: ");

        Beneficiary beneficiary =
                new Beneficiary(
                        name,
                        bankName,
                        accountNumber,
                        ifsc
                );

        String id =
                bank.transactionId();

        if (bank.externalTransfer(
                from,
                beneficiary,
                amount)) {

            System.out.println("\n==================================================");
            System.out.println("          ✓ EXTERNAL TRANSFER SUCCESSFUL");
            System.out.println("==================================================");

            System.out.println(
                    "Transaction ID : " + id
            );

            System.out.println(
                    "From Account   : " + from
            );

            System.out.println(
                    "Beneficiary    : " + name
            );

            System.out.println(
                    "Bank           : " + bankName
            );

            System.out.println(
                    "Account        : " + accountNumber
            );

            System.out.println(
                    "IFSC           : " + ifsc
            );

            System.out.printf(
                    "Amount         : Rs. %.2f%n",
                    amount
            );

            System.out.println(
                    "Status         : SUCCESS"
            );

            System.out.println("==================================================");

        } else {

            System.out.println(
                    "✗ External transfer failed."
            );
        }
    }

    static void transactionHistory() {

        int number =
                readInt("\nAccount number: ");

        BankAccount account =
                bank.search(number);

        if (account == null) {

            System.out.println(
                    "✗ Account not found."
            );

            return;
        }

        System.out.println(
                "\n=================================================="
        );

        System.out.println(
                "                TRANSACTION HISTORY"
        );

        System.out.println(
                "=================================================="
        );

        account.showTransactions();
    }

    static void beneficiaryMenu() {

        while (true) {

            System.out.println(
                    "\n------------- BENEFICIARY MANAGEMENT -------------"
            );

            System.out.println("1. Add Beneficiary");
            System.out.println("2. View Beneficiaries");
            System.out.println("0. Back");

            int choice =
                    readInt("Choose option: ");

            if (choice == 1)
                addBeneficiary();

            else if (choice == 2)
                bank.showBeneficiaries();

            else if (choice == 0)
                return;

            else
                System.out.println(
                        "✗ Invalid option."
                );
        }
    }

    static void addBeneficiary() {

        String name =
                readText("Beneficiary name: ");

        String bankName =
                readText("Bank name: ");

        String account =
                readText("Account number: ");

        String ifsc =
                readText("IFSC code: ");

        Beneficiary beneficiary =
                new Beneficiary(
                        name,
                        bankName,
                        account,
                        ifsc
                );

        bank.addBeneficiary(beneficiary);

        System.out.println(
                "✓ Beneficiary added successfully."
        );
    }

    static void updateCustomer() {

        int number =
                readInt("\nAccount number: ");

        BankAccount account =
                bank.search(number);

        if (account == null) {

            System.out.println(
                    "✗ Account not found."
            );

            return;
        }

        String name =
                readText("New name: ");

        String phone =
                readText("New phone: ");

        bank.update(number, name, phone);

        System.out.println(
                "✓ Customer details updated."
        );
    }

    static void deleteAccount() {

        int number =
                readInt("\nAccount number: ");

        if (bank.search(number) == null) {

            System.out.println(
                    "✗ Account not found."
            );

            return;
        }

        String confirm =
                readText(
                        "Confirm deletion (yes/no): "
                );

        if (confirm.equalsIgnoreCase("yes")) {

            bank.delete(number);

            System.out.println(
                    "✓ Account deleted successfully."
            );

        } else {

            System.out.println(
                    "Deletion cancelled."
            );
        }
    }

    static void bankSummary() {

        System.out.println(
                "\n=================================================="
        );

        System.out.println(
                "                 NEXA BANK SUMMARY"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "Total Accounts     : "
                        + bank.totalAccounts()
        );

        System.out.println(
                "Savings Accounts   : "
                        + bank.countType("Savings")
        );

        System.out.println(
                "Current Accounts   : "
                        + bank.countType("Current")
        );

        System.out.println(
                "Total Transactions : "
                        + bank.totalTransactions()
        );

        System.out.printf(
                "Total Bank Balance : Rs. %.2f%n",
                bank.totalBalance()
        );

        System.out.println(
                "=================================================="
        );
    }

    static void addSampleAccounts() {

        Customer c1 =
                new Customer(
                        1001,
                        "Nishi",
                        "9876501234"
                );

        Customer c2 =
                new Customer(
                        1002,
                        "Riya",
                        "9876543210"
                );

        BankAccount a1 =
                new BankAccount(
                        1001,
                        c1,
                        "Savings",
                        50000
                );

        BankAccount a2 =
                new BankAccount(
                        1002,
                        c2,
                        "Current",
                        100000
                );

        bank.addAccount(a1);
        bank.addAccount(a2);
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

    static void exit() {

        System.out.println();
        System.out.println(
                "=================================================="
        );

        System.out.println(
                "        THANK YOU FOR BANKING WITH NEXA"
        );

        System.out.println(
                "=================================================="
        );

        sc.close();

        System.exit(0);
    }
}