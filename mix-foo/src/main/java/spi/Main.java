package spi;

import java.util.ServiceLoader;

/**
 * SPI 涉及到的 3 个类型: Service, ServiceProvider, ServiceLoader
 * SPI 的 ServiceProvider 配置文件: META-INF/services/${interface full qualified name}
 *     META-INF/services/spi.Service
 *
 * 提示: MySQL connector 高一点的版本就使用了 SPI 的机制加载驱动，com.mysql.jdbc.Driver 的 static 代码块中注册驱动，
 * 也即是只要把 MySQL connector 放到 classpath 里，然后代码中使用 DriverManager.getConnection 时会
 * 调用方法 ensureDriversInitialized 使用 SPI 加载 java.sql.Driver 的实现类，在其 static 代码块中注册 JDBC 驱动，
 * 不需要我们再手动的使用 Class.forName 加载 JDBC 驱动类了。
 */
public class Main {
    public static void main(String[] args) {
        ServiceLoader<Service> loader = ServiceLoader.load(Service.class);

        for (Service service : loader) {
            service.serve();
        }
    }
}
