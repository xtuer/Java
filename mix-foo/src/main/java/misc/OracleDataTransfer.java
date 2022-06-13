package misc;

import com.google.common.base.Preconditions;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Oracle 数据转储
 * 数据库列名使用了关键字怎么办: https://codeantenna.com/a/pwA0qPIKen
 * 在 Oracle 中，字符串常量只能用单引号，不能用双引号
 */
public class OracleDataTransfer {
    static final String DB_URL = "jdbc:oracle:thin:@//192.168.12.16:31002/orcl";
    static final String USER   = "system";
    static final String PASS   = "system";

    public static void main(String[] args) throws Exception {
        transferData("dbmon", "select * from test");
        // insertData();
    }

    public static void insertData() throws SQLException {
        try (Connection conn = createConnection()) {
            conn.setSchema("dbmon");
            String sql = "INSERT INTO TEST(\"NAME\") VALUES ('John')";
            Statement stmt = conn.createStatement();
            int count = stmt.executeUpdate(sql);
            System.out.println(count);
        }
    }

    /**
     * 数据转储 (只支持单表操作，直接使用 JDBC 操作方便使用 ResultSet，不要使用 SqlExecute 服务)
     */
    public static void transferData(String schema, String selectSql) throws SQLException {
        /*
         转储逻辑:
         1. 从 SQL 语句中获取表名
         2. 查询得到结果集
         3. 从结果集中得到所有列名
         4. 获取建表语句
         5. 构建新的建表语句
         6. 在转储的目标库创建表
         7. 把结果集的数据插入到转储的表中 (直接使用 ResultSet 避免了不同数据类型的手动转换)
         */
        try (Connection conn = createConnection()) {
            conn.setSchema(schema);

            // [1] 从 SQL 语句中获取表名
            String tableName = OracleDataTransferUtils.extractTableName(selectSql);
            Preconditions.checkArgument(tableName != null, "查询语句不符合单表查询要求");

            // [2] 查询得到结果集
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectSql);
            System.out.println(selectSql);

            // 如果没有记录则返回，不进行数据的转储
            if (!rs.next()) {
                System.out.println("表中无数据，不进行转储");
                return;
            }

            // [3] 从结果集中得到所有列名
            // [4] 获取建表语句
            // [5] 构建新的建表语句
            Set<String> columnNames = OracleDataTransferUtils.getColumnNames(rs);
            String originalCreateTableSql = OracleDataTransferUtils.findCreateTableSql(conn, schema, tableName);
            Map<String, String> newCreateTableSqlMap = OracleDataTransferUtils.generateDataTransferCreateTableSql(tableName, columnNames, originalCreateTableSql);

            // 连接目标库
            try (Connection conn2 = createConnection()) {
                conn2.setSchema("DBMON");

                // [6] 在转储的目标库创建表
                String createTableSql = newCreateTableSqlMap.get(OracleDataTransferUtils.CREATE_TABLE_SQL);
                Statement s2 = conn2.createStatement();
                s2.executeUpdate(createTableSql);
                s2.close();
                System.out.println("创建数据库表: \n" + createTableSql);

                // [7] 把结果集的数据插入到转储的表中 (直接使用 ResultSet 避免了不同数据类型的手动转换)
                // INSERT INSERT INTO ${tableName} ("COL1, "COL2", "COL3") VALUES(?, ?, ?)
                // PreparedStatement.setObject(i, rs.getObject(colName))
                // 可以如 500 条开启一个事务进行提交
                String insertSql = OracleDataTransferUtils.generatePreparedInsertSql(newCreateTableSqlMap.get("tableName"), columnNames);
                PreparedStatement pstmt = conn2.prepareStatement(insertSql);
                System.out.println("插入语句: " + insertSql);

                int executeCount = 0;
                int len = 500;

                do {
                    int colIndex = 0;
                    for (String col : columnNames) {
                        pstmt.setObject(++colIndex, rs.getObject(col));
                    }
                    pstmt.addBatch();

                    // len 次作为一次批量提交
                    if (++executeCount % len == 0) {
                        pstmt.executeBatch();
                        System.out.printf("插入数据到 %d 条", executeCount);
                    }
                } while (rs.next());

                // 非 len 整数倍则说明最后一次批量未执行
                if (executeCount % len != 0) {
                    pstmt.executeBatch();
                    System.out.printf("插入数据到 %d 条", executeCount);
                }
            }
        }
    }

    /**
     * 创建数据库连接
     *
     * @return 返回数据库连接
     */
    public static Connection createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASS);
        props.setProperty("oracle.jdbc.ReadTimeout", 10_000 + ""); // 10 秒
        props.setProperty("oracle.net.CONNECT_TIMEOUT", 10_000 + "");

        return DriverManager.getDriver(DB_URL).connect(DB_URL, props);
    }
}
