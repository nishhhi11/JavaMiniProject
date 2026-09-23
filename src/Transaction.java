import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    String type;
    double amount;
    LocalDateTime dateTime;

    Transaction(String type, double amount) {
        this.type = type;
        this.amount = amount;
        this.dateTime = LocalDateTime.now();
    }

    public String toString() {
        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return String.format("%-18s %-15s Rs. %.2f",
                dateTime.format(format), type, amount);
    }
}