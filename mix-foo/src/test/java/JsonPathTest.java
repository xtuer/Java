import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;

import java.util.List;

public class JsonPathTest {
    @Test
    public void testSimple() {
        String json = "{\"name\": \"John Smith\", \"age\": 30, \"email\": \"john@example.com\"}";
        DocumentContext document = JsonPath.parse(json);

        String name = document.read("$.name");
        int age = document.read("$.age");
        String email = document.read("$.email");

        System.out.printf("Name: %s, Age: %d, Email: %s\n", name, age, email);
    }

    @Test
    public void testArray() {
        String json = "{ \"store\": { \"book\": [ { \"title\": \"The Hitchhiker's Guide to the Galaxy\", \"author\": \"Douglas Adams\" }, { \"title\": \"The Restaurant at the End of the Universe\", \"author\": \"Douglas Adams\" }, { \"title\": \"Life, the Universe and Everything\", \"author\": \"Douglas Adams\" } ] } }";

        // Query the JSON document using JSONPath
        List<String> titles = JsonPath.read(json, "$.store.book[*].title");

        // Print the results
        System.out.println("Titles: " + titles);
    }
}
