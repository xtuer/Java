package misc.util;

import misc.ByteHex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

public class DesUtils {
    private static final String DES = "DES";

    /**
     * DES 加密。
     *
     * @param datasource byte[]
     * @param password   String
     * @return byte[]
     */
    public static byte[] encrypt(byte[] datasource, String password) {
        /*
         DES 加密的步骤如下：
         1. 创建一个 KeyGenerator 对象，并指定 DES 算法。
         2. 使用 KeyGenerator 生成一个 SecretKey 对象。
         3. 创建一个 Cipher 对象，并指定其为加密模式。
         4. 使用 Cipher 对象的 init() 方法，将 SecretKey 对象作为参数传入。
         5. 使用 Cipher 对象的 doFinal() 方法，对要加密的字符串进行加密处理，并返回加密后的字节数组。
         6. 将加密后的字节数组转换为字符串，即为加密后的结果。
         */
        try {
            SecureRandom random = new SecureRandom();

            // 创建一个密匙工厂，用它把 DESKeySpec 转换成 SecretKey
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            SecretKey secureKey = keyFactory.generateSecret(desKey);

            // Cipher 对象实际完成加密操作, 用密匙初始化 Cipher 对象
            Cipher cipher = Cipher.getInstance(DES);
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, random);

            // 正式执行加密操作
            return cipher.doFinal(datasource);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * DES 解密。
     *
     * @param src      byte[]
     * @param password String
     * @return byte[]
     */
    public static byte[] decrypt(byte[] src, String password) {
        try {
            // DES 算法要求有一个可信任的随机数源
            SecureRandom random = new SecureRandom();
            // 创建一个 DESKeySpec 对象
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
            // 将 DESKeySpec 对象转换成 SecretKey 对象
            SecretKey secureKey = keyFactory.generateSecret(desKey);
            // Cipher 对象实际完成解密操作
            Cipher cipher = Cipher.getInstance(DES);
            // 用密匙初始化 Cipher 对象
            cipher.init(Cipher.DECRYPT_MODE, secureKey, random);

            // 真正开始解密操作
            return cipher.doFinal(src);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        String password = "Passw0rd";
        String plain = "What are you 弄啥咧";

        byte[] cipherBytes = DesUtils.encrypt(plain.getBytes(), password);
        System.out.println(ByteHex.byteToHex(cipherBytes));

        plain = new String(DesUtils.decrypt(cipherBytes, password));
        System.out.println(plain);
    }
}
