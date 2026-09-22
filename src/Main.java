public class Main {
    public static void main(String[] args) {
        BankSystem bank = new BankSystem();

        bank.addAccount(new BankAccount(1002, new Customer(2, "Riya", "9876543210"), "Current", 10000));
        bank.addAccount(new BankAccount(1001, new Customer(1, "Nishi", "9876501234"), "Savings", 5000));

        bank.deposit(1001, 2000);
        bank.withdraw(1002, 1500);
        bank.search(1001);
        bank.update(1002, "Riya Mehta", "9999999999");
        bank.showSorted();
        bank.delete(1002);
    }
}