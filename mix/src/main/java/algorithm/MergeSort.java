package algorithm;

import org.apache.http.util.Asserts;

import java.util.Arrays;

public class MergeSort {
    public static void mergeSort(int[] a) {
        int[] original = a;
        int[] c = new int[a.length];

        for (int n = 1; n < a.length; n += n) {
            // System.out.println("\nTotal: " + a.length + ", N: " + n);

            for (int i = 0; i < a.length; i += n*2) {
                int aFirst = i;
                int aLast  = aFirst + n - 1;
                int bFirst = aLast + 1;
                int bLast  = Math.min(bFirst+n-1, a.length-1);
                int cFirst = aFirst;

                merge(a, aFirst, aLast, a, bFirst, bLast, c, cFirst, a.length);
            }

            // 交换 a 和 c
            int[] temp = c;
            c = a;
            a = temp;
        }

        // 这时 a 实际指向了 c，它是有序的，c 指向了原来的数组 a, 它还是无序的，
        // 需要复制排序后的数组到原来的数组 a(函数传值的问题)，这样函数外访问 a 时才是排序后的数组
        System.arraycopy(a, 0, original, 0, a.length);
    }

    public static void merge(int[] a, int aFirst, int aLast,
                             int[] b, int bFirst, int bLast,
                             int c[], int cFirst, int end) {
        // System.out.printf("aFirst: %d, aLast: %d, bFirst: %d, bLast: %d, cFirst: %d\n", aFirst, aLast, bFirst, bLast, cFirst);
        int i = aFirst;
        int j = bFirst;
        int k = cFirst;

        while (i<= aLast && i<end && j<= bLast && j<end) {
            if (a[i] < b[j]) {
                c[k++] = a[i++];
            } else {
                c[k++] = b[j++];
            }
        }

        while (i<= aLast && i<end) {
            c[k++] = a[i++];
        }

        while (j<= bLast && j<end) {
            c[k++] = b[j++];
        }

        // System.out.println(Arrays.toString(c));
    }

    public static int[] generateArray(int length) {
        int[] a = new int[length];
        java.util.Random rand = new java.util.Random(System.nanoTime());

        for (int i = 0; i < length; ++i) {
            a[i] = rand.nextInt(1000);
        }

        return a;
    }

    public static boolean checkAsc(int[] a) {
        for (int i = 0; i < a.length - 1; ++i) {
            if (a[i+1] < a[i]) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) {
        int[] a = {3, 1, 4, 2, 56, 7, 9, 6, 23, 11, 57};
        mergeSort(a);
        Asserts.check(checkAsc(a), "Not asc");
        System.out.println(Arrays.toString(a));

        a = generateArray(10);
        mergeSort(a);
        Asserts.check(checkAsc(a), "Not asc");
        System.out.println(Arrays.toString(a));

        a = generateArray(32);
        mergeSort(a);
        Asserts.check(checkAsc(a), "Not asc");
        System.out.println(Arrays.toString(a));

        a = generateArray(100);
        mergeSort(a);
        Asserts.check(checkAsc(a), "Not asc");
        System.out.println(Arrays.toString(a));
    }
}
