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
     * @param reader 读取 SQL 的 reader (源 reader 可以是文件的 FileReader 也可以是字符串的 StringReader)。
     * @param lineCount 每次从 reader 中读取的行数 (分批读取，而不是一次读取所有内容)。
     * @param stopped 是否结束的标记，例如在 sqlHandler 中发生异常的时候设置 stopped 为 true 不继续读取后面的 SQL。
     * @param sqlHandler 处理提取到的 SQL 的 consumer，可能会被调用多次。
     * @throws IOException 读取文件异常。
     */
    public static void extractSqls(BufferedReader reader,
                                   int lineCount,
                                   AtomicBoolean stopped,
                                   Consumer<List<String>> sqlHandler) throws IOException {
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
            sqlHandler.accept(sqls);
        }
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
     * @return 返回最后一个 SQL 语句分隔符的分号的下一个位置 (分号的位置 +1)。
     */
    private static int extractSqls(StringBuilder buffer, List<String> sqls) {
        /*
         业务逻辑:
         1. 单引号 ' 处理:
            1.1. 在注释中、双引号字符串中就继续读取
            1.2. 非单引号字符串中，则说明是单引号字符串开始
            1.3. 在单引号字符串中: 如果下一个字符是 ' 则说明是转义字符，否则是单引号字符串结束
         2. 双引号 " 处理: <参考单引号 ' 处理>
         3. 斜杠 / 处理:
            3.1. 单行注释中、单行字符串中、多行字符串中则继续
            3.2. 非多行注释中，且下一个字符是 * 则是多行注释开始
         4. 反斜杠 \ 处理: 在单引号字符串或者双引号字符串中，且下一个字符是 ' 或者 " 则说明是转义字符
         5. 横杠 - 处理:
            5.1. 在注释、字符串中则继续读取
            5.2. 第一个 -，如果后面是 "- " (不包含双引号) 则是单行注释开始
         6. 星号 * 处理: 在多行注释中，且下一个字符是 / 则说明是多行注释结束
         7. 回车 \r 处理:
            7.1. 下一个字符是 \n 则 cur++，走 [8] 处理行结束
            7.2. 下一个字符不是 \n 则是行结束，走 [8] 处理行结束
         8. 换行 \n 处理: 如果在单行注释中，则单行注释结束
         9. 分号 ; 处理: 不在字符串和注释中则是语句分隔符
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

        boolean inSingleComment     = false; // 在单行注释中
        boolean inBlockComment      = false; // 在多行注释中
        boolean inSingleQuoteString = false; // 在单引号字符串中
        boolean inDoubleQuoteString = false; // 在双引号字符串中

        int sqlStart = 0; // SQL 语句的开始位置
        final int len = buffer.length();

        // cur 为当前处理的字符位置。
        for (int cur = 0; cur < len; cur++) {
            char c = buffer.charAt(cur); // 当前字符
            char nextChar = (cur+1 < len ? buffer.charAt(cur+1) : (char) 0); // 当前字符的后面第 1 个字符

            switch (c) {
                case '\'':
                    // [1] 单引号 ' 处理:
                    // [1.1] 在注释中、双引号字符串中就继续读取
                    if (inSingleComment || inBlockComment || inDoubleQuoteString) {
                        break;
                    }

                    // [1.2] 非单引号字符串中，则说明是单引号字符串开始
                    if (!inSingleQuoteString) {
                        inSingleQuoteString = true;
                        break;
                    }

                    // [1.3] 在单引号字符串中: 如果下一个字符是 ' 则说明是转义字符，否则是单引号字符串结束
                    if (nextChar == '\'') {
                        cur++;
                    } else {
                        inSingleQuoteString = false;
                    }

                    break;
                case '"':
                    // [2] 双引号 " 处理:
                    // [2.1] 在注释中、单引号字符串中就继续读取
                    if (inSingleComment || inBlockComment || inSingleQuoteString) {
                        break;
                    }

                    // [2.2] 非双引号字符串中，则说明是双引号字符串开始
                    if (!inDoubleQuoteString) {
                        inDoubleQuoteString = true;
                        break;
                    }

                    // [2.3] 在双引号字符串中: 如果下一个字符是 " 则说明是转义字符，否则是双引号字符串结束
                    if (nextChar == '\"') {
                        cur++;
                    } else {
                        inDoubleQuoteString = false;
                    }

                    break;
                case '/':
                    // [3] 斜杠 / 处理:
                    // [3.1] 单行注释中、单行字符串中、多行字符串中则继续
                    if (inSingleComment || inSingleQuoteString || inDoubleQuoteString) {
                        break;
                    }

                    // [3.2] 非多行注释中，且下一个字符是 * 则是多行注释开始
                    if (!inBlockComment && nextChar == '*') {
                        inBlockComment = true;
                        cur++;
                    }

                    break;
                case '\\':
                    // [4] 反斜杠 \ 处理: 在单引号字符串或者双引号字符串中，且下一个字符是 ' 或者 " 则说明是转义字符
                    if (inSingleQuoteString || inDoubleQuoteString) {
                        if (nextChar == '\'' || nextChar == '"') {
                            cur++;
                        }
                    }

                    break;
                case '-':
                    // [5] 横杠 - 处理:
                    // [5.1] 在注释、字符串中则继续读取
                    if (inSingleComment || inBlockComment || inSingleQuoteString || inDoubleQuoteString) {
                        break;
                    }

                    // [5.2] 第一个 -，如果后面是 "- " (不包含双引号) 则是单行注释开始
                    char nextNextChar = (cur+2 < len ? buffer.charAt(cur+2) : (char) 0); // 当前字符的后面第 2 个字符
                    if (nextChar == '-' && nextNextChar == ' ') {
                        inSingleComment = true;
                        cur += 2;
                    }

                    break;
                case '*':
                    // [6] 星号 * 处理: 在多行注释中，且下一个字符是 / 则说明是多行注释结束
                    if (inBlockComment && nextChar == '/') {
                        inBlockComment = false;
                        cur++;
                    }

                    break;
                case '\r':
                    // [7] 回车 \r 处理:
                    // [7.1] 下一个字符是 \n 则 cur++，走 [8] 处理行结束
                    // [7.2] 下一个字符不是 \n 则是行结束，走 [8] 处理行结束
                    if (nextChar == '\n') {
                        cur++;
                    }
                case '\n':
                    // [8] 换行 \n 处理: 如果在单行注释中，则单行注释结束
                    if (inSingleComment) {
                        inSingleComment = false;
                    }

                    break;
                case ';':
                    // [9] 分号 ; 处理: 不在字符串和注释中则是语句分隔符
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
