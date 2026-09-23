import java.util.LinkedList;

public class BankAccount {

    int accountNo;
    Customer customer;
    String type;
    double balance;

    LinkedList<Transaction> transactions = new LinkedList<>();

    BankAccount(int accountNo, Customer customer, String type, double balance) {
        this.accountNo = accountNo;
        this.customer = customer;
        this.type = type;
        this.balance = balance;
    }

    boolean deposit(double amount, String transactionId) {

        if (amount <= 0)
            return false;

        balance += amount;

        transactions.add(new Transaction(
                transactionId,
                "Deposit",
                amount,
                "External",
                String.valueOf(accountNo),
                "SUCCESS"
        ));

        return true;
    }

    boolean withdraw(double amount, String transactionId) {

        if (amount <= 0 || amount > balance)
            return false;

        balance -= amount;

        transactions.add(new Transaction(
                transactionId,
                "Withdrawal",
                amount,
                String.valueOf(accountNo),
                "External",
                "SUCCESS"
        ));

        return true;
    }

    boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }

    void showBalance() {

        System.out.println("Account Number : " + accountNo);
        System.out.println("Customer       : " + customer.name);
        System.out.printf("Available      : Rs. %.2f%n", balance);
    }

    void display() {

        System.out.println("Account No. : " + accountNo);
        System.out.println("Customer    : " + customer.name);
        System.out.println("Phone       : " + customer.phone);
        System.out.println("Type        : " + type);
        System.out.printf("Balance     : Rs. %.2f%n", balance);
        System.out.println("Transactions: " + transactions.size());
    }

    void showTransactions() {

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf(
                "%-10s %-18s %-15s %-10s%n",
                "ID", "Date & Time", "Type", "Status"
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        for (Transaction transaction : transactions)
            System.out.println(transaction);

        System.out.println(
                "------------------------------------------------------------"
        );

        System.out.printf(
                "Current Balance : Rs. %.2f%n",
                balance
        );
    }
}