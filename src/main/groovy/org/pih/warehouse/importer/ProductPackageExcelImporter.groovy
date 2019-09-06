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

import org.grails.plugins.excelimport.ExcelImportService


// import java.text.ParseException;
// import java.text.SimpleDateFormat;
class ProductPackageExcelImporter extends AbstractExcelImporter {

	def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'status',
                'B':'productCode',
                'C':'productName',
                'D':'category',
                'E':'tags',
                'F':'manufacturer',
                'G':'manufacturerCode',
                'H':'vendor',
                'I':'vendorCode',
                'J':'binLocation',
                'K':'unitOfMeasure',
                'L':'package',
                'M':'packageUom',
                'N':'packageSize',
                'O':'pricePerPackage',
                'P':'pricePerUnit',
                'Q':'minQuantity',
                'R':'reorderQuantity',
                'S':'maxQuantity',
                'T':'currentQuantity'
		]
	]

    static Map propertyMap = [
            status:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            productCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            productName: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            tags: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            category: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturer:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturerCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendor:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendorCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            binLocation:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            unitOfMeasure:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            package:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            packageUom:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            packageSize:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            pricePerPackage:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            pricePerUnit:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            minQuantity:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            reorderQuantity:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            maxQuantity:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null]),
            currentQuantity:([expectedType: ExcelImportService.PROPERTY_TYPE_INT, defaultValue:null])
	]




	public ProductPackageExcelImporter(String fileName) {
		super(fileName)
		//inventoryService = ApplicationHolder.getApplication().getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return ExcelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null,propertyMap)
	}



	public void validateData(ImportDataCommand command) { 
		//inventoryService.validateData(command)
	}

	public void importData(ImportDataCommand command) { 
		//inventoryService.importData(command)
	}






}