package com.dvc.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.dvc.models.QRYData;

public class QRNewUtils {
    private static final String key1 = "3a9e3c9815d66d3db25f574519b01d93cd9d7ca7e3a350fd";
    private static final String key2 = "4fa3ded87dd16111ed1faeb7519b02299d1d59abdbed55ad";

    public static String generateY(QRYData data) {
        String cid = data.getCid();
        byte[] bData = new byte[24];
        ;
        System.arraycopy(cid.trim().getBytes(), 0, bData, 0, cid.trim().length());
        byte[] bY = getEncryptMessage(bData, getKeyBytes(key2));
        return Base64.getEncoder().encodeToString(bY);
    }

    public static String decryptY(String yToken) {
        byte[] bY = Base64.getDecoder().decode(yToken);
        byte[] decryptBData = getDecryptKey(bY, getKeyBytes(key2));
        // return byteToString(decryptBData);
        return new String(decryptBData);
    }

    public static String decryptQRToken(String qrtoken) {
        byte[] Ystrb = Base64.getDecoder().decode(qrtoken);
        byte[] decryptBData = getDecryptKey(Ystrb, getKeyBytes(key1));
        return byteToString(decryptBData);
    }

    private static String byteToString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; ++i) {
            ret = ret + String.format("%c", Byte.valueOf(b[i]));
        }
        return ret.trim();
    }

    private static byte[] getKeyBytes(String key) {
        byte[] bKey = new byte[24];
        bKey = hexStringToByteArray(key);
        return bKey;
    }

    private static byte[] getDecryptKey(byte[] wkey, byte[] key) {
        byte[] plainKey;

        Encrypter enc = new Encrypter();
        enc.setKey(key);
        plainKey = enc.dencrypt(wkey);

        return plainKey;
    }

    public static String generateQRToken(QRYData data) {
        String Y = generateY(data);
        String Ystr = System.getenv("ICODE") + "," + Y;
        byte[] bKey = new byte[24];
        byte[] bData = new byte[40];
        bKey = hexStringToByteArray(key1);
        System.arraycopy(Ystr.trim().getBytes(), 0, bData, 0, Ystr.trim().length());
        byte[] bQr = getEncryptMessage2(bData, bKey);
        return Base64.getEncoder().encodeToString(bQr);
    }

    private static byte[] getEncryptMessage(byte[] data, byte[] key) {
        byte[] temp = new byte[24];// 128 bytes track data
        System.arraycopy(data, 0, temp, 0, 24);
        Encrypter enc = new Encrypter();
        // enc.setKey(key);
        enc.setTDescKey(key);
        byte[] ret = enc.encrypt(temp);
        return ret;
    }

    private static byte[] getEncryptMessage2(byte[] data, byte[] key) {
        byte[] temp = new byte[40];// 128 bytes track data
        System.arraycopy(data, 0, temp, 0, 40);
        Encrypter enc = new Encrypter();
        // enc.setKey(key);
        enc.setTDescKey(key);
        byte[] ret = enc.encrypt(temp);
        return ret;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
