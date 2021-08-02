import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import algorithm.Sort;

/**
 * 排序测试:
 * 1. 每个测试开始前生成随机数组
 * 2. 排序
 * 3. 每个测试结束后测试数组是否升序排序, 如果不是则抛出异常
 */
public class SortTest {
    private int[] a;

    @Before
    public void setup() {
        a = generateValues(16);
        System.out.println("排序前: " + Arrays.toString(a));
    }

    @After
    public void cleanup() {
        System.out.println("排序后: " + Arrays.toString(a));
        Assert.assertTrue("不是升序排序", isAscSorted(a));
    }

    // 冒泡排序
    @Test
    public void bubbleSortTest() {
        Sort.bubbleSort(a);
    }

    // 插入排序
    @Test
    public void insertSortTest() {
        Sort.insertSort(a);
    }

    // 希尔排序
    @Test
    public void shellSortTest() {
        Sort.shellSort(a);
    }

    /**
     * 选择排序
     */
    @Test
    public void selectionSortTest() {
        Sort.selectionSort(a);
    }

    /**
     * 快速排序
     */
    @Test
    public void quickSortTest() {
        Sort.quickSort(a);
    }

    /**
     * 归并排序
     */
    @Test
    public void mergeSortTest() {
        Sort.mergeSort(a);
    }

    /**
     * 堆排序
     */
    @Test
    public void heapSortTest() {
        Sort.heapSort(a);
    }

    ///////////////////////////////////////////////////////////////////
    // 辅助函数
    ///////////////////////////////////////////////////////////////////
    /**
     * 生成 length 个随机数的数组
     */
    private static int[] generateValues(int length) {
        int[] a = new int[length];
        Random rand = new Random(System.nanoTime());

        for (int i = 0; i < a.length; ++i) {
            a[i] = rand.nextInt(100);
        }

        return a;
    }

    /**
     * 检查是否升序排列
     */
    private static boolean isAscSorted(int[] a) {
        for (int i = 0; i < a.length - 1; ++i) {
            if (a[i+1] < a[i]) {
                return false;
            }
        }

        return true;
    }
}
