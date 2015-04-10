/*
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
 */
import javolution.util.FastList;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.condition.EntityConditionBuilder;

viewIndex = Integer.valueOf(parameters.VIEW_INDEX ?: 0);
viewSize = Integer.valueOf(parameters.VIEW_SIZE ?: 10);
context.viewIndex = viewIndex;
context.viewSize = viewSize;
lowIndex = viewIndex * viewSize + 1;
highIndex = (viewIndex + 1) * viewSize;
findOptions = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
findOptions.setMaxRows(highIndex);

cond = [];
exprBldr =  new EntityConditionBuilder();
cond = exprBldr.AND() {
    EQUALS(contentTypeId: "PROD_FAC_CSV_CNT")
    NOT_IN(statusId: ["PROD_FAC_CSV_INPRGRS"])
}
contentList = delegator.find("Content", cond, null, null, ["createdDate DESC"], findOptions);
contentListSize = contentList.getResultsSizeAfterPartialList();
if (highIndex > contentListSize) {
    highIndex = contentListSize;
}

// get the partial list for this page
if (contentListSize > 0) {
    partialContentList = contentList.getPartialList(lowIndex, viewSize);
} else {
    partialContentList = [] as ArrayList;
}
contentList.close();

contentAssocMap = [:];
partialContentList.each { content ->
    contentAssocList = delegator.findByAnd("ContentAssocDataResourceViewTo", ["caContentId" : content.contentId]);
    if(contentAssocList) {
        contentAssocMap.put(content.contentId, contentAssocList);
    }
}
Integer viewIndexLast = Math.ceil(contentListSize / viewSize)-1;
context.partialContentList = partialContentList;
context.contentAssocMap = contentAssocMap;
context.contentListSize = contentListSize;
context.highIndex = highIndex;
context.lowIndex = lowIndex;
context.viewIndexLast = viewIndexLast;