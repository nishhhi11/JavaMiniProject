import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedList;

public class BankSystem {

    HashMap<Integer, BankAccount> accounts = new HashMap<>();
    TreeMap<Integer, BankAccount> sortedAccounts = new TreeMap<>();

    LinkedList<Beneficiary> beneficiaries = new LinkedList<>();

    String[] accountTypes = {"Savings", "Current"};

    int transactionNumber = 10001;

    void addAccount(BankAccount account) {

        accounts.put(account.accountNo, account);
        sortedAccounts.put(account.accountNo, account);
    }

    BankAccount search(int accountNo) {

        return accounts.get(accountNo);
    }

    String transactionId() {

        return "TXN" + transactionNumber++;
    }

    boolean deposit(int accountNo, double amount) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        if (amount <= 0) {

            account.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Deposit",
                            amount,
                            "External",
                            String.valueOf(accountNo),
                            "FAILED",
                            "Invalid amount"
                    )
            );

            return false;
        }

        return account.deposit(amount, transactionId());
    }

    boolean withdraw(int accountNo, double amount) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        if (amount <= 0) {

            account.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Withdrawal",
                            amount,
                            String.valueOf(accountNo),
                            "External",
                            "FAILED",
                            "Invalid amount"
                    )
            );

            return false;
        }

        if (amount > account.balance) {

            account.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Withdrawal",
                            amount,
                            String.valueOf(accountNo),
                            "External",
                            "FAILED",
                            "Insufficient balance"
                    )
            );

            return false;
        }

        return account.withdraw(amount, transactionId());
    }

    boolean transfer(int from, int to, double amount) {

        BankAccount sender = search(from);
        BankAccount receiver = search(to);

        if (sender == null || receiver == null)
            return false;

        if (from == to) {

            sender.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Transfer",
                            amount,
                            String.valueOf(from),
                            String.valueOf(to),
                            "FAILED",
                            "Cannot transfer to same account"
                    )
            );

            return false;
        }

        if (amount <= 0) {

            sender.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Transfer",
                            amount,
                            String.valueOf(from),
                            String.valueOf(to),
                            "FAILED",
                            "Invalid amount"
                    )
            );

            return false;
        }

        if (!sender.canWithdraw(amount)) {

            sender.transactions.add(
                    new Transaction(
                            transactionId(),
                            "Transfer",
                            amount,
                            String.valueOf(from),
                            String.valueOf(to),
                            "FAILED",
                            "Insufficient balance"
                    )
            );

            return false;
        }

        String id = transactionId();

        sender.balance -= amount;
        receiver.balance += amount;

        sender.transactions.add(
                new Transaction(
                        id,
                        "Transfer Sent",
                        amount,
                        String.valueOf(from),
                        String.valueOf(to),
                        "SUCCESS"
                )
        );

        receiver.transactions.add(
                new Transaction(
                        id,
                        "Transfer Received",
                        amount,
                        String.valueOf(from),
                        String.valueOf(to),
                        "SUCCESS"
                )
        );

        return true;
    }

    boolean externalTransfer(
            int from,
            Beneficiary beneficiary,
            double amount) {

        BankAccount sender = search(from);

        if (sender == null)
            return false;

        if (amount <= 0) {

            sender.transactions.add(
                    new Transaction(
                            transactionId(),
                            "External Transfer",
                            amount,
                            String.valueOf(from),
                            beneficiary.bankName,
                            "FAILED",
                            "Invalid amount"
                    )
            );

            return false;
        }

        if (!sender.canWithdraw(amount)) {

            sender.transactions.add(
                    new Transaction(
                            transactionId(),
                            "External Transfer",
                            amount,
                            String.valueOf(from),
                            beneficiary.bankName,
                            "FAILED",
                            "Insufficient balance"
                    )
            );

            return false;
        }

        String id = transactionId();

        sender.balance -= amount;

        sender.transactions.add(
                new Transaction(
                        id,
                        "External Transfer",
                        amount,
                        String.valueOf(from),
                        beneficiary.bankName
                                + " - "
                                + beneficiary.accountNumber,
                        "SUCCESS"
                )
        );

        return true;
    }

    void addBeneficiary(Beneficiary beneficiary) {

        beneficiaries.add(beneficiary);
    }

    void showBeneficiaries() {

        if (beneficiaries.isEmpty()) {
            System.out.println("No beneficiaries found.");
            return;
        }

        int number = 1;

        for (Beneficiary beneficiary : beneficiaries) {

            System.out.println("\nBeneficiary " + number);
            System.out.println("-------------------------");

            beneficiary.display();

            number++;
        }
    }

    BankAccount searchByName(String name) {

        for (BankAccount account : accounts.values()) {

            if (account.customer.name.equalsIgnoreCase(name))
                return account;
        }

        return null;
    }

    void showAll() {

        if (sortedAccounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        System.out.printf(
                "%-12s %-18s %-12s %s%n",
                "Account No.",
                "Customer",
                "Type",
                "Balance"
        );

        System.out.println(
                "--------------------------------------------------------"
        );

        for (BankAccount account : sortedAccounts.values()) {

            System.out.printf(
                    "%-12d %-18s %-12s Rs. %.2f%n",
                    account.accountNo,
                    account.customer.name,
                    account.type,
                    account.balance
            );
        }

        System.out.println(
                "--------------------------------------------------------"
        );

        System.out.println(
                "Total Accounts: " + totalAccounts()
        );
    }

    boolean update(
            int accountNo,
            String name,
            String phone) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        account.customer.update(name, phone);

        return true;
    }

    boolean delete(int accountNo) {

        if (!accounts.containsKey(accountNo))
            return false;

        accounts.remove(accountNo);
        sortedAccounts.remove(accountNo);

        return true;
    }

    int totalAccounts() {

        return accounts.size();
    }

    int totalTransactions() {

        int total = 0;

        for (BankAccount account : accounts.values())
            total += account.transactions.size();

        return total;
    }

    double totalBalance() {

        double total = 0;

        for (BankAccount account : accounts.values())
            total += account.balance;

        return total;
    }

    int countType(String type) {

        int count = 0;

        for (BankAccount account : accounts.values()) {

            if (account.type.equalsIgnoreCase(type))
                count++;
        }

        return count;
    }

    Transaction searchTransaction(String id) {

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction : account.transactions) {

                if (transaction.id.equalsIgnoreCase(id))
                    return transaction;
            }
        }

        return null;
    }

    int successfulTransactions() {

        int count = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction : account.transactions) {

                if (transaction.status.equals("SUCCESS"))
                    count++;
            }
        }

        return count;
    }

    int failedTransactions() {

        int count = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction : account.transactions) {

                if (transaction.status.equals("FAILED"))
                    count++;
            }
        }

        return count;
    }

    double totalTransactionVolume() {

        double total = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction : account.transactions) {

                if (transaction.status.equals("SUCCESS"))
                    total += transaction.amount;
            }
        }

        return total;
    }

    void showBankStatistics() {

        System.out.println("\n========== BANK STATISTICS ==========");

        System.out.println(
                "Total Accounts       : " + totalAccounts()
        );

        System.out.printf(
                "Total Balance        : Rs. %.2f%n",
                totalBalance()
        );

        System.out.println(
                "Total Transactions   : " + totalTransactions()
        );

        System.out.println(
                "Successful           : " + successfulTransactions()
        );

        System.out.println(
                "Failed               : " + failedTransactions()
        );

        System.out.printf(
                "Transaction Volume   : Rs. %.2f%n",
                totalTransactionVolume()
        );

        System.out.println(
                "Savings Accounts     : " + countType("Savings")
        );

        System.out.println(
                "Current Accounts     : " + countType("Current")
        );

        System.out.println(
                "===================================="
        );
    }

    void showStatement(int accountNo) {

        BankAccount account = search(accountNo);

        if (account == null) {

            System.out.println("Account not found.");
            return;
        }

        System.out.println("\n========================================");
        System.out.println("          FINBANK STATEMENT");
        System.out.println("========================================");

        System.out.println(
                "Account No. : " + account.accountNo
        );

        System.out.println(
                "Customer    : " + account.customer.name
        );

        System.out.println(
                "Phone       : " + account.customer.phone
        );

        System.out.println(
                "Account Type: " + account.type
        );

        System.out.println("----------------------------------------");

        if (account.transactions.isEmpty()) {

            System.out.println("No transactions found.");

        } else {

            for (Transaction transaction :
                    account.transactions) {

                System.out.println(transaction);
            }
        }

        System.out.println("----------------------------------------");

        System.out.printf(
                "Current Balance : Rs. %.2f%n",
                account.balance
        );

        System.out.println("========================================");
    }
}