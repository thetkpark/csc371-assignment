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
        long task1Serial = runTask(transactions, false, Main::Task1Serialize);
        long task1Parallel = runTask(transactions, false, Main::Task1Parallel);
        long task21Serial = runTask(transactions, false, Main::Task21Serialize);
        long task21Parallel = runTask(transactions, false, Main::Task21Parallel);
        long task22Serial = runTask(transactions, false, Main::Task22Serialize);
        long task22Parallel = runTask(transactions, false, Main::Task22Parallel);

        printSpeedupAndEfficiency(task1Serial, task1Parallel, "Task 1");
        printSpeedupAndEfficiency(task21Serial, task21Parallel, "Task 2.1");
        printSpeedupAndEfficiency(task22Serial, task22Parallel, "Task 2.2");

//        System.out.println("");
//        transactions = readFromCSV("5000000-BT-Records.csv");
//        runTask(transactions, false, Main::Task1Serialize, "Task 1 Serialize");
//        runTask(transactions, false, Main::Task1Parallel, "Task 1 Parallel");
//        runTask(transactions, false, Main::Task2Serialize, "Task 2 Serialize");
//        runTask(transactions, false, Main::Task2Parallel, "Task 2 Parallel");
    }

    public static long runTask(ArrayList<Transaction> txs, boolean print, Task task) {
        int n = 3;
        long totalTimeMs = 0;
        for (int i=0; i<n; i++) {
            LocalDateTime tsStart = LocalDateTime.now();
            task.run(txs, print);
            LocalDateTime tsFinish = LocalDateTime.now();
            totalTimeMs += Duration.between(tsStart, tsFinish).toMillis();
        }
        return totalTimeMs/3;
    }

    public static void printSpeedupAndEfficiency(long tSerial, long tParallel, String taskName) {
        float speedup = tSerial/tParallel;
        float efficiency = speedup / Runtime.getRuntime().availableProcessors() / 2;
        System.out.println(taskName + ": Serial " + tSerial + " ms, Parallel " + tParallel + " ms");
        System.out.println("Speedup: " + speedup + "\tEfficiency: " + efficiency);
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

//    public static void Task21Serialize(ArrayList<Transaction> transactions, boolean print) {
//        transactions.stream()
//                .collect(Collectors.groupingBy(Transaction::getMonthYear))
//                .entrySet()
//                .stream()
//                .forEach((key, value) -> {
//
//                });
//    }

    public static void Task21Serialize(ArrayList<Transaction> transactions, boolean print) {
        transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getMonthYear))
                .forEach((key, value) -> {
                    DepositWithdrawls sum = new DepositWithdrawls();
                    float depositSum = value.stream().map(Transaction::getDeposit).reduce(0f, Float::sum);
                    float withdrawlsSum = value.stream().map(Transaction::getWithdrawl).reduce(0f, Float::sum);
                    if (print) {
                        System.out.println(key + ": Deposit " + depositSum + ", Withdrawls: " + withdrawlsSum);
                    }
                });
    }

    public static void Task21Parallel(ArrayList<Transaction> transactions, boolean print) {
        transactions.parallelStream()
                .collect(Collectors.groupingBy(Transaction::getMonthYear))
                .forEach((key, value) -> {
                    DepositWithdrawls sum = new DepositWithdrawls();
                    float depositSum = value.parallelStream().map(Transaction::getDeposit).reduce(0f, Float::sum);
                    float withdrawlsSum = value.parallelStream().map(Transaction::getWithdrawl).reduce(0f, Float::sum);
                    if (print) {
                        System.out.println(key + ": Deposit " + depositSum + ", Withdrawls: " + withdrawlsSum);
                    }
                });
    }

    public static void Task22Serialize(ArrayList<Transaction> transactions, boolean print) {
        transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getMonthYear))
                .forEach((key, value) -> {
                    Transaction firstTx = value.get(0);
                    float sum = firstTx.getBalance() + firstTx.getWithdrawl() - firstTx.getDeposit(); // Get initial balance
                    sum += value.stream()
                            .map(Transaction::sumDepositWithdrawl)
                            .reduce(0f, Float::sum);
                    if (print) System.out.println(key + ": " + sum);
                });
    }

    public static void Task22Parallel(ArrayList<Transaction> transactions, boolean print) {
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

class DepositWithdrawls {
     float deposit;
     float withdrawls;

    public DepositWithdrawls() {
        this.deposit = 0;
        this.withdrawls = 0;
    }
}