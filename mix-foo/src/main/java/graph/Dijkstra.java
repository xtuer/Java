package graph;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.*;

/**
 * 迪杰斯特拉算法求一点到其他所有点的最短路径
 */
public class Dijkstra {
    public static void main(String[] args) {
        // 1. 把起点加入到队列
        // 2. 队列中距离最小的顶点出队 (min)
        // 3. 未访问过的顶点标记为已访问，忽略被访问过的顶点 (添加到访问过的节点)
        // 4. 遍历 min 的所有未访问过的临接点，调整其距离为 weight + min.distance，并加入优先级队列
        // 5. 直到队列为空结束循环

        Graph graph = Graph.build("A-B:16,B-C:10,C-D:3,D-E:4,E-F:8,F-A:14,B-G:7,C-G:6,E-G:2,F-G:9,A-G:12,C-E:5");
        Map<String, Entry> visited = new HashMap<>(); // 访问过的顶点
        PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparing(Entry::getDistance)); // 优先级队列，距离小的先出队

        // [1] 把起点加入到队列
        String startVertex = "C";
        queue.offer(new Entry(startVertex, null, 0));

        while (!queue.isEmpty()) {
            // [2] 队列中距离最小的顶点出队
            Entry min = queue.poll();
            String currentVertex = min.getVertex();

            // [3] 未访问过的顶点标记为已访问，忽略被访问过的顶点 (添加到访问过的节点)
            if (visited.containsKey(currentVertex)) {
                continue;
            }

            // 添加到访问过的节点
            visited.put(currentVertex, min);

            // [4] 遍历 min 的所有未访问过的临接点，调整其距离为 weight + min.distance，并加入优先级队列
            for (Graph.Edge edge : graph.getVertexEdges(currentVertex)) {
                // 忽略访问过的顶点
                if (visited.containsKey(edge.getEnd())) {
                    continue;
                }

                queue.offer(new Entry(edge.getEnd(), currentVertex, edge.getWeight() + min.getDistance()));
            }
        }

        // 输出最短距离的顶点信息，可以得到起点到它的距离、前驱
        System.out.println(JSON.toJSONString(visited, true));

        // 输出起点到其他点的最短路径
        for (String vertex : graph.getVertices()) {
            System.out.printf("\n最短路径: %s 到 %s，距离 %.2f\n", startVertex, vertex, visited.get(vertex).getDistance());

            List<String> path = new LinkedList<>();
            Entry current = visited.get(vertex);
            while (current != null) {
                path.add(0, current.getVertex());
                current = visited.get(current.getPrevVertex());
            }
            System.out.println(String.join("->", path));
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Entry {
        String vertex;     // 顶点
        String prevVertex; // 前驱
        double distance;   // 距离

        public Entry(String vertex, String prevVertex, double distance) {
            this.vertex     = vertex;
            this.prevVertex = prevVertex;
            this.distance   = distance;
        }
    }
}
