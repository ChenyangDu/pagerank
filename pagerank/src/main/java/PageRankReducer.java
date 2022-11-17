import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class PageRankReducer extends Reducer<Text, Text,Text,Text> {
    public void reduce(Text clusterId, Iterable<Text> rows, Context context)
            throws IOException, InterruptedException
    {
        Text text = null;
        for(Text line : rows){
            text = line;
        }
        context.write(clusterId,text);
    }
}
