import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class StringAndInt implements Comparable<StringAndInt>, Writable{

	public int occur;
	public Text tag;
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
		this.tag.set(arg0.readUTF());
		this.occur = arg0.readInt();
	       
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		arg0.writeUTF(tag.toString());
		arg0.writeInt(occur);
		
		
	}

}
