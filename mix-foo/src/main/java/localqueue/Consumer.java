package localqueue;

import com.leansoft.bigqueue.BigQueueImpl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Consumer {
    public static void main(String[] args) throws InterruptedException, IOException {
        String queueDir = "/Users/biao/Documents/temp/queue";
        String queueName = "test-queue";
        BigQueueImpl bigQueue = new BigQueueImpl(queueDir, queueName);

        // 控制队列结束的条件
        boolean stopped = false;
        while (!stopped) {
            // 如果队列为空，则 2 秒后再次查看是否有消息
            if (bigQueue.isEmpty()) {
                System.out.println(bigQueue.peek());
                System.out.println("没有消息: " + System.currentTimeMillis());
                TimeUnit.SECONDS.sleep(2);
                continue;
            }

            // 读取消息
            String record = new String(bigQueue.dequeue());
            System.out.println(record);
        }

        bigQueue.close();
    }
}
