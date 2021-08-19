package com.dvc.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.dvc.constants.ServerStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.dvc.models.CheckTokenData;
import com.dvc.models.PartnerUserDto;

public class ApiUtil {
    public static String userID = "959404888722";
    public static String password = "";
    public static String iv = "";
    public static String dm = "";
    public static String salt = "";
    public static String api = "http://13.76.169.39:8080/AppService/module001/";
    // public static String api =
    // "https://devmis.mitcloud.com:8443/AppService/module001/";
    public static String apiv2 = "http://103.101.18.229:8080/cbsapiservice/module001/";

    public static boolean addOrUpdateIAMUser(PartnerUserDto dto, String method) throws IOException {

        String userid = dto.getEmailaddress();
        String username = dto.getDvrsusername();
        String email = dto.getEmailaddress();
        String mobile = "";
        String password = "";
        String mfa = "";

        String appid = "dvrs";
        String domainid = "DDOI3XO9";
        String shortcode = "DVRS";
        String description = dto.getRemark();
        String userlevel = "500";
        Map<String, Object> postBody = new HashMap<String, Object>();
        Map<String, Object> userData = new HashMap<String, Object>();
        Map<String, Object> userDomain = new HashMap<String, Object>();

        userData.put("userid", userid);
        userData.put("username", username);
        userData.put("email", email);
        userData.put("mobile", mobile);
        userData.put("password", password);
        userData.put("mfa", mfa);
        userData.put("appid", appid);
        userData.put("remark", description);

        userDomain.put("domainid", domainid);
        userDomain.put("shortcode", shortcode);
        userDomain.put("description", description);
        userDomain.put("userlevel", userlevel);

        postBody.put("userdata", userData);
        postBody.put("userDomain", userDomain);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(postBody);

        String res = postWithFullUrlandHeader(System.getenv("IAMRegistrationURL"), json, dto.getAtoken(), method);
        CheckTokenData data = new ObjectMapper().readValue(res, CheckTokenData.class);
        if (data.getReturncode().equals(ServerStatus.SUCCESS)) {
            return true;
        }
        return false;
    }

    public static String getApiUrl(String url) {
        return api + url;
    }

    public static String getApiUrlV2(String url) {
        return apiv2 + url;
    }

    public static String postV2(String urlString, String json) throws IOException {
        URL url = new URL(getApiUrlV2(urlString));
        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String post(String urlString, String json) throws IOException {
        URL url = new URL(getApiUrl(urlString));
        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String checkOtp(String phone, String otp, String otpsession) throws IOException {
        final String json = String.format(
                "{\"appid\":\"001\",\"accesskey\":\"123456\",\"phoneno\":\"%s\",\"otp\":\"%s\",\"otpsession\":\"%s\"}",
                phone, otp, otpsession);

        URL url = new URL("https://msg.kunyek.com/sms/checkotp");
        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String postWithFullUrl(String httpUrl, String json) throws IOException {
        URL url = new URL(httpUrl);
        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String postWithFullUrlandHeader(String httpUrl, String json, String token, String method)
            throws IOException {
        URL url = new URL(httpUrl);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        // SSLContext sc;
        // try {
        // sc = SSLContext.getInstance("SSL");

        // sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new
        // java.security.SecureRandom());
        // connection.setSSLSocketFactory(sc.getSocketFactory());

        // } catch (KeyManagementException e1) {
        // e1.printStackTrace();
        // } catch (NoSuchAlgorithmException e2) {
        // e2.printStackTrace();
        // }

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.addRequestProperty("atoken", token);
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String sendOtp(String phone) throws IOException {
        final String json = String.format(
                "{\"appid\":\"001\",\"accesskey\":\"123456\",\"phoneno\":\"%s\",\"msg_template\":\"Dear Customer, Please use this one time password {otp} \",\"sender_info\":\"\"}",
                phone);
        URL url = new URL("https://msg.kunyek.com/sms/sendotp");
        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    public static String getApiToken(String phone, String customerID) throws IOException {
        final String json = String.format(
                "{\"appid\":\"ibanking\",\"appkey\":\"3ay3ay1032smepapz18acp$\",\"cifid\":\"%s\", \"userid\":\"%s\"}",
                customerID, phone.replace("09", "959"));
        String res = ApiUtil.postV2("service001/gettoken", json);
        return (String) new ObjectMapper().readValue(res, Map.class).get("token");
    }

    public static boolean isATokenValid(String atoken, String appid, String userid) throws IOException {
        final String json = String.format("{\"appid\":\"%s\",\"atoken\":\"%s\",\"userid\":\"%s\"}", appid, atoken,
                userid);

        String res = postWithFullUrl(System.getenv("IAM_URL") + "/checktoken", json);
        CheckTokenData data = new ObjectMapper().readValue(res, CheckTokenData.class);
        if (data.getReturncode().equals(ServerStatus.SUCCESS)) {
            return true;
        }
        return false;
    }
}
