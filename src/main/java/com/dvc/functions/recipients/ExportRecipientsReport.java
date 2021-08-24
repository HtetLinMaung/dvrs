package com.dvc.functions.recipients;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.FilterDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PaginationResponse;
import com.dvc.models.TokenData;
import com.dvc.utils.AzureBlobUtils;
import com.dvc.utils.EasyData;
import com.dvc.utils.ExcelUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class ExportRecipientsReport {
    /**
     * This function listens at endpoint "/api/ExportRecipientsReport". Two ways to
     * invoke it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ExportRecipientsReport 2. curl {your
     * host}/api/ExportRecipientsReport?name=HTTP%20Query
     */
    @FunctionName("exportrecipientsreport")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }

            FilterDto dto = new ObjectMapper().readValue(request.getBody().get(), FilterDto.class);

            dto.setRole(auth.getTokenData().getRole());
            String partnersyskey = auth.getTokenData().getPartnersyskey();
            dto.setPartnersyskey(
                    !auth.getTokenData().getRole().equals("Partner") ? dto.getPartnersyskey() : partnersyskey);
            dto.setReverse(false);
            PaginationResponse<Map<String, Object>> resData = new RecipientsDao().getRecipients(dto);

            ByteArrayOutputStream out = ExcelUtil.writeExcel(resData.getDatalist().stream().map(m -> {
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                data.put("CID", m.get("cid"));
                data.put("Name", m.get("recipientsname"));
                data.put("Sex", m.get("gender"));
                data.put("Father's Name", m.get("fathername"));
                data.put("DOB", m.get("dob"));
                data.put("NRC", m.get("nric"));
                data.put("Passport", m.get("passport"));
                data.put("Organization", m.get("organization"));
                data.put("Mobile", m.get("mobilephone"));
                data.put("State/Region", m.get("division"));
                data.put("Address", m.get("address1"));
                data.put("Dose", m.get("dose"));
                data.put("Lot No.", m.get("lot"));
                data.put("Doctor/Nurse", m.get("doctor"));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                // Asia/Rangoon
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Rangoon"));
                try {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date parsed = parser.parse((String) m.get("doseupdatetime"));

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

                    data.put("1st Dose Date", formatter.format(parsed) + " " + (String) m.get("userid"));
                    data.put("2nd Dose Date", "");
                } catch (ParseException e) {

                    e.printStackTrace();
                }
                data.put("Township", m.get("township"));
                data.put("Remark", m.get("remark"));
                return data;
            }).collect(Collectors.toList()), "Sheet1");
            String url = AzureBlobUtils.createBlob(out,
                    "Vaccinated_List" + "-" + UUID.randomUUID().toString() + ".xls");
            url += "?" + AzureBlobUtils.getSasToken();
            Map<String, Object> res = new EasyData<BaseResponse>(new BaseResponse()).toMap();
            res.put("url", url);
            return request.createResponseBuilder(HttpStatus.OK).body(res).build();
        } catch (Exception e) {
            context.getLogger().severe(e.getMessage());
            BaseResponse res = new BaseResponse();
            res.setRetcode(ServerStatus.SERVER_ERROR);
            res.setRetmessage(ServerMessage.SERVER_ERROR);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(res).build();
        }
    }
}
