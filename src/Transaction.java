import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    String id;
    String type;
    double amount;
    String from;
    String to;
    String status;
    String reason;
    LocalDateTime dateTime;

    Transaction(String id, String type, double amount,
                String from, String to, String status) {

        this(id, type, amount, from, to, status, "");
    }

    Transaction(String id, String type, double amount,
                String from, String to, String status,
                String reason) {

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.from = from;
        this.to = to;
        this.status = status;
        this.reason = reason;
        this.dateTime = LocalDateTime.now();
    }

    public String toString() {

        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        return String.format(
                "%-10s %-18s %-18s Rs. %-10.2f %-10s",
                id,
                dateTime.format(format),
                type,
                amount,
                status
        );
    }

    void showDetails() {

        DateTimeFormatter format =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        System.out.println("----------------------------------------");
        System.out.println("Transaction ID : " + id);
        System.out.println("Date & Time    : " + dateTime.format(format));
        System.out.println("Type           : " + type);
        System.out.printf("Amount         : Rs. %.2f%n", amount);
        System.out.println("From           : " + from);
        System.out.println("To             : " + to);
        System.out.println("Status         : " + status);

        if (!reason.isEmpty())
            System.out.println("Reason         : " + reason);

        System.out.println("----------------------------------------");
    }
}