package kafka.graph;

import com.alibaba.fastjson.JSON;

import java.util.*;

/**
 * 拓扑排序
 */
public class TopologicalSort {
    public static void main(String[] args) {
        Graph graph = Graph.build("A-C:1,A-D:1,B-D:1,C-E:1,D-E:1,E-F:1,S-A:1,S-B:1", true);
        System.out.println(JSON.toJSONString(graph.getAdjacentList()));
        System.out.println(graph.getVertices());

        // 入度表
        Map<String, Integer> inMap = new HashMap<>();
        for (String vertex : graph.getVertices()) {
            inMap.put(vertex, 0);
        }
        for (String vertex : graph.getVertices()) {
            for (Graph.Edge edge : graph.getVertexEdges(vertex)) {
                String end = edge.getEnd();
                inMap.put(end, inMap.get(end) + 1);
            }
        }

        System.out.println(inMap);

        // 1. 选择一个入度为 0 的顶点，输出
        // 2. 它点临接点的入度减 1
        // 3. 从入度表中删除它
        // 4. 重复步骤 1 直到入度表为空
        // 注意: 如果要找出所有拓扑排序的序列，可以使用递归方法

        while (!inMap.isEmpty()) {
            // [1] 选择一个入度为 0 的顶点，输出
            // [3] 从入度表中删除它
            List<String> starts = new LinkedList<>();
            Iterator<Map.Entry<String, Integer>> iter = inMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Integer> entry = iter.next();

                // [1] 选择一个入度为 0 的顶点，输出 (一次选择所有入度为 0 的点)
                if (entry.getValue().equals(0)) {
                    starts.add(entry.getKey());
                    iter.remove();
                }
            }

            System.out.println(starts);

            // [2] 它点临接点的入度减 1
            for (String start : starts) {
                for (Graph.Edge edge : graph.getVertexEdges(start)) {
                    String end = edge.getEnd();
                    if (inMap.get(end) > 0) {
                        inMap.put(end, inMap.get(end) - 1);
                    }
                }
            }
        }
    }
}
