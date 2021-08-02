import com.flowpowered.noise.NoiseQuality;
import com.flowpowered.noise.module.source.Perlin;

public class NoiseRandom {
    public static void main(String[] args) throws Exception {
        Perlin noise = new Perlin();
        noise.setNoiseQuality(NoiseQuality.BEST);

        double x = 0;
        for (int i = 0; i < 100; i++) {
            System.out.println(noise.getValue(x, 0, 0));
            x += 0.2; // x 的差值越大，随机数之间的差值也越大
        }
    }
}
