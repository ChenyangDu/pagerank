import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class PageRankBase {

    LinkedList<Edge>[] G;
    int[] outDegree;
    int n;
    public static void main(String[] args) throws IOException {
        String inputFile = args[0];
        String outputFile = args[1];
        PageRankBase pageRankBase = new PageRankBase();
        pageRankBase.init(inputFile,outputFile);
        pageRankBase.solve();
    }
    /**
     * 读入，初始化图
     */
    private void init(String inputFilePath,String outputFilePath) throws IOException {
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

        BufferedWriter bw = getBufferWriter(outputFilePath);
        assert bw != null;
        for(int i=0;i<n;i++){
            bw.write(i+'0');
            bw.write(' ');
            for(Edge edge : G[i]){
                edge.value = BigDecimal.valueOf(1.0/outDegree[edge.target]);
                bw.write(edge.value.toString());
                bw.write(' ');
            }
            bw.write('\n');
        }
        bw.close();
    }

    /**
     * PageRank
     */
    private void solve(){
        BigDecimal[] values = new BigDecimal[n];
        BigDecimal[] newValues = new BigDecimal[n];
        for(int i=0;i<n;i++){
            values[i] = BigDecimal.valueOf(1.0/n);
        }
        int T = 1000000;
        while(T-- > 0){
            DecimalFormat df1 = new DecimalFormat("0.00");
            for(int i=0;i<n;i++){
                System.out.printf("%s ",df1.format(values[i]));
            }
            System.out.println();
            for(int i=0;i<n;i++){
                BigDecimal[] row = getRow(i);
                BigDecimal res = BigDecimal.ZERO;
                for(int j=0;j<n;j++){
                    assert row[j] != null;
                    res = res.add( row[j].multiply(values[j]) );
                }
                newValues[i] = res;
            }


            values = newValues;
        }
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
//            Path input_file = new Path(filepath);
//            FileSystem fs = FileSystem.get(new Configuration());
//            DataInputStream stream = new DataInputStream(fs.open(input_file));
            File file = new File(filepath);
            FileInputStream fis = new FileInputStream(file);

            return new BufferedReader(new InputStreamReader(fis));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private static BufferedWriter getBufferWriter(String filePath){
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
