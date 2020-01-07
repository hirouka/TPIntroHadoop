import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;




public class Question31 {

	public static enum CMP {
		PAS_PAYS; 
	}
	public static class StringAndInt implements Comparable<StringAndInt>, Writable,WritableComparable<StringAndInt>{

		public int occur;
		public Text tag;
		public String pays;
		public int getOccur() {
			return occur;
		}

		public void setOccur(int occur) {
			this.occur = occur;
		}

		public Text getTag() {
			return tag;
		}

		public void setTag(Text tag) {
			this.tag = tag;
		}

		

		public StringAndInt(String tag, int occur) {
			super();
			this.tag = new Text(tag);
			this.occur = occur;
		}

		public StringAndInt(String country, String tag, int occur) {
			this.pays = country;
			this.tag =new Text(tag);
			this.occur = occur;
		}


		public StringAndInt() {
			this.tag = new Text();
			this.occur = 0;
		}
		
		@Override
		public int compareTo(StringAndInt arg0) {
			if (occur > arg0.occur)
				return -1;
			else if (occur < arg0.occur)
				return 1;
			else
				return 0;
		}
		
		public String toString() {
			return this.tag.toString() + " " +this.occur +" ";
			
		}

		@Override
		public void readFields(DataInput arg0) throws IOException {
			this.pays = arg0.readUTF();
			this.tag.set(arg0.readUTF());
			this.occur = arg0.readInt();
		       
		}

		@Override
		public void write(DataOutput arg0) throws IOException {
			arg0.writeUTF(pays);
			arg0.writeUTF(tag.toString());
			arg0.writeInt(occur);
			
			
		}

	}

	public static class MyMapper extends Mapper<LongWritable, Text, Text, StringAndInt> {
		@Override
		protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			
			String[] lignesmots = value.toString().split("\t");
			// recuperer pays
			Country c = Country.getCountryAt(Double.parseDouble(lignesmots[11]), Double.parseDouble(lignesmots[10]));
			
			if (c != null){
				//chaine de tags
				String[] tags = URLDecoder.decode(lignesmots[8].toString(), "UTF-8").split(",");

				if(tags.length > 0) {
					for (String tag : tags) {
						context.write(new Text(c.toString()) , new StringAndInt(c.toString(),tag.toString(),1));
					}
					System.out.println();
				}
			}
			else {
				context.getCounter(CMP.PAS_PAYS).increment(1);
			}
				
		}
	}
	public static class MyMapper2 extends Mapper<Text, StringAndInt, StringAndInt, StringAndInt> {
		@Override
		protected void map(Text key, StringAndInt value, Context context) throws IOException, InterruptedException {
			context.write(new StringAndInt(key.toString(), value.getTag().toString(), value.getOccur()), value);
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
	public static class MyReducer extends Reducer<Text, StringAndInt, Text, StringAndInt> {
		@Override
		protected void reduce(Text key, Iterable<StringAndInt> values, Context context) throws IOException, InterruptedException {
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (StringAndInt value : values) {
				if (map.containsKey(value.getTag().toString()))
					
					map.put(value.getTag().toString(), map.get(value.getTag().toString()) + value.getOccur());
				
				else
					map.put(value.getTag().toString(), value.getOccur());
			}

			for (String k : map.keySet()){
				context.write(key, new StringAndInt(key.toString(), k, map.get(k)));
			}
		}
	}
	public static class MyReducer2 extends Reducer<StringAndInt, StringAndInt, Text, Text> {
		@Override
		protected void reduce(StringAndInt key, Iterable<StringAndInt> values, Context context) throws IOException, InterruptedException {
				for (StringAndInt value : values) {
				context.write(new Text(key.pays), new Text(value.tag.toString() + "  " + value.getOccur()));
				
			}
		}
	}
	
	
	
	

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		String input = otherArgs[0];
		String output = otherArgs[1];
		conf.setInt("K", Integer.parseInt(otherArgs[2]));

		Job job = Job.getInstance(conf, "Question31");
		job.setJarByClass(Question31.class);

		job.setMapperClass(MyMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(StringAndInt.class);

		job.setReducerClass(MyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(StringAndInt.class);

		job.setCombinerClass(MyReducer.class);

		FileInputFormat.addInputPath(job, new Path(input));
		job.setInputFormatClass(TextInputFormat.class);

		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.waitForCompletion(true);

		Job job2 = Job.getInstance(conf, "Question31");
		job2.setJarByClass(Question31.class);

		job2.setMapperClass(MyMapper2.class);
		job2.setMapOutputKeyClass(StringAndInt.class);
		job2.setMapOutputValueClass(StringAndInt.class);
		job2.setReducerClass(MyReducer2.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(Text.class);

		

		FileInputFormat.addInputPath(job2, new Path(output));
		job2.setInputFormatClass(SequenceFileInputFormat.class);

		FileOutputFormat.setOutputPath(job2, new Path(output+"/dossierRes"));
		job2.setOutputFormatClass(TextOutputFormat.class);

		job2.waitForCompletion(true);

		
	}
}