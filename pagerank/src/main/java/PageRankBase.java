import java.io.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;

public class PageRankBase {

    LinkedList<Edge>[] G;
    int[] outDegree;
    int n;
    static double beta;
    String deadEnds;
    static BufferedWriter log;
    public static void main(String[] args) throws Exception {
        if(args.length != 4){
            System.out.print("Usage <inputFile> <T> <beta> <logFile>\n");
        }
        String inputFile = args[0]; // 输入文件
        int T = Integer.parseInt(args[1]); // 设置最大轮次
        beta = Double.parseDouble(args[2]); // β值
        String logFile = args[3]; // 日志文件

        log = getBufferWriter(logFile);
        PageRankBase pageRankBase = new PageRankBase();
        pageRankBase.init(inputFile);
        pageRankBase.solve(T);
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

        for(int i=0;i<n;i++){
            for(Edge edge : G[i]){
                edge.value = BigDecimal.valueOf(1.0/outDegree[edge.target]);
            }
        }

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
    private void solve(int T) throws IOException, InterruptedException, ClassNotFoundException {
        BigDecimal[] values = new BigDecimal[n];
        BigDecimal[] newValues = new BigDecimal[n];
        for(int i=0;i<n;i++){
            values[i] = BigDecimal.valueOf(1.0/n);
        }

        for(int t = 0;t<T;t++){
            // 设置总数n
//            DecimalFormat df1 = new DecimalFormat("0.000000");
//            for(int i=0;i<n;i++){
//                System.out.printf("%s ",df1.format(values[i]));
//            }
            for(int i=0;i<n;i++){
                BigDecimal[] row = getRow(i);
                BigDecimal res = BigDecimal.ZERO;
                for(int j=0;j<n;j++){
                    assert row[j] != null;
                    res = res.add( row[j].multiply(values[j]) );
                }
                newValues[i] = res;
            }
            // 计算差值
            double diff = 0;
            for(int i=0;i<n;i++){
                diff += Math.abs(values[i].subtract(newValues[i]).doubleValue());
                values[i] = newValues[i];
            }

            // 考虑阻尼系数
            for(int i=0;i<n;i++){
                values[i] = values[i].multiply(new BigDecimal(beta))
                        .add(BigDecimal.ONE.divide(new BigDecimal(n)).multiply(new BigDecimal(1-beta)));
            }
//            diff = Math.sqrt(diff/n);
//            diff*=n;
            //标准差 <= 1/n*0.01
            String temp = new Date() + String.format(" 第%d轮结果，差值%f\n",t,diff);
            System.out.print(temp);
            log.write(temp);
            log.flush();
            if(diff <= 0.01)break;
        }
        StringBuilder sb = new StringBuilder();
        for(BigDecimal value : values){
            sb.append(value).append(" ");
        }
        log.write(sb.toString());
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
