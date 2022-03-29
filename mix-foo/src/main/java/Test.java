import java.util.concurrent.CountDownLatch;

public class Test {
    public volatile int value = 0; // volatile 不保证原子性，只保证内存可见性

    public static void main(String[] args) throws InterruptedException {
        final int len = 10;
        CountDownLatch latch = new CountDownLatch(len);
        Test t = new Test();

        for (int i = 0; i < len; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10_000; j++) {
                    t.value++;
                }

                latch.countDown();
            }).start();
        }

        latch.await();
        System.out.println(t.value); // 输出值大概在 3  万多，不到 100_000

        // Nono
        // N3
        // N4
    }
}
