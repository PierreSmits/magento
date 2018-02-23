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
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.apache.ofbiz.base.util.UtilProperties;
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityConditionBuilder
import org.apache.ofbiz.entity.condition.EntityOperator

HttpServletResponse response = context.response
HttpServletRequest request = context.request

exprBldr = new EntityConditionBuilder()
Map parameters = context.parameters
facilityId = parameters['facilityId']

List productList = []

magentoProducts = delegator.findList("GoodIdentification", EntityCondition.makeCondition("goodIdentificationTypeId", EntityOperator.EQUALS, "MAGENTO_ID"), ["productId"] as Set, null, null, false);
magentoProducts.each { magentoProduct ->
    productId = magentoProduct.productId
    product = delegator.findOne("Product", [productId : productId], false)
    if(product && !(product.isVirtual=="Y")) {
        locationSeqId = null
        areaId = null
        aisleId = null
        sectionId = null
        levelId = null
        positionId = null
        inventoryCount = 0

        productFacility = delegator.findOne("ProductFacility", [productId : productId, facilityId : facilityId], false)
        if (productFacility) {
            if (productFacility.lastInventoryCount) {
                inventoryCount = (productFacility.lastInventoryCount).toString()
            } else {
                inventoryCount = inventoryCount.toString()
            }
            condition = exprBldr.AND() {
                EQUALS(productId: magentoProduct.productId)
                EQUALS(locationTypeEnumId : "FLT_PICKLOC")
                EQUALS(facilityId : facilityId)
            }
            productFacilityLocations = delegator.findList("ProductFacilityLocationView", condition, null, null, null, false)
            if(!productFacilityLocations) {
                productFacilityLocationMap = [:];
                productFacilityLocationMap.put("productId", productId)
                productFacilityLocationMap.put("inventoryCount", inventoryCount)
                productList.add(productFacilityLocationMap)
            }
        }
    }
}

if(productList) {
    response.setContentType("text/csv")
    response.setHeader("Content-Disposition", "attachment;filename=WarehouseLocation.csv")
    PrintWriter writer = response.getWriter()
    StringBuilder csvHeader = getCSVHedaer()
    writeCSVData(csvHeader,productList)
    writer.print(csvHeader.toString())
} else {
    response.setContentType("text/csv")
    response.setHeader("Content-Disposition", "attachment;filename=WarehouseLocation.txt")
    PrintWriter writer = response.getWriter()
    writer.print(UtilProperties.getMessage("MagentoUiLabels", "MagentoExportWarehouseLocationErrorInfo", locale))
}

def StringBuilder getCSVHedaer(){
    StringBuilder sb = new StringBuilder()
    sb.append('Product Id').append(',')
    sb.append('Location Seq Id').append(',')
    sb.append('Area Id').append(',')
    sb.append('Aisle Id').append(',')
    sb.append('Section Id').append(',')
    sb.append('Level Id').append(',')
    sb.append('Position Id').append(',')
    sb.append('Inventory Count')
    return sb
}

def void writeCSVData(StringBuilder exportProductFacilityData, List records){
    exportProductFacilityData.append("\n")
    for(Map record:records){
        if(record['productId']){
            String productId = record['productId'].replace(',','')
            exportProductFacilityData.append(productId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['locationSeqId']){
            String locationSeqId = record['locationSeqId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['areaId']){
            String areaId = record['areaId'].replace(',','')
            exportProductFacilityData.append(areaId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['aisleId']){
            String aisleId = record['aisleId'].replace(',','')
            exportProductFacilityData.append(aisleId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['sectionId']){
            String sectionId = record['sectionId'].replace(',','')
            exportProductFacilityData.append(sectionId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['levelId']){
            String levelId = record['levelId'].replace(',','')
            exportProductFacilityData.append(levelId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['positionId']){
            String positionId = record['positionId'].replace(',','')
            exportProductFacilityData.append(positionId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['inventoryCount']){
            String inventoryCount = record['inventoryCount'].replace(',','')
            exportProductFacilityData.append(inventoryCount?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        exportProductFacilityData.append("\n")
    }
}