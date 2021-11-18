import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;
import java.util.regex.Pattern;

import com.opencsv.CSVReader;
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
            extends Mapper<Object, Text, Text, Transaction>{

        private String uuid = UUID.randomUUID().toString();
        private boolean pair = true;

        public void map(Object key, Text value, Context context) {
            CSVReader reader = new CSVReader(new StringReader(value.toString()));
            try {
                Transaction tx = new Transaction(reader.readNext());
                context.write(new Text(uuid), tx);
                if (!pair) {
                    uuid = UUID.randomUUID().toString();
                    context.write(new Text(uuid), tx);
                }
                pair = !pair;
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    public static class SumSamePairReducer extends Reducer<Text, Transaction, Text, Text> {
        public void reduce(Text key, Iterable<Transaction> txs, Context context) throws IOException, InterruptedException {
            Transaction[] txsList = new Transaction[2];
            int i = 0;
            for (Transaction tx : txs) {
                txsList[i] = tx;
            }

            float balance = txsList[0].getBalance() + txsList[1].sumDepositWithdrawl();
            if (Math.abs(balance - txsList[1].getBalance()) > 0.001) {
                context.write(key, new Text(txsList[1].toString()));
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