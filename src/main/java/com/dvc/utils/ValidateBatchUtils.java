package com.dvc.utils;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dvc.dao.RecipientsDao;
import com.dvc.models.ResultInfo;
import com.dvc.models.ValidationResult;

public class ValidateBatchUtils {
    public static boolean isGenderValid(String gender) {
        String g = gender.replaceAll("\\s", "");
        return Arrays.asList("M", "F").contains(g) || g.matches("\\u1000\\u103b\\u102c\\u1038") || g.matches("\\u1019");
    }

    public static String normalizeDob(String dob) {

        String d = dob.split("\\.")[0].replaceAll("\\s", "").replaceAll("[a-zA-Z]", "");
        if (dob.split("\\.").length == 3) {
            d = dob.replaceAll("\\s", "").replaceAll("[a-zA-Z]", "");
        }
        if (d.matches("^[0-9\\u1040-\\u1049]{4}$")) {
            String y = d;
            if (d.matches("^[\\u1040-\\u1049]{4}$")) {
                y = LanguageUtils.toEngNum(d);
            }
            if (Integer.parseInt(y) >= 1900)
                return "01/01/" + y;

        } else if (d.matches("^[\\u1040-\\u1049]{1,3}|[0-9]{1,3}$")) {
            String num = d;
            if (d.matches("^[\\u1040-\\u1049]{1,3}$")) {
                num = LanguageUtils.toEngNum(d);
            }
            int year = LocalDate.now().getYear() - Integer.parseInt(num);
            d = "01/07/" + String.valueOf(year);
        }

        String[] dArr = d.replaceAll("\\.", "/").replaceAll("-", "/").split("/");
        if (dArr.length < 3) {
            return d;
        }
        String day = !dArr[0].matches("[0-9]{1,4}") ? LanguageUtils.toEngNum(dArr[0]) : dArr[0];
        String month = !dArr[1].matches("[0-9]{1,4}") ? LanguageUtils.toEngNum(dArr[1]) : dArr[1];
        String year = !dArr[2].matches("[0-9]{1,4}") ? LanguageUtils.toEngNum(dArr[2]) : dArr[2];
        return String.join("/", Arrays.asList(day, month, year));
    }

    public static boolean isDobValid(String dob) {
        int age = Integer.parseInt(System.getenv("AGE"));
        String d = normalizeDob(dob);
        return (d.matches("([0-9]{1,2})/([0-9]{1,2})/([0-9]{2,4})")
                && LocalDate.now().getYear() - Integer.parseInt(d.split("/")[2]) >= age)
                || (d.matches("([0-9]{1,2})\\.([0-9]{1,2})\\.([0-9]{2,4})")
                        && LocalDate.now().getYear() - Integer.parseInt(d.split("\\.")[2]) >= age)
                || (d.matches("([0-9]{1,2})-([0-9]{1,2})-([0-9]{2,4})")
                        && LocalDate.now().getYear() - Integer.parseInt(d.split("-")[2]) >= age)

                || (d.matches("([\\u1040-\\u1049]{1,2})/([\\u1040-\\u1049]{1,2})/([\\u1040-\\u1049]{2,4})")
                        && LocalDate.now().getYear() - Integer.parseInt(LanguageUtils.toEngNum(d.split("/")[2])) >= age)
                || (d.matches("([\\u1040-\\u1049]{1,2})\\.([\\u1040-\\u1049]{1,2})\\.([\\u1040-\\u1049]{2,4})")
                        && LocalDate.now().getYear()
                                - Integer.parseInt(LanguageUtils.toEngNum(d.split("\\.")[2])) >= age)
                || (d.matches("([\\u1040-\\u1049]{1,2})-([\\u1040-\\u1049]{1,2})-([\\u1040-\\u1049]{2,4})")
                        && LocalDate.now().getYear()
                                - Integer.parseInt(LanguageUtils.toEngNum(d.split("-")[2])) >= age);
    }

    public static boolean isNricValid(String nric) {
        return nric.matches("^([0-9]{1,2})/([a-zA-Z]{3}|[a-zA-Z]{6,8})\\((N|E)\\)([0-9]{6})$");
    }

    public static String normalizeNrc(String nric) {
        return nric.replaceAll("\\s", "").replaceAll("\\.", "");
    }

    // public static String normalizeName(String name) {

    // if (!name.matches("(.*)[a-zA-Z](.*)") &&
    // !name.matches("(.*)[\\u1000-\\u1027](.*)")) {
    // return Converter.zg12uni51(name);
    // }
    // return name;
    // }

