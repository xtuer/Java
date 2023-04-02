package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 从文件中提取 SQL 语句。
 */
public class SqlExtractor {
    /**
     * 使用分片技术从 reader 中提取 SQL 进行处理。
     *
     * @param reader 读取 SQL 的 reader。
     * @param batchLineCount 每次读取文件的行数。
     * @param stopped 是否结束的标记，例如在 sqlHandler 中发生异常的时候设置 stopped 为 true 不继续读取后面的 SQL。
     * @param sqlHandler SQL 语句处理对象。
     * @throws IOException 读取文件异常。
     */
    public static void extractSqls(BufferedReader reader,
                                   int batchLineCount,
                                   AtomicBoolean stopped,
                                   Consumer<List<String>> sqlHandler) throws IOException {
        /*
         业务逻辑:
         1. 读取 lineCount 行字符串到 buffer。
         2. 从 buffer 中提取 SQL 语句。
         3. 从 buffer 中删除被提取过的 SQL 释放内存。
         4. 文件的内容读取完后，buffer 中剩下的部分为最后一个 SQL 语句的内容。
         5. 处理本次提取到的 SQL。
         */
        boolean canRead = true;
        StringBuilder buffer = new StringBuilder();

        while (canRead && !stopped.get()) {
            // [1] 读取 lineCount 行字符串到 buffer。
            canRead = readLines(reader, buffer, batchLineCount);

            // [2] 从 buffer 中提取 SQL 语句。
            List<String> sqls = new LinkedList<>();
            int pivot = extractSqls(buffer, sqls); // buffer 中最后一个分号的下一个位置 (分号的位置 +1)。

            // [3] 从 buffer 中删除被提取过的 SQL 释放内存。
            buffer.delete(0, pivot);

            // [4] 文件的内容读取完后，buffer 中剩下的部分为最后一个 SQL 语句的内容。
            if (!canRead) {
                String lastSql = buffer.toString().trim();
                if (!lastSql.equals("")) {
                    sqls.add(lastSql);
                }
            }

            // [5] 处理本次提取到的 SQL。
            sqlHandler.accept(sqls);
        }
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
     * @return 返回最后一个分号的下一个位置 (分号的位置 +1)。
     */
    private static int extractSqls(StringBuilder buffer, List<String> sqls) {
        /*
         业务逻辑:
         1. 单引号 ' 处理:
            1.1. 在注释中、双引号字符串中就继续读取
            1.2. 第一个单引号则说明是单引号字符串开始
            1.3. 在单引号字符串中:
                 1.3.1. 如果前一个字符是 \ 则说明是转义字符
                 1.3.2. 如果下一个字符是 ' 则说明是转义字符
                 1.3.3. 其他情况为单引号字符串结束
         2. 双引号 " 处理: <参考单引号 ' 处理>
         3. 斜杠 / 处理:
            3.1. 单行注释中、单行字符串中、多行字符串中则继续
            3.2. 在多行注释中，且前一个字符是 * 则是多行注释结束
            3.3. 非多行注释中，且下一个字符是 * 则是多行注释开始
         4. 横杠 - 处理:
            4.1. 在注释、字符串中则继续读取
            4.2. 第一个 -，如果后面是 "- " (不包含双引号) 则是单行注释开始
         5. 回车 \r 处理:
            5.1. 下一个字符是 \n 则 cur++，走 [6] 处理行结束
            5.2. 下一个字符不是 \n 则是行结束，走 [6] 处理行结束
         6. 换行 \n 处理: 如果在单行注释中，则单行注释结束
         7. 分号 ; 处理: 不在字符串和注释中则是语句分隔符。
         */
        /*
         SQL 语句:
         -- 单行注释
         /* 块注释 (* 后面没有空格) * /
         insert into test(id, name) values(3, 'Biao''s Huang');
         insert into test(id, name) values(4, 'Biao\'s Huang');
         insert into test(id, name) values(5, 'Biao"s Huang');
         insert into test(id, name) values(6, "Biao's Huang");
         insert into test(id, name) values(7, "Biao\"s Huang");
         insert into test(id, name) values(8, "Biao""s Huang");

         回车换行:
         在 Windows 操作系统中，回车换行符是 "\r\n"（carriage return 和 line feed 的组合）。
         在 Linux 和类 Unix 操作系统（例如 macOS 和 Ubuntu）中，回车换行符是 "\n"（line feed）。
         在老的 MacOS 系统中，回车换行符是 "\r"（carriage return）。
         */

        int sqlStart = 0; // 下一个 SQL 语句的开始位置
        boolean inSingleComment     = false; // 在单行注释中
        boolean inBlockComment      = false; // 在多行注释中
        boolean inSingleQuoteString = false; // 在单引号字符串中
        boolean inDoubleQuoteString = false; // 在双引号字符串中
        int len = buffer.length();

        // cur 为当前处理的字符位置。
        for (int cur = 0; cur < len; cur++) {
            char c = buffer.charAt(cur);
            switch (c) {
                case '\'':
                    // [1] 单引号 ' 处理
                    // [1.1] 在注释中、双引号字符串中就继续读取
                    if (inSingleComment || inBlockComment || inDoubleQuoteString) {
                        break;
                    }

                    // [1.2] 第一个单引号则说明是单引号字符串开始
                    if (!inSingleQuoteString) {
                        inSingleQuoteString = true;
                        break;
                    }

                    // [1.3] 在单引号字符串中:
                    // [1.3.1] 如果前一个字符是 \ 则说明是转义字符
                    if (buffer.charAt(cur-1) == '\\') {
                        break;
                    }
                    // [1.3.2] 如果下一个字符是 ' 则说明是转义字符
                    if (cur < len-1 && buffer.charAt(cur+1) == '\'') {
                        cur++;
                        break;
                    }

                    // [1.3.3] 其他情况为单引号字符串结束
                    inSingleQuoteString = false;
                    break;
                case '"':
                    // [1] 双引号 " 处理:
                    // [1.1] 在注释中、单引号字符串中就继续读取
                    if (inSingleComment || inBlockComment || inSingleQuoteString) {
                        break;
                    }

                    // [1.2] 第一个双引号则说明是双引号字符串开始
                    if (!inDoubleQuoteString) {
                        inDoubleQuoteString = true;
                        break;
                    }

                    // [1.3] 在双引号字符串中:
                    // [1.3.1] 如果前一个字符是 \ 则说明是转义字符
                    if (buffer.charAt(cur-1) == '\\') {
                        break;
                    }
                    // [1.3.2] 如果下一个字符是 " 则说明是转义字符
                    if (cur < len-1 && buffer.charAt(cur+1) == '\"') {
                        cur++;
                        break;
                    }

                    // [1.3.3] 其他情况为双引号字符串结束
                    inDoubleQuoteString = false;
                    break;
                case '/':
                    // [3] 斜杠 / 处理:
                    // [3.1] 单行注释中、单行字符串中、多行字符串中则继续
                    if (inSingleComment || inSingleQuoteString || inDoubleQuoteString) {
                        break;
                    }

                    // [3.2] 在多行注释中，且前一个字符是 * 则是多行注释结束
                    if (inBlockComment && buffer.charAt(cur-1) == '*') {
                        inBlockComment = false;
                        break;
                    }

                    // [3.3] 非多行注释中，且下一个字符是 * 则是多行注释开始
                    if (!inBlockComment && cur < len-1 && buffer.charAt(cur+1) == '*') {
                        inBlockComment = true;
                        cur++;
                        break;
                    }

                    break;
                case '-':
                    // [4] 横杠 - 处理:
                    // [4.1] 在注释、字符串中则继续读取
                    if (inSingleComment || inBlockComment || inSingleQuoteString || inDoubleQuoteString) {
                        break;
                    }

                    // [4.2] 第一个 -，如果后面是 "- " (不包含双引号) 则是单行注释开始
                    if (cur < len-2 && buffer.charAt(cur+1) == '-' && buffer.charAt(cur+2) == ' ') {
                        inSingleComment = true;
                        cur += 2;
                        break;
                    }

                    break;
                case '\r':
                    // [5] 回车 \r 处理:
                    // [5.1] 下一个字符是 \n 则 cur++，走 [6] 处理行结束
                    // [5.2] 下一个字符不是 \n 则是行结束，走 [6] 处理行结束
                    if (cur < len-1 && buffer.charAt(cur+1) == '\n') {
                        cur++;
                    }
                    break;
                case '\n':
                    // [6] 换行 \n 处理: 如果在单行注释中，则单行注释结束
                    if (inSingleComment) {
                        inSingleComment = false;
                    }
                    break;
                case ';':
                    // [7] 分号 ; 处理: 不在字符串和注释中则是语句分隔符。
                    if (!inSingleComment && !inBlockComment && !inSingleQuoteString && !inDoubleQuoteString) {
                        int sqlEnd = cur + 1;
                        String sql = buffer.substring(sqlStart, sqlEnd).trim();
                        sqls.add(sql);

                        // 下一个 SQL 语句开始的位置。
                        sqlStart = sqlEnd;
                    }
                    break;
                default:
            }
        }

        return sqlStart;
    }
}
