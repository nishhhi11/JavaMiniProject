import java.util.HashMap;
import java.util.TreeMap;

public class BankSystem {
    HashMap<Integer, BankAccount> accounts = new HashMap<>();
    TreeMap<Integer, BankAccount> sortedAccounts = new TreeMap<>();
    String[] accountTypes = {"Savings", "Current"};

    void addAccount(BankAccount a) {
        accounts.put(a.accountNo, a);
        sortedAccounts.put(a.accountNo, a);
    }

    void search(int accountNo) {
        BankAccount a = accounts.get(accountNo);
        if (a != null) a.display();
        else System.out.println("Account not found");
    }

    void deposit(int accountNo, double amount) {
        BankAccount a = accounts.get(accountNo);
        if (a != null) a.deposit(amount);
    }

    void withdraw(int accountNo, double amount) {
        BankAccount a = accounts.get(accountNo);
        if (a != null) a.withdraw(amount);
    }

    void update(int accountNo, String name, String phone) {
        BankAccount a = accounts.get(accountNo);
        if (a != null) a.customer.update(name, phone);
    }

    void delete(int accountNo) {
        accounts.remove(accountNo);
        sortedAccounts.remove(accountNo);
    }

    void showSorted() {
        for (BankAccount a : sortedAccounts.values()) a.display();
    }
}
