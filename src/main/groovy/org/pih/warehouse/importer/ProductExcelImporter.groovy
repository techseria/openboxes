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

// import org.codehaus.groovy.grails.commons.ApplicationHolder
// import org.grails.plugins.excelimport.ExcelImportUtils

class ProductExcelImporter extends AbstractExcelImporter {

	def productService 

	static Map cellMap = [
		sheet:'Sheet1', startRow: 1, cellMap: []]
	
	static Map columnMap = [
		sheet:'Sheet1',
		startRow: 1,
		columnMap: [
			'A':'idaCode',
			'B':'openBoxesId',
			'C':'productDescription',
			'D':'french',
			'E':'search1',
			'F':'search2',
			'G':'packaging',
			'H':'unit',
			'I':'manufacturer',
			'J':'comment',
			'K':'code'
		]
	]

	static Map propertyMap = [
		idaCode:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		openBoxesId:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		productDescription: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		french: ([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		search1:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		search2:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		packaging:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		unit:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		manufacturer:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		comment:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null]),
		code:([expectedType: ExcelImportService.PROPERTY_TYPE_STRING, defaultValue:null])
	]



	public ProductExcelImporter(String fileName) {
		super(fileName)
		productService = Holders.getGrailsApplication().getMainContext().getBean("productService")
	}


	List<Map> getData() {
		return ExcelImportService.convertColumnMapConfigManyRows(workbook, columnMap, null,null, propertyMap)
	}


	public void validateData(ImportDataCommand command) {
		productService.validateData(command)
	}
	

	/**
	 * Import data from given inventoryMapList into database.
	 *
	 * @param location
	 * @param inventoryMapList
	 * @param errors
	 */
	public void importData(ImportDataCommand command) {
		productService.importData(command)

	}



}