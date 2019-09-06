/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.importer

import grails.util.Holders
import org.grails.plugins.excelimport.ExcelImportService
import org.pih.warehouse.data.ProductSupplierDataService

class ProductSupplierExcelImporter extends AbstractExcelImporter {

	ProductSupplierDataService productSupplierDataService

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
				'A':'id',
                'B':'code',
                'C':'productCode',
				'D':'legacyProductCode',
                'E':'productName',
                'F':'description',
                'G':'supplierId',
                'H':'supplierName',
                'I':'supplierCode',
                'J':'supplierProductName',
				'K':'manufacturerId',
				'L':'manufacturerName',
				'M':'manufacturerCode',
				'N':'manufacturerProductName',
				'O':'unitPrice',
				'P':'standardLeadTimeDays',
				'Q':'preferenceTypeCode',
				'R':'ratingTypeCode',
				'S':'comments',
		]
	]

	static Map propertyMap = [
			id:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            code:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			productCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			legacyProductCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            productName:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            description:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierId:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierName:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			supplierProductName:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerId:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerName:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			manufacturerProductName:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitPrice:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
			standardLeadTimeDays:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
			preferenceTypeCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			ratingTypeCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            comments:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null])
	]


	ProductSupplierExcelImporter(String fileName) {
		super(fileName)
	}

	def getDataService() {
		return Holders.getGrailsApplication().getMainContext().getBean("productSupplierDataService")
	}


	List<Map> getData() {
		return ExcelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null,null, propertyMap)
    }


    void validateData(ImportDataCommand command) {
		dataService.validate(command)
    }

    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    void importData(ImportDataCommand command) {
		dataService.process(command)
    }

}