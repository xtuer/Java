package misc;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;

/**
 * 数据库连接池。
 */
public class DatabaseConnectionPool {
    private final ObjectPool<Connection> pool;

    public DatabaseConnectionPool() {
        // 创建连接池配置。
        GenericObjectPoolConfig<Connection> cfg = new GenericObjectPoolConfig<>();
        cfg.setMaxTotal(10); // 池里保存连接的最大数量。
        cfg.setMaxIdle(8);
        cfg.setMinIdle(3);   // 最少闲置 (可用) 的连接数，如果连接被 evict 踢出了，还会继续创建连接保持池里最少有 3 个连接。
        cfg.setMaxWait(Duration.ofSeconds(3)); // 获取连接时，没有可用连接，最多等待 N 秒，超时抛异常，默认 30 分钟。

        // 下面 3 个参数配合一起使用，详情参考下面的配置解释，默认不开启。
        cfg.setTimeBetweenEvictionRuns(Duration.ofSeconds(5));
        cfg.setSoftMinEvictableIdleTime(Duration.ofSeconds(4));
        cfg.setMinEvictableIdleTime(Duration.ofSeconds(10));

        // 创建连接池。
        pool = new GenericObjectPool<>(new ConnectionFactory(), cfg);
    }

    public Connection getConnection() throws Exception {
        // 从连接池获取连接对象。
        return pool.borrowObject();
    }

    public void releaseConnection(Connection conn) throws Exception {
        // 把连接归还给连接池。
        pool.returnObject(conn);
    }

    public void close() {
        // 关闭连接池，调用 destroyObject 销毁所有池对象。
        pool.close();
    }

    // 创建连接的工厂类。
    public static class ConnectionFactory extends BasePooledObjectFactory<Connection> {
        static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/test?useSSL=false";
        static final String USER   = "root";
        static final String PASS   = "root";
        static int sn = 0;

        @Override
        public Connection create() throws SQLException {
            // 创建真实的连接。
            ++sn;
            System.out.println("+--> 创建链接: " + sn);
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setClientInfo("sn", String.valueOf(sn));
            return conn;
        }

        @Override
        public void destroyObject(final PooledObject<Connection> conn) throws SQLException {
            // 例如归还连接后，池里连接的数量超过了 maxIdle 则会调用 destroyObject 关闭连接。
            System.out.println("---x 关闭连接: " + conn.getObject().getClientInfo("sn"));
            conn.getObject().close();
        }

        @Override
        public PooledObject<Connection> wrap(Connection conn) {
            return new DefaultPooledObject<>(conn);
        }
    }
}
