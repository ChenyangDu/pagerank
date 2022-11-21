# pagerank
## PageRank算法

[PageRank算法](PageRank%E7%AE%97%E6%B3%95.docx)



### 1. 数据准备
完整代码 data/generate_data.ipynb
#### 1.1 主要代码
```python
import networkx as nx
import matplotlib.pyplot as plt
import math
import random
random.seed(9)

node_num = 20  # 网页节点个数
edge_num = node_num * 10  # 边的个数，默认按平均每个网页10条边设置
max_selfcycle = int(node_num * 0.2) # 限制自环的数量
cur_selfcycle = 0

node_pairs = []
for i in range(edge_num):
    j = random.randint(0, node_num-1)  
    k = random.randint(0, node_num-1)

    # 限制自环数量
    if j == k:
        cur_selfcycle += 1
        if cur_selfcycle > max_selfcycle:
            continue

    node_pairs.append((k, j))

# 消除重边
node_pairs = list(set(node_pairs))
```
#### 1.2 输出数据
n:网页节点数
e: 有向边个数
dead ends: 没有任何出链的节点个数
self-cycle: 节点自己指向自己

1. **edges_n100_e947.txt**       
n=100, e=947, dead_ends=xx, self-cycle=xx
2. **edges_n1000_e9948.txt**
n=1000, e=9948, dead_ends=xx, self-cycle=xx
2. **edges_n10000_e99961.txt**
n=10000, e=99961, dead_ends=xx, self-cycle=xx
2. **edges_n100000_e999954.txt**
n=100000, e=999954, dead_ends=xx, self-cycle=xx
2. **edges_n1000000_e9999947.txt**
n=1000000, e=9999947, dead_ends=xx, self-cycle=xx

### 2. 数据可视化
通过 python networkx 绘制有向图，以下以一个节点数20的图作为例子  

<img src=".\img\DiGraph_n20.png" alt="MapReduce" style="zoom:75%;" />

```python
import networkx as nx
import matplotlib.pyplot as plt

from (i, j) in node_pairs:
    Graph.add_edge(k, j)
nx.draw(Graph, with_labels=True)
plt.show()
```


### 3. 复杂度分析
#### 3.1 不同数据规模的计算时间

| 实现方式 | n=100 | n=1000 | n=10000 | n=10w | n=100w |
| :---: | :---: | :---: | :---: | :---: | :---: |
| 普通 | 1067ms | 5660ms | 164.462s | - | - |
| 矩阵 |9ms|46ms|509ms|10.81s|141.59s|

#### 3.2 时间复杂度分析
时间复杂度是 $O(t(ε)n^2)$，其中 n 是网络节点个数，t(ε) 是迭代次数，这个迭代次数与收敛的阈值 ε 有关。  
```java

$$ 
 \sum_{i=1}^{T}(n*(n+n) + 2n) = \sum_{i=1}^{T}(2n^2 + 2n) = 2Tn^2 + 2Tn = O(n^2)
$$

 for(int t = 0;t<T;t++){
    // 遍历 n 个节点
    for(int i=0;i<n;i++){   
        BigDecimal[] row = getRow(i);    // 遍历第i个节点的n个入链权重，复杂度为 O(n)
        BigDecimal res = BigDecimal.ZERO;
        for(int j=0;j<n;j++){            // 将 row[i] * values[i]，复杂度为 O(n)
            assert row[j] != null;
            res = res.add( row[j].multiply(values[j]) );
        }
        newValues[i] = res;  // nv = m * v  Note: 此时的 m 已经在getRow(i)中对 dead ends 做了修正
    }

    // 考虑阻尼系数，复杂度O(n)
    for(int i=0;i<n;i++){ // 遍历 n 个节点，计算修正后的 nv = nv*β + nv*[1/n*(1-β)]
        newValues[i] = newValues[i].multiply(new BigDecimal(beta))
                .add(newValues[i].multiply(
                        BigDecimal.ONE.divide(new BigDecimal(n)).multiply(new BigDecimal(1-beta))));
    }

    // 计算差值，复杂度为O(n)
    double diff = 0;
    for(int i=0;i<n;i++){
        diff += Math.abs(values[i].subtract(newValues[i]).doubleValue());
        values[i] = newValues[i];
    }
```


