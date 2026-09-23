import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedList;

public class BankSystem {

    HashMap<Integer, BankAccount> accounts = new HashMap<>();

    TreeMap<Integer, BankAccount> sortedAccounts =
            new TreeMap<>();

    LinkedList<Beneficiary> beneficiaries =
            new LinkedList<>();

    String[] accountTypes = {
            "Savings",
            "Current"
    };

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

        return account.deposit(
                amount,
                transactionId()
        );
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

        return account.withdraw(
                amount,
                transactionId()
        );
    }

    boolean transfer(
            int from,
            int to,
            double amount) {

        BankAccount sender = search(from);
        BankAccount receiver = search(to);

        if (sender == null || receiver == null)
            return false;

        if (from == to) {

            addFailedTransaction(
                    sender,
                    "Transfer",
                    amount,
                    String.valueOf(from),
                    String.valueOf(to),
                    "Cannot transfer to same account"
            );

            return false;
        }

        if (amount <= 0) {

            addFailedTransaction(
                    sender,
                    "Transfer",
                    amount,
                    String.valueOf(from),
                    String.valueOf(to),
                    "Invalid amount"
            );

            return false;
        }

        if (!sender.canWithdraw(amount)) {

            addFailedTransaction(
                    sender,
                    "Transfer",
                    amount,
                    String.valueOf(from),
                    String.valueOf(to),
                    "Insufficient balance"
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

        if (sender == null || beneficiary == null)
            return false;

        if (amount <= 0) {

            addFailedTransaction(
                    sender,
                    "External Transfer",
                    amount,
                    String.valueOf(from),
                    beneficiary.bankName,
                    "Invalid amount"
            );

            return false;
        }

        if (!sender.canWithdraw(amount)) {

            addFailedTransaction(
                    sender,
                    "External Transfer",
                    amount,
                    String.valueOf(from),
                    beneficiary.bankName,
                    "Insufficient balance"
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

    private void addFailedTransaction(
            BankAccount account,
            String type,
            double amount,
            String from,
            String to,
            String reason) {

        account.transactions.add(
                new Transaction(
                        transactionId(),
                        type,
                        amount,
                        from,
                        to,
                        "FAILED",
                        reason
                )
        );
    }

    void addBeneficiary(Beneficiary beneficiary) {

        if (beneficiary != null)
            beneficiaries.add(beneficiary);
    }

    BankAccount searchByName(String name) {

        if (name == null)
            return null;

        for (BankAccount account : accounts.values()) {

            if (account.customer.name.equalsIgnoreCase(name))
                return account;
        }

        return null;
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

        if (id == null)
            return null;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction :
                    account.transactions) {

                if (transaction.id.equalsIgnoreCase(id))
                    return transaction;
            }
        }

        return null;
    }

    int successfulTransactions() {

        int count = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction :
                    account.transactions) {

                if ("SUCCESS".equals(transaction.status))
                    count++;
            }
        }

        return count;
    }

    int failedTransactions() {

        int count = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction :
                    account.transactions) {

                if ("FAILED".equals(transaction.status))
                    count++;
            }
        }

        return count;
    }

    double totalTransactionVolume() {

        double total = 0;

        for (BankAccount account : accounts.values()) {

            for (Transaction transaction :
                    account.transactions) {

                if ("SUCCESS".equals(transaction.status))
                    total += transaction.amount;
            }
        }

        return total;
    }
}