import java.time.Duration;

public class Test {
    public static void main(String[] args) throws Exception {
        System.out.println(Duration.ofMillis(3600*1000*24+10).toDays());
    }
}
