import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.ofbiz.entity.condition.EntityCondition
import org.ofbiz.entity.condition.EntityConditionBuilder
import org.ofbiz.entity.condition.EntityOperator

HttpServletResponse response = context.response
HttpServletRequest request = context.request

response.setContentType("text/csv")
response.setHeader("Content-Disposition", "attachment;filename=WarehouseLocation.csv")
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
        productFacilityLocationMap = [:];
        productFacilityLocationMap.put("productId", productId)
    
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
            if(productFacilityLocations) {
                productFacilityLocation = productFacilityLocations.get(0)
                locationSeqId = productFacilityLocation.locationSeqId
                areaId = productFacilityLocation.areaId
                aisleId = productFacilityLocation.
                sectionId = productFacilityLocation.sectionId
                levelId = productFacilityLocation.levelId
                positionId = productFacilityLocation.positionId
            }
        }
        productFacilityLocationMap.put("locationSeqId", locationSeqId)
        productFacilityLocationMap.put("areaId", areaId)
        productFacilityLocationMap.put("aisleId", aisleId)
        productFacilityLocationMap.put("sectionId", sectionId)
        productFacilityLocationMap.put("levelId", levelId)
        productFacilityLocationMap.put("positionId", positionId)
        productFacilityLocationMap.put("inventoryCount", inventoryCount)
        productList.add(productFacilityLocationMap)
    }
}

PrintWriter writer = response.getWriter()
StringBuilder csvHeader = getCSVHedaer()

writeCSVData(csvHeader,productList)
writer.print(csvHeader.toString())

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
            String locationSeqId = record['areaId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['aisleId']){
            String locationSeqId = record['aisleId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['sectionId']){
            String locationSeqId = record['sectionId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['levelId']){
            String locationSeqId = record['levelId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
            exportProductFacilityData.append(',')
        }else{
            exportProductFacilityData.append('');exportProductFacilityData.append(',')
        }
        if(record['positionId']){
            String locationSeqId = record['positionId'].replace(',','')
            exportProductFacilityData.append(locationSeqId?:'')
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