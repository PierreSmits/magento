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
package org.apache.ofbiz.magento;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;

import org.apache.ofbiz.base.util.Debug;
import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.base.util.UtilValidate;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.entity.GenericEntityException;
import org.apache.ofbiz.entity.GenericValue;
import org.apache.ofbiz.entity.condition.EntityCondition;
import org.apache.ofbiz.entity.util.EntityUtil;

public class FileUploadHelper {

    public static final String module = FileUploadHelper.class.getName();

    public static String getJobId(Delegator delegator, String serviceName) {
        EntityCondition condition = EntityCondition.makeCondition(UtilMisc.toMap("serviceName", serviceName));
        String jobId=null;
        try {
            GenericValue jobSandbox = EntityUtil.getFirst(delegator.findList("JobSandbox", condition, null, UtilMisc.toList("-createdStamp"), null, false));
            if (UtilValidate.isNotEmpty(jobSandbox)) {
                jobId = jobSandbox.getString("jobId");
            }
        } catch (GenericEntityException e) {
            Debug.logError("Unable to get JobSandbox record: " + e.getMessage(), module);
        }
        return jobId;
    }

    public static String getPlainCustomMessage(Map<String, Object> processedResult, int errorRecords, int processedRecords) {
        List<String> productIds = new ArrayList<String>();
        productIds.addAll(processedResult.keySet());
        String message = "Result summary:";
        message += "\n\n";
        if (errorRecords!=0) {
            message += "Total number of records: "+processedRecords;
            message += "\n";
            message += "Number of records successfully loaded: "+(processedRecords - errorRecords);
            message += "\n";
            message += "Number of records failed to load: "+errorRecords;
        } else {
            message += "All "+processedRecords+ " records processed successfully";
        }
        message += "\n\n";
        message += "Detailed result set:";
        message += "\n\n";
        message += "ProductId \t Results";
        message += "\n----------------------";
        for(String productId :productIds) {
            message += "\n";
            message += productId+" \t "+processedResult.get(productId);
        }
        return message;
    }
}