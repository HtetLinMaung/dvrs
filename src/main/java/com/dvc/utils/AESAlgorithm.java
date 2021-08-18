package com.dvc.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESAlgorithm {
    private static final String KEY = "dR$&$veFplpEYLpi";
    private static final String IV = "xk!P$@@#NSGxp8&a";

    public static String encryptString(String value) {
        try {
            byte[] ivBytes = getHashByte(IV, "MD5");
            byte[] keyBytes = getHashByte(KEY, "SHA-256");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getHashByte(String key, String method) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(method);
        md.update(key.getBytes());
        return md.digest();
    }

    public static String decryptString(String value) {
        try {
            byte[] ivBytes = getHashByte(IV, "MD5");
            byte[] keyBytes = getHashByte(KEY, "SHA-256");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)), "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String STSKEY = "zif8*NEb6@Av!BP$";
    private static final String STSIV = "!IN%zMY!*N$k#3Av";

    public static String STSdecryptString(String value) {
        try {
            byte[] ivBytes = getHashByte(STSIV, "MD5");
            byte[] keyBytes = getHashByte(STSKEY, "SHA-256");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, paramSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(value)), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String STSencryptString(String value) {
        try {
            byte[] ivBytes = getHashByte(STSIV, "MD5");
            byte[] keyBytes = getHashByte(STSKEY, "SHA-256");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, paramSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes("UTF-8")));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
