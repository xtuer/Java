package pg;

import org.junit.Test;
import xtuer.procedure.Function;
import xtuer.procedure.FunctionFetcher;
import xtuer.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ProcedureFetchTest {
    static final String DB_URL  = "jdbc:postgresql://192.168.12.19:33005/postgres";
    static final String USER    = "postgres";
    static final String PASS    = "123456";
    static final String CATALOG = "postgres";
    static final String SCHEMA  = "public";

    // 测试获取函数 setof。
    @Test
    public void testFuncSetof() throws SQLException {
        // 只有一个返回值，返回值在前面
        // CREATE OR REPLACE FUNCTION func_setof1(firstId integer) RETURNS setof integer
        // [RETURN:4:returnValue, IN:1:firstid]
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_setof1");
            function.printOriginalArgTypes();
            // Utils.dump(function);
        }
    }

    // 测试有返回值的函数。
    @Test
    public void testFuncWithReturn() throws SQLException {
        // 返回值在前面: returnValue + 4
        // CREATE OR REPLACE FUNCTION func_add_with_return(a NUMERIC, b NUMERIC) RETURNS NUMERIC
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_with_return");
            // Utils.dump(function);
        }
    }

    // 测试获取函数返回值为 cursor。
    @Test
    public void testFuncWithReturnCursor() throws SQLException {
        // "COLUMN_TYPE" : 4,
        // "DATA_TYPE" : 2012,
        // "TYPE_NAME" : "refcursor",
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_with_return_cursor");
            function.printOriginalArgTypes();
        }
    }

    // 测试有 OUT 参数的函数，没有返回值。
    @Test
    public void testFuncWithOutOne() throws SQLException {
        // 返回值在前面，且有 OUT 参数在后面，需要把前面的返回值去掉。
        // CREATE FUNCTION func_with_out(IN x int, IN y int, OUT sum int)
        // [RETURN, IN, IN, OUT]
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_with_inout");
            // Utils.dump(function);
            function.printOriginalArgTypes();
        }
    }
    @Test
    public void testFuncWithOutMulti() throws SQLException {
        // 没有返回值，有 OUT 参数在后面。
        // CREATE FUNCTION func_with_out_multi(x int, y int, OUT sum int, OUT product int)
        // [IN, IN, OUT, OUT]
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_with_out_multi");
            // Utils.dump(function);
            function.printOriginalArgTypes();
        }
    }

    // [*] 后面有返回值，前面也有返回值，干掉前面的 "COLUMN_NAME" : "returnValue" 和 "COLUMN_TYPE" : 4
    // 测试获取函数 table: 有入参。
    @Test
    public void testFuncTableWithParam() throws SQLException {
        // 有多个返回值，返回值在后面
        // CREATE FUNCTION func_table_with_param(firstId int) RETURNS TABLE(id int, name text)
        // [RETURN, IN, RETURN]
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_table_one_column_with_param");
            // Utils.dump(function);
            function.printOriginalArgTypes();
        }
    }

    // 测试获取函数 table: 没有入参。
    @Test
    public void testFuncTableWithoutParam() throws SQLException {
        // 有多个返回值，返回值在后面
        // CREATE FUNCTION func_table_without_param() RETURNS TABLE(id int, name text)
        // [RETURN, RETURN]
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            Function function = FunctionFetcher.fetch(conn, CATALOG, SCHEMA, "func_table_one_column_without_param");
            function.printOriginalArgTypes();
            // Utils.dump(function);
        }
    }
}
