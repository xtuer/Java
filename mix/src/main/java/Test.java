import java.util.Arrays;

public class Test {
    private static final int NO_OPT  = Integer.MAX_VALUE - 1; // 无解
    private static final int[] COINS = { 1, 2, 5, 7, 10 };    // 可选金币

    public static void main(String[] args) throws Exception {
        int target = 20; // 目标金币

        // 最优解 (金币个数), 每个目标的解存储在以他下标的数组中，
        // opts[0] 为 0，这个是已知方案
        int[] opts = new int[target+1];

        for (int currentCoin = 1; currentCoin < target+1; ++currentCoin) {
            int preOpt = NO_OPT; // 前一个可行的最优解

            // 当前金币减去可选金币，得到前一个可用方案，选择其中的最优解
            for (int coin : COINS) {
                int preCoin = currentCoin - coin;
                if (preCoin >= 0) {
                    preOpt = Math.min(preOpt, opts[preCoin]);
                }
            }

            opts[currentCoin] = preOpt + 1; // 如果值为 NO_OPT，则说明无解
        }

        for (int coin = 1; coin <= target; ++coin) {
            System.out.printf("金币: %2d, 最少 %d 个\n", coin, opts[coin]);
        }
    }
}
