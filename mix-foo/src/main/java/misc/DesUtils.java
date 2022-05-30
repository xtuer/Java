package misc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

/**
 * DES 加密解密 (https://www.cxyzjd.com/article/zhouzhiwengang/90037556)
 */
public abstract class DesUtils {
    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    /**
     * 密钥算法 <br>
     * Java 6 只支持 56bit 密钥 <br>
     * Bouncy Castle 支持 64bit 密钥
     */
    public static final String KEY_ALGORITHM = "DES";

    /**
     * 加密/解密算法 / 工作模式 / 填充方式
     */
    public static final String CIPHER_ALGORITHM = "DES/ECB/PKCS5PADDING";

    /**
     * 生成密钥 <br>
     * Java 6 只支持56bit密钥 <br>
     * Bouncy Castle 支持64bit密钥 <br>
     *
     * @return byte[] 二进制密钥
     * @throws NoSuchAlgorithmException ignored
     */
    public static byte[] generateKey() throws NoSuchAlgorithmException {
        // 实例化密钥生成器
        // 若要使用 64bit 密钥需要替换将 KeyGenerator.getInstance(CIPHER_ALGORITHM) 替换为 KeyGenerator.getInstance(CIPHER_ALGORITHM, "BC")
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);

        // 初始化密钥生成器若要使用 64bit 密钥注意替换 将下述代码 kg.init(56) 替换为 kg.init(64)
        kg.init(56, new SecureRandom());

        // 生成秘密密钥
        SecretKey secretKey = kg.generateKey();

        // 获得密钥的二进制编码形式
        return secretKey.getEncoded();
    }

    /**
     * 加密
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[] 加密数据
     * @throws Exception ignored
     */
    public static byte[] encrypt(byte[] key, byte[] data) throws Exception {
        // 还原密钥
        Key k = toKey(key);

        // 实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 初始化，设置为加密模式
        cipher.init(Cipher.ENCRYPT_MODE, k);

        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 解密
     *
     * @param key  密钥
     * @param data 待解密数据
     * @return byte[] 解密数据
     * @throws Exception ignored
     */
    public static byte[] decrypt(byte[] key, byte[] data) throws Exception {
        // 还原密钥
        Key k = toKey(key);

        // 实例化
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        // 初始化，设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, k);

        // 执行操作
        return cipher.doFinal(data);
    }

    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return 返回秘钥对象
     * @throws InvalidKeyException ignored
     * @throws NoSuchAlgorithmException ignored
     * @throws InvalidKeySpecException ignored
     */
    private static Key toKey(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        // 实例化DES密钥材料
        DESKeySpec dks = new DESKeySpec(key);

        // 实例化秘密密钥工厂
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);

        // 生成秘密密钥
        SecretKey secretKey = keyFactory.generateSecret(dks);

        return secretKey;
    }

    public static void main(String[] args) throws Exception {
        String plain = "你好吗? Are you OK 的?";

        byte[] key = DesUtils.generateKey(); // 保存起来
        byte[] data = plain.getBytes();

        byte[] ret1 = encrypt(key, data);
        System.out.println(data.length + " -> " + ret1.length);
        System.out.println(ByteHex.byteToHex(ret1));

        byte[] ret2 = decrypt(key, ret1);
        System.out.println(new String(ret2));
    }
}
