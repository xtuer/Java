package util;

import misc.ByteHex;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaUtils {
    private static final String RSA = "RSA";
    private static final int KEY_SIZE = 512;

    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA);
        generator.initialize(KEY_SIZE, new SecureRandom()); // 可以是 512, 1024, 2048
        KeyPair pair = generator.generateKeyPair();
        return pair;
    }

    public static byte[] getPrivateKey(KeyPair pair) {
        PrivateKey privateKey = pair.getPrivate();
        return privateKey.getEncoded();
    }

    public static byte[] getPublicKey(KeyPair pair) {
        PublicKey publicKey = pair.getPublic();
        return publicKey.getEncoded();
    }

    public static byte[] encrypt(byte[] publicKey, byte[] inputData) throws Exception {
        PublicKey key = KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(inputData);
        return encryptedBytes;
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData) throws Exception {
        PrivateKey key = KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(inputData);
        return decryptedBytes;
    }

    /**
     * [1] 上面的方法是公钥加密、私钥解密。
     * [2] 如果想要私钥加密、公钥解密，需要把 encrypt 和 decrypt 里相关的 PublicKey, PrivateKey, generatePublic, generatePrivate,
     * X509EncodedKeySpec, PKCS8EncodedKeySpec 都都互相替换一下。
     */
    public static void main(String[] args) throws Exception {
        // 生成密钥对后，保存到数据库
        KeyPair kp = RsaUtils.generateKeyPair();

        System.out.println(RsaUtils.getPublicKey(kp).length);
        System.out.println(RsaUtils.getPrivateKey(kp).length);

        // 公钥加密
        byte[] cipherBytes = RsaUtils.encrypt(RsaUtils.getPublicKey(kp), "What are you 弄啥咧".getBytes());
        System.out.println(ByteHex.byteToHex(cipherBytes));

        // 私钥解密
        byte[] plainBytes = RsaUtils.decrypt(RsaUtils.getPrivateKey(kp), cipherBytes);
        System.out.println(new String(plainBytes));
    }
}
