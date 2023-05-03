package misc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DatabaseConnectionPoolTest {
    public static void main(String[] args) throws Exception {
        DatabaseConnectionPool connectionPool = new DatabaseConnectionPool();
        List<Connection> conns = new ArrayList<>();

        // 创建连接。
        for (int i = 0; i < 10; i++) {
            Connection conn = connectionPool.getConnection();
            conns.add(conn);
        }

        System.out.println("start release");

        // 归还所有连接，观察连接池信息: 例如 MySQL show processlist 查看连接数，应该至少还剩下 minIdle 个连接。
        for (Connection conn : conns) {
            connectionPool.releaseConnection(conn);
        }
        conns.clear();

        // 关闭连接池会销毁所有池对象。
        // connectionPool.close();

        // 开启线程池让 main 函数不结束。
        new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
