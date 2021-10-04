package util;

import java.util.concurrent.TimeUnit;

public class Utils {
    /**
     * 休眠 time 秒
     *
     * @param timeInSeconds 时间，单位秒
     */
    public static void sleep(long timeInSeconds) {
        try {
            TimeUnit.SECONDS.sleep(timeInSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
