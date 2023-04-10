package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

/**
 * 从文件中提取 SQL 语句。
 */
public class SqlExtractorUsingStateMachine {
    /**
     * 使用分片技术从 reader 中提取 SQL 进行处理。
     *
     * @param reader 读取 SQL 的 reader (源 reader 可以是文件的 FileReader 也可以是字符串的 StringReader)。
     * @param lineCount 每次从 reader 中读取的行数 (分批读取，而不是一次读取所有内容)。
     * @param stopped 是否结束的标记，例如在 sqlHandler 中发生异常的时候设置 stopped 为 true 不继续读取后面的 SQL。
     * @param sqlHandler 处理提取到的 SQL 的函数，第一个参数为 SQL 列表，第二个参数为是否所有 SQL 都提取完了 (可能会被调用多次)。
     * @throws IOException 读取文件异常。
     */
    public static void extractSqls(BufferedReader reader,
                                   int lineCount,
                                   AtomicBoolean stopped,
                                   BiFunction<List<String>, Boolean, Void> sqlHandler) throws IOException {
        /*
         业务逻辑:
         1. 读取 lineCount 行字符串到 buffer。
         2. 从 buffer 中提取 SQL 语句。
         3. 从 buffer 中删除被提取过的 SQL 释放内存。
         4. 内容读取完后，buffer 中剩下的部分为最后一个 SQL 语句的内容。
         5. 处理本次提取到的 SQL。
         */
        boolean canRead = true;
        StringBuilder buffer = new StringBuilder();

        while (canRead && !stopped.get()) {
            // [1] 读取 lineCount 行字符串到 buffer。
            canRead = readLines(reader, buffer, lineCount);

            // [2] 从 buffer 中提取 SQL 语句。
            List<String> sqls = new LinkedList<>();
            int pivot = extractSqls(buffer, sqls); // pivot 为 buffer 中最后一个 SQL 分隔符分号的下一个位置 (分号的位置 +1)。

            // [3] 从 buffer 中删除被提取过的 SQL 释放内存。
            buffer.delete(0, pivot);

            // [4] 内容读取完后，buffer 中剩下的部分为最后一个 SQL 语句的内容。
            if (!canRead) {
                String lastSql = buffer.toString().trim();
                if (!lastSql.equals("")) {
                    sqls.add(lastSql);
                }
            }

            // [5] 处理本次提取到的 SQL。
            sqlHandler.apply(sqls, !canRead);
        }
    }

    /**
     * 当前解析 SQL 的语句状态。
     */
    private static final int NORMAL              = 0; // 普通状态
    private static final int LINE_COMMENT        = 1; // 单行注释
    private static final int BLOCK_COMMENT       = 2; // 多行注释
    private static final int SINGLE_QUOTE_STRING = 3; // 单引号字符串
    private static final int DOUBLE_QUOTE_STRING = 4; // 双引号字符串

