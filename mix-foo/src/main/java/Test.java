import java.util.Random;

public class Test {
    public static void main(String[] args) {
        Random rand = new Random();
        StringBuilder buf = new StringBuilder("SELECT * FROM test_performance WHERE id in (");
        for (int i = 0; i < 500; i++) {
            buf.append(rand.nextInt(5000000)).append(",");
        }
        buf.deleteCharAt(buf.length()-1);
        buf.append(");");
        System.out.println(buf);
    }
}
