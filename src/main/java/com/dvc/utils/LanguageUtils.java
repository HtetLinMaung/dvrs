package com.dvc.utils;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LanguageUtils {
    private static String MM_NUM = "\u1040-\u1049";
    private static String MM_NUM_CHARS = "\u1040\u1041\u1042\u1043\u1044\u1045\u1046\u1047\u1048\u1049";
    public static String mmChar = "\u1000\u1001\u1002\u1003\u1004\u1005\u1006\u1007\u1008\u100A\u100E\u100F\u1010\u1011\u1012\u1013\u1014\u1015\u1016\u1017\u1018\u1019\u101A\u101B\u101C\u101D\u101E\u101F\u1020\u1025\u1027";
    private static String NAING_MM = "\u1014\u102D\u102F\u1004\u103A";
    private static String NY_MM = "\u1014";

    public static Map<String, String> getCharacters() throws JsonMappingException, JsonProcessingException {
        return new ObjectMapper().readValue(
                "{\"\u1000\": \"KA\",\"\u1001\": \"KH\", \"\u1002\": \"GA\",\"\u1003\": \"GH\",\"\u1004\": \"NG\",\"\u1005\": \"CA\",\"\u1006\": \"CH\",\"\u1007\": \"JA\",\"\u1008\": \"JH\",\"\u100A\": \"NY\",\"\u100E\": \"DD\",\"\u100F\": \"NN\",\"\u1010\": \"TA\",\"\u1011\": \"TH\",\"\u1012\": \"DA\",\"\u1013\": \"DH\",\"\u1014\": \"NA\",\"\u1015\": \"PA\",\"\u1016\": \"PH\",\"\u1017\": \"BA\",\"\u1018\": \"BH\",\"\u1019\": \"MA\",\"\u101A\": \"YA\",\"\u101B\": \"RA\",\"\u101C\": \"LA\",\"\u101D\": \"WA\",\"\u101E\": \"SA\",\"\u101F\": \"HA\",\"\u1020\": \"LL\",\"\u1025\": \"OU\",\"\u1027\": \"AE\",\"KA\": \"\u1000\",\"KH\": \"\u1001\",\"GA\": \"\u1002\",\"GH\": \"\u1003\",\"NG\": \"\u1004\",\"CA\": \"\u1005\",\"CH\": \"\u1006\",\"JA\": \"\u1007\",\"JH\": \"\u1008\",\"NY\": \"\u100A\",\"DD\": \"\u100E\",\"NN\": \"\u100F\",\"TA\": \"\u1010\",\"TH\": \"\u1011\",\"DA\": \"\u1012\",\"DH\": \"\u1013\",\"NA\": \"\u1014\",\"PA\": \"\u1015\",\"PH\": \"\u1016\",\"BA\": \"\u1017\",\"BH\": \"\u1018\",\"MA\": \"\u1019\",\"YA\": \"\u101A\",\"RA\": \"\u101B\",\"LA\": \"\u101C\",\"WA\": \"\u101D\",\"SA\": \"\u101E\",\"HA\": \"\u101F\",\"LL\": \"\u1020\",\"OU\": \"\u1025\",\"AE\": \"\u1027\"}",
                Map.class);

    }

    public static String toEngNum(String m) {
        String eng = "";
        for (int i = 0; i < m.length(); i++) {
            eng += MM_NUM_CHARS.indexOf(m.charAt(i));
        }
        return eng;
    }

    public static String toEngChar(String m) throws JsonMappingException, JsonProcessingException {
        String eng = "";
        Map<String, String> chars = getCharacters();
        String temp = chars.get("က");
        for (int i = 0; i < m.length(); i++) {
            String ucode = str2UnicodeRepresentation(String.valueOf(m.charAt(i)));
            System.out.println(str2UnicodeRepresentation(String.valueOf(m.charAt(i))));
            String engtest = chars.get(ucode);
            eng += chars.get(ucode);
        }
        return eng;
    }

    public static String toEngNric(String m) {
        return toEngNum(m.split("/")[0]) + "/" + m.split("/")[1].substring(0, m.split("/")[1].length() - 6)
                + toEngNum(m.substring(m.length() - 6));
    }

    private static String str2UnicodeRepresentation(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            int cp = Character.codePointAt(str, i);
            int charCount = Character.charCount(cp);
            // UTF characters may use more than 1 char to be represented
            if (charCount == 2) {
                i++;
            }
            result.append(String.format("\\u%x", cp));
        }
        return result.toString();
    }

}
