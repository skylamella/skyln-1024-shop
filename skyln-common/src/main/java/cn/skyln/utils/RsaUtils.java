package cn.skyln.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Rsa工具类
 *
 * @Date: 2022/01/22/11:58
 */
public class RsaUtils {
    private static final int DEFAULT_KEY_SIZE = 4096;

    private static final String PUBLIC_KEY_FILE_NAME = "publicKey";

    private static final String PRIVATE_KEY_FILE_NAME = "privateKey";

    private static final String SECRET = "SKYLN-1024-SHOP";

    /**
     * 从文件中读取公钥
     *
     * @return java.security.PublicKey
     * @date 2022/1/22/12:01
     */
    public static PublicKey getPublicKey() throws Exception {
        byte[] bytes = readFile(PUBLIC_KEY_FILE_NAME);
        return getPublicKey(bytes);
    }

    /**
     * 从文件中读取私钥
     *
     * @return java.security.PublicKey
     * @date 2022/1/22/12:01
     */
    public static PrivateKey getPrivateKey() throws Exception {
        byte[] bytes = readFile(PRIVATE_KEY_FILE_NAME);
        return getPrivateKey(bytes);
    }

    /**
     * 获取公钥
     *
     * @param bytes 公钥的字节形式
     * @return java.security.PublicKey
     * @date 2022/1/22/12:02
     */
    private static PublicKey getPublicKey(byte[] bytes) throws Exception {
        bytes = Base64.getDecoder().decode(bytes);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 获取私钥
     *
     * @param bytes 私钥的字节形式
     * @return java.security.PublicKey
     * @date 2022/1/22/12:02
     */
    private static PrivateKey getPrivateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        bytes = Base64.getDecoder().decode(bytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 根据密文，生存不低于2048位的rsa公钥和私钥,并写入指定文件
     *
     * @date 2022/1/22/12:05
     */
    public static void generateKey() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(SECRET.getBytes());
        keyPairGenerator.initialize(DEFAULT_KEY_SIZE, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 获取公钥并写出
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        publicKeyBytes = Base64.getEncoder().encode(publicKeyBytes);
        writeFile(setFolder() + PUBLIC_KEY_FILE_NAME, publicKeyBytes);
        // 获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        privateKeyBytes = Base64.getEncoder().encode(privateKeyBytes);
        writeFile(setFolder() + PRIVATE_KEY_FILE_NAME, privateKeyBytes);
    }

    private static byte[] readFile(String fileName) throws Exception {
        return Files.readAllBytes(new File(setFolder() + fileName).toPath());
    }

    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        if (!dest.exists()) {
            //dest.mkdirs();
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }

    private static String setFolder() {
        StringBuilder sb = new StringBuilder();
        String separator = File.separator;
        String osName = System.getProperty("os.name");
        String basePath = (StringUtils.isNotBlank(osName) && !osName.startsWith("Windows")) ? separator + "root" : System.getProperty("user.home");
        sb.append(basePath).append(separator).append("rsa").append(separator);
        return sb.toString();
    }
}
