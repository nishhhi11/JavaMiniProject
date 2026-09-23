import java.util.LinkedList;

public class BankAccount {

    int accountNo;
    Customer customer;
    String type;
    double balance;

    LinkedList<Transaction> transactions = new LinkedList<>();

    BankAccount(int accountNo, Customer customer,
                String type, double balance) {

        this.accountNo = accountNo;
        this.customer = customer;
        this.type = type;
        this.balance = balance;
    }

    boolean deposit(double amount, String transactionId) {

        if (amount <= 0)
            return false;

        balance += amount;

        transactions.add(
                new Transaction(
                        transactionId,
                        "Deposit",
                        amount,
                        "External",
                        String.valueOf(accountNo),
                        "SUCCESS"
                )
        );

        return true;
    }

    boolean withdraw(double amount, String transactionId) {

        if (amount <= 0 || amount > balance)
            return false;

        balance -= amount;

        transactions.add(
                new Transaction(
                        transactionId,
                        "Withdrawal",
                        amount,
                        String.valueOf(accountNo),
                        "External",
                        "SUCCESS"
                )
        );

        return true;
    }

    boolean canWithdraw(double amount) {

        return amount > 0 && amount <= balance;
    }
}