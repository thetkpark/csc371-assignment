import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static ArrayList<Transaction> readFromCSV(String csvFileName) throws IOException, CsvValidationException {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        CSVReader reader = new CSVReader(new FileReader(csvFileName));
        reader.skip(1);
        String[] line;
        while ((line = reader.readNext()) != null) {
            Transaction tx = new Transaction(line);
            transactions.add(tx);
        }
        return transactions;
    }

    public static void main(String[] args) throws IOException, CsvValidationException {
        ArrayList<Transaction>  transactions = readFromCSV("5000-BT-Records.csv");
    }

}
