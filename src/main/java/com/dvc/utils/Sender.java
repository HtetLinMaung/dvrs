package com.dvc.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Sender {
    public static Map<String, Object> sendEmail(String toemail, String bodytext, String subject, String senderinfo)
            throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("appid", "001");
        body.put("accesskey", "123456");
        body.put("toemail", toemail);
        body.put("body_text", bodytext);
        body.put("subject", subject);
        body.put("sender_info", senderinfo);
        return RestClient.post(System.getenv("GATEWAY_URL") + "/email/sendmessage", body, new HashMap<>());
    }
}
