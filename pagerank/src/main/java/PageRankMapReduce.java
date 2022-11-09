import java.io.*;
import java.util.LinkedList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
public class PageRankMapReduce {
    class Edge{
        int target;
        double value;
        public Edge(int target){
            this.target = target;
        }
        public Edge(int target,double value){
            this.target = target;
            this.value = value;
        }
    }
    LinkedList<Edge>[] G;
    int[] outDegree;
    int n;
    public static void main(String[] args) throws IOException {
        String inputfile = args[0];
//        String outputfile = args[2];
        PageRankMapReduce pageRankBase = new PageRankMapReduce();
        pageRankBase.init(inputfile);
        pageRankBase.solve();
    }
    /**
     * 读入，初始化图
     */
    private void init(String inputFilePath) throws IOException {
        BufferedReader br = getBuffer(inputFilePath);
        assert br != null;
        String line = br.readLine();
        n = Integer.parseInt(line);

        G = new LinkedList[n];
        outDegree = new int[n];

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
        }
        for(int i=0;i<n;i++){
            for(Edge edge : G[i]){
                edge.value = 1.0/outDegree[edge.target];
            }
        }
//        for(int i=0;i<n;i++){
//            System.out.printf("%n%d:%n",i);
//            for(Edge edge : G[i]){
//                System.out.printf("%d %f%n",edge.target,edge.value);
//            }
//        }
    }

    /**
     * PageRank
     */
    private void solve(){
        double[] values = new double[n];
        double[] newValues = new double[n];
        for(int i=0;i<n;i++){
            values[i] = 1.0/n;
        }
        int T = 1000000;
        while(T-- > 0){
            for(int i=0;i<n;i++){
                System.out.printf("%.6f ",values[i]);
            }
            System.out.println();
            for(int i=0;i<n;i++){
                double[] row = getRow(i);
                double res = 0;
                for(int j=0;j<n;j++){
                    res += row[j] * values[j];
                }
                newValues[i] = res;
            }


            values = newValues;
        }
    }

    private double[] getRow(int i){
        double[] res = new double[n];
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
}
