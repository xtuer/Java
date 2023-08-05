package newdt.dsc.service.db;

import com.google.common.collect.ImmutableList;
import newdt.dsc.bean.db.DatabaseType;
import org.javatuples.Quartet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试库的连接。
 */
public class TestConnections {
    public static final Map<DatabaseType, List<Quartet<Integer, String, String, String>>> CONNECT_INFO = new HashMap<>();

    static {
        CONNECT_INFO.put(DatabaseType.MYSQL,
                ImmutableList.of(Quartet.with(1, "jdbc:mysql://127.0.0.1:3306/test?useSSL=false", "root", "root")));

        CONNECT_INFO.put(DatabaseType.ORACLE,
                ImmutableList.of(Quartet.with(2, "jdbc:oracle:thin:@//192.168.12.16:31001/orcl", "system", "system")));

        CONNECT_INFO.put(DatabaseType.POSTGRES,
                ImmutableList.of(Quartet.with(3, "jdbc:postgresql://192.168.12.19:33005/postgres", "postgres", "123456")));
    }

    /**
     * 创建获取数据库元数据的连接 (使用 SQL 管控用户)。
     *
     * @param type 数据库类型。
     * @param dbid 数据库的 DBID。
     * @return 返回数据库连接。
     */
    public static Connection openConnection(DatabaseType type, int dbid) throws SQLException {
        for (Quartet<Integer, String, String, String> info : CONNECT_INFO.get(type)) {
            if (info.getValue0().equals(dbid)) {
                String url = info.getValue1();
                String user = info.getValue2();
                String pass = info.getValue3();

                return DriverManager.getConnection(url, user, pass);
            }
        }

        throw new RuntimeException("没找到数据库连接");
    }
}
