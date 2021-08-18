package com.dvc.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhoneValidation {
    private static final String regex = "^\\d{7}$" + "|^\\d{9}$" + "|^((09)\\d{7})$" + "|^((09)\\d{9})$"
            + "|^\\+((959)\\d{7})$" + "|^\\+((959)\\d{9})$" + "|^((959)\\d{7})$" + "|^((959)\\d{9})$";
    private static final String regex1 = "^\\+((959)\\d{7})$" + "|^\\+((959)\\d{9})$";
    private static final String regex2 = "[0-9]+" + "|\\+(959)[0-9]+";

    public static boolean validatePhone(String phone) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(phone);
        return (matcher.matches() ? true : false);
    }

    public static boolean check(String phone) {
        Pattern pattern = Pattern.compile(regex1);
        Matcher matcher = pattern.matcher(phone);
        return (matcher.matches() ? true : false);
    }

    public static boolean isPhone(String data) {
        Pattern pattern = Pattern.compile(regex2);
        Matcher matcher = pattern.matcher(data);
        return (matcher.matches() ? true : false);
    }

    public static String convertUniNumber(String word) {
        return word.replace('1', '၁').replace('2', '၂').replace('3', '၃').replace('4', '၄').replace('5', '၅')
                .replace('6', '၆').replace('7', '၇').replace('8', '၈').replace('9', '၉').replace('0', 'ဝ');
    }

    public static String convertEngNumber(String word) {
        return word.replace('၁', '1').replace('၂', '2').replace('၃', '3').replace('၄', '4').replace('၅', '5')
                .replace('၆', '6').replace('၇', '7').replace('၈', '8').replace('၉', '9').replace('ဝ', '0')
                .replace('၀', '0');
    }
}
