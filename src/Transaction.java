import java.time.LocalDateTime;

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
}