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

        return account.deposit(amount, transactionId());
    }

    boolean withdraw(int accountNo, double amount) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        return account.withdraw(amount, transactionId());
    }

    boolean transfer(int from, int to, double amount) {

        BankAccount sender = search(from);
        BankAccount receiver = search(to);

        if (sender == null || receiver == null)
            return false;

        if (!sender.canWithdraw(amount))
            return false;

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

        if (!sender.canWithdraw(amount))
            return false;

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
}