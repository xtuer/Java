package localqueue;

import com.leansoft.bigqueue.BigQueueImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 文件消息队列，生产和消费必须在同一个 JVM 才行，可能是因为防止同一个文件被多个对象同时写造成问题。
 * DMP 中只是为了缓存取到的数据先放起来，然后慢慢计算，避免阻塞取数逻辑。
 * 每个取数服务 JVM 使用自己的文件队列，应该能满足需求 (队列路径可以使用取数服务名作为区分)。
 *
 * 参考 https://www.baeldung.com/java-big-queue
 *
 * 注意: 如果跨 JVM，如果生产者在一个 JVM，消费者在另一个 JVM
 * 1. 生产者生产消息
 * 2. 启动消费者，消费者第一次消费完未消费的消息后，新生产的消息不会被自动发现
 * 3. 重启消费者后仍然会消费完未消费的消息，但是再新生产的消息又发现不了
 */
public class LocalFileMessageQueueOfBigQueue {
    private static final BigQueueImpl QUEUE;

    private static final LongAdder SN = new LongAdder();

    /**
     * 控制队列结束的条件
     */
    private static boolean stopped = false;

    static {
        BigQueueImpl queue = null;
        String queueDir = "/Users/biao/Documents/temp/queue";
        String queueName = "test-queue";

        try {
            queue = new BigQueueImpl(queueDir, queueName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        QUEUE = queue;
    }

    public static void main(String[] args) throws Exception {
        /*
         以下几个测试用例，确定了消息不会丢失，消费过的消息不会重复消费。

         测试一: 边生产，边消费
         1. productMessage() 和 consumeMessage()

         测试二: 生产一段时间，然后只消费
         1. productMessage() 并注释掉 consumeMessage()
         2. 重启程序: consumeMessage() 并注释掉 productMessage()

         测试三: 生产一段时间，然后同时生产和消费
         1. productMessage() 并注释掉 consumeMessage()
         2. 重启程序: productMessage() 和 consumeMessage()
         */

        // QUEUE.gc();
        productMessage();
        consumeMessage();

        // QUEUE.close(); // 程序退出时关闭 queue
    }

    /**
     * 定时生产消息
     */
    public static void productMessage() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            SN.increment();
            String msg = String.format("MSG-%d-%d", SN.intValue(), System.nanoTime());

            try {
                QUEUE.enqueue(msg.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 循环中拉取消费消息
     */
    public static void consumeMessage() throws Exception {
        while (!stopped) {
            // 如果队列为空，则 2 秒后再次查看是否有消息
            if (QUEUE.isEmpty()) {
                System.out.println(">>> 没有消息: " + LocalDateTime.now());
                TimeUnit.SECONDS.sleep(2);
                continue;
            }

            // 读取消息，进行业务处理
            String record = new String(QUEUE.dequeue());
            System.out.println(record);
        }
    }
}
