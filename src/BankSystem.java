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

    void showAll() {
        if (sortedAccounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        for (BankAccount account : sortedAccounts.values()) {
            System.out.println("----------------------------------------");
            System.out.println("Account No. : " + account.accountNo);
            System.out.println("Customer    : " + account.customer.name);
            System.out.println("Type        : " + account.type);
            System.out.printf("Balance     : Rs. %.2f%n", account.balance);
        }

        System.out.println("----------------------------------------");
    }

    int totalAccounts() {
        return accounts.size();
    }

    double totalBalance() {
        double total = 0;

        for (BankAccount account : accounts.values())
            total += account.balance;

        return total;
    }
}