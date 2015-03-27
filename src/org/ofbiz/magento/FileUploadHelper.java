package org.ofbiz.magento;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;

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
        List<String> productIds = FastList.newInstance();
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