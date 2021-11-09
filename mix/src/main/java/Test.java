import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int n = 8;
        int[] mem = new int[n+1];
        fibonacci(n, mem);
        System.out.println(Arrays.toString(mem));
    }

    public static int fibonacci(int n, int[] mem) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            mem[1] = 1;
            return 1;
        }
        if (mem[n] != 0) {
            return mem[n];
        }

        mem[n] = fibonacci(n-1, mem) + fibonacci(n-2, mem);
        return mem[n];
    }
}
