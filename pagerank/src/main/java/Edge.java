import java.math.BigDecimal;

public class Edge {
    int target;
    BigDecimal value;
    public Edge(int target){
        this.target = target;
    }
    public Edge(int target,BigDecimal value){
        this.target = target;
        this.value = value;
    }
}
