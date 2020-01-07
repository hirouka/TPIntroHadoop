import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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

import com.google.common.collect.MinMaxPriorityQueue;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;




public class Question22 {

	public static enum CMP {
		PAS_PAYS; 
	}
		

	public static class MyMapper extends Mapper<LongWritable, Text, Text, StringAndInt> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			String[] lignesmots = value.toString().split("\t");
		if(((lignesmots[11]).isEmpty()==false) &&  ((lignesmots[10]).isEmpty()==false)) {
			// recuperer pays
			Country c = Country.getCountryAt(Double.parseDouble(lignesmots[11]), Double.parseDouble(lignesmots[10]));
			
			if (c != null){
				//chaine de tags
				String[] tags = URLDecoder.decode(lignesmots[8].toString(), "UTF-8").split(",");

				if(tags.length > 0) {
					for (String tag : tags) {
						if(!tag.isEmpty())
							context.write(new Text(c.toString()) , new StringAndInt(tag.toString(),1));
					}
					System.out.println();
				}
			}
			else {
				context.getCounter(CMP.PAS_PAYS).increment(1);
			}
				
		}
		}
	}

	/**
	 * 
	 * le combiner envoie la liste des pays tag occurrence au reducer 
	 *
	 */
	public static class MyCombiner extends Reducer<Text, StringAndInt, Text, StringAndInt> {
		@Override
		protected void reduce(Text key, Iterable<StringAndInt> values, Context context) throws IOException, InterruptedException {
			
			Map<String, Integer> hashmap = new HashMap<String, Integer>();
			for (StringAndInt value : values) {
				if (hashmap.containsKey(value.getTag().toString())) {
					hashmap.put(value.getTag().toString(), hashmap.get(value.getTag().toString())+1);
				}
				else
					hashmap.put(value.getTag().toString(), 1);
			}
			System.out.println("---->"+key.toString()+ "\n-------------------\n "+ hashmap.toString());
			

			for (Entry<String, Integer> mapentry : hashmap.entrySet()) {	
				context.write(key, new StringAndInt(mapentry.getKey(), mapentry.getValue()));
		    }
			
	
			//hashmap.clear();
		}
	}
	

	public static class MyReducer extends Reducer<Text, StringAndInt, Text, Text> {
		@Override
		protected void reduce(Text key, Iterable<StringAndInt> values, Context context) throws IOException, InterruptedException {
			Map<String, Integer> hashmap = new HashMap<String, Integer>();
			//parcourir et remplir la la hashmap
			for (StringAndInt value : values) {
				//System.out.println("valeur = "+value.toString());
				
				if (hashmap.containsKey(value.getTag().toString())) {
				//	System.out.println(" la valeur est de "+hashmap.get(value.toString()));
					hashmap.put(value.getTag().toString(), hashmap.get(value.getTag().toString())+value.getOccur());
				}
				else
					hashmap.put(value.getTag().toString(), value.getOccur());
			}
			System.out.println("---->"+key.toString()+ "\n-------------------\n "+ hashmap.toString());
			int K = context.getConfiguration().getInt("K", 2);
			
			MinMaxPriorityQueue<StringAndInt> prioliste = MinMaxPriorityQueue.maximumSize(K).create();
			for (String mot : hashmap.keySet()) {
				prioliste.add(new StringAndInt(mot, hashmap.get(mot)));
			}
			String sortie = "\n --------- \n";
		
			while (!prioliste.isEmpty()){
				sortie += prioliste.pollFirst().toString() + " \n";
			}
			sortie+= "\n------\n";
			context.write(key, new Text(sortie));
			hashmap.clear();
		}	
		
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		String input = otherArgs[0];
		String output = otherArgs[1];
		conf.setInt("K", Integer.parseInt(otherArgs[2]));

		Job job = Job.getInstance(conf, "Question22");
		job.setJarByClass(Question22.class);

		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(StringAndInt.class);

		job.setReducerClass(MyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		
		job.setCombinerClass(MyCombiner.class);

		FileInputFormat.addInputPath(job, new Path(input));
		job.setInputFormatClass(TextInputFormat.class);

		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setOutputFormatClass(TextOutputFormat.class);

		job.waitForCompletion(true);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

		
	}
}