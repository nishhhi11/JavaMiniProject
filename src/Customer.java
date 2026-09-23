import java.io.Serializable;

public class Customer implements Serializable {

    int id;
    String name;
    String phone;
    String pin;

    Customer(int id, String name, String phone, String pin) {

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.pin = pin;
    }

    void update(String name, String phone) {

        this.name = name;
        this.phone = phone;
    }

    boolean verifyPin(String enteredPin) {

        return pin.equals(enteredPin);
    }
}