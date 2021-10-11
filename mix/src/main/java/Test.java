import com.google.common.collect.*;

import java.util.Set;

public class Test {
    public static void main(String[] args) throws Exception {
        Table<String, String, String> table = HashBasedTable.create();
        table.put("Java", "Version", "1.8");
        table.put("Java", "Company", "Oracle");
        System.out.println(table);
        System.out.println(table.row("Java").get("Version"));
    }
}
