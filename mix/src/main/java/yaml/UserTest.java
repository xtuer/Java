package yaml;

import com.alibaba.fastjson.JSON;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;

public class UserTest {
    public static void main(String[] args) throws IOException {
        unmarshalUserTest1();
        // unmarshalUserTest2();
        // unmarshalUserHolderTest();
    }

    public static void unmarshalUserTest1() {
        Yaml yaml = new Yaml(new Constructor(User.class));
        InputStream in = ClassLoader.getSystemResourceAsStream("user.yml"); // 找不到返回 null
        User user = yaml.load(in);

        System.out.println(JSON.toJSONString(user));
        System.out.println(user.getToken());
    }

    public static void unmarshalUserTest2() throws IOException {
        Yaml yaml = new Yaml();
        InputStream in = ClassLoader.getSystemResourceAsStream("user.yml"); // 找不到返回 null
        User user = yaml.loadAs(in, User.class);
        System.out.println(JSON.toJSONString(user));
    }

    public static void unmarshalUserHolderTest() {
        // 忽略不识别的属性
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        Yaml yaml = new Yaml(new Constructor(UserHolder.class), representer);

        // Yaml yaml = new Yaml(new Constructor(UserHolder.class));
        InputStream in = ClassLoader.getSystemResourceAsStream("user-holder.yml");
        UserHolder user = yaml.load(in);

        System.out.println(JSON.toJSONString(user, true));
    }
}