    /**
     * 从传入的 buffer 中提取 SQL 语句，返回值为最后一个分号的下一个位置，在最后一个分号后面还可能不分 SQL 语句内容。
     *
     * 示例:
     * 1. insert into test(id, name) values(3, 'Biao''s ;Huang'); insert into test(id, name) values(4, 'Biao\'s ;Huang');
     *    得到 [insert into test(id, name) values(3, 'Biao''s ;Huang');, insert into test(id, name) values(4, 'Biao\'s ;Huang');]
     *    返回 111
     * 2. insert into test(id, name) values(3, 'Biao''s ;Huang'); insert into test(id, name)
     *    得到 [insert into test(id, name) values(3, 'Biao''s ;Huang');]
     *    返回 55 (55 后面是另一个 SQL 语句的部分内容，需要调用者进行处理，为的是分片读取文件内容追加处理)
     *
     * @param buffer SQL 内容。
     * @param sqls 保存提取到的 SQL 语句。
     * @return 返回最后一个 SQL 语句分隔符的分号的下一个位置 (分号的位置 +1)。
     */
    private static int extractSqls(StringBuilder buffer, List<String> sqls) {
        /*
         业务逻辑:
         1. 读取字符
         2. 普通情况:
            2.1. ' 表示单行字符串开始
            2.2. " 表示单行字符串开始
            2.3. -- 跟随空格表示单行注释开始
            2.4. /* 表示多行注释开始
            2.5. ; 表示 SQL 语句分隔符
         3. 单引号字符串:
            3.1. \ 表示转义字符，吃掉下一个字符
            3.2. ' 表示单行字符串结束
         4. 双引号字符串:
            4.1. \ 表示转义字符，吃掉下一个字符
            4.2 " 表示双引号字符串结束
         5. 单行注释:
            5.1. \n 表示单行字符串结束
            5.2. \r 下一个字符不为 \n 表示单行字符串结束
         6. 多行注释: * 且下一个字符为 / 表示多行注释结束
         */

        /*
         SQL 语句:
         -- 单行注释
         /* 块注释 * / (* 后面没有空格)
         insert into test(id, name) values(1, 'Biao''s ;Huang');
         insert into test(id, name) values(2, 'Biao"s Huang');
         insert into test(id, name) values(3, 'Biao\'s ;Huang');
         insert into test(id, name) values(4, 'Biao\"s ;Huang');
         insert into test(id, name) values(5, "Biao's Huang");
         insert into test(id, name) values(6, "Biao""s Huang");
         insert into test(id, name) values(7, "Biao\'s Huang");
         insert into test(id, name) values(8, "Biao\"s Huang");
         insert into test(id, name) values(9, 'Biao\\s Huang');
         insert into test(id, name) values(10, "Biao\\s Huang");

         回车换行:
         在 Windows 操作系统中，回车换行符是 "\r\n"（carriage return 和 line feed 的组合）。
         在 Linux 和类 Unix 操作系统（例如 macOS 和 Ubuntu）中，回车换行符是 "\n"（line feed）。
         在老的 MacOS 系统中，回车换行符是 "\r"（carriage return）。
         */

        final int len = buffer.length();
        int sqlStart = 0; // SQL 语句的开始位置
        int state = NORMAL; // 解析 SQL 语句的状态

        // curr 为当前处理的字符位置。
        for (int curr = 0; curr < len; curr++) {
            // [1] 读取字符
            char ch = buffer.charAt(curr); // 当前字符
            char nextChar = (curr+1 < len ? buffer.charAt(curr+1) : (char) 0); // 当前字符的后面第 1 个字符

            switch (state) {
                case NORMAL:
                    // [2] 普通情况
                    if (ch == '\'') {
                        // [2.1] ' 表示单行字符串开始
                        state = SINGLE_QUOTE_STRING;
                    } else if (ch == '\"') {
                        // [2.2] " 表示单行字符串开始
                        state = DOUBLE_QUOTE_STRING;
                    } else if (ch == '-' && nextChar == '-') {
                        // [2.3] -- 跟随空格表示单行注释开始
                        char nextNextChar = (curr+2 < len ? buffer.charAt(curr+2) : (char) 0);
                        if (nextNextChar == ' ') {
                            state = LINE_COMMENT;
                            curr += 2;
                        }
                    } else if (ch =='/' && nextChar == '*') {
                        // [2.4] /* 表示多行注释开始
                        state = BLOCK_COMMENT;
                        curr++;
                    } else if (ch == ';') {
                        // [2.5] ; 表示 SQL 语句分隔符
                        int sqlEnd = curr + 1;
                        String sql = buffer.substring(sqlStart, sqlEnd).trim();
                        sqls.add(sql);

                        // 下一个 SQL 语句开始的位置。
                        sqlStart = sqlEnd;
                    }

                    break;
                case SINGLE_QUOTE_STRING:
                    // [3] 单引号字符串
                    // [3.1] \ 表示转义字符，吃掉下一个字符
                    // [3.2] ' 表示单行字符串结束
                    if (ch == '\\') {
                        curr++;
                    } else if (ch == '\'') {
                        state = NORMAL;
                    }
                    break;
                case DOUBLE_QUOTE_STRING:
                    // [4] 双引号字符串
                    if (ch == '\\') {
                        curr++;
                    } else if (ch == '\"') {
                        state = NORMAL;
                    }
                    break;
                case LINE_COMMENT:
                    // [5] 单行注释
                    if (ch == '\n') {
                        // [5.1] \n 表示单行字符串结束
                        state = NORMAL;
                    } else if (ch == '\r' && nextChar != '\n') {
                        // [5.2] \r 下一个字符不为 \n 表示单行字符串结束
                        state = NORMAL;
                    }
                    break;
                case BLOCK_COMMENT:
                    // [6] 多行注释: * 且下一个字符为 / 表示多行注释结束
                    if (ch == '*' && nextChar == '/') {
                        state = NORMAL;
                        curr++;
                    }
                    break;
                default:
                    break;
            }
        }

        return sqlStart;
    }

    /**
     * 从 reader 中读取 lineCount 行字符串到 buffer 里。
     *
     * @param reader 读取文件的 reader。
     * @param buffer 保存读取内容的 buffer。
     * @param lineCount 要读取的行数。
     * @return 如果 reader 中还剩下内容可读取返回 true，否则返回 false。
     * @throws IOException 读取文件异常。
     */
    public static boolean readLines(BufferedReader reader, StringBuilder buffer, final int lineCount) throws IOException {
        for (int i = 0; i < lineCount; i++) {
            String line = reader.readLine();

            if (line != null) {
                buffer.append(line).append("\n");
            } else {
                return false;
            }
        }

        return true;
    }
}
