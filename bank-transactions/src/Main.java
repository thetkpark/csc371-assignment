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
    void run(ArrayList<Transaction> transactions, boolean print);
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
        runTask(transactions, false, Main::Task1Serialize, "Task 1 Serialize");
        runTask(transactions, false, Main::Task1Parallel, "Task 1 Parallel");
        runTask(transactions, false, Main::Task2Serialize, "Task 2 Serialize");
        runTask(transactions, false, Main::Task2Parallel, "Task 2 Parallel");

//        System.out.println("");
//        transactions = readFromCSV("5000000-BT-Records.csv");
//        runTask(transactions, false, Main::Task1Serialize, "Task 1 Serialize");
//        runTask(transactions, false, Main::Task1Parallel, "Task 1 Parallel");
//        runTask(transactions, false, Main::Task2Serialize, "Task 2 Serialize");
//        runTask(transactions, false, Main::Task2Parallel, "Task 2 Parallel");
    }

    public static void runTask(ArrayList<Transaction> txs, boolean print, Task task, String taskName) {
        int n = 3;
        long totalTimeMs = 0;
        for (int i=0; i<n; i++) {
            LocalDateTime tsStart = LocalDateTime.now();
            task.run(txs, print);
            LocalDateTime tsFinish = LocalDateTime.now();
            totalTimeMs += Duration.between(tsStart, tsFinish).toMillis();
        }
        System.out.println(taskName + ": " + totalTimeMs/3 + " ms");
    }

    public static void Task1Serialize(ArrayList<Transaction> transactions, boolean print) {
        transactions.stream()
                .filter((Transaction t) -> t.getBalance() == 0)
                .collect(Collectors.groupingBy(Transaction::getDescription))
                .values()
                .stream()
                .map((i) -> i.get(0))
                .forEach(tx -> {
                    if (print) System.out.println(tx.toString());
                });
    }

    public static void Task1Parallel(ArrayList<Transaction> transactions, boolean print) {
        transactions.parallelStream()
                .filter((Transaction t) -> t.getBalance() == 0)
                .collect(Collectors.groupingBy(Transaction::getDescription))
                .values()
                .parallelStream()
                .map((i) -> i.get(0))
                .forEach(tx -> {
                    if (print) System.out.println(tx.toString());
                });
    }

    public static void Task2Serialize(ArrayList<Transaction> transactions, boolean print) {
        transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getMonthYear))
                .forEach((key, value) -> {
                    Transaction firstTx = value.get(0);
                    float sum = firstTx.getBalance() + firstTx.getWithdrawl() - firstTx.getDeposit();
                    sum += value.stream()
                            .map(Transaction::sumDepositWithdrawl)
                            .reduce(0f, Float::sum);
                    if (print) System.out.println(key + ": " + sum);
                });
    }

    public static void Task2Parallel(ArrayList<Transaction> transactions, boolean print) {
        transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getMonthYear))
                .forEach((key, value) -> {
                    Transaction firstTx = value.get(0);
                    float sum = firstTx.getBalance() + firstTx.getWithdrawl() - firstTx.getDeposit(); // Get initial balance
                    sum += value.parallelStream()
                            .map(Transaction::sumDepositWithdrawl)
                            .reduce(0f, Float::sum);
                    if (print) System.out.println(key + ": " + sum);
                });
    }

}
