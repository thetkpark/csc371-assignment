import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import com.opencsv.CSVReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Main {

    public static class TokenizerMapper
            extends Mapper<Object, String, String, Transaction>{

        private final static IntWritable one = new IntWritable(1);
        private UUID uuid = UUID.randomUUID();
        private boolean pair = true;

        public void map(Object key, String value, Context context
        ) {
            CSVReader reader = new CSVReader(new StringReader(value));
            try {
                Transaction tx = new Transaction(reader.readNext());
                context.write(uuid.toString(), tx);
                if (!pair) {
                    uuid = UUID.randomUUID();
                    context.write(uuid.toString(), tx);
                }
                pair = !pair;
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public static class SumSamePairReducer extends Reducer<String, Transaction, String, String> {
        public void reduce(String key, Iterable<Transaction> txs, Context context) throws IOException, InterruptedException {
            Transaction[] txsList = new Transaction[2];
            int i = 0;
            for (Transaction tx : txs) {
                txsList[i] = tx;
            }

            float balance = txsList[0].getBalance() + txsList[1].sumDepositWithdrawl();
            if (Math.abs(balance - txsList[1].getBalance()) > 0.001) {
                context.write(key, txsList[0].toString());
            }
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
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(Main.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(SumSamePairReducer.class);
        job.setReducerClass(SumSamePairReducer.class);
        job.setOutputKeyClass(String.class);
        job.setOutputValueClass(String.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}