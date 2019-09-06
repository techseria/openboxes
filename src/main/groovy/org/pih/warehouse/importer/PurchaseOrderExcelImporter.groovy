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


/**
 * Product code
 * Product
 * Manufacturer
 * Manufacturer code
 * Vendor
 * Vendor code
 * Total Order Quantity Round Up
 * Order notes
 * Lead time
 * Package cost
 * Units per package
 * Unit cost
 * Quantity of units quoted
 * Total cost
 * Quote notes
 * Quantity to expedite to Miami
 * Remaining
 * Miami Status
 * UHM Status
 * Reception notes

 */
class PurchaseOrderExcelImporter extends AbstractExcelImporter {

	def productService

    static Map cellMap = [
		sheet:'Sheet1', startRow: 1, cellMap: []]

	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
			'A':'productCode',
			'B':'product',
			'C':'manufacturer',
			'D':'manufacturerCode',
            'E':'vendor',
            'F':'vendorCode',
            'G':'totalOrderQuantity',
            'H':'orderNotes',
            'I':'leadTime',
            'J':'packageCost',
            'K':'unitsPerPackage',
            'L':'unitCost',
            'M':'quantityUnitsCosted',
            'N':'totalCost',
            'O':'quoteNotes',
            'P':'quantityToExpediteToMiami',
            'Q':'remaining',
            'R':'miamiStatus',
            'S':'uhmStatus',
            'T':'receptionNotes'
		]
	]

	static Map propertyMap = [
            productCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            product:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturer: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            manufacturerCode: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendor: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
            vendorCode: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			totalOrderQuantity: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			orderNotes: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			leadTime: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			packageCost: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitsPerPackage: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			unitCost: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			quantityUnitsCosted: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			totalCost: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			quoteNotes: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			quantityToExpediteToMiami: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			remaining: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			miamiStatus: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			uhmStatus: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
			receptionNotes: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null])
	]

    public PurchaseOrderExcelImporter(String fileName) {
        super(fileName)
    }

	List<Map> getData() {
		return ExcelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null, null,propertyMap)
	}

	@Override
	void validateData(ImportDataCommand command) {

	}

	@Override
	void importData(ImportDataCommand command) {

	}



}