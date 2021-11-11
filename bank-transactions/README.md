# Bank Transactions

## Runtime platform overview

The program is run on the virtual machine on Microsoft Azure. We decided to use VM on Azure because we can control how many vCPUs are available to Java runtime. The VM has the following specification.

- VM Sizes: Standard F4s_v2
- vCPUs: 4
- Memory: 8GB
- OS: Windows 10 Pro

- Java version: 17.0.1
- IDE: IntelliJ IDEA

## Task Running

To run the task, we create the following helper class and functions and to avoid duplicate codes. 

### Transaction

Transaction class is created to represent the data on each row of transactions in a CSV file. It contains the following attributes and some other getter, setter, and printing methods.

- date
- description
- deposit
- withdrawal 
- balance

### Task

This is a functional interface for our task which is written in a different method. We can easily use this interface for running the task in `runTask` function.

### readFromCSV

We created this function to receive the filename and read the CSV file. We used an external library calle `OpenCSV` for parsing each row in CSV into the array of strings. Then the data is used to create a Transaction object and added to ArrayList of Transaction. After that, the ArrayList of Transaction object is returned.

### runTask

This function is used to run the task by passing ArrayList of Transaction, boolean whether to print the result and the task as function interface. It is responsible for timing the task using `LocalDateTime.now()` function and returning the duration between start time and finish time as `long` in a nanosecond.

### printSpeedupAndEfficiency

This function is responsible for calculate the speedup and efficiency with given inputs of time in serial and time in parallel. The output is printed to the console.

### roundToTwoDecimal

This function is used to round the value in `double` to two decimal places.

### printMachineInfo

This function is used to print the available processor and memory of the running machine. The information is obtain from the `Runtime` class

### Main

Main function is the entry point of the application. First, it called `readFromCSV` function to get ArrayList of Transaction. Then it used `runTask` function to run the specific task. After that, it will use `printSpeedupAndEfficiency` function to print speedup and efficiency to the console. These steps are repeated for different tasks and data.

## Task 1

First, we transform the input data source into stream by using `.stream()` and `parallelStream()`. Then, we used `.filter((Transaction t) -> t.getBalance() == 0)` to filter only transaction that made account balance equal to 0. After that, the results are collected with `.collect()` and grouped by their description. At this point, we have Map of String, the description, and List of Transaction objects. Therefore, we transform the values of Map (List of Transaction objects) into the stream again. Then, we used `.map()` to get only the first Transaction from each description. Since the ArrayList has encountered order, the results that we get is the first transaction of each description that made the balance equal to 0. In the end, we used `.forEach()` to print out the result to the console.

### Result
> Task 1: Serial 15.62 ms, Parallel 15.63 ms
> Speedup: 1.0	Efficiency: 0.25
>
> Large Task 1: Serial 124.99 ms, Parallel 62.51 ms
> Speedup: 2.0	Efficiency: 0.5

## Task 2



### Result of Task 2.1

> Task 2.1: Serial 31.25 ms, Parallel 15.62 ms
> Speedup: 2.0	Efficiency: 0.5
>
> Large Task 2.1: Serial 2453.11 ms, Parallel 1093.76 ms
> Speedup: 2.24	Efficiency: 0.56

### Result of Task 2.2

> Task 2.2: Serial 15.62 ms, Parallel 15.62 ms
> Speedup: 1.0	Efficiency: 0.25
>
> Large Task 2.2: Serial 2000.05 ms, Parallel 1078.12 ms
> Speedup: 1.86	Efficiency: 0.47

## Conclusion