### 4.验证数据结果
脚本 data/check_result.py
通过 python 中的 networkx 库，计算 pagerank PR 值

4.1 验证方法
networkx 中的 pagerank 实现思路与我们 java 中实现思路一致，因此可以作为验证我们代码的依据

4.2 核心代码
```python
# 对比 
pr = nx.pagerank(Graph, max_iter=100, alpha=0.85)  
node_val_list = [(key, round(val, 6))for key, val in pr.items()]
node_val_list.sort(key=lambda x:x[1], reverse=True)

# Java 生成的结果
with open(java_res_path, 'r') as f:
    node_val_list2 = []
    for i, line in enumerate(f.readlines()):
        node, val = line.split(' ')
        node_val_list2.append((int(node), round(float(val.strip()), 6)))
# 按节点得分的倒序排序
node_val_list2.sort(key=lambda x:x[1], reverse=True)

print(node_val_list[:10])
print(node_val_list2[:10])

```

## Hadoop集群搭建
[Hadoop集群搭建](Hadoop%E9%9B%86%E7%BE%A4%E6%90%AD%E5%BB%BA.docx)

## 分布式算法实现

## 分布式方案

### 原本公式

$$
PR(a)=[\beta(M+a^T(\frac e n))+(1-\beta)\frac{ee^T}n]*V
$$

### 分布式方案

#### 描述

公式当中的  $$ [\beta(M+a^T(\frac e n))+(1-\beta)\frac{ee^T}n] $$  是定值，记为 $ M^* $ ，则公式可以化简为 $PR=M^*\times V$ ，根据矩阵乘法的特性，只需要将矩阵按行进行拆分，每个节点只负责部分行的计算，最后将结果汇总即可。加入某个节点负责第 $i$ 行的计算，那么只需要获取 $ M^* $ 第 $i$ 行的数据和 $V$ ，即可计算出 $PR$ 的第 $i$ 行的值，将所有节点计算的结果汇总就可以得到完成的 $PR$ 。

#### 缺点

需要存储 $ M^* $ ，占用内存 $O(n^2)$ 极大，不适合大数据规模。

#### 优化内存

  $$M^* = [\beta(M+a^T(\frac e n))+(1-\beta)\frac{ee^T}n] $$  ，其中 $ M $ 是稀疏矩阵，因为每个网页向外跳转的链接数不多。 矩阵 $ a $ 的数据规模也只有 $1* n $ ，可以考虑通过邻接矩阵来保存 $ M $ ，将 $ M^* $ 的内存占用从 $O(n^2)$ 降低为 $O(m+n)$ 。

#### 优化计算

$$
PR(a)=[\beta(M+a^T(\frac e n))+(1-\beta)\frac{ee^T}n]*V
$$

$$
PR(a)=\beta(M+a^T(\frac e n))*V+(1-\beta)\frac{ee^T}n*V
$$

$$
令:PR_2=(M+a^T(\frac e n))*V
$$

$$
则:PR(a)=\beta*PR_2+(1-\beta)\frac{ee^T}n*V
$$

$$
PR(a)=\beta*PR_2+(1-\beta)\frac1n*V
$$

通过这种化简方案，在计算 $PR_2$ 时式中的 $(M+a^T(\frac e n))$ 仍是一个稀疏矩阵，而阻尼系数部分的计算则与矩阵运算进行了分离，将 $n^2$ 的计算量化简为了 $2n$ 的计算量。

### MapReduce

#### 介绍

