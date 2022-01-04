import com.xtuer.TokenWhiteListService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;

// import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:bean.xml"})
public class PropertyTest {
    @Autowired
    private TokenWhiteListService tokenWhiteListService;

    // @Resource(name="ns")
    private List<String> ns;

    @Test
    public void test() {
        tokenWhiteListService.foo();
    }

    @Test
    public void testBean() {
        System.out.println(ns);
    }
}
