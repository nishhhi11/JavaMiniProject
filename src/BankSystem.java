import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class BankSystem implements Serializable {

    private static final long serialVersionUID = 1L;

    // account storage
    HashMap<Integer, BankAccount> accounts = new HashMap<>();

    // sorted account view
    TreeMap<Integer, BankAccount> sortedAccounts = new TreeMap<>();

    // saved beneficiaries
    ArrayList<Beneficiary> beneficiaries = new ArrayList<>();

    // transaction counter
    private int transactionCounter = 1001;

    void addAccount(BankAccount account) {

        accounts.put(account.accountNo, account);
        sortedAccounts.put(account.accountNo, account);

        DataManager.save(this);
    }

    BankAccount search(int accountNo) {

        return accounts.get(accountNo);
    }

    boolean deposit(int accountNo, double amount) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        boolean success = account.deposit(amount, transactionId());

        if (success)
            DataManager.save(this);

        return success;
    }

    boolean withdraw(int accountNo, double amount) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        boolean success = account.withdraw(amount, transactionId());

        if (success)
            DataManager.save(this);

        return success;
    }

    boolean transfer(int from, int to, double amount) {

        BankAccount sender = search(from);
        BankAccount receiver = search(to);

        if (sender == null || receiver == null || from == to)
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

        DataManager.save(this);

        return true;
    }

    boolean externalTransfer(
            int from,
            Beneficiary beneficiary,
            double amount
    ) {

        BankAccount sender = search(from);

        if (sender == null || beneficiary == null)
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
                        beneficiary.accountNumber,
                        "SUCCESS"
                )
        );

        DataManager.save(this);

        return true;
    }

    void addBeneficiary(Beneficiary beneficiary) {

        beneficiaries.add(beneficiary);

        DataManager.save(this);
    }

    boolean update(int accountNo, String name, String phone) {

        BankAccount account = search(accountNo);

        if (account == null)
            return false;

        account.customer.update(name, phone);

        DataManager.save(this);

        return true;
    }

    boolean delete(int accountNo) {

        BankAccount account = accounts.remove(accountNo);

        if (account == null)
            return false;

        sortedAccounts.remove(accountNo);

        DataManager.save(this);

        return true;
    }

    int totalAccounts() {

        return accounts.size();
    }

    double totalBalance() {

        double total = 0;

        for (BankAccount account : accounts.values()) {
            total += account.balance;
        }

        return total;
    }

    String transactionId() {

        return "TXN" + transactionCounter++;
    }

    BankAccount login(int accountNo, String pin) {

        BankAccount account = search(accountNo);

        if (account == null)
            return null;

        if (!account.customer.verifyPin(pin))
            return null;

        return account;
    }
}
