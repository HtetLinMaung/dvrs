package com.dvc.utils;

public class Cid {
    public static String getCenterFromCid(String cid) {
        return cid.substring(0, cid.length() - 7);
    }

    public static int getNumberFromCid(String cid) {
        return Integer.parseInt(cid.substring(cid.length() - 7));
    }
}
