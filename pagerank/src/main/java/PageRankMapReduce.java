import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class PageRankMapReduce {


    LinkedList<Edge>[] G;
    int[] outDegree;
    int n;
    String deadEnds;
    static BufferedWriter log;
    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String matrixFile = args[1];
        String outputFile = args[2];
        int T = Integer.parseInt(args[3]);
        String logFile = args[4];
        log = getBufferWriterLocal(logFile);
        PageRankMapReduce pageRankBase = new PageRankMapReduce();
        pageRankBase.init(inputFile,matrixFile);
        pageRankBase.solve(T,matrixFile,outputFile);
    }
    /**
     * 读入，初始化图
     */
    private void init(String inputFilePath,String matrixFilePath) throws IOException {
        BufferedReader br = getBuffer(inputFilePath);
        assert br != null;
        String line = br.readLine();
        n = Integer.parseInt(line);

        G = new LinkedList[n];
        outDegree = new int[n];
        boolean[] hasOut = new boolean[n];

        while((line = br.readLine()) != null){
            // 一行一行地处理...
            String[] num = line.split(" ");
            assert num.length == 2;
            int s = Integer.parseInt(num[0]);
            int t = Integer.parseInt(num[1]);
            // 边反向
            if(G[t] == null)G[t] = new LinkedList<>();
            G[t].add(new Edge(s));
            outDegree[s]++;
            hasOut[s] = true;
        }

        BufferedWriter bw = getBufferWriter(matrixFilePath);
        assert bw != null;
        for(int i=0;i<n;i++){
            bw.write(String.valueOf(i));
            bw.write(' ');
            for(Edge edge : G[i]){
                edge.value = BigDecimal.valueOf(1.0/outDegree[edge.target]);
                bw.write(String.valueOf(edge.target));
                bw.write(':');
                bw.write(edge.value.toString());
                bw.write(' ');
            }
            bw.write('\n');
        }
        bw.close();

        // 处理deadEnds
        StringBuilder stringBuffer = new StringBuilder();
        for(int i=0;i<n;i++){
            if(!hasOut[i])stringBuffer.append(i).append(" ");
        }
        deadEnds = stringBuffer.toString();
    }

    /**
     * PageRank
     */
    private void solve(int T,String matrixFilePath,String outputFilePath) throws IOException, InterruptedException, ClassNotFoundException {
        BigDecimal[] values = new BigDecimal[n];
        BigDecimal[] newValues = new BigDecimal[n];
        for(int i=0;i<n;i++){
            values[i] = BigDecimal.valueOf(1.0/n);
        }

        Configuration conf = new Configuration();
        for(int t = 0;t<T;t++){
            // 设置总数n
            conf.set("n",String.valueOf(n));
            StringBuffer sb = new StringBuffer();
            for(BigDecimal value : values){
                sb.append(value).append(" ");
            }
            conf.set("values",sb.toString());
            conf.set("deadEnds",deadEnds);
            Job job = Job.getInstance(conf, "PageRank");

            job.setJarByClass(PageRankMapReduce.class);
            job.setMapperClass(PageRankMapper.class);
            job.setReducerClass(PageRankReducer.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            FileInputFormat.addInputPath(job, new Path(matrixFilePath));
            FileOutputFormat.setOutputPath(job, new Path(outputFilePath+'/'+t));

            job.waitForCompletion(true);

            BufferedReader buffer = getBuffer(outputFilePath +"/" + t +"/part-r-00000");
            String line;
            double diff = 0;
            while ((line = buffer.readLine())!=null){
                String[] row = line.split("\t");
                assert row.length == 2;
                int id = Integer.parseInt(row[0]);
                diff += Math.pow(values[id].doubleValue() - Double.parseDouble(row[1]),2);
                values[id] = new BigDecimal(row[1]);
            }
            diff = Math.sqrt(diff/n)*n;
            String temp = new Date() + String.format(" 第%d轮结果，差值%f\n",t,diff);
            System.out.print(temp);
            log.write(temp);
            log.flush();
            if(diff <= 0.001)break;
        }
        log.close();
    }

    private BigDecimal[] getRow(int i){
        BigDecimal[] res = new BigDecimal[n];
        for(int j=0;j<n;j++){
            res[j] = BigDecimal.ZERO;
        }
        for(Edge edge : G[i]){
            res[edge.target] = edge.value;
        }
        return res;
    }
    private static BufferedReader getBuffer(String filepath){
        try{
            Path input_file = new Path(filepath);
            FileSystem fs = FileSystem.get(new Configuration());
            DataInputStream stream = new DataInputStream(fs.open(input_file));
            return new BufferedReader(new InputStreamReader(stream));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static BufferedWriter getBufferWriter(String filePath){
        try {
            FileSystem fs = FileSystem.get(new Configuration());
            FSDataOutputStream fout = fs.create(new Path(filePath));
            return new BufferedWriter(new OutputStreamWriter(fout, "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static BufferedWriter getBufferWriterLocal(String filePath){
        try {
            File file = new File(filePath);
            FileOutputStream fis = new FileOutputStream(file);
            return new BufferedWriter(new OutputStreamWriter(fis, "UTF-8"));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
