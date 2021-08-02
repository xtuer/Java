package algorithm;

import java.util.HashSet;
import java.util.Set;

public class SnowFlakeIdWorkerTest {
    static class IdWorkThread implements Runnable {
        private Set<Long> set;
        private SnowflakeIdWorker idWorker;

        public IdWorkThread(Set<Long> set, SnowflakeIdWorker idWorker) {
            this.set = set;
            this.idWorker = idWorker;
        }

        @Override
        public void run() {
            while (true) {
                for(int x=0; x<10000; x++){
                    long id = idWorker.nextId();

                    if (!set.add(id)) {
                        System.out.println("duplicate:" + id);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Set<Long> set = new HashSet<>();

        // 单例：多个线程使用同一个 idWorker 不会生成重复的 ID
        final SnowflakeIdWorker idWorker = new SnowflakeIdWorker(1, 1);

        for(int x=0; x<500; x++){
            Thread t = new Thread(new IdWorkThread(set, idWorker));
            t.start();
        }
    }
}
