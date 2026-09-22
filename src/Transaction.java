public class Transaction {
    String type;
    double amount;

    Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public String toString() {
        return String.format("%-15s Rs. %.2f", type, amount);
    }
}