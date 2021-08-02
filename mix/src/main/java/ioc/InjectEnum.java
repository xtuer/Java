package ioc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Getter
@Setter
public class InjectEnum {
    private SerializerFeature[] features;
    private Color color;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("enum.xml");
        InjectEnum obj = context.getBean("injectEnum", InjectEnum.class);

        System.out.println(JSON.toJSONString(obj));
        System.out.println(obj.color);
    }
}
