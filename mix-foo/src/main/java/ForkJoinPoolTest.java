import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinPoolTest {
    public static void main(String[] args) {
        Random random = new Random();
        int[] array = new int[2000];

        for (int i = 0; i < array.length; i++) {
            array[i] = random.nextInt(300);
        }

        // 直接计算结果。
        int sum1 = Arrays.stream(array).sum();

        // 使用 ForkJoin 计算结果。
        // [*] 创建最上层的任务对象，提交给 ForkJoinPool.commonPool().invoke() 执行。
        SumTask task = new SumTask(array, 0, array.length);
        int sum2 = ForkJoinPool.commonPool().invoke(task);

        System.out.println("直接计算结果: " + sum1);
        System.out.println("ForkJoin 计算结果: " + sum2);
    }
}

class SumTask extends RecursiveTask<Integer> {
    final int[] array;
    final int start;
    final int end;

    // [start, end)
    public SumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end   = end;
    }

    @Override
    protected Integer compute() {
        // [1] 不够拆分时就直接进行计算: 递归的结束条件。
        if (end - start < 500) {
            int sum = 0;
            for (int i = start; i < end; i++) {
                sum += array[i];
            }

            return sum;
        }

        // [2] 足够多就进行拆分:
        // [A] 创建 2 个子任务 (也可多个)。
        int mid = start + (end - start) / 2;
        SumTask sub1 = new SumTask(array, start, mid);
        SumTask sub2 = new SumTask(array, mid, end);

        // [B] 调用 invokeAll() 执行所有的子任务。
        invokeAll(sub1, sub2);

        // [C] 调用子任务的 join() 等待计算完成进行结果合并。
        return sub1.join() + sub2.join();
    }
}
