public class Test {
    public static void main(String[] args) throws InterruptedException {
        // kill PID, kill -1 PID 可以触发 shutdown hook
        // kill -9 PID 不能触发
        // Ctrl + C 强制退出可以触发
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("One");
        }));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Two");
        }));

        Thread.sleep(100000);
    }
}
