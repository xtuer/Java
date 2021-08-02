package algorithm;

import java.util.Arrays;
import java.util.Random;

public class Sort {
    public static void main(String[] args) {
        int[] a, b, c, d;

        System.out.println("Insert sort:");
        a = generateValues(10);
        insertSort(a);
        System.out.println(Arrays.toString(a));

        System.out.println("Quick sort:");
        a = generateValues(10);
        quickSort(a);
        System.out.println(Arrays.toString(a));

        System.out.println("Heap sort:");
        b = generateValues(10);
        heapSort(b);
        System.out.println(Arrays.toString(b));

        System.out.println("Merge Sorted Arrays:");
        c = mergeSortedArrays(a, b);
        System.out.println(Arrays.toString(c));

        System.out.println("Smallest k values:");
        c = generateValues(10);
        d = topK(c, 5);
        quickSort(c);
        System.out.println(Arrays.toString(c));
        System.out.println(Arrays.toString(d));
    }

    /**
     * Generate an array with random integer values
     *
     * @param length is the length of the array
     * @return an array with random integer values
     */
    public static int[] generateValues(int length) {
        int[] a = new int[length];
        Random rand = new Random(System.nanoTime());

        for (int i = 0; i < a.length; ++i) {
            a[i] = rand.nextInt(100);
        }

        return a;
    }

    /**
     * 冒泡排序
     */
    public static void bubbleSort(int[] a) {
        for (int i = 1; i < a.length; ++i) {
            for (int j = 0; j < a.length-i; ++j) {
                // 如果 a[j] 大于 a[j+1], 则把大的交换到后面
                if (a[j] > a[j+1]) {
                    swap(a, j, j+1);
                }
            }
        }
    }

    /**
     * 插入排序
     * Directly insert sort (improved insert sort: shell sort, jump)
     */
    public static void insertSort(int[] a) {
        // for (int i = 1; i < a.length; ++i) {
        //     int current = a[i];
        //     int j = i;
        //
        //     while (j > 0 && current < a[j-1]) {
        //         a[j] = a[j-1];
        //         j--;
        //     }
        //
        //     a[j] = current;
        // }

        for (int i = 1; i < a.length; ++i) {
            for (int j = i-1; j>=0 && a[j]>a[j+1]; --j) {
                swap(a, j, j+1);
            }
        }
    }

