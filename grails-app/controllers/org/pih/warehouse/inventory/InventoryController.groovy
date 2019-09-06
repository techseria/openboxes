/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/

package org.pih.warehouse.inventory

import grails.converters.JSON
import grails.validation.ValidationException
import groovy.time.TimeCategory
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.apache.commons.lang.StringEscapeUtils
import org.pih.warehouse.core.Constants
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Tag
import org.pih.warehouse.core.User
import org.pih.warehouse.importer.InventoryExcelImporter
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.SimpleDateFormat;
// import java.util.Calendar;

import org.pih.warehouse.util.DateUtil

class InventoryController {

	def dataSource
    def productService
    def dashboardService
	def inventoryService
    def requisitionService
    def inventorySnapshotService
    def userService

    static allowedMethods = [show: "GET", search: "POST", download: "GET"];

    def index = {
		redirect(action: "browse");
	}

    def manage = { ManageInventoryCommand command ->
        [command: command]
    }
    def binLocations = {
        Location location = Location.load(session.warehouse.id)
        List binLocations = inventorySnapshotService.getQuantityOnHandByBinLocation(location)

        def data = binLocations.collect {
            [
                    it?.inventoryItem.product?.productCode,
                    it?.inventoryItem.product?.name,
                    it?.binLocation?.name,
                    it?.inventoryItem.lotNumber,
                    it?.inventoryItem?.expirationDate ? Constants.EXPIRATION_DATE_FORMATTER.format(it?.inventoryItem?.expirationDate) : null,
                    it?.quantity,
                    it?.quantity,
                    "None"
            ]
        }

        def results = ["aaData": data]
        render(results as JSON)
    }

    def editBinLocation = {
        Product product = Product.findByProductCode(params.productCode)
        Location location = Location.get(session.warehouse.id)
        Location binLocation = Location.findByParentLocationAndName(location, params.binLocation)
        InventoryItem inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(product, params.lotNumber)
        Integer quantity = inventoryService.getQuantityFromBinLocation(location, binLocation, inventoryItem)
        [location: location, binLocation: binLocation, inventoryItem: inventoryItem, quantity: quantity]
    }

    def saveInventoryChanges = { ManageInventoryCommand command ->
        Transaction transaction = new Transaction(params)
        try {
            //transaction.transactionDate = params.transactionDate
            transaction.createdBy = User.load(session.user.id)
            transaction.inventory = Location.load(session.warehouse.id).inventory

            command.entries.each { entry ->
                if (entry?.quantity > 0) {
                    def transactionEntry = new TransactionEntry()
                    transactionEntry.inventoryItem = entry.inventoryItem
                    transactionEntry.product = entry.inventoryItem.product
                    transactionEntry.quantity = entry.quantity
                    transaction.addToTransactionEntries(transactionEntry)
                }
            }


            if (!transaction?.transactionEntries) {
                //transaction.errors.reject("default.errors.nonEmpty.message", "List must not be empty")
                throw new ValidationException("Transaction entries must not be empty", transaction.errors)
            }


            if (transaction.validate() && transaction.save()) {
                flash.message = "Transaction ${transaction.id} saved"
            } else {
                throw new ValidationException("Transaction errors", transaction.errors)
            }
        } catch (Exception e){
            command.errors = transaction.errors
            chain(action: "manage", model: [command: command], params: params)
            return
        }

        redirect(action: "manage", params: [tags: params.tags])

    }

    def analyze = {
        /* single page app using */
    }

    def pivot = {
        /* single page app using angularjs */
    }


	/**manage
	 * Allows a user to browse the inventory for a particular warehouse.
	 */
    //@Cacheable("inventoryControllerCache")
	def browse = { InventoryCommand commandInstance ->
        if(!params.max) params.max = 10
        if(!params.offset) params.offset = 0

        log.info "Tags: " + commandInstance.tags
        log.info "Catalogs: " + commandInstance.catalogs

		// Get the current warehouse from either the request or the session
        commandInstance.warehouseInstance = Location.get(params?.warehouse?.id)
		if (!commandInstance.warehouseInstance) {
            commandInstance.warehouseInstance = Location.get(session?.warehouse?.id);
		}

		// if we have arrived via a quick link tab, reset any subcategories or search terms in the session
		if (params?.resetSearch) {
			session?.inventorySubcategoryId = null
			session?.inventorySearchTerms = null
		}

		// Pre-populate the sub-category and search terms from the session
        commandInstance.subcategoryInstance = Category.get(session?.inventorySubcategoryId)
        commandInstance.searchTerms = session?.inventorySearchTerms
        commandInstance.showHiddenProducts = session?.showHiddenProducts
        commandInstance.showUnsupportedProducts = session?.showUnsupportedProducts
        commandInstance.showNonInventoryProducts = session?.showNonInventoryProducts
        commandInstance.showOutOfStockProducts = session?.showOutOfStockProducts ?: true

		// If a new search is being performed, override the session-based terms from the request
		if (request.getParameter("searchPerformed")) {
            commandInstance.subcategoryInstance = Category.get(params?.subcategoryId)
			session?.inventorySubcategoryId = commandInstance.subcategoryInstance?.id

            commandInstance.searchTerms = params.searchTerms
			session?.inventorySearchTerms = commandInstance.searchTerms

            commandInstance.showHiddenProducts = params?.showHiddenProducts == "on"
			session?.showHiddenProducts = commandInstance.showHiddenProducts

            commandInstance.showUnsupportedProducts = params?.showUnsupportedProducts == "on"
			session?.showUnsupportedProducts = commandInstance.showUnsupportedProducts

            commandInstance.showOutOfStockProducts = params?.showOutOfStockProducts == "on"
			session?.showOutOfStockProducts = commandInstance.showOutOfStockProducts

            commandInstance.showNonInventoryProducts = params?.showNonInventoryProducts == "on"
			session?.showNonInventoryProducts = commandInstance.showNonInventoryProducts

		}
        commandInstance.maxResults = params?.max
        commandInstance.offset = params?.offset

		// Pass this to populate the matching inventory items
		inventoryService.browseInventory(commandInstance);

		def tags = productService.getPopularTags()

        def catalogs = productService.getAllCatalogs()

		def categories = productService.getTopLevelCategories()

		[ commandInstance: commandInstance, tags : tags, catalogs : catalogs, numProducts : commandInstance.numResults, categories: categories, rootCategory: productService.getRootCategory() ]
	}


