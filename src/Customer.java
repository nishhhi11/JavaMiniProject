public class Customer {

    int id;
    String name;
    String phone;

    Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    void update(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}