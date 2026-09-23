public class Beneficiary {

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

    void display() {

        System.out.println("Name          : " + name);
        System.out.println("Bank          : " + bankName);
        System.out.println("Account       : " + accountNumber);
        System.out.println("IFSC          : " + ifsc);
    }
}