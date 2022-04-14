package spi;

import java.util.Iterator;

/**
 * 实现 Iterable 就能使用 for each 进行遍历。
 * ServiceLoader 就实现了 Iterable 接口。
 */
public class SnGenerator implements Iterable<Integer> {
    @Override
    public Iterator<Integer> iterator() {
        return new SnIterator();
    }

    public static class SnIterator implements Iterator<Integer> {
        private int sn = 0;

        @Override
        public boolean hasNext() {
            return sn < 10;
        }

        @Override
        public Integer next() {
            return sn++;
        }
    }

    public static void main(String[] args) {
        for (int sn : new SnGenerator()) {
            System.out.println(sn);
        }
    }
}
