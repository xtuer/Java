package spi;

public class PaymentService implements Service {
    public void serve() {
        System.out.println("PaymentService#serve()");
    }
}
