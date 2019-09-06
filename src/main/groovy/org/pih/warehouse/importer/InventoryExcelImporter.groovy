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

//import org.codehaus.groovy.grails.commons.ApplicationHolder
//import org.grails.plugins.excelimport.ExcelImportUtils

class InventoryExcelImporter extends AbstractExcelImporter {

    def inventoryService

	static Map cellMap = [ sheet:'Sheet1', startRow: 1, cellMap: [] ]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
                'A':'productCode',
                'B':'product',
                'C':'lotNumber',
                'D':'expirationDate',
                'E':'manufacturer',
                'F':'manufacturerCode',
                'G':'quantity',
                'H':'binLocation',
                'I':'comments'
		]
	]

	static Map propertyMap = [
            productCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            product:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            lotNumber:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            expirationDate:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturer:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturerCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			quantity:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			binLocation:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			comments:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null])
	]


	public InventoryExcelImporter(String fileName) {
		super(fileName)
        inventoryService = Holders.getGrailsApplication().getMainContext().getBean("inventoryService")
	}


	List<Map> getData() {
		return ExcelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, propertyMap)
    }


    public void validateData(ImportDataCommand command) {
        inventoryService.validateInventoryData(command)
    }


    /**
     * Import data from given inventoryMapList into database.
     *
     * @param location
     * @param inventoryMapList
     * @param errors
     */
    public void importData(ImportDataCommand command) {
        inventoryService.importInventoryData(command)

    }

}