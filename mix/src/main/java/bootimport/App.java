package bootimport;

import bean.User;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 练习 Spring 的 @Import，参考 https://www.toutiao.com/i6964714416520692236/
 */
public class App {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Tom tom = context.getBean(Tom.class);
        System.out.println(tom);

        // 手动注册 Bean
        // 使用场景: 创建动态代理对象到 Spring 容器。参考 https://www.cnblogs.com/lgjlife/p/11060570.html。
        User jack = new User().setUserId(1L).setUsername("Jack").setEmail("jack@gmail.com");
        DefaultListableBeanFactory listableBeanFactory = context.getDefaultListableBeanFactory();
        listableBeanFactory.registerSingleton("jack", jack);
        User user = context.getBean("jack", User.class);
        System.out.println(user);
    }
}