    public static boolean isMyanmarNricValid(String nric) {
        // String nrc = nric.replaceAll("\\s", "").replaceAll("\\u101d", "\\u1040");
        // \u101c\u103b\u1031\u102c\u1000\u103a\u1011\u102c\u1038\u1006\u1032
        String nrc = normalizeNrc(nric);
        if (System.getenv("ALLOW_ENG_NRC").equals("1") && System.getenv("ALLOW_MIX_NRC").equals("1")) {
            return nrc.matches("\u101c\u103b\u1031\u102c\u1000\u103a\u1011\u102c\u1038\u1006\u1032")
                    || nrc.matches("\u1011\u1031\u102c\u1000\u103a\u1001\u1036\u1005\u102c")
                    || nrc.matches("\\u1011\\u1001\\u1005")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1044]{1,2})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9]{1})/([a-zA-Z]{3,8})\\((N|E|naing|Naing|NAING|n|e)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([a-zA-Z]{3,8})\\((N|E|naing|Naing|NAING|n|e)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches("^([0-9]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$")
                    || nrc.matches("^1([0-4]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$")
                    || nrc.matches(
                            "^([0-9]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^\\u1041([\\u1040-\\u1044]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches("^([A-Z]{1,5})(.*)([0-9\\u1040-\\u1049]{5,10})$")
                    || nrc.matches("^([0-9]{1,2})/(.*)([0-9\\u1040-\\u1049]{6,10})$") || nrc.matches(".*");
        }
        if (System.getenv("ALLOW_ENG_NRC").equals("1")) {
            return nrc.matches("\u101c\u103b\u1031\u102c\u1000\u103a\u1011\u102c\u1038\u1006\u1032")
                    || nrc.matches("\u1011\u1031\u102c\u1000\u103a\u1001\u1036\u1005\u102c")
                    || nrc.matches("\\u1011\\u1001\\u1005")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1044]{1,2})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9]{1})/([a-zA-Z]{3,8})\\((N|E|naing|Naing|NAING|n|e)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([a-zA-Z]{3,8})\\((N|E|naing|Naing|NAING|n|e)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches("^([A-Z]{1,5})(.*)([0-9\\u1040-\\u1049]{5,10})$")
                    || nrc.matches("^([0-9]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$")
                    || nrc.matches("^1([0-4]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$");

        }
        if (System.getenv("ALLOW_MIX_NRC").equals("1")) {
            return nrc.matches("\u101c\u103b\u1031\u102c\u1000\u103a\u1011\u102c\u1038\u1006\u1032")
                    || nrc.matches("\u1011\u1031\u102c\u1000\u103a\u1001\u1036\u1005\u102c")
                    || nrc.matches("\\u1011\\u1001\\u1005")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1044]{1,2})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^\\u1041([\\u1040-\\u1044]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^([0-9]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches(
                            "^1([0-4]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                    || nrc.matches("^([A-Z]{1,5})(.*)([0-9\\u1040-\\u1049]{5,10})$")
                    || nrc.matches("^([0-9]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$")
                    || nrc.matches("^1([0-4]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$");
        }
        return nrc.matches("\u101c\u103b\u1031\u102c\u1000\u103a\u1011\u102c\u1038\u1006\u1032")
                || nrc.matches("\u1011\u1031\u102c\u1000\u103a\u1001\u1036\u1005\u102c")
                || nrc.matches("\\u1011\\u1001\\u1005")
                || nrc.matches(
                        "^([0-9\\u1040-\\u1049]{1})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                || nrc.matches(
                        "^([0-9\\u1040-\\u1044]{1,2})/([\\u1000-\\u1027]{3})\\((\\u1014\\u102D\\u102F\\u1004\\u103A|\\u1014\\u102d\\u1004\\u103a|.*)\\)([0-9\\u1040-\\u1049]{6})$")
                || nrc.matches("^([A-Z]{1,5})(.*)([0-9\\u1040-\\u1049]{5,10})$")
                || nrc.matches("^([0-9]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$")
                || nrc.matches("^1([0-4]{1})/(.*)\\(.*\\)([0-9\\u1040-\\u1049]{6,10})$");
    }

    public static boolean isPhoneValid(String phone) {

        // return phone.matches("^959([0-9]{7,11})$") ||
        // phone.matches("^\\+959([0-9]{7,11})$")
        // || phone.matches("^([0-9]{2})([0-9]{7,11})$") ||
        // phone.matches("^09-([0-9]{7,11})$")
        // || phone.matches("^\\+95\\s+([0-9]{7,11})$");
        String p = normalizePhone(phone);
        return p.matches("^09([0-9]{7,15})$") || p.matches("^\\u1040\\u1049([\\u1040-\\u1049]{7,15})$");
    }

    public static boolean isPassportValid(String passport) {
        return passport.matches("^([A-Z]{1,3})([0-9]{5,7})$");
    }

    public static boolean isDivisionValid(String division) {
        return Arrays.asList("KACHIN", "KAYAR", "KAYIN", "CHIN", "SAGAING", "TANINTHARYI", "BAGO", "BAGO(WEST)",
                "MAGWE", "MANDALAY", "MON", "RAKHAING", "YANGON", "SHAN(SOUNTH)", "SHAN(NORTH)", "SHAN(EAST)",
                "AYARWADDY", "NAYPYITAW").contains(division);
    }

    public static boolean isMyanmarDivisionValid(String division) {
        return Arrays.asList("KACHIN", "KAYAR", "KAYIN", "CHIN", "SAGAING", "TANINTHARYI", "BAGO", "BAGO(WEST)",
                "MAGWE", "MANDALAY", "MON", "RAKHAING", "YANGON", "SHAN(SOUNTH)", "SHAN(NORTH)", "SHAN(EAST)",
                "AYARWADDY", "NAYPYITAW").contains(division);
    }

    public static String normalizePhone(String phone) {
        String p = phone.replaceAll("/", ",").split(",")[0].replaceAll("-", "").replaceAll("\\s", "")
                .replaceAll("_", "").replaceAll("\\+", "").replaceAll("\\.", "");
        if (p.startsWith("959")) {
            return p.replaceAll("^959", "09");
        } else if (p.startsWith("9")) {
            return p.replaceAll("^9", "09");
        } else {
            return p;
        }
    }

    public static ValidationResult validateExcel(List<Map<String, Object>> datalist) throws SQLException {
        ValidationResult result = new ValidationResult();
        List<ResultInfo> infolist = new ArrayList<>();
        int i = 1;
        String oldNric = "";
        String oldPassport = "";

        for (Map<String, Object> data : datalist) {
            boolean isRowValid = true;

            ResultInfo info = new ResultInfo();

            for (String key : Arrays.asList("serialno", "recipientsname", "gender", "fathername", "dob", "nationality",
                    "mobilephone")) {
                // "division", "township", "address1","nameicpp",
                if (((String) data.get(key)).isEmpty()) {
                    isRowValid = false;
                }
            }

            String nationality = ((String) data.get("nationality")).toLowerCase();

            if (!isGenderValid(((String) data.get("gender")))) {
                isRowValid = false;

            }

            if (!isDobValid((String) data.get("dob"))) {

                isRowValid = false;
            }

            if (nationality.equals("myanmar") && !isMyanmarNricValid((String) data.get("nric"))) {

                isRowValid = false;
            }

            String passport = (String) data.get("passport");
            if (!nationality.equals("myanmar")) {
                if (!isPassportValid(passport)) {
                    isRowValid = false;
                }
            } else {
                if (!passport.isEmpty() && !isPassportValid(passport)) {

                    isRowValid = false;
                }
            }
            RecipientsDao dao = new RecipientsDao();
            if (dao.isAvailable("nric", data.get("nric"))) {
                isRowValid = false;

            }
            if (dao.isAvailable("passport", data.get("passport"))) {
                isRowValid = false;
            }

            if (!isPhoneValid((String) data.get("mobilephone"))) {
                isRowValid = false;
            }

            // if (!isDivisionValid((String) data.get("division"))) {
            // isRowValid = false;
            // }

            if (!oldNric.isEmpty() && oldNric.equals(data.get("nric"))) {
                isRowValid = false;

            }
            if (!oldPassport.isEmpty() && oldPassport.equals(passport)) {
                isRowValid = false;
            }
            data.put("isvalid", true);
            if (!isRowValid) {
                result.setValid(false);
                info.setLinenumber(i);
                infolist.add(info);
                data.put("isvalid", false);
            }
            oldNric = (String) data.get("nric");
            oldPassport = passport;
            i++;
        }

        result.setInfos(infolist);
        result.setDatalist(datalist);
        return result;
    }

    public static ValidationResult validateExcel(List<Map<String, Object>> datalist, Map<String, String> headerDesc)
            throws SQLException {
        ValidationResult result = new ValidationResult();
        List<ResultInfo> infolist = new ArrayList<>();
        int i = 1;
        String oldNric = "";
        String oldPassport = "";

        for (Map<String, Object> data : datalist) {
            List<String> descriptionlist = new ArrayList<>();
            boolean isRowValid = true;
            List<Map<String, Object>> keys = new ArrayList<>();

            ResultInfo info = new ResultInfo();

            for (String key : Arrays.asList("recipientsname", "gender", "fathername", "dob", "nationality",
                    "mobilephone")) {
                // "division", "township", "address1", "nameicpp"
                if (((String) data.get(key)).isEmpty()) {
                    isRowValid = false;
                    descriptionlist.add(headerDesc.get(key).trim() + " is blank");
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", key);
                    keyData.put("description", headerDesc.get(key).trim() + " is blank");
                    keys.add(keyData);

                }
            }

            String nationality = ((String) data.get("nationality")).toLowerCase();

            if (!isGenderValid(((String) data.get("gender")))) {
                isRowValid = false;
                descriptionlist.add(headerDesc.get("gender") + " must be M or F");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "gender");
                keyData.put("description", headerDesc.get("gender") + " must be M or F");
                keys.add(keyData);
            }

            if (!isDobValid((String) data.get("dob"))) {
                descriptionlist.add(headerDesc.get("dob") + " must be DD/MM/YYYY");
                isRowValid = false;
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "dob");
                keyData.put("description", headerDesc.get("dob") + " must be DD/MM/YYYY");
                keys.add(keyData);
            }

            if (nationality.equals("myanmar") && !isMyanmarNricValid((String) data.get("nric"))) {
                descriptionlist.add(headerDesc.get("nric")
                        + " must be (eg. 1/MaKaNa(N), 7/KaKaNa(N), 11/GaMaNa(N), 14/PaThaNa(N)) followed by 6 digits");
                isRowValid = false;
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "nric");
                keyData.put("description", headerDesc.get("nric")
                        + " must be (eg. 1/MaKaNa(N), 7/KaKaNa(N), 11/GaMaNa(N), 14/PaThaNa(N)) followed by 6 digits");
                keys.add(keyData);
            }

            String passport = (String) data.get("passport");
            if (!nationality.equals("myanmar")) {
                if (!isPassportValid(passport)) {
                    descriptionlist
                            .add(headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    isRowValid = false;
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "passport");
                    keyData.put("description",
                            headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    keys.add(keyData);
                }
            } else {
                if (!passport.isEmpty() && !isPassportValid(passport)) {
                    descriptionlist
                            .add(headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    isRowValid = false;
                    Map<String, Object> keyData = new HashMap<>();
                    keyData.put("key", "passport");
                    keyData.put("description",
                            headerDesc.get("passport") + " must be 2-3 alphabats and must end with 6 digits");
                    keys.add(keyData);
                }
            }
            RecipientsDao dao = new RecipientsDao();
            if (dao.isAvailable("nric", data.get("nric"))) {
                isRowValid = false;
                descriptionlist.add(headerDesc.get("nric") + " already existed");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("nric", headerDesc.get("nric") + " already existed");
                keyData.put("key", "nric");
                keyData.put("description", headerDesc.get("nric") + " already existed");
                keys.add(keyData);
            }
            if (dao.isAvailable("passport", data.get("passport"))) {
                isRowValid = false;
                descriptionlist.add(headerDesc.get("passport") + " already existed");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("passport", headerDesc.get("passport") + " already existed");
                keyData.put("key", "passport");
                keyData.put("description", headerDesc.get("passport") + " already existed");
                keys.add(keyData);
            }

            if (!isPhoneValid((String) data.get("mobilephone"))) {
                isRowValid = false;
                descriptionlist.add(headerDesc.get("mobilephone") + " must be 09 followed by 7-9 digits");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "mobilephone");
                keyData.put("description", headerDesc.get("mobilephone") + " must be 09 followed by 7-9 digits");
                keys.add(keyData);
            }

            // if (!isDivisionValid((String) data.get("division"))) {
            // isRowValid = false;
            // descriptionlist.add(headerDesc.get("division")
            // + " must be one of those values (KACHIN, KAYAR, KAYIN, CHIN,
            // SAGAING,TANINTHARYI, BAGO,BAGO(WEST), MAGWE, MANDALAY, MON, RAKHAING,YANGON,
            // SHAN(SOUNTH), SHAN(NORTH), SHAN(EAST), AYARWADDY, NAYPYITAW)");
            // keys.add("division");
            // }

            if (!oldNric.isEmpty() && oldNric.equals(data.get("nric"))) {
                isRowValid = false;

                descriptionlist.add(headerDesc.get("nric") + " is duplicate");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "nric");
                keyData.put("description", headerDesc.get("nric") + " is duplicate");
                keys.add(keyData);

            }
            if (!oldPassport.isEmpty() && oldPassport.equals(passport)) {
                isRowValid = false;
                descriptionlist.add(headerDesc.get("passport") + " is duplicate");
                Map<String, Object> keyData = new HashMap<>();
                keyData.put("key", "passport");
                keyData.put("description", headerDesc.get("passport") + " is duplicate");
                keys.add(keyData);
            }
            data.put("isvalid", true);
            data.put("errorkeylist", keys);
            if (!isRowValid) {
                info.setDescriptionlist(descriptionlist);
                result.setValid(false);
                info.setLinenumber(i);
                infolist.add(info);
                data.put("isvalid", false);
            }
            oldNric = (String) data.get("nric");
            oldPassport = passport;
            System.out.println("validating row " + String.valueOf(i) + " completed");
            i++;
        }

        result.setInfos(infolist);
        result.setDatalist(datalist);
        return result;
    }
}
