package com.dvc.functions.recipientsPreview;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.dvc.constants.ServerMessage;
import com.dvc.constants.ServerStatus;
import com.dvc.dao.RecipientsDownloadDao;
import com.dvc.middlewares.SecurityMiddleware;
import com.dvc.models.MiddlewareData;
import com.dvc.models.SingleResponse;
import com.dvc.utils.ServerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class PreviewRecipient {
    /**
     * This function listens at endpoint "/api/PreviewRecipient". Two ways to invoke
     * it using "curl" command in bash: 1. curl -d "HTTP Body" {your
     * host}/api/PreviewRecipient 2. curl {your
     * host}/api/PreviewRecipient?name=HTTP%20Query
     */
    @FunctionName("previewRecipient")
    public HttpResponseMessage run(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
            HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        String query = request.getQueryParameters().get("syskey");
        String syskey = request.getBody().orElse(query);
        String btoken = request.getHeaders().get("authorization");
        SingleResponse<Map<String, Object>> resData = new SingleResponse<>();

        if (syskey == null) {
            context.getLogger().warning("syskey null!");
            resData.setRetcode(ServerStatus.INVALID_REQUEST);
            resData.setRetmessage(ServerMessage.INVALID_REQUEST);
            return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
        } else {

            try {
                MiddlewareData auth = SecurityMiddleware.checkAuth(request);
                if (!auth.isSuccess()) {
                    return auth.getResponse();
                }
                long recipientSyskey = 0l;
                long partnerSyskey = 0l;
                boolean isAdmin = false;
                try {
                    recipientSyskey = Long.parseLong(syskey);
                    if (btoken != null) {
                        if (btoken.contains("Bearer")) {
                            btoken = btoken.replace("Bearer ", "");
                        }
                        isAdmin = ServerUtil.isBTokenAuthAdmin(btoken);
                        partnerSyskey = Long.parseLong(ServerUtil.getPartnerSkFromBToken(btoken));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                context.getLogger()
                        .info(String.format("Retrieving data was successed with recipient syskey : %s", syskey));
                return request
                        .createResponseBuilder(HttpStatus.OK).body(new RecipientsDownloadDao()
                                .getRecipentBySyskeyOrCID(recipientSyskey, true, "0", partnerSyskey, isAdmin, true))
                        .build();
            } catch (JsonProcessingException e) {
                context.getLogger().warning(e.getMessage());
                e.printStackTrace();
                resData.setRetcode(ServerStatus.SERVER_ERROR);
                resData.setRetmessage(ServerMessage.SERVER_ERROR);
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body(resData).build();
            }

        }
    }
}
