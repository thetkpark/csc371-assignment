import java.io.IOException;
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

//        private String uuid = UUID.randomUUID().toString();
        private boolean pair = true;

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] cols = value.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            Transaction tx = new Transaction(cols);
            context.write(new Text(), new Text(tx.toString()));

//            context.write(new Text(uuid), value);
//            if (!pair) {
//                uuid = UUID.randomUUID().toString();
//                context.write(new Text(uuid), value);
//            }
//            pair = !pair;
        }
    }

//    public static class SumSamePairReducer extends Reducer<Text, Text, Text, Text> {
//        public void reduce(Text key, Iterable<Text> textTxs, Context context) throws IOException, InterruptedException {
//
//            float[] balances = new float[2];
//            float[] deposit = new float[2];
//            float[] withdrawl = new float[2];
//            int i = 0;
//
//            for (Text text : textTxs) {
//                String[] token = text.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//                context.write(key, new Text(text.toString() + " #" + token.length));
////                if (token.length < 5) {
////                    return;
////                } else {
////                    deposit[i] = Float.parseFloat(token[2].replace("\"", "").replace(",", ""));
////                    withdrawl[i] = Float.parseFloat(token[3].replace("\"", "").replace(",", ""));
////                    balances[i] = Float.parseFloat(token[4].replace("\"", "").replace(",", ""));
////                    i++;
////                    context.write(key, text);
////                }
//            }
////            Transaction[] txsList = new Transaction[2];
////            int i = 0;
////            for (Transaction tx : txs) {
////                txsList[i] = tx;
////            }
////
////            float calculatedBalance = txs[0].getBalance() + txs[1].sumDepositWithdrawl();
////            if (Math.abs(calculatedBalance - txs[1].getBalance()) > 0.001) {
////                context.write(key, new Text(txs[1].toString()));
////            }
//        }
//    }


//    public static class Transaction {
//        public LocalDate date;
//        public String description;
//        public float deposit;
//        public float withdrawl;
//        public float balance;
//
//        public Transaction(String[] token) {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MMM-yyyy");
//            this.date = LocalDate.parse(token[0], formatter);
//            this.description = token[1];
//            this.deposit = Float.parseFloat(token[2].replace("\"", "").replace(",", ""));
//            this.withdrawl = Float.parseFloat(token[3].replace("\"", "").replace(",", ""));
//            this.balance = Float.parseFloat(token[4].replace("\"", "").replace(",", ""));
//        }



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
        job.setNumReduceTasks(0);
//        job.setCombinerClass(SumSamePairReducer.class);
//        job.setReducerClass(SumSamePairReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}