	/**
	 *
	 */
	def create = {
		def warehouseInstance = Location.get(params?.warehouse?.id)
		if (!warehouseInstance) {
			warehouseInstance = Location.get(session?.warehouse?.id);
		}
		return [warehouseInstance: warehouseInstance]
	}


	/**
	 *
	 */
	def save = {
		def warehouseInstance = Location.get(params.warehouse?.id)
		if (!warehouseInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'warehouse.label', default: 'Location'), params.id])}"
			redirect(action: "list")
		} else {
			warehouseInstance.inventory = new Inventory(params);
			//inventoryInstance.warehouse = session.warehouse;
			if (warehouseInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), warehouseInstance.inventory.id])}"
				redirect(action: "browse")
			}
			else {
				render(view: "create", model: [warehouseInstance: warehouseInstance])
			}
		}
	}

	/**
	 *
	 */
	def show = {
        def quantityMap = [:]
        def startTime = System.currentTimeMillis()
        def location = Location.get(session.warehouse.id)
		def inventoryInstance = Inventory.get(params.id)
        if (!inventoryInstance) {
            inventoryInstance = location.inventory
        }
        if (!inventoryInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
            redirect(action: "list")
            return
        }


        def elapsedTime = (System.currentTimeMillis() - startTime)
        [
            //inventoryMapping: inventoryMapping,
            location: location,
            elapsedTime: elapsedTime,
            quantityMap: quantityMap
        ]

	}

	def search = { QuantityOnHandReportCommand command ->
        def quantityMapByDate = [:]
        def startTime = System.currentTimeMillis()
        println "search " + params

        println "Locations: " + command?.locations?.toString() + ", Start date = " + command?.startDate + ", End Date = " + command?.endDate + ", Tag: " + command.tags

        if (command.validate()) {

            if (!command?.locations) {
                command.locations = [Location.get(session?.warehouse?.id)]
            }
            //def transactions = Transaction.findAllByInventory(location.inventory)
            //def transactionEntries = (transactions*.transactionEntries).flatten()
            //log.info "transactionEntries: " + transactionEntries.size()
            //def quantityMap = inventoryService.getQuantityByProductMap(transactionEntries)

            if (command.startDate && command.endDate) {
                //def duration = command?.endDate - command?.startDate
                //command.dates = new Date[duration+1]
                //(command?.startDate .. command?.endDate).eachWithIndex { date, i ->
                //    println "Date " + date + " i " + i
                //    command.dates[i] = date
                //}

                //def duration = command.endDate - command.endDate
                def count = 0;

                command.dates = getDatesBetween(command.startDate, command.endDate, command.frequency)
                //if (command.dates.size() >= 61) {
                //    command.dates = []
                    //throw new Exception("Choose a different frequency")
                //    command.errors.rejectValue("frequency","errors.frequency.code","Cannot run report for more than 60 days")
                //    render(view: "show", model: [quantityMapByDate: quantityMapByDate, command: command])
                //    return
                //}

                println "dates : " + command?.dates

            }

            else if (command.startDate) {
                command?.dates << command?.startDate
            }
            else if (command.endDate) {
                command?.dates << command?.endDate
            }

            println "dates: " + command?.dates

            command.locations.each { location ->
                for (date in command?.dates) {
                    println "Get quantity map " + date + " location = " + location
                    def quantityMap = [:]
                    quantityMap = inventoryService.getQuantityOnHandAsOfDate(location, date, command.tags)
                    def existingQuantityMap = quantityMapByDate[date]
                    if (existingQuantityMap) {
                        quantityMapByDate[date] = mergeQuantityMap(existingQuantityMap, quantityMap)
                    }
                    else {
                        quantityMapByDate[date] = quantityMap
                    }
                    println "quantityMap = " + quantityMap?.keySet()?.size() + " results "
                    println "Time " + (System.currentTimeMillis() - startTime) + " ms"
                }
            }


            def keys = quantityMapByDate[command.dates[0]]?.keySet()?.sort()
            println "keys: " + keys
            keys.each { product ->
                command.products << product
            }
        }

        if (params.button == 'download') {
            if (command.products) {
                def date = new Date();
                response.setHeader("Content-disposition", "attachment; filename=\"Baseline-QoH-${date.format("yyyyMMdd-hhmmss")}.csv\"")
                response.contentType = "text/csv"
                def csv = inventoryService.exportBaselineQoH(command.products, quantityMapByDate)
                println "export products: " + csv
                render csv
            }
            else {
                render(text: 'No products found', status: 404)
            }
            return;
        }




        render(view: "show", model: [quantityMapByDate: quantityMapByDate, command: command, elapsedTime:  (System.currentTimeMillis() - startTime)])

    }

    def mergeQuantityMap(oldQuantityMap, newQuantityMap) {
        oldQuantityMap.each { product, oldQuantity ->
            def newQuantity = newQuantityMap[product]?:0
            oldQuantityMap[product] =  newQuantity + oldQuantity

        }
        return oldQuantityMap
    }


    def getDatesBetween(startDate, endDate, frequency) {

        def count = 0
        def dates = []
        if (startDate.before(endDate)) {
            def date = startDate
            while(date.before(endDate)) {
                println "Start date = " + date + " endDate = " + endDate

                dates << date
                if (params.frequency in ['Daily']) {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }
                }
                else if (params.frequency in ['Weekly']) {
                    use(TimeCategory) {
                        date = date.plus(1.week)
                    }
                }
                else if (params.frequency in ['Monthly']) {
                    use(TimeCategory) {
                        date = date.plus(1.month)
                    }
                }
                else if (params.frequency in ['Quarterly']) {
                    use(TimeCategory) {
                        date = date.plus(3.month)
                    }
                }
                else if (params.frequency in ['Annually']) {
                    use(TimeCategory) {
                        date = date.plus(1.year)
                    }
                }
                else {
                    use(TimeCategory) {
                        date = date.plus(1.day)
                    }

                }
                count++
            }
        }
        return dates
    }


    def download = { QuantityOnHandReportCommand command ->

        println "search " + params
        println "search " + command.location + " " + command.startDate
        def quantityMap = inventoryService.getQuantityOnHandAsOfDate(command.location, command.startDate, command.tags)
        if (quantityMap) {
            def statusMap = dashboardService.getInventoryStatus(command.location)
            def filename = "Stock report - " +
                    (command?.tag?command?.tag?.tag:"All Products") + " - " +
                    command?.location?.name + " - " +
                    command?.startDate?.format("yyyyMMMdd") + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        flash.message = "There are no search results available to download - please try again."
        redirect(action: "show")

    }



	def addToInventory = {
		def inventoryInstance = Inventory.get( params.id )
		def productInstance = Product.get( params.product.id )

		if (!productInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'product.label', default: 'Product'), params?.product?.id])}"
			redirect(action: "browse");
		}
		else {
			def itemInstance = new InventoryItem(product: productInstance)
			if (!itemInstance.hasErrors() && itemInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				flash.message = "${warehouse.message(code: 'inventory.unableToCreateItem.message')}"
				//inventoryInstance.errors = itemInstance.errors;
				//render(view: "browse", model: [inventoryInstance: inventoryInstance])
			}
		}
	}


	def edit = {
		def inventoryInstance = Inventory.get(params.id)
		if (!inventoryInstance) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
		else {
			def productInstanceMap = Product.getAll().groupBy { it.productType }

			return [inventoryInstance: inventoryInstance, productInstanceMap: productInstanceMap]
		}
	}

	def update = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (inventoryInstance.version > version) {
					inventoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'inventory.label', default: 'Inventory')] as Object[],
						"Another user has updated this Inventory while you were editing")
					render(view: "edit", model: [inventoryInstance: inventoryInstance])
					return
				}
			}
			inventoryInstance.properties = params
			if (!inventoryInstance.hasErrors() && inventoryInstance.save(flush: true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), inventoryInstance.id])}"
				redirect(action: "browse", id: inventoryInstance.id)
			}
			else {
				render(view: "edit", model: [inventoryInstance: inventoryInstance])
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def inventoryInstance = Inventory.get(params.id)
		if (inventoryInstance) {
			try {
				inventoryInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "list")
		}
	}

	def addItem = {
		def inventoryInstance = Inventory.get(params?.inventory?.id)
		def productInstance = Product.get(params?.product?.id);
		def itemInstance = inventoryService.findByProductAndLotNumber(productInstance, params.lotNumber)
		if (itemInstance) {
			flash.message = "${warehouse.message(code: 'default.alreadyExists.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
			redirect(action: "show", id: inventoryInstance.id)
		}
		else {
			itemInstance = new InventoryItem(params)
			if (itemInstance.hasErrors() || !itemInstance.save(flush:true)) {
				flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				redirect(action: "show", id: inventoryInstance.id)
			}
			else {
				itemInstance.errors.each { println it }
				//redirect(action: "show", id: inventoryInstance.id)
				flash.message = "${warehouse.message(code: 'default.notUpdated.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory item'), inventoryInstance.id])}"
				render(view: "show", model: [inventoryInstance: inventoryInstance, itemInstance : itemInstance])
			}
		}
	}

	def deleteItem = {
		def itemInstance = InventoryItem.get(params.id)
		if (itemInstance) {
			try {
				itemInstance.delete(flush: true)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'inventoryItem.label', default: 'Inventory item'), params.id])}"
				redirect(action: "show", id: params.inventory.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'inventory.label', default: 'Inventory'), params.id])}"
			redirect(action: "show", id: params.inventory.id)
		}


	}

	def listTransactions = {
		redirect(action: listAllTransactions)
	}

	def listDailyTransactions = {
		def dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		def dateSelected = (params.date) ? dateFormat.parse(params.date) : new Date();

		def transactionsByDate = Transaction.list().groupBy { DateUtil.clearTime(it?.transactionDate) }?.entrySet()?.sort{ it.key }?.reverse()

		def transactions = Transaction.findAllByTransactionDate(dateSelected);

		[ transactions: transactions, transactionsByDate: transactionsByDate, dateSelected: dateSelected ]
	}



    def list = {
        println "List " + params
        def location = Location.get(session.warehouse.id)
        def quantityMap = inventorySnapshotService.getCurrentInventory(location)
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "${location.name} - ${status}.csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text: getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        [quantityMap:quantityMap, statusMap: statusMap]
    }



    def listReconditionedStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getReconditionedStock(location)
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Reconditioned stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listTotalStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getTotalStock(location);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Total stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])

    }

    def listInStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getInStock(location);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "In stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])

    }

    def listLowStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getLowStock(location);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Low stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }

    def listReorderStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getReorderStock(location);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Reorder stock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listQuantityOnHandZero = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getQuantityOnHandZero(location);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Out of stock  - all - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }

    def listHealthyStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getHealthyStock(location)
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Overstock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }


    def listOverStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getOverStock(location)
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Overstock - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }

        //[inventoryItems:lowStock, quantityMap:quantityMap]
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }

    def listOutOfStock = {
        def location = Location.get(session.warehouse.id)
        def quantityMap = dashboardService.getOutOfStock(location, params.abcClass);
        def statusMap = dashboardService.getInventoryStatus(location)
        if (params.format == "csv") {
            def filename = "Out of stock - supported - " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForProductMap(quantityMap, statusMap))
            return;
        }
        render (view: "list", model: [quantityMap:quantityMap, statusMap: statusMap])
    }



	def listExpiredStock = {
		def location = Location.get(session.warehouse.id)
		def categorySelected = (params.category) ? Category.get(params.category) : null;
		def inventoryItems = dashboardService.getExpiredStock(categorySelected, location);
		def categories = inventoryItems?.collect { it.product.category }?.unique()
		def quantityMap = inventorySnapshotService.getQuantityOnHandByInventoryItem(location)
        def expiredStockMap = [:]
		inventoryItems.each { inventoryItem ->
            expiredStockMap[inventoryItem] = quantityMap[inventoryItem]
        }
        if (params.format == "csv") {
            def filename = "Expired stock | " + location?.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForInventoryMap(expiredStockMap))
            return;
        }

		[inventoryItems:inventoryItems, quantityMap:quantityMap, categories:categories, categorySelected:categorySelected]
	}


	def listExpiringStock = {
        def expirationStatus = params.status
        def location = Location.get(session.warehouse.id)
		def category = (params.category) ? Category.get(params.category) : null;
		def inventoryItems = dashboardService.getExpiringStock(category, location, expirationStatus)
		def categories = inventoryItems?.collect { it?.product?.category }?.unique().sort { it.name } ;
		def quantityMap = inventorySnapshotService.getQuantityOnHandByInventoryItem(location)
        def expiringStockMap = [:]
		inventoryItems.each { inventoryItem ->
            expiringStockMap[inventoryItem] = quantityMap[inventoryItem]
        }

        if (params.format == "csv") {
            def filename = "Expiring stock | " + location.name + ".csv"
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"")
            render(contentType: "text/csv", text:getCsvForInventoryMap(expiringStockMap))
            return;
        }

		[inventoryItems:inventoryItems, quantityMap:quantityMap, categories:categories,
			categorySelected:category, expirationStatus:expirationStatus ]
	}


    protected def getCsvForInventoryMap(map) {
        return getCsvForInventoryMap(map, [:])
    }

    protected def getCsvForInventoryMap(map, statusMap) {
        def csv = "";
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.lotNumber.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryItem.expirationDate.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default:'Tags')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.binLocation.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.forecastQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.totalValue.label')}"  + '"'
        csv += "\n"

        def hasRoleFinance = userService.hasRoleFinance(session.user)

        map.each { inventoryItem, quantity ->

            def product = inventoryItem?.product
            def inventoryLevel = product?.getInventoryLevel(session.warehouse.id)
            def totalValue = (product?.pricePerUnit?:0) * (quantity?:0)
            def statusMessage = inventoryLevel?.statusMessage(quantity?:0)
            if (!statusMessage) {
                def status = quantity > 0 ? "IN_STOCK" : "STOCKOUT"
                statusMessage = "${warehouse.message(code:'enum.InventoryLevelStatusCsv.'+status)}"
            }
            csv += '"' + (statusMessage?:"")  + '"' + ","
            csv += '"' + (product.productCode?:"")  + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.name?:"") + ","
            csv += StringEscapeUtils.escapeCsv(inventoryItem?.lotNumber?:"") + ","
            csv += '"' + formatDate(date: inventoryItem?.expirationDate, format: 'dd/MM/yyyy')  + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.category?.name?:"") + ","
            csv += '"' + (product?.tagsToString()?:"")  + '"' + ","
            csv += '"' + (inventoryLevel?.binLocation?:"") + '"' + ","
            csv += '"' + (product?.unitOfMeasure?:"") + '"' + ","
            csv += (inventoryLevel?.minQuantity?:"") + ","
            csv += (inventoryLevel?.reorderQuantity?:"") + ","
            csv += (inventoryLevel?.maxQuantity?:"")+ ","
            csv += (inventoryLevel?.forecastQuantity?:"")+ ","
            csv += '' + (quantity?:"0")  + '' + ","
            csv += (hasRoleFinance ? (product?.pricePerUnit?:"") : "") + ","
            csv += (hasRoleFinance ? (totalValue?:""):"")
            csv += "\n"
        }
        return csv
    }

   protected def getCsvForProductMap(map) {
        return getCsvForProductMap(map, [:])
    }

   protected def getCsvForProductMap(map, statusMap) {
        def hasRoleFinance = userService.hasRoleFinance(session.user)

        def csv = "";
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.status.label')}" + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.productCode.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'category.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.tags.label', default:'Tags')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.binLocation.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.abcClass.label', default: 'ABC Class')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.unitOfMeasure.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.minQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.reorderQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.maxQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.forecastQuantity.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'inventoryLevel.currentQuantity.label', default: 'Current quantity')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.pricePerUnit.label')}"  + '"' + ","
        csv += '"' + "${warehouse.message(code: 'product.totalValue.label')}"  + '"'
        csv += "\n"

        map.sort().each { product, quantity ->
            InventoryLevel inventoryLevel = product?.getInventoryLevel(session.warehouse.id)
            def status = statusMap[product]
            def totalValue = (product?.pricePerUnit?:0) * (quantity?:0)
            def statusMessage = "${warehouse.message(code:'enum.InventoryLevelStatusCsv.'+status)}"
            csv += '"' + (statusMessage?:"")  + '"' + ","
            csv += '"' + (product.productCode?:"")  + '"' + ","
            csv += StringEscapeUtils.escapeCsv(product?.name) + ","
            csv += '"' + (product?.category?.name?:"")  + '"' + ","
            csv += '"' + (product?.tagsToString()?:"")  + '"' + ","
            csv += '"' + (inventoryLevel?.binLocation?:"")  + '"' + ","
            csv += '"' + (inventoryLevel?.abcClass?:"")  + '"' + ","
            csv += '"' + (product?.unitOfMeasure?:"")  + '"' + ","
            csv += (inventoryLevel?.minQuantity?:"") + ","
            csv += (inventoryLevel?.reorderQuantity?:"") + ","
            csv += (inventoryLevel?.maxQuantity?:"") + ","
            csv += (inventoryLevel?.forecastQuantity?:"") + ","
            csv += (quantity?:"0") + ","
            csv += (hasRoleFinance ? (product?.pricePerUnit?:"") : "") + ","
            csv += (hasRoleFinance ? (totalValue?:""):"")
            csv += "\n"
        }
        return csv
    }


    def exportLatestInventoryDate = {
        println params
        def location = Location.get(session.warehouse.id)

        if (location) {
            def date = new Date();
            response.setHeader("Content-disposition",
                    "attachment; filename=\"MostRecentStockCount-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            render dashboardService.exportLatestInventoryDate(location)
        }
        else {
            //render(text: 'No products found', status: 404)
            response.sendError(404)
        }
    }

	/**
	 * Used to create default inventory items.
	 * @return
	 */
	def createDefaultInventoryItems = {
		def products = inventoryService.findProductsWithoutEmptyLotNumber();
		products.each { product ->
			def inventoryItem = new InventoryItem()
			inventoryItem.product = product
			inventoryItem.lotNumber = null;
			inventoryItem.expirationDate = null;
			inventoryItem.save();
		}
		redirect(controller: "inventory", action: "showProducts")
	}


	def showProducts = {
		def products = inventoryService.findProductsWithoutEmptyLotNumber()
		[ products : products ]

	}


	def listAllTransactions = {

        Location location = Location.get(session.warehouse.id)
        def currentInventory = location.inventory

        Date transactionDateFrom = params.transactionDateFrom ? Date.parse("MM/dd/yyyy", params.transactionDateFrom): null
        Date transactionDateTo = params.transactionDateTo ? Date.parse("MM/dd/yyyy", params.transactionDateTo) : null

		// we are only showing transactions for the inventory associated with the current warehouse
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.sort = params?.sort ?: "dateCreated"
		params.order = params?.order ?: "desc"


		def transactionType = TransactionType.get(params?.transactionType?.id)
        def transactions = Transaction.createCriteria().list(params) {
            and {
                eq("inventory", currentInventory)
                if (transactionType) {
                    eq("transactionType", transactionType)
                }
                if (params.transactionNumber) {
                    ilike("transactionNumber", "%" + params.transactionNumber + "%")
                }
                if (params.transactionDateFrom) {
                    ge("transactionDate", transactionDateFrom)
                }
                if (params.transactionDateTo) {
                    le("transactionDate", transactionDateTo)
                }
            }
            maxResults(params.max)
            order(params.sort, params.order)
        }

		render(view: "listTransactions", model: [transactionInstanceList: transactions,
			transactionCount: transactions.totalCount, transactionTypeSelected: transactionType])
	}


	def listPendingTransactions = {
		def transactions = Transaction.findAllByConfirmedOrConfirmedIsNull(Boolean.FALSE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}

	def listConfirmedTransactions = {
		def transactions = Transaction.findAllByConfirmed(Boolean.TRUE)
		render(view: "listTransactions", model: [transactionInstanceList: transactions])
	}


	def deleteTransaction = {
		def transactionInstance = Transaction.get(params.id);

		if (transactionInstance) {
			try {

                inventoryService.deleteTransaction(transactionInstance)
				flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "listTransactions")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
				redirect(action: "editTransaction", id: params.id)
			}
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'transaction.label', default: 'Transaction'), params.id])}"
			redirect(action: "listTransactions")
		}
	}


	def saveTransaction = {
		log.debug "save transaction: " + params
		def transactionInstance = Transaction.get(params.id);
		// def inventoryInstance = Inventory.get(params.inventory.id);

		if (!transactionInstance) {
			transactionInstance = new Transaction();
		}

		transactionInstance.properties = params

		// either save as a local transfer, or a generic transaction
		// (catch any exceptions so that we display "nice" error messages)
		Boolean saved = null
		if (!transactionInstance.hasErrors()) {
			try {
				//if (inventoryService.isValidForLocalTransfer(transactionInstance)) {
				//	saved = inventoryService.saveLocalTransfer(transactionInstance)
				//}
				//else {
				saved = transactionInstance.save(flush:true)
				//}
			}
			catch (Exception e) {
				log.error("Unable to save transaction ", e);
			}
		}

		if (saved) {
			flash.message = "${warehouse.message(code: 'inventory.transactionSaved.message')}"
			redirect(action: "editTransaction", id: transactionInstance?.id);
		}
		else {
			flash.message = "${warehouse.message(code: 'inventory.unableToSaveTransaction.message')}"
			def model = [
				transactionInstance : transactionInstance,
				productInstanceMap: Product.list().groupBy { it.category },
				transactionTypeList: TransactionType.list(),
				locationInstanceList: Location.list(),
				warehouseInstance: Location.get(session?.warehouse?.id)
			]
			render(view: "editTransaction", model: model);
		}
	}


	/**
	 * Show the transaction.
	 */
	def showTransaction = {
		def transactionInstance = Transaction.get(params.id);
		if (!transactionInstance) {
			flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
			transactionInstance = new Transaction();
		}

		def model = [transactionInstance : transactionInstance ]
		render(view: "showTransaction", model: model);
	}

	/**
	* Show the transaction.
	*/
   def showTransactionDialog = {
	   def transactionInstance = Transaction.get(params.id);
	   if (!transactionInstance) {
		 	flash.message = "${warehouse.message(code: 'inventory.noTransactionWithId.message', args: [params.id])}"
		   transactionInstance = new Transaction();
	   }

	   def model = [
		   transactionInstance : transactionInstance,
		   productInstanceMap: Product.list().groupBy { it.category },
		   transactionTypeList: TransactionType.list(),
		   locationInstanceList: Location.list(),
		   warehouseInstance: Location.get(session?.warehouse?.id)
	   ];

	   render(view: "showTransactionDialog", model: model);

   }


	def confirmTransaction = {
		def transactionInstance = Transaction.get(params?.id)
		if (transactionInstance?.confirmed) {
			transactionInstance?.confirmed = Boolean.FALSE;
			transactionInstance?.confirmedBy = null;
			transactionInstance?.dateConfirmed = null;
					flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenUnconfirmed.message')}"
		}
		else {
			transactionInstance?.confirmed = Boolean.TRUE;
			transactionInstance?.confirmedBy = User.get(session?.user?.id);
			transactionInstance?.dateConfirmed = new Date();
			flash.message = "${warehouse.message(code: 'inventory.transactionHasBeenConfirmed.message')}"
		}
		redirect(action: "listAllTransactions")
	}

	def createInboundTransfer = {
        params.transactionType = TransactionType.get(Constants.TRANSFER_IN_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createOutboundTransfer = {
        params.transactionType = TransactionType.get(Constants.TRANSFER_OUT_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createInventory = {
        params.transactionType = TransactionType.get(Constants.INVENTORY_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createConsumed = {
        params.transactionType = TransactionType.get(Constants.CONSUMPTION_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createExpired = {
        params.transactionType = TransactionType.get(Constants.EXPIRATION_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createDamaged = {
        params.transactionType = TransactionType.get(Constants.DAMAGE_TRANSACTION_TYPE_ID)
        forward(action: "createTransaction", params:params)
    }

    def createTransaction = {
		println("createTransaction: " + params)
		def command = new TransactionCommand();
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def transactionInstance = new Transaction(params);

		if (!transactionInstance?.transactionType) {
			flash.message = "Cannot create transaction for unknown transaction type ${params['transactionType.id']}";
			redirect(controller: "inventoryItem", action: "showStockCard", id: params["product.id"])
            return
		}

        def products = []

		// Process productId parameters from inventory browser
		if (params?.product?.id) {
			def productIds = params.list('product.id')
			productIds = productIds.collect { String.valueOf(it); }
            if (productIds) {
                products = Product.getAll(productIds)
                command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, productIds);

                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
            }
		}
		// If given a list of inventory items, we just return those inventory items
		else if (params?.inventoryItem?.id) {
			def inventoryItemIds = params.list('inventoryItem.id')
			def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }

            def productIds = inventoryItems.collect { it?.product?.id }
            if (productIds) {
                products = Product.getAll(productIds)
            }
			//command?.productInventoryItems = inventoryItems.groupBy { it.product }
            command.binLocations = inventoryService.getItemQuantityByBinLocation(warehouseInstance, inventoryItems)
		}
        else {
            throw new RuntimeException("You must select at least one product or inventory item")
        }

        println "Product inventory items " + command?.productInventoryItems

		command.transactionInstance = transactionInstance
		command.warehouseInstance = warehouseInstance

		command.quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory, products);
		command.transactionTypeList = TransactionType.list();
		command.locationList = Location.list();

		[command : command]

	}

	/**
	 * Save a transaction that sets the current inventory level for stock.
	 */
   // @CacheFlush("inventoryBrowserCache")
	def saveInventoryTransaction = { TransactionCommand command ->
		log.info ("Saving inventory adjustment " + params)
        log.info "Command: " + command

		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		//def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory)

		// Item cannot have a negative quantity
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {

					// FIXME Need to do some validation at this point
					//def onHandQuantity = quantityMap[it.inventoryItem]
					// If the quantity changes, we record a new transaction entry
					//if (it.quantity != onHandQuantity) {
					def transactionEntry = new TransactionEntry()
                    transactionEntry.product = it.inventoryItem.product
					transactionEntry.inventoryItem = it.inventoryItem
					transactionEntry.quantity = it.quantity
                    transactionEntry.binLocation = it.binLocation
                    transactionEntry.comments = it.comment
					transaction.addToTransactionEntries(transactionEntry)
					//}
				}

				// Validate the transaction object
				if (!transaction.hasErrors() && transaction.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction?.transactionNumber?:transaction?.id
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "browse")
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction.hasErrors()) {
			log.debug ("has errors" + transaction.errors)

			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}

			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance

			render(view: "createTransaction", model: [command: command]);
		}
	}

	/**
	 * Save a transaction that debits stock from the given inventory.
	 *
	 * TRANSFER_OUT, CONSUMED, DAMAGED, EXPIRED
	 */

//    @CacheFlush("inventoryBrowserCache")
	def saveDebitTransaction = { TransactionCommand command ->
		log.info ("Saving debit transactions " + params)
		println("size: " + command?.transactionEntries?.size());


        // Get the products involved
        def productIds = params.list('product.id').collect { String.valueOf(it); }
        List products = Product.getAll(productIds)

		def transaction = command?.transactionInstance;
		def warehouseInstance = Location.get(session?.warehouse?.id);
		def quantityMap = inventoryService.getQuantityForInventory(warehouseInstance?.inventory, products)

		// Quantity cannot be greater than on hand quantity
		command.transactionEntries.each {
			def onHandQuantity = quantityMap[it.inventoryItem];
			if (it.quantity > onHandQuantity) {
				transaction.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// Check to see if there are errors, if not save the transaction
		if (!transaction?.hasErrors()) {
			try {
				// Add validated transaction entries to the transaction we want to persist
				command.transactionEntries.each {
					if (it.quantity) {
						def transactionEntry = new TransactionEntry()
						transactionEntry.inventoryItem = it.inventoryItem
                        transactionEntry.product = it.product
						transactionEntry.quantity = it.quantity
                        transactionEntry.binLocation = it.binLocation
						transaction.addToTransactionEntries(transactionEntry)
					}
				}

				// Validate the transaction object
				if (!transaction?.hasErrors() && transaction?.validate()) {
					transaction.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transaction?.transactionNumber?:transaction?.id
					//redirect(controller: "inventory", action: "browse")
                    if (productIds.size() > 1) {
                        redirect(controller: "inventoryItem", action: "showStockCard", id: productIds[0])
                    } else {
                        redirect(controller: "inventoryItem", action: "showStockCard", id: productIds)
                    }
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// After the attempt to save the transaction, there might be errors on the transaction
		if (transaction?.hasErrors()) {
			log.debug ("has errors" + transaction.errors)

			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, productIds);
                command.binLocations = inventoryService.getProductQuantityByBinLocation(warehouseInstance, products)
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}

			// Populate the command object and render the form view.
			command.transactionInstance = transaction
			command.warehouseInstance = warehouseInstance
			command.quantityMap = quantityMap;

			render(view: "createTransaction", model: [command: command]);
		}
	}



	/**
	 * Save a transaction that debits stock from the given inventory.
	 *
	 * TRANSFER_IN
	 */
//    @CacheFlush("inventoryBrowserCache")
	def saveCreditTransaction = { TransactionCommand command ->

		log.debug("Saving credit transaction: " + params)
		def transactionInstance = command?.transactionInstance
		def warehouseInstance = Location.get(session?.warehouse?.id);

		// Quantity cannot be less than 0 or else it would be in a debit transaction
		command.transactionEntries.each {
			if (it.quantity < 0) {
				transactionInstance.errors.rejectValue("transactionEntries", "transactionEntry.quantity.invalid", [it?.inventoryItem?.lotNumber] as Object[], "")
			}
		}

		// We need to process each transaction entry to make sure that it has a valid inventory item (or we will create one if not)
		command.transactionEntries.each {
			if (!it.inventoryItem) {
				// Find an existing inventory item for the given lot number and product and description
				log.debug("Find inventory item " + it.product + " " + it.lotNumber)
				def inventoryItem = inventoryService.findInventoryItemByProductAndLotNumber(it.product, it.lotNumber)
				log.debug("Found inventory item? " + inventoryItem)

				// If the inventory item doesn't exist, we create a new one
				if (!inventoryItem) {
					inventoryItem = new InventoryItem();
					inventoryItem.lotNumber = it.lotNumber
					inventoryItem.expirationDate = (it.lotNumber) ? it.expirationDate : null
					inventoryItem.product = it.product;
					log.debug("Save inventory item " + inventoryItem)
					if (inventoryItem.hasErrors() || !inventoryItem.save()) {
						inventoryItem.errors.allErrors.each { error->
							command.errors.reject("inventoryItem.invalid",
								[inventoryItem, error.getField(), error.getRejectedValue()] as Object[],
								"[${error.getField()} ${error.getRejectedValue()}] - ${error.defaultMessage} ");

						}
					}
				}
				it.inventoryItem = inventoryItem
			}
		}

		// Now that all transaction entries in the command have inventory items,
		// we need to create a persistable transaction entry
		command.transactionEntries.each {
			def transactionEntry = new TransactionEntry(inventoryItem: it.inventoryItem,
                    product: it.inventoryItem.product, binLocation: it.binLocation, quantity: it.quantity)
			transactionInstance.addToTransactionEntries(transactionEntry)
		}

		// Check to see if there are errors, if not save the transaction
		if (!transactionInstance.hasErrors()) {
			try {
				// Validate the transaction object
				if (!transactionInstance.hasErrors() && transactionInstance.validate()) {
					transactionInstance.save(failOnError: true)
					flash.message = "Successfully saved transaction " + transactionInstance?.transactionNumber?:transactionInstance?.id
					//redirect(controller: "inventory", action: "browse")
					redirect(controller: "inventory", action: "browse")
				}
			} catch (ValidationException e) {
				log.debug ("caught validation exception " + e)
			}
		}

		// Should be true if a validation exception was thrown
		if (transactionInstance.hasErrors()) {
			log.debug ("has errors" + transactionInstance.errors)

			// Get the list of products that the user selected from the inventory browser
			if (params.product?.id) {
				def productIds = params.list('product.id')
				def products = productIds.collect { String.valueOf(it); }
				command.productInventoryItems = inventoryService.getInventoryItemsByProducts(warehouseInstance, products);
			}
			// If given a list of inventory items, we just return those inventory items
			else if (params?.inventoryItem?.id) {
				def inventoryItemIds = params.list('inventoryItem.id')
				def inventoryItems = inventoryItemIds.collect { InventoryItem.get(String.valueOf(it)); }
				command?.productInventoryItems = inventoryItems.groupBy { it.product }
			}

			// Populate the command object and render the form view.
			command.warehouseInstance = warehouseInstance
			render(view: "createTransaction", model: [command: command]);
		}
	}

	def editTransaction = {
        def startTime = System.currentTimeMillis()
		log.info "edit transaction: " + params
        def model
        def warehouseInstance = Location.get(session?.warehouse?.id)
        if(params?.id){
		def transactionInstance = Transaction.get(params?.id)
        def products = transactionInstance?.transactionEntries.collect { it.inventoryItem.product }
		def inventoryItems = InventoryItem.findAllByProductInList(products)
		 model = [
			inventoryItemsMap: inventoryItems.groupBy { it.product?.id } ,
			transactionInstance: transactionInstance?:new Transaction(),
			transactionTypeList: TransactionType.list(),
			locationInstanceList: Location.findAllByParentLocationIsNull(),
			quantityMap: [:],//inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
			warehouseInstance: warehouseInstance
        ]
        }
        else
        {
            model = [
                    inventoryItemsMap: null  ,
                    transactionInstance: null ,
                    transactionTypeList: TransactionType.list(),
                    locationInstanceList: Location.findAllByParentLocationIsNull(),
                    quantityMap: [:],//inventoryService.getQuantityForInventory(warehouseInstance?.inventory),
                    warehouseInstance: warehouseInstance
            ]
        }
        println "Edit transaction " + (System.currentTimeMillis() - startTime) + " ms"

		render(view: "editTransaction", model: model)

	}


	/**
	* TODO These are the same methods used in the inventory browser.  Need to figure out a better
	* way to handle this (e.g. through a generic ajax call or taglib).
	*/
	def removeCategoryFilter = {
		def category = Category.get(params?.categoryId)
		if (category)
			session.inventoryCategoryFilters.remove(category?.id);
		redirect(action: browse);
	}

	def clearAllFilters = {
		session.inventoryCategoryFilters = [];
		session.inventorySearchTerms = [];
		redirect(action: browse);
	}
	def addCategoryFilter = {
		def category = Category.get(params?.categoryId);
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			session.inventoryCategoryFilters << category?.id;
		redirect(action: browse);
	}
	def narrowCategoryFilter = {
		def category = Category.get(params?.categoryId);
		session.inventoryCategoryFilters = []
		if (category && !session.inventoryCategoryFilters.contains(category?.id))
			   session.inventoryCategoryFilters << category?.id;
		redirect(action: browse);
	}
	def removeSearchTerm = {
		if (params.searchTerm)
			session.inventorySearchTerms.remove(params.searchTerm);
		redirect(action: browse);
	}


    def upload = {
        def inventoryList = [:]
        if (request.method == "POST") {
            File localFile = null;
            MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request;
            CommonsMultipartFile uploadFile = (CommonsMultipartFile) mpr.getFile("file");
            if (!uploadFile?.empty) {
                try {
                    localFile = new File("uploads/" + uploadFile.originalFilename);
                    localFile.mkdirs()
                    uploadFile.transferTo(localFile);
                    //flash.message = "File uploaded successfully"

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            //Workbook workbook = WorkbookFactory.create(file.inputStream)
            //Iterate through bookList and create/persists your domain instances
            //def bookList = excelImportService.columns(workbook, CONFIG_BOOK_COLUMN_MAP)
            def excelImporter = new InventoryExcelImporter(localFile.absolutePath)
            inventoryList = excelImporter.data
            println inventoryList
        }


        [inventoryList:inventoryList]

    }

}


class QuantityOnHandReportCommand {
    //Location location
    //def locations = ListUtils.lazyList([], FactoryUtils.instantiateFactory(Location))
    List<Location> locations = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(Location.class));
    List dates = []
    List products = []
    List tags = []
    Tag tag
    Date startDate = new Date()
    Date endDate
    String frequency


    static constraints = {
        locations(nullable: false,
                validator: { value, obj-> value?.size() >= 1 })
        startDate(nullable:false,
                validator: { value, obj-> !obj.endDate || value.before(obj.endDate) })
        endDate(nullable: false)
        frequency(nullable: false, blank: false)
    }
}

class ManageInventoryCommand {

    List<ManageInventoryEntryCommand> entries = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(ManageInventoryEntryCommand.class));
    List inventoryItems = []
    List binLocations = []
    String productCodes
    List tags = []
}

class ManageInventoryEntryCommand {
    InventoryItem inventoryItem
    Integer quantity

}
