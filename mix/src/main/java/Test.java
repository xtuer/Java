import java.util.Arrays;

public class Test {
    public static void main(String[] args) throws Exception {
        int[] ns = { 1, 2, 3, 4 };
        int[] bs = ns.clone();
        bs[0] = 3;
        System.out.println(Arrays.toString(ns));
        System.out.println(Arrays.toString(bs));
    }
}
