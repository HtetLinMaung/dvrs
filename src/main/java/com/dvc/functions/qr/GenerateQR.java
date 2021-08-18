package com.dvc.functions.qr;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.models.BaseResponse;
import com.dvc.models.QRYData;
import com.dvc.utils.EasyData;
import com.dvc.utils.QRNewUtils;
import com.dvc.utils.QRUtils;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class GenerateQR {
    /**
     * This function listens at endpoint "/api/GenerateQR". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/GenerateQR 2. curl {your host}/api/GenerateQR?name=HTTP%20Query
     */
    @FunctionName("generateqr")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            final String qraction = request.getQueryParameters().get("qraction");
            QRYData data = new QRYData();
            data.setCid("YRG10003302");

            // String token = QRNewUtils.generateQRToken(data);
            // String Ytoken = QRNewUtils.decryptQRToken(token);
            // String actualdata = QRNewUtils.decryptY(Ytoken.split(",")[1]);

            // data.setDob("1972-06-21T00:00:00.0000000");
            // data.setSyskey("157068676536843643");
            // data.setName("Htet Lin Maung");
            // data.setNric("12/OUKATA(N)194277");
            // data.setQraction(qraction);
            // Map<String, Object> map = new EasyData<QRYData>(data).toMap();
            // map.put("BTN-Dose",
            // "https://vrs2021.registrationsystem.org/#/main/dvrs/wpacalls/token/123");
            // map.put("TXT-1", "Name");
            // map.put("TXT-2", "Age");
            // map.put("TXT-3", "Gender");

            String qrtoken = QRNewUtils.generateQRToken(data);

            EasyData<BaseResponse> easyData = new EasyData<>(new BaseResponse());

            Map<String, Object> res = easyData.toMap();
            res.put("qrtoken", qrtoken);
            return request.createResponseBuilder(HttpStatus.OK).body(res).build();
        } catch (Exception e) {
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
