import os
import json
import networkx as nx
import matplotlib.pyplot as plt


def check(ori_res_path, java_res_path):
    """用于验证 java生成的结果与 python 中 networkx 生成的原始结果是否相同
    @ori_res_path: 通过 networkx.pagerank 计算的结果路径
    @java_res_path: 通过 Java 分布式计算的 pagerank 结果路径
    """

    # networkx.pagerank 生成
    node_val_dic = {}
    with open(ori_res_path, 'r') as f:
        node_val_dic = json.load(f)

    node_val_list = [(key, round(val, 6))for key, val in node_val_dic.items()]
    # 按节点得分的倒序排序
    node_val_list.sort(key=lambda x:x[1], reverse=True)
    
    # Java 生成的结果
    with open(java_res_path, 'r') as f:
        node_val_list2 = []
        for i, line in enumerate(f.readlines()):
            node, val = line.split('\t')
            node_val_list2.append((int(node), round(float(val.strip()), 6)))
    # 按节点得分的倒序排序
    node_val_list2.sort(key=lambda x:x[1], reverse=True)

    print(node_val_list[:10])
    print(node_val_list2[:10])


def pagerank(data_path='./data/n100/edges_n100_e947.txt'):
    """根据有向图数据计算 pagerank """
    
    Graph = nx.DiGraph()

    with open(data_path, 'r') as f:
        node_num = 0
        node_pairs = []
        for i, line in enumerate(f.readlines()):
            if i == 0:
                node_num = int(line)
                continue
            n1, n2 = line.split(' ')
            node_pairs.append((int(n1), int(n2.strip())))

    print(node_num)
    print("node_num:{}, edge_num:{}".format(node_num, len(node_pairs)))

    Graph.add_nodes_from(range(0, node_num))

    for (n1, n2) in node_pairs:
        Graph.add_edge(n1, n2)

    # 绘制（耗时）
    # nx.draw(Graph, with_labels=True)
    # plt.show()

    pr = nx.pagerank(Graph, max_iter=100, alpha=0.85)

    with open(os.path.join(os.path.dirname(data_path), f"pr_val_n{node_num}_e11{len(node_pairs)}.json"), "w") as f:
        json.dump(pr, f)



if __name__ == "__main__":

    ori_res_path='./n100/pr_val_n100_e947.json'
    java_res_path='./n100/100_8_19'

    # 1. 对比 python 与 java 计算的 pagerank 是否一致
    check(ori_res_path, java_res_path)

    # 2. 根据有向图数据计算 pagerank
    # pagerank(data_path='./n100/edges_n100_e947.txt')


