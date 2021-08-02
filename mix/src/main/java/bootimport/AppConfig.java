package bootimport;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration 与 ImportBeanDefinitionRegistrar 相似，可以自定义 Bean，但
 *     A. Configuration 直观，手动创建 Bean 比较合适
 *     B. ImportBeanDefinitionRegistrar 可以使用字符串的方式进行配置，读取配置文件来配置时更合适
 * ImportSelector 使用类的全路径字符串注册一个默认的 Bean
 *
 * EnableTom 的方式，在他里面在 @Import 上面几种实现之一，语义上更好
 *
 * 提示: 不在包扫描路径下，类使用 @Configuration 注解也是有意义的，虽然不会自动生成对象到 Spring Context 中，但符合 Spring 的编程规范。
 *      没有使用 @Configuration 的类，放到 @Import(X.class) 中也会创建他的对象，并且会创建 @Bean 注解的方法的对象。
 *      一般需要 @Import 的类，应该不在 Spring 的包扫描路径下，否则都自动生成对象了，还要 @Import 做什么呢。
 */
@Configuration
// @Import(CustomConfiguration.class) // [1] 注意: 按标准被导入的 Class 需要有 @Configuration 注解，但实际上可以没有，里面的 @Bean 也生效
// @Import(CustomSelector.class) // [2] 例如可以用来创建 XxxAutoConfiguration 里定义的类
// @Import(CustomRegistrar.class) // [3]
// @Import(Tom.class) // [4] 直接 Import 一个普通 Bean 也是可以的，但不符合 Spring 的编程规范
@EnableTom // [5] SpringBoot 的 Enable 模式，替代上面几种方式
public class AppConfig {
}
