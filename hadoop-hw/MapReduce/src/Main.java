import java.io.IOException;

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

        private Transaction tx = null;

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tx != null) {
                Transaction currentTx = new Transaction(tokens);
                float calculatedBalance = tx.getBalance() + currentTx.sumDepositWithdrawl();

                if (Math.abs(calculatedBalance - currentTx.getBalance()) > 0.001) {
                    String val = currentTx.getDate().toString() + " Actual balance: " + calculatedBalance + ", Balance: " + currentTx.getBalance();
                    context.write(new Text("1"), new Text(val));
                }
            }
            tx = new Transaction(tokens);
        }
    }

    public static class AggregationReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            Text emptyText = new Text();
            for (Text val : values) {
                context.write(emptyText, val);
            }
        }
    }



    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "find error balance");
        job.setJarByClass(Main.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setNumReduceTasks(1);
        job.setCombinerClass(AggregationReducer.class);
        job.setReducerClass(AggregationReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}