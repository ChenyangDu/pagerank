import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PageRankMapper extends Mapper<Object, Text, Text, Text> {

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString();

        String[] lineArray = line.split(" ");
        System.out.printf("key:%s value:%s",key.toString(),line);
        Text id = null;
        if(lineArray.length > 0)id = new Text(lineArray[0]);

        int n = Integer.parseInt(context.getConfiguration().get("n")); // 数据的维度
        //获取旧的values值
        String[] values_str = context.getConfiguration().get("values").split(" ");
        BigDecimal[] values = new BigDecimal[n];
        assert values_str.length == n;
        for(int i=0;i<n;i++){
            values[i] = new BigDecimal(values_str[i]);
        }

        // 获取deadends
        String deadEndStr = context.getConfiguration().get("deadEnds");
        String[] deadEnds = new String[0];
        if(deadEndStr != null && deadEndStr.length() > 0)
            deadEnds = deadEndStr.split(" ");

        BigDecimal[] row = new BigDecimal[n];
        for(int i=0;i<n;i++){
            row[i] = BigDecimal.ZERO;
        }
        for(String deadEnd : deadEnds){
            row[Integer.parseInt(deadEnd)] = BigDecimal.ONE.divide(new BigDecimal(n));
        }
        for(int i=1;i< lineArray.length;i++){
            String[] nums = lineArray[i].split(":");
            assert nums.length == 2;
            row[Integer.parseInt(nums[0])] = new BigDecimal(nums[1]);
        }
        BigDecimal res = BigDecimal.ZERO;
        for(int j=0;j<n;j++){
            res = res.add( row[j].multiply(values[j]) );
        }
        DecimalFormat df1 = new DecimalFormat("0.000000000000000");
        context.write(id,new Text(df1.format(res)));
    }
}
