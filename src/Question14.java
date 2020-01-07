import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;



public class Question14 {
	
	public static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
		 private final static IntWritable un = new IntWritable(1);
	     private Text mot = new Text();
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String ligne = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(ligne);
            while (tokenizer.hasMoreTokens()) {
                mot.set(tokenizer.nextToken());
                context.write(mot, un);
            }
			
		}
	}

	public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		 private IntWritable valeur = new IntWritable(0);
		 int Max = Integer.MIN_VALUE;
		@Override
		protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
			
			int somme =0 ;  
			for (IntWritable valeur : values) {
                somme += valeur.get();
			}
					
            valeur.set(somme);
            context.write(key, valeur);
            
		}
	}
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		String input = otherArgs[0];
		String output = otherArgs[1];
		//creer un une instance de job 
		Job job = Job.getInstance(conf, "WordCount");
		job.setJarByClass(Question14.class);
		job.setNumReduceTasks(3);
		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setCombinerClass(MyReducer.class);
 
		job.setReducerClass(MyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		FileInputFormat.addInputPath(job, new Path(input));
		job.setInputFormatClass(TextInputFormat.class);
		
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setOutputFormatClass(TextOutputFormat.class);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		
	}
	

}
