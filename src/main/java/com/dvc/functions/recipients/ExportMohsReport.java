package com.dvc.functions.recipients;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.FilterDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.PaginationResponse;
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
public class ExportMohsReport {
    /**
     * This function listens at endpoint "/api/ExportMohsReport". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/ExportMohsReport 2. curl {your
     * host}/api/ExportMohsReport?name=HTTP%20Query
     */
    @FunctionName("exportmohsreport")
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
            if (dto.getRole().equals("Partner")) {
                dto.setPartnersyskey(auth.getTokenData().getPartnersyskey());
            }
            RecipientsDao dao = new RecipientsDao();
            long excelsyskey = dao.saveMohsExcelFile(dto.getGroupcode(), dto.getSubgroupcode());
            dto.setContext(context);
            PaginationResponse<Map<String, Object>> resData = dao.getMohsRecipients(dto);

            List<LinkedHashMap<String, Object>> datalist = new ArrayList<>();
            int i = 1;
            for (Map<String, Object> m : resData.getDatalist()) {
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                data.put("စဉ်", String.valueOf(i));
                data.put("အမည် (မြန်မာ)", m.get("recipientsname"));
                data.put("အဘအမည် (မြန်မာ)", m.get("fathername"));
                data.put("မွေးသက္ကရာဇ် (dd-MM-yyyy)", m.get("dob"));
                data.put("ကျား/မ", m.get("gender"));
                String prefixnrc = (String) m.get("prefixnrc") == null ? "" : (String) m.get("prefixnrc");
                String nrccode = (String) m.get("nrccode") == null ? "" : (String) m.get("nrccode");
                String nrctype = (String) m.get("nrctype") == null ? "" : (String) m.get("nrctype");
                String nrcno = (String) m.get("nrcno") == null ? "" : (String) m.get("nrcno");
                data.put("တိုင်း/ပြည်နယ် Code", m.get("prefixnrc"));
                data.put("မြို့နယ် Code", m.get("nrccode"));
                if (!(!prefixnrc.isEmpty() && !nrccode.isEmpty() && nrctype.isEmpty() && !nrcno.isEmpty())) {
                    data.put("အမျိုးအစား", m.get("nrctype"));
                }
                data.put("မှတ်ပုံတင်အမှတ်", m.get("nrcno"));
                data.put("နိုင်ငံကူးလက်မှတ် အမှတ်(အင်္ဂလိပ်)", m.get("passport"));
                data.put("ကုမ္ပဏီ/အဖွဲ့အစည်း (အင်္ဂလိပ်)", m.get("organization"));
                data.put("ဖုန်းအမှတ်", m.get("mobilephone"));
                data.put("တိုင်းဒေသကြီး/ပြည်နယ်", m.get("division"));
                data.put("မြို့နယ်", m.get("township"));
                data.put("ဆေးထိုးလိပ်စာ", m.get("ward"));
                data.put("နေရပ်လိပ်စာ (အပြည့်အစုံ)", m.get("street"));
                data.put("အလုပ်အကိုင်", m.get("occupation"));
                data.put("မှတ်ချက်(ဆေးထိုးနေရာ)", m.get("vaccinationcenter"));
                data.put("Ref. Key", m.get("syskey"));
                data.put("Group Code", dto.getGroupcode());
                data.put("Sub Group Code", dto.getSubgroupcode());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Rangoon"));
                try {
                    String firstdosedate = (String) m.get("firstdosedate");
                    String seconddosedate = (String) m.get("seconddosedate");
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    parser.setTimeZone(TimeZone.getTimeZone("UTC"));
                    data.put("1st Dose Date", "");
                    data.put("1st Dose Doctor", "");
                    data.put("2nd Dose Date", "");
                    data.put("2nd Dose Doctor", "");
                    if (!firstdosedate.isEmpty()) {
                        Date parsed = parser.parse(firstdosedate);
                        data.put("1st Dose Date", formatter.format(parsed));
                        data.put("1st Dose Doctor", m.get("firstdosedoctor"));
                    }
                    if (!seconddosedate.isEmpty()) {
                        Date parsed = parser.parse(seconddosedate);
                        data.put("2nd Dose Date", formatter.format(parsed));
                        data.put("2nd Dose Doctor", m.get("firstdosedoctor"));
                    }

                } catch (ParseException e) {
                    context.getLogger().severe(e.getMessage());
                    e.printStackTrace();
                }
                datalist.add(data);
                context.getLogger().info(dto.getGroupcode() + "-" + dto.getSubgroupcode() + ": processing row "
                        + String.valueOf(i++) + " finished");
            }
            // dao.saveExported(resData.getDatalist(), dto.getGroupcode(),
            // dto.getSubgroupcode());
            ByteArrayOutputStream out = ExcelUtil.writeExcel(datalist, "Sheet1");
            String[] updatedate = dto.getDoseupdatedate().split("/");
            String selecteddate = "";
            if (updatedate.length == 3) {
                selecteddate = updatedate[2] + updatedate[1] + updatedate[0];
            }
            LocalDate lDate = LocalDate.now();
            String exporteddate = String.valueOf(lDate.getDayOfMonth()) + String.valueOf(lDate.getMonthValue())
                    + String.valueOf(lDate.getYear());
            String filename = "MOHS_Report" + selecteddate + "_" + exporteddate + "-" + UUID.randomUUID().toString()
                    + ".xls";
            dao.updateMohsExcelFile(excelsyskey, filename);
            String url = AzureBlobUtils.createBlob(out, filename);
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
