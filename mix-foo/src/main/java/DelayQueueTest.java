import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayQueueTest {
    public static void main(String[] args) {
        DelayQueue<DelayElement> queue = new DelayQueue<>();
        queue.add(new DelayElement(100, "Unit"));
        queue.add(new DelayElement(1000, "Pair"));
        queue.add(new DelayElement( 6000, "Quartet"));
        queue.add(new DelayElement(4000, "Triplet"));

        // 在固定的线程里等待可执行事件。
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    while (true) {
                        DelayElement md = queue.take(); // 阻塞等待可执行时间。
                        System.out.println(md);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }

        // 随机产生待执行事件。
        new Thread(() -> {
            Random rand = new Random();
            try {
                while (true) {
                    for (int i = 0; i < 5; i++) {
                        TimeUnit.SECONDS.sleep(1);
                        int delay = rand.nextInt(5000);
                        queue.add(new DelayElement(delay, "Event-" + delay));
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Data
    public static class DelayElement implements Delayed {
        /**
         * 过期时间，只要一到过期时间，就能从 DelayQueue 里获取到。
         */
        private final long elapsedAt;

        private final long createdAt;

        /**
         * 要处理的事件。
         * 实际使用时应该是一个对象，里面定义了事件的类型，可确定事件的 ID 等，以便可用获取事件详情进行业务处理。
         */
        private final String event;

        public DelayElement(long delayInMilliseconds, String event) {
            this.createdAt = System.currentTimeMillis();
            this.elapsedAt = System.currentTimeMillis() + delayInMilliseconds;
            this.event = event;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long diff = elapsedAt - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS); // Converts the given time duration in the given unit to this unit.

            // 这样写虽然业务正确，但会导致 getDelay() 函数被频繁调用。
            // return elapsedAt - System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.elapsedAt - ((DelayElement) o).elapsedAt);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        @Override
        public String toString() {
            return String.format("Thread: %s, 创建时间: %s, 当前时间: %s, 预计执行时间: %s, 事件: %s",
                    Thread.currentThread(),
                    formatter.format(new Date(createdAt)),
                    formatter.format(new Date()),
                    formatter.format(new Date(elapsedAt)),
                    event
            );
        }
    }
}
