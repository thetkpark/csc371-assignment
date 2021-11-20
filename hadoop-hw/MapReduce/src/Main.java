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
        private Transaction tx = null;

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tx != null) {
                Transaction currentTx = new Transaction(tokens);
                float calculatedBalance = tx.getBalance() + currentTx.sumDepositWithdrawl();

                if (Math.abs(calculatedBalance - currentTx.getBalance()) > 0.001) {
                    context.write(new Text(), new Text("Actual balance: " + calculatedBalance + ", Balance: " + currentTx.getBalance()));
                }
            }
            tx = new Transaction(tokens);
        }
    }

//    public static class SumSamePairReducer extends Reducer<Text, Text, Text, Text> {
//        public void reduce(Text key, Iterable<Text> textTxs, Context context) throws IOException, InterruptedException {
//
//            Transaction[] txs = new Transaction[2];
//            int i = 0;
//
//            for (Text text : textTxs) {
//                String[] token = text.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
//                txs[i] = new Transaction(token);
//                i++;
//            }
//
//            float calculatedBalance = txs[0].getBalance() + txs[1].sumDepositWithdrawl();
//            if (Math.abs(calculatedBalance - txs[1].getBalance()) > 0.001) {
//                context.write(key, new Text(txs[1].toString()));
//            }
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