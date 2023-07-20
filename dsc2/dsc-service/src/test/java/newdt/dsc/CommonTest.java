package newdt.dsc;

import newdt.dsc.util.Utils;
import org.junit.jupiter.api.Test;

public class CommonTest {
    @Test
    public void testStringReplace() {
        System.out.println(Utils.replace("${name}-${id}", "name", "Alice", "id", 20 ));
        System.out.println(Utils.replace("${name}-${id}", "name", "Alice", "id", 20, 1 ));
    }
}
