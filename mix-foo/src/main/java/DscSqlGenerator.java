import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DscSqlGenerator {
    public static void main(String[] args) throws Exception {
        int[] sizes = {5000, 10000, 20000, 30000, 40000, 50000, 60000, 70000 };

        for (int s : sizes) {
            generateInsertSqls(s);
            generateUpdateSqls(s);
        }
    }

    public static void generateInsertSqls(int size) throws IOException {
        // 生成插入语句
        try (PrintWriter writer = new PrintWriter(new FileWriter(String.format("/Users/biao/Desktop/sqls/insert-sql-%d.sql", size)))) {
            for (int i = 1; i <= size; i++) {
                String sql = String.format("insert into test_performance (id, name, email, phone, info) values (%d, 'alice-%d', 'alice@gmail.com', '1234567', 'If you have a programming foundation, you know that variables are placed in memory. Assign a value to a variable and the variable will remain unchanged.');", i, i);
                writer.println(sql);
            }
        }
    }

    public static void generateUpdateSqls(int size) throws IOException {
        // 生成更新语句
        try (PrintWriter writer = new PrintWriter(new FileWriter(String.format("/Users/biao/Desktop/sqls/update-sql-%d.sql", size)))) {
            for (int i = 1; i <= size; i++) {
                String sql = String.format("update test_performance set email='alice-%d@gmail.com', phone='0086-1234567', info='%d: If you have a programming foundation, you know that variables are placed in memory. Assign a value to a variable and the variable will remain unchanged.' where id=%d;", i, i, i);
                writer.println(sql);
            }
        }
    }
}
