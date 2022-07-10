package graph;

import com.alibaba.fastjson.JSON;

import java.util.HashSet;
import java.util.Set;

/**
 * 普鲁姆算法生成最小生成树，从顶点出发
 */
public class Prim {
    public static void main(String[] args) {
        // 1. 把图的顶点放到集合 unmstVertices 中
        // 2. 最小生成树的顶点放到集合 mstVertices 中
        // 3. 指定一个顶点，从它开始构建最小生成树
        // 4. 从最小生成树中的节点的边中找到符合要求的边: 一个顶点在 mstVertices 中，另一个顶点在 unmstVertices 中，并且在这些边中权值最小
        // 5. 把 4 中找到的边加入到最小生成树，且从 unmstVertices 中删除这条边的顶点
        // 6. 重复步骤 4，直到所有点都加入 mstVertices，即 unmstVertices 为空

        Graph graph = Graph.build("A-B:16,B-C:10,C-D:3,D-E:4,E-F:8,F-A:14,B-G:7,C-G:6,E-G:2,F-G:9,A-G:12,C-E:5");
        Set<String> unmstVertices = new HashSet<>(graph.getVertices()); // 非最小生成树的顶点
        Set<String> mstVertices   = new HashSet<>(); // 最小生成树的节点

        // [3] 指定一个顶点，从它开始构建最小生成树
        mstVertices.add("E");
        unmstVertices.remove("E");

        while (!unmstVertices.isEmpty()) {
            Graph.Edge minEdge = null;
            double minWeight   = Double.MAX_VALUE;

            // [4] 从最小生成树中的节点的边中找到符合要求的边: 一个顶点在 mstVertices 中，另一个顶点在 unmstVertices 中，并且在这些边中权值最小
            for (String vertex : mstVertices) {
                for (Graph.Edge edge : graph.getVertexEdges(vertex)) {
                    String end = edge.getEnd();

                    // 边的另一个顶点在 unmstVertices 中，并且在这些边中权值最小
                    if (unmstVertices.contains(end) && edge.getWeight() < minWeight) {
                        minEdge   = edge;
                        minWeight = edge.getWeight();
                    }
                }
            }

            // [5] 把 4 中找到的边加入到最小生成树，且从 unmstVertices 中删除这条边的顶点
            // 每次肯定能找到一条边加入最小生成树
            mstVertices.add(minEdge.getEnd());
            unmstVertices.remove(minEdge.getEnd());
            System.out.println(JSON.toJSONString(minEdge));
        }
    }
}
