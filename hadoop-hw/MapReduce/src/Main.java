import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Main {

    public static class TokenizerMapper
            extends Mapper<Object, Text, Text, Text>{

        private String uuid = UUID.randomUUID().toString();
        private boolean pair = true;

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
//            String[] cols = value.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//            Transaction tx = new Transaction(cols);

            context.write(new Text(uuid), value);
            if (!pair) {
                uuid = UUID.randomUUID().toString();
                context.write(new Text(uuid), value);
            }
            pair = !pair;
        }
    }

    public static class SumSamePairReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> textTxs, Context context) throws IOException, InterruptedException {
            Transaction[] txs = new Transaction[2];
            int i = 0;

//            String output = "";
            for (Text text : textTxs) {
                String[] token = text.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (token.length < 5) {
//                    context.write(key, text);
                    return;
                } else {
                    txs[i] = new Transaction(token);
                    context.write(key, new Text(txs[i].toString()));
                    i++;
                }
//                context.write(key, new Text(token.length + ""));
            }
//            Transaction[] txsList = new Transaction[2];
//            int i = 0;
//            for (Transaction tx : txs) {
//                txsList[i] = tx;
//            }
//
//            float calculatedBalance = txs[0].getBalance() + txs[1].sumDepositWithdrawl();
//            if (Math.abs(calculatedBalance - txs[1].getBalance()) > 0.001) {
//                context.write(key, new Text(txs[1].toString()));
//            }
        }
    }


    public static class Transaction {
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


//    public static class IntSumReducer
//            extends Reducer<Text,IntWritable,Text,IntWritable> {
//        private IntWritable result = new IntWritable();
//
//        public void reduce(Text key, Iterable<IntWritable> values,
//                           Context context
//        ) throws IOException, InterruptedException {
//            int sum = 0;
//            for (IntWritable val : values) {
//                sum += val.get();
//            }
//            result.set(sum);
//            context.write(key, result);
//        }
//    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "find error balance");
        job.setJarByClass(Main.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(SumSamePairReducer.class);
        job.setReducerClass(SumSamePairReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}