package util;

import misc.User;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TablePrinter {
    public static void main(String[] args) {
        // [1] 打印 List<Object>
        List<User> users = new LinkedList<>();
        users.add(new User(1, "Alice"));
        users.add(new User(2, "Bob"));
        TablePrinter.print(users);

        // [2] 打印 List<List>
        List<String> headers = new LinkedList<>(Arrays.asList("Name", "OriginalPosition", "ArgTyeValue", "ArgTypeName", "DataTypeName"));
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList("id", "1", "1", "IN", "int4"));
        rows.add(Arrays.asList("returnValue", "0", "4", "RETURN", null));
        TablePrinter.print(headers, rows);
    }

    /**
     * 把对象数组打印成表格格式，表头为属性名。
     */
    public static <T> void print(List<T> data, String... ignoredFields) {
        /*
         逻辑:
         1. 反射获取属性名，作为表头。
         2. 每个对象作为一行，按属性顺序获取对象的属性值。
         3. 打印为表格。
         */
        List<String> headers = new LinkedList<>();
        List<List<String>> rows = new LinkedList<>();
        List<Field> fields = new LinkedList<>();
        Set<String> ignoredFieldSet = new HashSet<>(Arrays.asList(ignoredFields));

        try {
            // [1] 反射获取属性名，作为表头。
            Class<?> clazz = data.get(0).getClass();
            for (Field field : TablePrinter.getAllFields(clazz)) {
                String fieldName = field.getName();

                // 被忽略的字段和 static 字段不显示。
                if (!ignoredFieldSet.contains(fieldName) && !Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    headers.add(fieldName);
                    fields.add(field);
                }
            }

            // [2] 每个对象作为一行，按属性顺序获取对象的属性值。
            for (Object obj : data) {
                List<String> row = new LinkedList<>();
                for (Field field : fields) {
                    row.add(String.valueOf(field.get(obj)));
                }
                rows.add(row);
            }

            // [3] 打印为表格。
            TablePrinter.print(headers, rows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把数据打印成表格样式。
     */
    public static void print(List<String> headers, List<List<String>> rows) {
        /*
         逻辑:
         1. 克隆数据，避免影响传入的 headers 和 rows。
         2. 每个元素前后加一个空格，输出结果更好看。
         3. 计算每一列的最大宽度。
         4. 表头下加入分割行。
         5. 按行格式化输出。
         */

        List<List<String>> table = new ArrayList<>();

        // [1] 克隆数据，避免影响传入的 headers 和 rows。
        table.add(new ArrayList<>(headers));
        rows.forEach(row -> table.add(new ArrayList<>(row)));

        // [2] 每个元素前后加一个空格，输出结果更好看。
        for (List<String> row : table) {
            row.replaceAll(s -> " " + s + " ");
        }

        // [3] 计算每一列的最大宽度。
        final int columnCount = headers.size();
        final int[] columnWidths = new int[columnCount];

        for (int col = 0; col < columnCount; col++) {
            for (List<String> row : table) {
                columnWidths[col] = Math.max(row.get(col).length(), columnWidths[col]);
            }
        }

        // [4] 表头下加入分割行。
        List<String> separatorRow = new ArrayList<>();
        for (int col = 0; col < columnCount; col++) {
            separatorRow.add(StringUtils.repeat('-', columnWidths[col]));
        }
        table.add(1, separatorRow);

        // [5] 按行格式化输出。
        StringBuilder buf = new StringBuilder();
        for (List<String> row : table) {
            for (int col = 0; col < columnCount; col++) {
                String cell = String.format("|%-" + columnWidths[col] + "s", row.get(col));
                buf.append(cell);
            }
            buf.append("|\n");
        }

        System.out.println(buf);
    }

    /**
     * 递归获取类的所有属性，包括父类定义的属性，父类的父类...。
     */
    public static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();

        while (clazz != null) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }

        return fields;
    }
}
