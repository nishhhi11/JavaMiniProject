import java.util.HashMap;
import java.util.TreeMap;

public class BankSystem {

    HashMap<Integer, BankAccount> accounts = new HashMap<>();
    TreeMap<Integer, BankAccount> sortedAccounts = new TreeMap<>();

    String[] accountTypes = {"Savings", "Current"};

    void addAccount(BankAccount account) {
        accounts.put(account.accountNo, account);
        sortedAccounts.put(account.accountNo, account);
    }

    BankAccount search(int accountNo) {
        return accounts.get(accountNo);
    }

    boolean deposit(int accountNo, double amount) {
        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        return account.deposit(amount);
    }

    boolean withdraw(int accountNo, double amount) {
        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        return account.withdraw(amount);
    }

    boolean transfer(int from, int to, double amount) {
        BankAccount sender = search(from);
        BankAccount receiver = search(to);

        if (sender == null || receiver == null)
            return false;

        if (!sender.canWithdraw(amount))
            return false;

        sender.withdraw(amount);
        receiver.deposit(amount);

        sender.transactions.add(
                new Transaction("Transfer Sent", amount));

        receiver.transactions.add(
                new Transaction("Transfer Received", amount));

        return true;
    }

    boolean update(int accountNo, String name, String phone) {
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

        System.out.printf("%-12s %-18s %-12s %s%n",
                "Account No.", "Customer", "Type", "Balance");

        System.out.println("--------------------------------------------------------");

        for (BankAccount account : sortedAccounts.values()) {
            System.out.printf("%-12d %-18s %-12s Rs. %.2f%n",
                    account.accountNo,
                    account.customer.name,
                    account.type,
                    account.balance);
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("Total Accounts: " + totalAccounts());
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