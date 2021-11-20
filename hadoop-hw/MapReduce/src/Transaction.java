import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    public LocalDate date;
    public String description;
    public float deposit;
    public float withdrawl;
    public float balance;

    public Transaction(String[] token) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");
        this.date = LocalDate.parse(token[0], formatter);
        this.description = token[1];
        this.deposit = Float.parseFloat(token[2].replace("\"", "").replace(",", ""));
        this.withdrawl = Float.parseFloat(token[3].replace("\"", "").replace(",", ""));
        this.balance = Float.parseFloat(token[4].replace("\"", "").replace(",", ""));
    }

    public Transaction(LocalDate date, String description, float deposit, float withdrawl, float balance) {
        this.date = date;
        this.description = description;
        this.deposit = deposit;
        this.withdrawl = withdrawl;
        this.balance = balance;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getDeposit() {
        return deposit;
    }

    public void setDeposit(float deposit) {
        this.deposit = deposit;
    }

    public float getWithdrawl() {
        return withdrawl;
    }

    public void setWithdrawl(float withdrawl) {
        this.withdrawl = withdrawl;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "date=" + date +
                ", description='" + description + '\'' +
                ", deposit=" + deposit +
                ", withdrawl=" + withdrawl +
                ", balance=" + balance +
                '}';
    }

    public String getMonthYear() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy");
        return this.date.format(formatter);
    }

    public float sumDepositWithdrawl() {
        return this.deposit - this.withdrawl;
    }
}