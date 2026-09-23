import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    String id;
    String type;
    double amount;
    String from;
    String to;
    String status;
    LocalDateTime dateTime;

    Transaction(String id, String type, double amount,
                String from, String to, String status) {

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.status = status;
        this.dateTime = LocalDateTime.now();
    }

    public String toString() {
        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return String.format(
                "%-10s %-18s %-15s Rs. %-10.2f %-10s",
                id,
                dateTime.format(format),
                type,
                amount,
                status
        );
    }
}