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

        if (balance > 0)
            transactions.add(new Transaction("Initial Deposit", balance));
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

    boolean canWithdraw(double amount) {
        return amount > 0 && amount <= balance;
    }

    void display() {
        System.out.println("Account No. : " + accountNo);
        System.out.println("Customer    : " + customer.name);
        System.out.println("Phone       : " + customer.phone);
        System.out.println("Type        : " + type);
        System.out.printf("Balance     : Rs. %.2f%n", balance);
        System.out.println("Transactions: " + transactions.size());
    }

    void showBalance() {
        System.out.printf("Account Number : %d%n", accountNo);
        System.out.printf("Available Balance : Rs. %.2f%n", balance);
    }

    void showTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.printf("%-18s %-15s %s%n",
                "Date & Time", "Type", "Amount");

        System.out.println("------------------------------------------------");

        for (Transaction t : transactions)
            System.out.println(t);

        System.out.println("------------------------------------------------");
        System.out.printf("Current Balance : Rs. %.2f%n", balance);
    }
}