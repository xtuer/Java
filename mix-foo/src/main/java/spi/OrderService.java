package spi;

public class OrderService implements Service {
    public OrderService() {
        System.out.println("构造函数 OrderService#OrderService()");
    }

    @Override
    public void serve() {
        System.out.println("OrderService#serve()");
    }
}
