import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

// import javax.annotation.Resource;
import java.util.Properties;

@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:spring-beans-config.xml"})
public class TestYamlPropertiesAndPropertiesConfig {
    // @Resource(name = "yamlProperties")
    private Properties yamlProperties;

    // @Resource(name = "propertiesConfig")
    private PropertiesConfiguration propertiesConfig;

    @Test
    public void testYamlProperties() {
        System.out.println(yamlProperties.getProperty("jdbc.url"));
        System.out.println(yamlProperties.getProperty("jdbc.port"));
        System.out.println(yamlProperties.getProperty("username"));
        System.out.println(yamlProperties.getProperty("tenantCode"));
    }

    @Test
    public void testPropertiesConfig() {
        System.out.println(propertiesConfig.getString("username"));
        System.out.println(propertiesConfig.getInteger("age", 0));
    }
}
