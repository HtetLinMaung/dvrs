package com.dvc.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.dvc.models.QRYData;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QRUtils {
    public static String generateY(QRYData data) throws IOException {
        return JwtUtils.generateTokenV2(new HashMap<>(), new ObjectMapper().writeValueAsString(data), false,
                System.getenv("EC2"));
    }

    public static String addEncryptLayer(String yValue) throws IOException {
        return JwtUtils.generateTokenV2(new HashMap<>(), "VRS" + "," + yValue, false, System.getenv("EC1"));
    }

    public static String generateQRToken(QRYData data) throws IOException {
        String Y = generateY(data);
        return addEncryptLayer(Y);
    }

    public static String generateY(Map<String, Object> data) throws IOException {
        return JwtUtils.generateTokenV2(new HashMap<>(), new ObjectMapper().writeValueAsString(data), false,
                System.getenv("EC2"));
    }

    public static String generateQRToken(Map<String, Object> data) throws IOException {
        String Y = generateY(data);
        return addEncryptLayer(Y);
    }
}
