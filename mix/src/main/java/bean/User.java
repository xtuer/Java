package bean;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class User {
    private long userId;
    private String username;
    private String email;

    public static void main(String[] args) throws Exception {
        User jack = new User()
                .setUserId(1L)
                .setUsername("Jack")
                .setEmail("jack@gmail.com");
        User clone = new User();

        // Apache 的 BeanUtils 支持链式调用 setter，需要设置 FluentPropertyBeanIntrospector
        // 而 Spring 的 BeanUtils 支持链式调用 setter
        // PropertyUtils.addBeanIntrospector(new FluentPropertyBeanIntrospector());
        BeanUtils.copyProperties(jack, clone);
        System.out.println(clone);
    }
}
