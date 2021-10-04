package lock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizedTest {
    private final AtomicBoolean locked = new AtomicBoolean(false);
    private final AtomicInteger times = new AtomicInteger();
    private int count = 0;

    public static void main(String[] args) throws InterruptedException {
        final int THREAD_COUNT = 1000;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        SynchronizedTest test = new SynchronizedTest();

        Runnable r = () -> {
            while (test.count < 10_000_000) {
                synchronized(SynchronizedTest.class) {
                    test.count++;
                    test.times.addAndGet(1);
                }
            }

            latch.countDown();
        };

        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_COUNT; i++) {
            new Thread(r).start();
        }

        // 等待线程结束
        latch.await();

        System.out.println(test.count);
        System.out.println(test.times);

        long end = System.currentTimeMillis();
        System.out.println("Time: " + (end - start) / 1000 + "s");
    }
}
