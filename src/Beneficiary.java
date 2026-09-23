import java.io.Serializable;

public class Beneficiary implements Serializable {

    String name;
    String bankName;
    String accountNumber;
    String ifsc;

    Beneficiary(String name, String bankName,
                String accountNumber, String ifsc) {

        this.name = name;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.ifsc = ifsc;
    }
}