public class Test {
    static final String DB_URL = "jdbc:mysql://192.168.12.21:35004/test?useSSL=false";
    static final String USER   = "root";
    static final String PASS   = "mypass";

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(() -> {
            System.out.println("Hello");
            throw new RuntimeException("no...");
        });
        t.setUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println(throwable.getMessage());
        });
        t.start();

        Thread.sleep(2000);
        System.out.println(t.getUncaughtExceptionHandler());
    }
}
