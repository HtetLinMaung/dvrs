package com.dvc.functions.recipients.quene;

import com.microsoft.azure.functions.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dvc.dao.BaseDao;
import com.dvc.dao.BatchUploadDao;
import com.dvc.dao.CenterDao;
import com.dvc.dao.PIDao;
import com.dvc.dao.RecipientsDao;
import com.dvc.models.QRYData;
import com.dvc.utils.CommonUtils;
import com.dvc.utils.KeyGenerator;
import com.dvc.utils.QRNewUtils;
import com.dvc.utils.QRUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with Azure Storage Queue trigger.
 */
public class AddBlankRecipients {
    /**
     * This function will be invoked when a new message is received at the specified
     * path. The message contents are provided as input to this function.
     */
    @FunctionName("addblankrecipients")
    public void run(
            @QueueTrigger(name = "message", queueName = "insert-blank-recipients", connection = "AzureStorageConnectionString") String message,
            final ExecutionContext context) {
        context.getLogger().info("Java Queue trigger function processed a message: " + message);
        try {
            Map<String, Object> messagedata = new ObjectMapper().readValue(message, Map.class);
            String syskey = (String) messagedata.get("pisyskey");
            Long batchsyskey = (Long) messagedata.get("batchsyskey");

            int totalcount = new BaseDao().getTotalCount("ProformaInvoice where syskey = ? and blankstatus = 1",
                    Arrays.asList(syskey));
            if (totalcount == 0) {
                Map<String, Object> pi = new PIDao().getPi(Long.parseLong(syskey));
                List<String> serialRange = new CenterDao().addLastSerial((String) pi.get("centerid"),
                        Integer.parseInt((String) pi.get("qty")));

                Map<String, Object> batch = new BatchUploadDao().getBatch(batchsyskey);

                int i = 1;
                List<Map<String, Object>> recipients = new ArrayList<>();
                for (String serial : serialRange) {
                    long rSyskey = KeyGenerator.generateSyskey();
                    String now = Instant.now().toString();
                    Map<String, Object> map = new HashMap<>();
                    map.put("cid", serial);
                    map.put("syskey", rSyskey);
                    map.put("createddate", now);
                    map.put("modifieddate", now);
                    map.put("userid", messagedata.get("userid"));
                    map.put("username", messagedata.get("username"));
                    map.put("pisyskey", pi.get("syskey"));
                    map.put("batchuploadsyskey", messagedata.get("batchsyskey"));
                    map.put("partnersyskey", pi.get("partnersyskey"));
                    map.put("remark", "");
                    map.put("rid", 0);
                    map.put("recipientsname", "");
                    map.put("fathername", "");
                    map.put("gender", "");
                    map.put("dob", null);
                    map.put("age", 0);
                    map.put("nric", "");
                    map.put("passport", "");
                    map.put("nationality", "");
                    map.put("organization", "");
                    map.put("address1", "");
                    map.put("township", "");
                    map.put("division", "");
                    map.put("mobilephone", "");
                    map.put("piref", pi.get("pirefnumber"));
                    map.put("voidstatus", 1);
                    map.put("batchrefcode", batch.get("batchrefcode") + "-" + String.valueOf(i++));
                    QRYData data = new QRYData();
                    data.setCid(serial);
                    // data.setDob("");
                    // data.setName("");
                    // data.setNric("");
                    // data.setQraction("url");
                    // data.setUrl("https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/"
                    // + data.getCid());
                    // data.setSyskey(String.valueOf(syskey));

                    String qrtoken = QRNewUtils.generateQRToken(data);

                    map.put("qrtoken", qrtoken);
                    recipients.add(map);
                }

                new RecipientsDao().saveRecipientsFromPI(recipients);
                // Map<String, String> options = new HashMap<>();
                // options.put("Authorization", (String) messagedata.get("btoken"));
                // Map<String, Object> ret = RestClient.post(
                // "https://apx.registrationsystem.org/api/writeFiles?batchno=" +
                // String.valueOf(batchsyskey),
                // new HashMap<>(), options);
                // System.out.println(ret);
                pi = new HashMap<>();
                pi.put("syskey", syskey);
                pi.put("blankstatus", 1);
                new PIDao().updatePI(pi);
            }

            CommonUtils.writeFiles(String.valueOf(batchsyskey));
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
        }
    }
}