    /**
     * 希尔排序
     */
    public static void shellSort(int[] a) {
        for (int gap = a.length / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < a.length; ++i) {
                for (int j = i - gap; j>=0 && a[j]>a[j+gap]; j -= gap) {
                    swap(a, j, j+gap);
                }
            }
        }
    }

    /**
     * 选择排序
     */
    public static void selectionSort(int[] a) {
        for (int i = 0; i < a.length-1; i++) {
            int minIndex = i;

            for (int j = i+1; j < a.length; ++j) {
                if (a[j] < a[minIndex]) {
                    minIndex = j;
                }
            }

            swap(a, i, minIndex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    // Quick sort
    ///////////////////////////////////////////////////////////////////

    /**
     * 使用快速排序对数组进行排序
     */
    public static void quickSort(int[] a) {
        quickSort(a, 0, a.length - 1);
    }

    /**
     * 使用快速排序对数组 a 中 low 到 high 之间的元素进行排序
     */
    public static void quickSort(int[] a, int low, int high) {
        if (low < high) {
            int pivot = partition(a, low, high);

            quickSort(a, low, pivot-1);
            quickSort(a, pivot+1, high);
        }
    }

    /**
     * 分区: 把数组中小于 a[pivot] 的数放到 pivot 的左边, 大于 a[pivot] 的数放到 pivot 的右边
     *
     * @return 返回分区的下标 pivot
     */
    public static int partition(int[] a, int low, int high) {
        int pivotValue = a[low]; // a[low] 空出来了

        // 大于 pivotValue 的数都放到右边
        // 小于 pivotValue 的数都放到左边
        while (low < high) {
            // 提示: low 和 high 的 ++ 与 -- 没有在赋值的时候进行, 而是在循环内执行,
            // 避免 while 中 low == high 时外部进行 ++, --, 导致 low > high

            // 从右向左找到小于第一个 pivotValue 的数，放到 a[low]，此时则 a[high] 空出来
            while (low < high && a[high] >= pivotValue) {
                --high;
            }
            a[low] = a[high];

            // 从左向右找到大于第一个 pivotValue 的数，放到 a[high]，此时则 a[low] 空出来
            while (low < high && a[low] <= pivotValue) {
                ++low;
            }
            a[high] = a[low];
        }

        // 循环结束时 low == high, pivot 的左边的数都小于 pivotValue, low 右边的数都大于 pivotValue
        int pivot = low;
        a[pivot] = pivotValue;

        return pivot;
    }

    /**
     * 交换数组中指定下标的 2 个元素的值
     */
    public static void swap(int[] a, int indexA, int indexB) {
        int temp  = a[indexA];
        a[indexA] = a[indexB];
        a[indexB] = temp;
    }

    ///////////////////////////////////////////////////////////////////
    // Heap sort
    ///////////////////////////////////////////////////////////////////

    /**
     * 使用堆排序对数组进行排序
     */
    public static void heapSort(int[] a) {
        // 把整个数组调整为大顶堆
        for (int parent = a.length/2-1; parent>=0; --parent) {
            adjustHeap(a, parent, a.length);
        }

        // a[0] 为最大的数，和最后一个交换，再把数组调整为大顶堆
        for (int end = a.length-1; end > 0; --end) {
            swap(a, 0, end);
            adjustHeap(a, 0, end);
        }
    }

    /**
     * 调整 a 为大顶堆, parent 的左右子树已经是大顶堆
     */
    public static void adjustHeap(int[] a, int parent, final int end) {
        int temp = a[parent];

        for (int left = parent*2+1; left < end; left = parent*2+1) {
            // max   存储左右节点中最大数的下标
            // left  存储左子节点的下标
            // right 存储右子节点的下标
            int max   = left;
            int right = left + 1;

            if (right<end && a[left]<a[right]) max = right; // 左右子节点中最大的数
            if (temp >= a[max]) break; // parent 已经是最大值, 说明已经是大顶堆, 不需要再进行调整了

            // a[parent] 存储左右子节点中的最大值, 并把 parent 指向此节点
            a[parent] = a[max];
            parent = max;
        }

        a[parent] = temp;
    }

    // 合并 2 个有序数组
    public static int[] mergeSortedArrays(int[] a, int[] b) {
        int[] r = new int[a.length + b.length];
        int i = 0, j = 0, k = 0;

        while (i<a.length && j<b.length) {
            if (a[i] < b[j]) {
                r[k++] = a[i++];
            } else {
                r[k++] = b[j++];
            }
        }

        while (i < a.length) {
            r[k++] = a[i++];
        }

        while (j < b.length) {
            r[k++] = b[j++];
        }

        return r;
    }

    public static int[] topK(int[] a, int k) {
        if (k >= a.length) {
            return a;
        }

        int[] h = new int[k];
        int i = 0;

        // 取前 k 个元素
        for (; i < k; ++i) {
            h[i] = a[i];
        }

        // 把前 k 个元素调整为大顶堆
        for (int j = h.length / 2; j >= 0; --j) {
            adjustHeap(h, j, h.length);
        }

        // 如果小于堆顶，替换掉堆顶，调整为大顶堆
        for (; i < a.length; ++i) {
            if (a[i] < h[0]) {
                h[0] = a[i];
                adjustHeap(h, 0, h.length);
            }
        }

        return h;
    }

    /**
     * 归并排序
     */
    public static void mergeSort(int[] a) {
        int[] original = a;
        int[] c = new int[a.length];

        for (int n = 1; n < a.length; n += n) {
            for (int i = 0; i < a.length; i += n*2) {
                // 前 n 个元素和后 n 个元素作为一组进行归并 (有序数组合并)
                int aFirst = i;
                int aLast  = aFirst + n - 1;
                int bFirst = aLast  + 1;
                int bLast  = bFirst + n - 1;
                int cFirst = aFirst;

                mergeSortedArray(a, aFirst, aLast, a, bFirst, bLast, c, cFirst);
            }

            // 一轮归并结束后, c 是此轮归并后的数组, 新一轮归并需要在前一轮归并的基础上进行
            // 交互 a 和 c, 使得 a 是归并后的数组
            int[] temp = c;
            c = a;
            a = temp;
        }

        // 归并结束时, a 的数据是有序的, 但是此时 a 不一定指向传入进来的数组, 也就是说 a 不一定等于 original,
        // 需要复制数组 a 到原来的数组 original 中，这样函数外访问传入的数组才是排序后的有序数组 (引用使用传值传递)
        System.arraycopy(a, 0, original, 0, a.length);
    }

    /**
     * 有序数组合并
     */
    public static void mergeSortedArray(int[] a, int aFirst, int aLast,
                                        int[] b, int bFirst, int bLast,
                                        int[] c, int cFirst) {
        int i = aFirst;
        int j = bFirst;
        int k = cFirst;

        while (i<= aLast && j<= bLast && i<c.length && j<c.length) {
            if (a[i] < b[j]) {
                c[k++] = a[i++];
            } else {
                c[k++] = b[j++];
            }
        }

        while (i<= aLast && i<c.length) {
            c[k++] = a[i++];
        }

        while (j<= bLast && j<c.length) {
            c[k++] = b[j++];
        }
    }
}
