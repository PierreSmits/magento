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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

public class LogFileEvents {

    public static final String module = LogFileEvents.class.getName();
    public static final String resource = "MagentoUiLabels";

    /** Streams any binary data to the browser */
    public static String downloadLogFile(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> httpParams = UtilHttp.getParameterMap(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Locale locale = (Locale)request.getSession().getServletContext().getAttribute("locale");
        String contentId = (String) httpParams.get("contentId");
        String isCsv = (String) httpParams.get("isCsv");
        if (UtilValidate.isEmpty(contentId)) {
            String errorMsg = UtilProperties.getMessage(resource, "MagentoRequiredFileNameNotFound", locale);
            Debug.logError(errorMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errorMsg);
            return "error";
        }
        // get the mime type
        String mimeType = "application/octet-stream";
        try {
            // get the stream data
            String contentName = (String) delegator.findOne("Content", UtilMisc.<String, Object>toMap("contentId", contentId), false).get("contentName");
            if(UtilValidate.isEmpty(contentName)) {
                contentName = "Uploaded_File_"+contentId;
                if(UtilValidate.isNotEmpty(isCsv) && "Y".equals(isCsv)) {
                    contentName += ".csv";
                } else {
                    contentName += ".txt";
                }
            }
            String xmlString = ContentWorker.renderContentAsText(dispatcher, delegator, contentId, null, locale, "text/plain", false);
            InputStream stream = new ByteArrayInputStream(xmlString.getBytes());
            int length = xmlString.length();
            Debug.logInfo("Got resource data stream: " + length + " bytes", module);
           // stream the content to the browser
            if (stream != null && length != 0) {
                UtilHttp.streamContentToBrowser(response, stream, length, mimeType, contentName);
            } else {
                String errorMsg = UtilProperties.getMessage(resource, "MagentoNoDataIsAvailable", locale);
                Debug.logError(errorMsg, module);
                request.setAttribute("_ERROR_MESSAGE_", errorMsg);
                return "error";
            }
        } catch (GenericEntityException e) {
            Debug.logError(e.getMessage(), module);
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        } catch (GenericServiceException e) {
            Debug.logError(e.getMessage(), module);
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        } catch (IOException e) {
            Debug.logError(e, UtilProperties.getMessage(resource, "MagentoUnableToWriteContentToBrowser", locale), module);
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        } catch (GeneralException e) {
            Debug.logError(e, UtilProperties.getMessage(resource, "MagentoUnableToWriteContentToBrowser", locale), module);
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        }
        return "success";
    }
}
