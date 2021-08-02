package algorithm;

/**
 * 随机数生成类，相同的种子生成的随机数序列相同。
 */
public class Random {
    private long seed; // 生成随机数的种子

    public Random(long seed) {
        this(seed + "");
    }

    public Random(String seed) {
        this.seed = seed.hashCode();
    }

    /**
     * 生成 0 到 1 千万之间的随机整数
     *
     * @return 返回一个随机整数
     */
    public int nextInt() {
        seed = (seed * 9301 + 49297) % 233280; // 这几个神奇的数字可不是随意写的，搜索一下就明白了。
        double t = this.seed / 233280.0;

        return (int) Math.abs(Math.ceil(t * 10000000));
    }
}
