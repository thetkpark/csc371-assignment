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
import java.util.function.Function;
import java.util.stream.Collectors;

 interface Task {
    void run(ArrayList<Transaction> transactions);
}

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

    public static void printMachineInfo() {
        System.out.println("Available processors (thread): " + Runtime.getRuntime().availableProcessors());
        System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
    }

    public static void main(String[] args) throws IOException, CsvValidationException {
        printMachineInfo();
        ArrayList<Transaction>  transactions = readFromCSV("5000-BT-Records.csv");
        runTask(transactions, Main::Task1Serialize, "Task 1 Serialize");
        runTask(transactions, Main::Task1Parallel, "Task 1 Parallel");
        runTask(transactions, Main::Task2Serialize, "Task 2 Serialize");
        runTask(transactions, Main::Task2Parallel, "Task 2 Parallel");
    }

    public static void runTask(ArrayList<Transaction> txs, Task task, String taskName) {
        LocalDateTime tsStart = LocalDateTime.now();
        task.run(txs);
        LocalDateTime tsFinish = LocalDateTime.now();
        System.out.println(taskName + ": " + Duration.between(tsStart, tsFinish).toMillis() + " ms");
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

    public static void Task2Serialize(ArrayList<Transaction> transactions) {

    }

    public static void Task2Parallel(ArrayList<Transaction> transactions) {

    }

}
