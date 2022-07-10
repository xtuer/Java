package graph;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 图
 */
@Getter
@Setter
@Accessors(chain = true)
public class Graph {
    Map<String, List<Edge>> adjacentList = new HashMap<>(); // 邻接表存储图: key 为顶点，Edge 为此顶点和临接点构成的边

    /**
     * 获取图的顶点
     *
     * @return 返回顶点的集合
     */
    public Set<String> getVertices() {
        return adjacentList.keySet();
    }

    /**
     * 获取图的边，无向图中 2 条相同的边只输出一条
     *
     * @return 返回边的集合
     */
    public Set<Edge> getEdges() {
        return adjacentList.values().stream().flatMap(List::stream).collect(Collectors.toSet());
    }

    /**
     * 获取传入的顶点的所有边
     *
     * @param vertex 顶点
     * @return 返回边的数组
     */
    public List<Edge> getVertexEdges(String vertex) {
        return adjacentList.get(vertex);
    }

    public static Graph build(String edges) {
        return build(edges, false);
    }

    /**
     * 使用图的边构建图，边的格式为 start-end:weight，边之间使用逗号分隔，例如 A-B:10,A-G:5
     *
     * @param edges 图的所有边
     * @param directed true 表示有向图，false 表示无向图
     * @return 返回图的对象
     */
    public static Graph build(String edges, boolean directed) {
        Graph graph = new Graph();

        for (String edgeContent : StringUtils.split(edges, ",")) {
            // 边: A-B:10
            int indexOfDash  = edgeContent.indexOf("-");
            int indexOfColon = edgeContent.indexOf(":");
            String vertex1   = edgeContent.substring(0, indexOfDash);
            String vertex2   = edgeContent.substring(indexOfDash+1, indexOfColon);
            double weight    = Double.parseDouble(edgeContent.substring(indexOfColon+1));

            graph.adjacentList.putIfAbsent(vertex1, new LinkedList<>());
            graph.adjacentList.putIfAbsent(vertex2, new LinkedList<>());

            // 找到顶点 vertex1 的边集，添加它的边
            graph.adjacentList.get(vertex1).add(new Edge(vertex1, vertex2, weight));

            // 无向图添加对应的边
            if (!directed) {
                // 找到顶点 vertex2 的边集，添加它的边
                graph.adjacentList.get(vertex2).add(new Edge(vertex2, vertex1, weight));
            }
        }

        return graph;
    }

    /**
     * 图的边，由起点、终点和权重构成
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Edge {
        String start;  // 边的起点
        String end;    // 边的终点
        double weight; // 边的权重

        public Edge(String start, String end, double weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }

        /**
         * 2 个顶点相同的边则为同一条边
         */
        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != getClass()) {
                return false;
            }

            Edge other = (Edge) obj;

            if (this.start.equals(other.start) && this.end.equals(other.end)) {
                return true;
            }

            if (this.start.equals(other.end) && this.end.equals(other.start)) {
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            // start, end 从小到大排序
            if (start.compareTo(end) < 0) {
                return Objects.hash(start, end);
            } else {
                return Objects.hash(end, start);
            }
        }
    }

    public static void main(String[] args) {
        Graph graph = Graph.build("A-B:16,B-C:10,C-D:3,D-E:4,E-F:8,F-A:14,B-G:7,C-G:6,E-G:2,F-G:9,A-G:12,C-E:5");
        System.out.println(JSON.toJSONString(graph.getAdjacentList()));
        System.out.println(graph.getVertices());
        System.out.println(JSON.toJSONString(graph.getEdges()));
        System.out.println(JSON.toJSONString(graph.getVertexEdges("A"), true));
    }
}
