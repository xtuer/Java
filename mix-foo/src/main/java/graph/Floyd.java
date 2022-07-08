package kafka.graph;

import java.util.LinkedList;
import java.util.List;

public class Floyd {
    private static final int N = 1000; // 表示无穷大距离

    public static void main(String[] args) {
        // 顶点
        char[] vertices = { 'A', 'B', 'C', 'D', 'E', 'F', 'G' };

        // 距离表
        int[][] distance = {
                { 0, 5, 7, N, N, N, 2 },
                { 5, 0, N, 9, N, N, 3 },
                { 7, N, 0, N, 8, N, N },
                { N, 9, N, 0, N, 4, N },
                { N, N, 8, N, 0, 5, 4 },
                { N, N, N, 4, 5, 0, 6 },
                { 2, 3, N, N, 4, 6, 0 },
        };

        // 前驱表
        int[][] path = new int[7][7];
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path[0].length; j++) {
                path[i][j] = i;
            }
        }

        // 核心: Floyd 算法计算任意 2 点之间的最短距离
        // 对于每个顶点 v，和任一顶点对 (i, j), i!=j, v!=i, v!=j，
        // 如果 A[i][j] > A[i][v]+A[v][j]，则将 A[i][j] 更新为 A[i][v]+A[v][j] 的值，并且将 Prev[i][j] 改为 Path[v][j]
        final int len = distance.length;
        for (int v = 0; v < len; v++) {         // 第一层: 中间点
            for (int i = 0; i < len; i++) {     // 第二层: 出发点
                for (int j = 0; j < len; j++) { // 第三层: 终结点
                    if (distance[i][v] + distance[v][j] < distance[i][j]) {
                        distance[i][j] = distance[i][v] + distance[v][j];
                        path[i][j] = path[v][j];
                    }
                }
            }
        }

        showDistance(distance, vertices);
        showPath(path, vertices);

        System.out.println(getPath('D', 'C', path, vertices));
    }

    /**
     * 求 a 点到 b 点的最短路径
     */
    public static List<Character> getPath(char a, char b, int[][] path, char[] vertices) {
        List<Character> vs = new LinkedList<>();
        int start = a - 'A';
        int end   = b - 'A';

        while (end != start) {
            vs.add(0, vertices[end]);
            end = path[start][end];
        }
        vs.add(0, vertices[start]);

        return vs;
    }

    // 显示距离表
    private static void showDistance(int[][] distance, char[] vertices) {
        final int len = distance.length;

        out(' ');
        for (int i = 0; i < len; i++) {
            out(vertices[i]);
        }
        for (int i = 0; i < len; i++) {
            System.out.println();
            out(vertices[i]);

            for (int j = 0; j < len; j++) {
                out(distance[i][j]);
            }
        }
        System.out.println();
        System.out.println();
    }

    // 显示路径表
    private static void showPath(int[][] path, char[] vertices) {
        final int len = path.length;

        out(' ');
        for (int i = 0; i < len; i++) {
            out(vertices[i]);
        }
        for (int i = 0; i < len; i++) {
            System.out.println();
            out(vertices[i]);

            for (int j = 0; j < len; j++) {
                out(vertices[path[i][j]]);
            }
        }
        System.out.println();
        System.out.println();
    }

    private static void out(int n) {
        System.out.printf("%5s", (n == N) ? "N" : n);
    }
    private static void out(char c) {
        System.out.printf("%5s", c);
    }
}
