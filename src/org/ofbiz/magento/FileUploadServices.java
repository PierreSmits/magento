/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.magento;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

public class FileUploadServices {

    public static final String module = FileUploadServices.class.getName();

    public static Map<String, Object> fileUploadToServer(DispatchContext dctx, Map<String, Object> context) {
        ByteBuffer fileData = (ByteBuffer) context.get("uploadedFile");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String encoding = System.getProperty("file.encoding");
        String file = Charset.forName(encoding).decode(fileData).toString();
        String contentTypeId = (String) context.get("contentTypeId");
        String statusId = (String) context.get("statusId");
        try {
            Map<String, Object> createSimpleTextContentDataResp = dispatcher.runSync("createSimpleTextContentData", UtilMisc.<String, Object>toMap("contentTypeId", contentTypeId , "text", file, "statusId", statusId, "userLogin", userLogin));
            if (ServiceUtil.isError(createSimpleTextContentDataResp)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createSimpleTextContentDataResp));
            }
            result.put("contentId", createSimpleTextContentDataResp.get("contentId"));
        } catch (GenericServiceException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        return result;
    }

    public static Map<String, Object> createSimpleTextContentData(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        if(UtilValidate.isEmpty(context.get("contentTypeId"))) {
            return ServiceUtil.returnError("No contentTypeId Found");
        }
        try {
            Map<String, Object> createDataResourceCtx = FastMap.newInstance();
            createDataResourceCtx.put("mimeTypeId", "text/plain");
            createDataResourceCtx.put("dataResourceTypeId", "ELECTRONIC_TEXT");
            createDataResourceCtx.put("userLogin", userLogin);
            Map<String, Object> createDataResourceResp = dispatcher.runSync("createDataResource", createDataResourceCtx);
            if (ServiceUtil.isError(createDataResourceResp)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createDataResourceResp));
            }
            String dataResourceId = (String) createDataResourceResp.get("dataResourceId");
            
            Map<String, Object> createElectronicTextCtx = FastMap.newInstance();
            createElectronicTextCtx.put("dataResourceId", dataResourceId);
            createElectronicTextCtx.put("textData", context.get("text"));
            createElectronicTextCtx.put("userLogin", userLogin);
            Map<String, Object> createElectronicTextResp = dispatcher.runSync("createElectronicText", createElectronicTextCtx);
            if (ServiceUtil.isError(createElectronicTextResp)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createElectronicTextResp));
            }
            Map<String, Object> createContentCtx = dctx.getModelService("createContent").makeValid(context, ModelService.IN_PARAM);
            createContentCtx.put("dataResourceId", dataResourceId);
            Map<String, Object> createContentResp = dispatcher.runSync("createContent", createContentCtx);
            if (ServiceUtil.isError(createContentResp)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(createContentResp));
            }
            result.put("contentId", createContentResp.get("contentId"));
        } catch (GenericServiceException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        return result;
    }
}
