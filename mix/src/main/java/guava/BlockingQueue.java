package guava;

import com.google.common.util.concurrent.Monitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 使用 Guava 的 Monitor 实现阻塞队列，可比较 {@link java.util.concurrent.locks.ReentrantLock} 的<br> {@link java.util.concurrent.locks.Condition} 文档中的案例。
 */
public class BlockingQueue {
    private final List<Integer> queue = new LinkedList<>();
    private final Monitor monitor = new Monitor();
    private final Monitor.Guard notFull;
    private final Monitor.Guard notEmpty;

    public BlockingQueue(int max) {
        notFull = monitor.newGuard(() -> queue.size() < max);
        notEmpty = monitor.newGuard(() -> queue.size() > 0);
    }

    /**
     * 把传入的元素放到队列尾部，如果队列满则阻塞直到有空间放入传入的元素。
     *
     * @param e 放入队列尾的元素
     */
    public void put(Integer e) throws InterruptedException {
        try {
            monitor.enterWhen(notFull);
            queue.add(e);
        } finally {
            monitor.leave();
        }
    }

    /**
     * 获取队列头部的元素，如果队列空则阻塞直到队列中有元素。
     *
     * @return 返回队列头部元素
     */
    public Integer take() throws InterruptedException {
        try {
            monitor.enterWhen(notEmpty);
            return queue.remove(0);
        } finally {
            monitor.leave();
        }
    }

    public static void main(String[] args) {
        // [0] 阻塞队列
        BlockingQueue queue = new BlockingQueue(10);
        Random random = new Random();

        // [1] 生产者线程
        new Thread(() -> {
            while (true) {
                try {
                    int e = random.nextInt(10000);
                    queue.put(e);
                    System.out.println("生产: " + e);
                    TimeUnit.SECONDS.sleep(random.nextInt(3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // [2] 消费者线程
        new Thread(() -> {
            while (true) {
                try {
                    System.out.println("消费: " + queue.take());
                    TimeUnit.SECONDS.sleep(random.nextInt(3));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
