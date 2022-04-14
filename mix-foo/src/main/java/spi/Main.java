package spi;

import java.util.ServiceLoader;

/**
 * SPI 涉及到的 3 个类型: Service, ServiceProvider, ServiceLoader
 * SPI 的 ServiceProvider 配置文件: META-INF/services/${interface full qualified name}
 *     META-INF/services/spi.Service
 */
public class Main {
    public static void main(String[] args) {
        ServiceLoader<Service> loader = ServiceLoader.load(Service.class);

        for (Service service : loader) {
            service.serve();
        }
    }
}
