package com.dvc.utils;

import org.apache.tomcat.util.codec.binary.Base64;
import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPublicKey;

public class RSAUtil {
    private static final KeyPair keyPair = initKey();

    private static KeyPair initKey() {
        try {
            // Provider provider =new org.bouncycastle.jce.provider.BouncyCastleProvider();
            // Security.addProvider(provider);
            SecureRandom random = new SecureRandom();
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024, random);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateBase64PublicKey() {
        PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        return new String(Base64.encodeBase64(publicKey.getEncoded()));
    }

    public static String encryptBase64(String plainText) throws Exception {
        byte[] cipherTextArray = encrypt(plainText);
        return new String(Base64.encodeBase64(cipherTextArray));
        // String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
    }

    public static byte[] encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return cipherText;
    }

    public static String decryptBase64(String string) {
        try {
            // Provider provider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
            // Security.addProvider(provider);
            byte[] byteArray = Base64.decodeBase64(string.getBytes());
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            PrivateKey privateKey = keyPair.getPrivate();
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainText = cipher.doFinal(byteArray);
            return new String(plainText);
        } catch (Exception e) {
            return "derror";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Encrypt" + encryptBase64("Syt@12345"));
        System.out.println("Decrypt" + RSAUtil.decryptBase64(
                "qBe/siy+nLhqJeCj6lMPfy5hNgQ8H7IId5hc6+nzB6uGpI6o+vm5Xd8Nd01sJ1i4GaAhephHboPYf4bkTKi5PZ5Y4Os+Inz+kQtXX0WqmKK+0SUSz52yZqpe5ctDmyDRXU4EJFDDnAMPX+HnJnrIHW8KxNU9GAtYDgK9lFnVHe0="));
    }
}
