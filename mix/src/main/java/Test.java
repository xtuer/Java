import java.nio.ByteBuffer;

public class Test {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte)1);
        buffer.put((byte)2);
        buffer.put((byte)3);
        buffer.put((byte)4);
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer.getInt());
        }
    }
}
