

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