import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Test {
    public static void main(String[] args) throws Exception {
        long milli = TimeUnit.SECONDS.toMillis(10);
        System.out.println(milli);
    }

    public static Integer gen() {
        try {
            TimeUnit.SECONDS.sleep(3);
            Random rand = new Random();
            return rand.nextInt(100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
