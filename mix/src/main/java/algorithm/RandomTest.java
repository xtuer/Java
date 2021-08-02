package algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RandomTest {
    public static void main(String[] args) throws IOException {
        testRand();
        testRandFrequent();
    }

    public static void testRand() {
        Random rand = new Random(103);

        for (int i = 0; i < 100; ++i) {
            System.out.println(i + ": " + rand.nextInt() % 100);
        }
    }

    public static void testRandFrequent() {
        Random rand = new Random("102");
        Map<Integer, Integer> map = new HashMap<>();
        int max = 10;

        for (int i = 0; i < max; ++i) {
            map.put(i, 0);
        }

        for (int i = 0; i < 1000; ++i) {
            int r = rand.nextInt() % max;
            map.put(r, map.get(r) + 1);
        }

        System.out.println(map);
    }
}

