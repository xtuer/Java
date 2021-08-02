package algorithm;

/**
 * 循环队列
 */
public class CyclicQueue {
    private int head = 0;
    private int size = 0;
    private int[] data;

    public CyclicQueue(int capacity) {
        data = new int[capacity];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == data.length;
    }

    public void enqueue(int n) {
        if (isFull()) {
            // 队列满的时候覆盖队首元素
            head = (head + 1) % data.length;
            size--;
        }

        data[(head + size) % data.length] = n;
        size++;
    }

    public int dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("No elements");
        }

        int n = data[head];
        head = (head + 1) % data.length;
        size--;

        return n;
    }

    public static void main(String[] args) {
        CyclicQueue queue = new CyclicQueue(5);

        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        queue.enqueue(4);
        queue.enqueue(5);
        queue.enqueue(6);
        queue.enqueue(7);
        queue.enqueue(8);

        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
    }
}
