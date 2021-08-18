package com.dvc.functions.batchupload;

import com.microsoft.azure.functions.annotation.*;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Arrays;
import java.util.HashMap;

import java.util.Map;
import java.util.Optional;

import com.dvc.constants.CommonConstants;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;

import com.dvc.dao.BatchUploadDao;
import com.dvc.factory.DbFactory;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.BaseResponse;
import com.dvc.models.BatchDto;
import com.dvc.models.MiddlewareData;
import com.dvc.models.TokenData;
import com.dvc.utils.CommonUtils;
import com.dvc.utils.EasySql;
import com.dvc.utils.ExcelUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class UploadBatch {
    /**
     * This function listens at endpoint "/api/UploadBatch". Two ways to invoke it
     * using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/UploadBatch 2. curl {your host}/api/UploadBatch?name=HTTP%20Query
     */
    @FunctionName("uploadbatch")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = {
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        try {
            MiddlewareData auth = SecurityMiddleware.checkAuth(request);
            if (!auth.isSuccess()) {
                return auth.getResponse();
            }
            BatchDto dto = new ObjectMapper().readValue(request.getBody().get(), BatchDto.class);
            BatchUploadDao dao = new BatchUploadDao();
            if (dto.getFile().isEmpty() || dto.getFilename().isEmpty()) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage(ServerMessage.INVALID_REQUEST);
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            if (!Arrays.asList("xlsx", "xls", "csv").contains(dto.getFileext())) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage("Invalid file");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }

            try {
                new XSSFWorkbook(CommonUtils.getInputStreamFromBase64(dto.getFile())).close();
            } catch (Exception e) {
                BaseResponse res = new BaseResponse();
                res.setRetcode(ServerStatus.INVALID_REQUEST);
                res.setRetmessage("Excel 97 to 2003 format not supported");
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            }
            // Map<String, Object> firstrow = ExcelUtil.getExcelFirstRow(dto.getFile(),
            // CommonConstants.HEADERS);

            // if (!dto.getFilename().matches("^(F|M)-([0-9]{8})-B([0-9]{3})$")
            // || dao.isBatchRefCodeAvailable(dto.getFilename())) {
            // BaseResponse res = new BaseResponse();
            // res.setRetcode(ServerStatus.INVALID_REQUEST);
            // res.setRetmessage("Invalid file name or file already existed");
            // return
            // request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            // }

            // if
            // (!auth.getTokenData().getPartnerid().equals(dto.getFilename().split("-")[1]))
            // {
            // BaseResponse res = new BaseResponse();
            // res.setRetcode(ServerStatus.UNAUTHORIZED);
            // res.setRetmessage(ServerMessage.UNAUTHORIZED);
            // return
            // request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            // }
            // if (!dao.isBatchNoValid(dto.getFilename())) {
            // BaseResponse res = new BaseResponse();
            // res.setRetcode(ServerStatus.UNAUTHORIZED);
            // res.setRetmessage("Can't skip batch no!");
            // return
            // request.createResponseBuilder(HttpStatus.BAD_REQUEST).body(res).build();
            // }

            TokenData tokenData = auth.getTokenData();
            if (tokenData.getRole().equals("Partner")) {
                dto.setPartnerid(tokenData.getPartnerid());
                dto.setPartnersyskey(tokenData.getPartnersyskey());
            } else {
                String partnerid = (String) new EasySql(DbFactory.getConnection()).getOne(Arrays.asList("partnerid"),
                        "Partners where recordstatus <> 4 and syskey = ?", Arrays.asList(dto.getPartnersyskey()))
                        .get("partnerid");
                dto.setPartnerid(partnerid);
                dto.setPartnersyskey(dto.getPartnersyskey());
            }

            dto.setUserid(tokenData.getDvrsuserid());
            dto.setUsername(tokenData.getDvrsusername());
            long syskey = dao.saveBatch(dto);

            Map<String, Object> res = new HashMap<>();
            res.put("fileid", String.valueOf(syskey));
            res.put("retcode", ServerStatus.SUCCESS);
            res.put("retmessage", "Uploaded Successfully");
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
