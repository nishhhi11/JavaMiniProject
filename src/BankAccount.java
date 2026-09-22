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

    boolean deposit(double amount) {
        if (amount <= 0)
            return false;

        balance += amount;
        transactions.add(new Transaction("Deposit", amount));
        return true;
    }

    boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance)
            return false;

        balance -= amount;
        transactions.add(new Transaction("Withdrawal", amount));
        return true;
    }

    void display() {
        System.out.println("Account No. : " + accountNo);
        System.out.println("Customer    : " + customer.name);
        System.out.println("Phone       : " + customer.phone);
        System.out.println("Type        : " + type);
        System.out.printf("Balance     : Rs. %.2f%n", balance);
    }

    void showTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf("%-15s %s%n", "Type", "Amount");
        System.out.println("--------------------------------");
        for (Transaction t : transactions)
            System.out.println(t);
    }
}