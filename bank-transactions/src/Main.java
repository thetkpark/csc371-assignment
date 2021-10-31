import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        LocalDateTime tsStart = LocalDateTime.now();
        Task1Serialize(transactions);
        LocalDateTime tsFinish = LocalDateTime.now();
        System.out.println("Task 1 Serialize: " + Duration.between(tsStart, tsFinish).toMillis() + " ms");

        tsStart = LocalDateTime.now();
        Task1Serialize(transactions);
        tsFinish = LocalDateTime.now();
        System.out.println("Task 1 Parallel: " + Duration.between(tsStart, tsFinish).toMillis() + " ms");
    }

    public static void Task1Serialize(ArrayList<Transaction> transactions) {
        transactions.stream()
                .filter((Transaction t) -> t.getBalance() == 0)
                .collect(Collectors.groupingBy(Transaction::getDescription))
                .values()
                .stream()
                .map((i) -> i.get(0))
                .forEach(tx -> System.out.println(tx.toString()));
    }

    public static void Task1Parallel(ArrayList<Transaction> transactions) {
        transactions.parallelStream()
                .filter((Transaction t) -> t.getBalance() == 0)
                .collect(Collectors.groupingBy(Transaction::getDescription))
                .values()
                .parallelStream()
                .map((i) -> i.get(0))
                .forEach(tx -> System.out.println(tx.toString()));
    }

}
