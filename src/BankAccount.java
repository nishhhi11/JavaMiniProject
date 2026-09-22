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

    void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactions.add(new Transaction("Deposit", amount));
        }
    }

    void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            transactions.add(new Transaction("Withdrawal", amount));
        }
    }

    String[] details() {
        return new String[]{String.valueOf(accountNo), customer.name, type};
    }

    void display() {
        System.out.println(accountNo + " | " + customer.name + " | " + type + " | " + balance);
        System.out.println(transactions);
    }
}
