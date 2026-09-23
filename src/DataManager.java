import java.io.*;

public class DataManager {

    static String fileName = "bankdata.dat";

    static void save(BankSystem bank) {

        try {

            ObjectOutputStream out =
                    new ObjectOutputStream(
                            new FileOutputStream(fileName)
                    );

            out.writeObject(bank);
            out.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    static BankSystem load() {

        try {

            ObjectInputStream in =
                    new ObjectInputStream(
                            new FileInputStream(fileName)
                    );

            BankSystem bank =
                    (BankSystem) in.readObject();

            in.close();

            return bank;

        } catch (Exception e) {

            return null;
        }
    }
}
