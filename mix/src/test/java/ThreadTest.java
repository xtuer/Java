import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ThreadTest {
    // 主线程结束，在他里面创建的线程被强制结束
    @Test
    public void test() throws InterruptedException {
        new Thread(() -> {
           for (int i = 0; i < 10; i++) {
               System.out.println(System.currentTimeMillis());

               try {
                   TimeUnit.SECONDS.sleep(1);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        }).start();

        TimeUnit.SECONDS.sleep(5);
    }
}