Hadoop就是一个大数据开发所使用的分布式系统基础架构，由Apache软件基金会开发，主要用于海量数据的分布式处理。其核心是HDFS和MapReduce，二者分别用于大数据的存储和处理。HDFS即Hadoop分布式文件系统，可用于廉价计算机搭建的服务器集群，用于存储大量的数据，使得整个系统具备了高吞吐率、高容错性和高扩展性。MapReduce是面向大数据并行处理的计算模型，可以将大作业拆分成小作业进行作业调度和容错管理，适用于数据的批量处理。MapReduce将复杂的分布式计算方式，高度抽象成了Map函数和Reduce函数，屏蔽了复杂的分布式系统底层实现，大大方便了分布式程序的开发。

MapReduce的处理过程大致分为5个步骤，输入及数据分片，Map过程，Shuffle过程，Reduce过程，输出。

<img src=".\img\MapReduce.png" alt="MapReduce" style="zoom:75%;" />

其中输入数据会被处理成形如<key1,value1>的若干分片，经过Map处理成形如<key2,value2>的分片。经过Shuffle过程的整合，会将相同的key2合并成为<key2,list(value2)>的分片移交给Reduce，最后Reduce会输出形如<key3,value3>的结果。

#### 整合

将矩阵的边 $ M $ 作为MapReduce的输入，这里采用了邻接矩阵的存储形式，每个Map单位单独负责一行的计算
<img src=".\img\graph.png" alt="MapReduce" style="zoom:75%;" />

| 过程名称  | MapReduce |
| --------- | --------- |
| 原始数据 | 反向邻接表,格式如下<br>终点编号 边1起点:边1权重 边2起点:边2权重<br>0 2:0.5 3:1<br>1 0:0.5<br>2 1:1 0:0.5<br>3 2:0.5 |
| 数据切分 | 0 2:0.5 3:1 |
| map处理结果 | 假设V=[0.25,0.25,0.25,0.25]计算结果为0.25\*0.5+0.25\*1=0.375<br>输出<0,0.375>，<点编号，点的新权重> |
| suffer处理结果 | 由于每个map输出的键值都不同，所以结果同上 |
| reduce处理结果 | 无需处理，将输入原样输出即可 |

## 分布式实现

### 流程图

<img src=".\img\PageRank分布式流程图.png" alt="MapReduce" style="zoom:75%;" />

### 核心代码

邻接链表，采用了反向存储，其中存储了每条边的起点和权重

```java
// 边的结构
public class Edge {
    int target;
    BigDecimal value;
    public Edge(int target){
        this.target = target;
    }
}

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
```

处理dead ends，对于没有出度的点，添加其对所有点的边，权重为 $\frac1n$ 。

```java
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
```

考虑阻尼系数，经化简：$PR=\beta*PR_2+(1-\beta)\frac1n*V$

```java
// 考虑阻尼系数
for(int i=0;i<n;i++){
    newValues[i] = newValues[i].multiply(new BigDecimal(beta))
        .add(values[i].multiply(BigDecimal.ONE.divide(
            new BigDecimal(n)).multiply(new BigDecimal(1-beta))));
}
```

## 分布式效果

运行效果

```
0.250000 0.250000 0.250000 0.250000  第0轮结果，差值0.500000
0.332812 0.110937 0.332812 0.110937  第1轮结果，差值0.221875
0.246143 0.147686 0.246143 0.147686  第2轮结果，差值0.098457
0.240297 0.109226 0.240297 0.109226  第3轮结果，差值0.043690
0.203570 0.106632 0.203570 0.106632  第4轮结果，差值0.019388
0.184970 0.090334 0.184970 0.090334  第5轮结果，差值0.008603
0.162252 0.082080 0.162252 0.082080  第6轮结果，差值0.003818
0.144845 0.071999 0.144845 0.071999  第7轮结果，差值0.001694
0.128174 0.064275 0.128174 0.064275  第8轮结果，差值0.000752
```

