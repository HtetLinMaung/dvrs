package com.dvc.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Encrypter {
    private Cipher cipher;
    private SecretKey key;

    public Encrypter() {
        try {
            cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setKey(byte[] key) {
        this.key = new SecretKeySpec(key, "DESede");
    }

    public void setTDescKey(byte[] key) {
        this.key = new SecretKeySpec(key, "TripleDES");
    }

    public byte[] encrypt(byte[] data) {
        byte[] ret;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ret = cipher.doFinal(data);
            return ret;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public byte[] dencrypt(byte[] data) {
        byte[] ret;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            ret = cipher.doFinal(data);
            return ret;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
