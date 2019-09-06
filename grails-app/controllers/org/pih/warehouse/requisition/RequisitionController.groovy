/**
 * Copyright (c) 2012 Partners In Health.  All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 * */
package org.pih.warehouse.requisition

import grails.converters.JSON
import grails.validation.ValidationException
import org.apache.commons.collections.FactoryUtils
import org.apache.commons.collections.list.LazyList
import org.hibernate.HibernateException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.picklist.Picklist
import org.pih.warehouse.picklist.PicklistItem
import org.pih.warehouse.product.Product
import org.pih.warehouse.product.ProductPackage
import org.springframework.orm.hibernate3.HibernateSystemException

class RequisitionController {

    def dataService
    def requisitionService
    def inventoryService
	def productService

    static allowedMethods = [save: "POST", update: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        def user = User.get(session?.user?.id)
        def location = Location.get(session?.warehouse?.id)

        params.origin = Location.get(session?.warehouse?.id)
        def requisition = new Requisition(params)

        // Requisitions to display in the table
        def requisitions = requisitionService.getRequisitions(requisition, params)
        def requisitionStatistics = requisitionService.getRequisitionStatistics(null, requisition.origin, user)

        render(view:"list", model:[requisitions: requisitions, requisitionStatistics:requisitionStatistics])
    }

    def chooseTemplate = {
        render(view:"chooseTemplate")
    }

    def createStockFromTemplate = {
        def requisition = new Requisition()
        def requisitionTemplate = Requisition.get(params.id)
        if (requisitionTemplate) {
            requisition.type = requisitionTemplate.type
            requisition.origin = requisitionTemplate.origin
            requisition.destination = requisitionTemplate.destination
            requisition.commodityClass = requisitionTemplate.commodityClass
            requisition.createdBy = User.get(session.user.id)
            requisition.dateRequested = new Date()

            requisitionTemplate.requisitionItems.each {
                println "Adding requisition item " + it.product.name + " [" + it.orderIndex + "]"
                def requisitionItem = new RequisitionItem()
                requisitionItem.inventoryItem = it.inventoryItem
                requisitionItem.quantity = it.quantity
                requisitionItem.product = it.product
                requisitionItem.productPackage = it.productPackage
                requisitionItem.orderIndex = it.orderIndex
                requisition.addToRequisitionItems(requisitionItem)
            }
        }
        else {
            flash.message = "Could not find requisition template"
        }

        println "redirecting to create stock page " + requisition.id
        render(view:"createStock", model:[requisition:requisition])
    }

    def create = {
        def requisition = new Requisition(status: RequisitionStatus.CREATED)
        requisition.type = params.type as RequisitionType
        render(view:"createNonStock", model:[requisition:requisition])
    }

    def save = {
        log.info "Save requisition " + params
        withForm {
            def requisition = new Requisition(params)
            // Need to handle commodity class since it is an enum
            if (params.commodityClass) {
                requisition.commodityClass = params.commodityClass as CommodityClass
            }
            requisition.name = getName(requisition)
            requisition.requestNumber = requisitionService.getIdentifierService().generateRequisitionIdentifier()
            requisition = requisitionService.saveRequisition(requisition)
            if (!requisition.hasErrors()) {
                redirect(controller: "requisition", action: "edit", id: requisition?.id)
            } else {

                if (requisition.type == RequisitionType.STOCK) {
                    render(view: "createStock", model: [requisition: requisition])
                }
                else {
                    render(view: "createNonStock", model: [requisition: requisition])
                }
            }
        }.invalidToken {
            flash.message = "${g.message(code: 'requisition.invalid.duplicate.message')}"
            def requisition = Requisition.findByRequestNumber(params.requestNumber)
            if (requisition) {
                redirect(action: "show", id: requisition?.id)
            }
            else {
                redirect(action: "list")
            }
        }
    }




	def edit = {
		def requisition = Requisition.get(params.id)
		if(requisition) {

            if (requisition.status < RequisitionStatus.EDITING) {
                requisition.status = RequisitionStatus.EDITING
                requisition.save(flush:true)
            }


            println "Requisition json: " + requisition.toJson()

            return [requisition: requisition];



		}else {
			response.sendError(404)
		}
	}
	
	def editHeader = {
		def requisition = Requisition.get(params.id)
        [requisition: requisition];
	}
	



	def saveHeader = { 
	//	def success = true
		
		def requisition = Requisition.get(params.id)
		
		if(requisition) {
			requisition.properties = params
            requisition.name = getName(requisition)
		    requisition = requisitionService.saveRequisition(requisition)

			if (requisition.hasErrors()) {
				render(view:"editHeader", model:[requisition:requisition])
			    return;
            }
		}
		
		redirect(action: "edit", id: requisition?.id)
		
		
	}
	
    def normalize = {
        def requisition = Requisition.get(params.id)
        requisitionService.normalizeRequisition(requisition)
        redirect(action: "review", id: requisition.id)
    }

    def normalizeAll = {
        def requisitions = Requisition.list()
        requisitions.each { requisition ->
            requisitionService.normalizeRequisition(requisition)
        }

    }


	def review = {
		def requisition = Requisition.findById(params.id)

        if (requisition.status < RequisitionStatus.VERIFYING) {
            requisition.status = RequisitionStatus.VERIFYING
            //requisition.verifiedBy = User.load(session.user.id)
            //requisition.dateVerified = new Date()
            requisition.save(flush:true)
        }

		if (!requisition) {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
			redirect(action: "list")
		}
		else {

            def location = Location.get(session.warehouse.id)
            def quantityAvailableToPromiseMap = [:]
            def quantityOnHandMap = [:]

            def products = requisition.requisitionItems.collect { it.product }
            def quantityProductMap = inventoryService.getQuantityByProductMap(location.inventory, products)

            requisition?.requisitionItems?.each { requisitionItem ->
                quantityOnHandMap[requisitionItem?.product?.id] = quantityProductMap[requisitionItem?.product]?:0
                    //inventoryService.getQuantityOnHand(location, requisitionItem?.product)
                //quantityAvailableToPromiseMap[requisitionItem?.product?.id] = quantityProductMap[requisitionItem?.product]?:0
                    //inventoryService.getQuantityAvailableToPromise(location, requisitionItem?.product)
            }

            def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
            //def quantityOnHand = (requisitionItem)?inventoryService.getQuantityOnHand(location, requisitionItem?.product):0
            //def quantityOutgoing = (requisitionItem)?inventoryService.getQuantityToShip(location, requisitionItem?.product):0
            //def quantityAvailableToPromise = (quantityOnHand - quantityOutgoing)?:0;


			println "Requisition Status: " + requisition.id + " [" + requisition.status + "]"
			[requisition: requisition,
                    quantityOnHandMap:quantityOnHandMap,
                    selectedRequisitionItem: requisitionItem]
		}
    }


    def saveRequisitionItems = {
        def jsonResponse = []
        def requisition = new Requisition()
        try {
            def jsonRequest = request.JSON
            println "Save requisition: " + jsonRequest
            requisition = requisitionService.saveRequisition(jsonRequest, Location.get(session?.warehouse?.id))
            if (!requisition.hasErrors()) {
                jsonResponse = [success: true, data: requisition.toJson()]
            }
            else {
                jsonResponse = [success: false, errors: requisition.errors]
            }
            println(jsonResponse as JSON)

        } catch (HibernateException e) {
            println "hibernate exception " + e.message
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]

        } catch (HibernateSystemException e) {
            println "hibernate system exception " + e.message
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]

        } catch (Exception e) {
            log.error("Error saving requisition: " + e.message, e)
            println("Errors: " + requisition.errors)
            println("Message: " + e.message)
            jsonResponse = [success: false, errors: requisition?.errors, message: e.message]
        }
        render jsonResponse as JSON
    }


	def confirm = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {

            if (requisition.status < RequisitionStatus.CHECKING) {
                requisition.status = RequisitionStatus.CHECKING
                //requisition.checkedBy = User.load(session.user.id)
                //requisition.dateChecked = new Date()
                requisition.save(flush:true)
            }

			def currentInventory = Location.get(session.warehouse.id).inventory
	//		def picklist = Picklist.findByRequisition(requisition)
			def productInventoryItemsMap = [:]
			def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
			productInventoryItems.keySet().each { product ->
				productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
			}
		}
		[requisition: requisition]
	}

    def saveDetails = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisition.properties = params
            if(params.verifiedBy){
                requisition.verifiedBy = Person.findById(params.verifiedBy.id)
            }
            requisition.save(flush: true)
            requisitionService.saveRequisition(requisition)
        }
        [requisition:requisition]
        redirect(action: params.redirectAction ?: "show", id: requisition.id,requisition:requisition)
    }




	def pick = {
		println "Pick " + params
		def requisition = Requisition.get(params?.id)
		if (requisition) {

            if (!requisition.verifiedBy) {
                //requisition.errors.rejectValue("verifiedBy", "Must specify verified by user before continuing")
                flash.message = "${warehouse.message(code: 'requisition.verifiedBy.invalid.message')}"
                chain(controller: "requisition", action: "review", id: params.id,  model: [requisition:requisition])
                //render(view: "review", model: [requisition:requisition])
                return;
            }

            if (requisition.status < RequisitionStatus.PICKING) {
    			requisition.status = RequisitionStatus.PICKING
                // Approve all pending requisition items
                requisition.requisitionItems.each { requisitionItem ->
                    if (requisitionItem.isPending()) {
                        requisitionItem.approveQuantity()
                    }
                }

                requisition.save(flush:true)
            }

						
			def currentInventory = Location.get(session.warehouse.id).inventory
			def picklist = Picklist.findByRequisition(requisition)

			if (!picklist) {
				picklist = new Picklist();
				picklist.requisition = requisition
				if (!picklist.save(flush:true)) {
					throw new ValidationException("Unable to create new picklist", picklist.errors)
				}
			}
			
			def productInventoryItemsMap = [:]
			def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
			productInventoryItems.keySet().each { product ->
				productInventoryItemsMap[product.id] = productInventoryItems[product]//.collect{it.toJson()}
			}

			def location = Location.get(session.warehouse.id)
			def inventoryLevelMap = [:]
			productInventoryItems.keySet().each { product ->				
				inventoryLevelMap[product] = product.getInventoryLevel(location.id)
			}
			
			
			def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
			//if (!requisitionItem) {
				//requisitionItem = requisition.requisitionItems.first()
			//}
			
			//def similarProducts = productService.findSimilarProducts(requisitionItem?.product)
			//def similarProductInventoryItems = inventoryService.getInventoryItemsWithQuantity(similarProducts, currentInventory)
			
			//String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
			//return [data: jsonString, requisitionId: requisition.id, requisition:requisition]
			[requisition:requisition, productInventoryItemsMap: productInventoryItemsMap, 
				picklist: picklist, inventoryLevelMap: inventoryLevelMap, 
				selectedRequisitionItem: requisitionItem]
			
		}
		else { 
			response.sendError(404)
		}
	}

    def generatePicklist = {
        def requisition = Requisition.get(params?.id)
        requisitionService.generatePicklist(requisition)

        redirect(action:  "pick", id:  requisition?.id)
    }

    def clearPicklist = {
        def requisition = Requisition.get(params?.id)
        requisitionService.clearPicklist(requisition)
        redirect(action:  "pick", id:  requisition?.id)
    }
	
	def pickNextItem = {
        def nextItem
		def requisition = Requisition.get(params?.id)
		def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
        if (!requisitionItem) {
            nextItem = requisition?.requisitionItems?.first()
        }
        else {
		    def currentIndex = requisition?.requisitionItems?.findIndexOf { it == requisitionItem }
		    nextItem = requisition?.requisitionItems[currentIndex+1]?:requisition?.requisitionItems?.first()
        }
		redirect(action: "pick", id: requisition?.id, 
				params: ["requisitionItem.id":nextItem?.id])
	}
	
	def pickPreviousItem = { 
		def requisition = Requisition.get(params?.id)
		def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        if (!requisitionItem) {
            requisitionItem = requisition?.requisitionItems?.first()
        }
		def lastItem = requisition?.requisitionItems?.size()-1
		def currentIndex = requisition.requisitionItems.findIndexOf { it == requisitionItem }
		def previousItem = requisition?.requisitionItems[currentIndex-1]?:requisition?.requisitionItems[lastItem]
		
		redirect(action: "pick", id: requisition?.id, 
				params: ["requisitionItem.id":previousItem?.id])
		
	}
	

	def picked = {
		def requisition = Requisition.get(params.id)
		if (requisition) {
			requisition.status = RequisitionStatus.PENDING
			requisition.save(flush:true)
		}
		redirect(controller: "requisition", action: "confirm", id: requisition.id)				
	}
		
		
    def process = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            def currentInventory = Location.get(session.warehouse.id).inventory
            def picklist = Picklist.findByRequisition(requisition)?: new Picklist()
            def productInventoryItemsMap = [:]
            def productInventoryItems = inventoryService.getInventoryItemsWithQuantity(requisition.requisitionItems?.collect{ it.product}, currentInventory)
            productInventoryItems.keySet().each { product ->
                productInventoryItemsMap[product.id] = productInventoryItems[product].collect{it.toJson()}
            }
			
			
			
            String jsonString = [requisition: requisition.toJson(), productInventoryItemsMap: productInventoryItemsMap, picklist: picklist.toJson()] as JSON
            return [data: jsonString, requisitionId: requisition.id, requisition:requisition]
        } else{
            response.sendError(404)
        }
    }

	def transfer = { 
		def requisition = Requisition.get(params.id)
		def picklist = Picklist.findByRequisition(requisition)



		//requisitionService.transferStock(requisition)
		
		[requisition:requisition, picklist:picklist]
	}
	
	def complete = {
        def requisition = Requisition.get(params.id)
		try {
            User issuedBy = User.get(params?.issuedBy?.id)
            Person deliveredBy = Person.get(params?.deliveredBy?.id)
            String comments = params.comments

			requisitionService.issueRequisition(requisition, issuedBy, deliveredBy, comments)
		} 
		catch (ValidationException e) { 
			//flash.message = e.message 
			requisition = Requisition.read(params.id)
			def picklist = Picklist.findByRequisition(requisition)
			requisition.errors = e.errors
			render(view: "transfer", model:[requisition:requisition,picklist:picklist])
			return
		}
		flash.message = "Successfully issued requisition " + requisition?.requestNumber
		redirect(action: "show", id: params.id)
	}
	
    def cancel = {
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisitionService.cancelRequisition(requisition)
            flash.message = "${warehouse.message(code: 'default.cancelled.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list")
    }

    def rollback = {
        def action = "show"
        def requisition = Requisition.get(params?.id)
        if (requisition) {
            requisitionService.rollbackRequisition(requisition)
            flash.message = "${warehouse.message(code: 'default.rollback.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"

            switch (requisition.status) {
                case RequisitionStatus.CHECKING:
                    action = "transfer"
                    break;

                case RequisitionStatus.PICKING:
                    action = "pick"
                    break;

                case RequisitionStatus.EDITING:
                    action = "edit"
                    break;

                case RequisitionStatus.VERIFYING:
                    action = "review"
                    break;
            }
        }
        flash.message = "Successfully rolled back requisition " + requisition.requestNumber
        redirect(action: action, id: params.id)

    }


	def undoCancel = {
		def requisition = Requisition.get(params?.id)
		if (requisition) {
			requisitionService.undoCancelRequisition(requisition)
			flash.message = "${warehouse.message(code: 'default.undone.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
		}
		redirect(action: "list")
	}

    def show = {
        def requisition = Requisition.get(params.id)
        if (!requisition) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'request.label', default: 'Request'), params.id])}"
            redirect(action: "list")
        }
        else {

            // Redirect to stock movement page
            if (requisition?.type == RequisitionType.DEFAULT && !params?.override) {
                redirect(controller: "stockMovement", action: "show", id: requisition?.id)
                return
            }

            return [requisition: requisition]
        }
    }

    def delete = {
        def requisition = Requisition.get(params.id)
        if (requisition) {
            try {
                requisitionService.deleteRequisition(requisition)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisition.label', default: 'Requisition'), params.id])}"
        }
        redirect(action: "list", id:params.id)
    }

    def printDraft = {
		println "print draft " + params 
        def requisition = Requisition.get(params.id)
        def picklist = Picklist.findByRequisition(requisition)
        def location = Location.get(session.warehouse.id)
        //render (view: "printDraft", model:[requisition:requisition, picklist: picklist, location:location])
		[requisition:requisition, picklist: picklist, location:location]
    }


	def addToPicklistItems = { AddToPicklistItemsCommand command ->
		println "Add to picklist items " + params
		def requisition = Requisition.get(params.id)
	//	def requisitionItem = Requisition.get(params.requisitionItem.id)
        println "Requisition " + command?.requisition
		def picklist = Picklist.findByRequisition(command.requisition)
        if (!picklist) {
            picklist = new Picklist();
            picklist.requisition = command.requisition
            if (!picklist.save(flush:true)) {
                throw new ValidationException("Unable to create new picklist", picklist.errors)
            }
        }
        command?.picklistItems.each { picklistItem ->
            def existingPicklistItem  = PicklistItem.get(picklistItem.id)
            if (picklistItem.quantity > 0) {
                if (existingPicklistItem) {
                    existingPicklistItem.quantity = picklistItem.quantity
                    existingPicklistItem.save(flush:true)
                }
                else {
                    println "Adding new item to picklist " + picklistItem?.id + " inventoryItem.id=" + picklistItem?.inventoryItem?.id + " qty=" + picklistItem?.quantity
                    picklist.addToPicklistItems(picklistItem)
                    //picklistItem.save(flush:true)
                    picklist.save(flush:true)
                }
            }
            // Otherwise, if quantity <= 0 then we want to remove this item
            else {
                if (existingPicklistItem) {
                    picklist.removeFromPicklistItems(existingPicklistItem)
                }
            }
        }
		redirect(action:  "pick", id:  command?.requisition?.id)
	}
	
	
	
	private List<Location> getDepots() {
		Location.list().findAll {location -> location.id != session.warehouse.id && location.isWarehouse()}.sort{ it.name }
	}

	private List<Location> getWardsPharmacies() {
		def current = Location.get(session.warehouse.id)
		def locations = []
		if (current) { 
			if(current?.locationGroup == null) {
				locations = Location.list().findAll { location -> location.isWardOrPharmacy() }.sort { it.name }
			} else {
				locations = Location.list().findAll { location -> location.locationGroup?.id == current.locationGroup?.id }.findAll {location -> location.isWardOrPharmacy()}.sort { it.name }
			}
		}				
		return locations
	}

    /**
     * Generate the name of the requisition.
     *
     * @param requisition
     * @return
     */
    def getName(requisition) {
        def commodityClass = (requisition.commodityClass) ? "${warehouse.message(code:'enum.CommodityClass.' + requisition.commodityClass)}" : null
        def requisitionType = (requisition.type) ? "${warehouse.message(code: 'enum.RequisitionType.' + requisition.type)}" : null
        def requisitionName =
            [
                requisitionType,
                requisition.destination,
                requisition.recipientProgram,
                commodityClass,
                requisition?.dateRequested?.format("MMM dd yyyy")
            ]

        return requisitionName.findAll{ it }.join(" - ")
    }


    def exportRequisitions = {
        def requisitions = getRequisitions(params)
        if (requisitions) {
            def date = new Date();
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Requisitions-${date?date.format("yyyyMMdd-hhmmss"):""}.csv\"")
            response.contentType = "text/csv"
            def csv = dataService.exportRequisitions(requisitions)
            println "export requisitions: " + csv
            render csv
        }
        else {
            render(text: 'No requisitions found', status: 404)
        }

    }

    def exportRequisitionItems = {
        def requisitions = getRequisitions(params)
        if (requisitions) {
            def date = new Date();
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Requisitions-${date?date.format("yyyyMMdd-hhmmss"):""}.csv\"")
            response.contentType = "text/csv"
            def csv = dataService.exportRequisitionItems(requisitions)
            println "export requisitions: " + csv
            render csv
        }
        else {
            render(text: 'No requisitions found', status: 404)
        }

    }

    def export = {
        def requisitions = getRequisitions(params)
        if (requisitions) {
            def date = new Date();
            response.setHeader("Content-disposition",
                    "attachment; filename=\"Requisitions-${date?date.format("yyyyMMdd-hhmmss"):""}.csv\"")
            response.contentType = "text/csv"
            def csv = dataService.exportRequisitions(requisitions)
            println "export requisitions: " + csv
            render csv
        }
        else {
            render(text: 'No requisitions found', status: 404)
        }

    }

    def getRequisitions(params) {

        // Requisition that encapsulates the basic parameters in the search form
        def requisition = new Requisition(params)
        requisition.origin = Location.get(session?.warehouse?.id)

        // Disables pagination
        params.max = -1

        // Requisitions to export
        def requisitions = requisitionService.getRequisitions(requisition, params)

        return requisitions
    }


    def getReviewRequisitionModel(params) {
        def requisition = Requisition.get(params.id)
        def location = Location.get(session.warehouse.id)
        def status = params.status ? RequisitionItemStatus.valueOf(RequisitionItemStatus, params.status) : null
        println "Status: " + status
        // Get all product for all requisitions items (including substitutions, modifications)
        def products = requisition?.requisitionItems.collect { it.product }

        // But we only want to show the original requisition items
        def requisitionItems = requisition?.originalRequisitionItems
        def quantityProductMap = inventoryService.getQuantityByProductMap(location.inventory, products)
        def quantityOnHandMap = [:]
        def quantityAvailableToPromiseMap = [:]

        requisitionItems?.each { requisitionItem ->
            quantityOnHandMap[requisitionItem?.product?.id] = quantityProductMap[requisitionItem?.product]?:0
            //inventoryService.getQuantityOnHand(location, requisitionItem?.product)
            //quantityAvailableToPromiseMap[requisitionItem?.product?.id] = quantityProductMap[requisitionItem?.product]?:0
            //inventoryService.getQuantityAvailableToPromise(location, requisitionItem?.product)
        }
        return [requisition:requisition, requisitionItems:requisitionItems, quantityOnHandMap: quantityOnHandMap]

    }

    // FIXME Move to requisition item class
    def getRelatedProducts(requisitionItem) {
        def products = []
        products << requisitionItem.product
        requisitionItem.product.productGroups.each { productGroup ->
            //println "PRODUCT GROUP " + productGroup
            productGroup.products.each { product ->
                //println "PRODUCT " + product
                products.add(product)
            }
        }
        products = products.unique()
        return products
    }


    def getQuantityOnHandMap(location, products) {
        def quantityOnHandMap = [:]
        if (products) {
            products.eachWithIndex { product, index ->
                quantityOnHandMap[product] = inventoryService.getQuantityOnHand(location, product)
            }
            quantityOnHandMap = quantityOnHandMap.sort {a, b -> b.value <=> a.value}
            //quantityOnHandMap = quantityOnHandMap.sort { it.value }
        }
        return quantityOnHandMap
    }



    def showRequisitionItems = {
        log.info ("Show requisition items " + params)
        render(template:'requisitionItems2', model: getReviewRequisitionModel(params))
    }

    def editRequisitionItem = {
        log.info "edit requisition item: " + params
        def location = Location.get(session.warehouse.id)
        def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        def products = getRelatedProducts(requisitionItem)
        def quantityOnHandMap = getQuantityOnHandMap(location, products)

        render(template: "editRequisitionItem", model: [requisition:requisition,requisitionItem:requisitionItem,actionType:params.actionType,quantityOnHandMap:quantityOnHandMap])
    }


    def nextRequisitionItem = {
        log.info "next: " + params
        def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        def originalItems = requisition?.originalRequisitionItems?.sort()
        def currentIndex = originalItems.findIndexOf { it == requisitionItem }
        def nextItem = originalItems[currentIndex+1]?:originalItems[0]

        //render(template: "editRequisitionItem", model: [requisition:requisition,requisitionItem:requisitionItem,actionType:params.actionType,quantityOnHandMap:quantityOnHandMap])
        redirect(action: "editRequisitionItem", id: params.id, params: ['requisitionItem.id':nextItem?.id,'actionType':'show'])
    }

    def previousRequisitionItem = {
        log.info "previous: " + params
        def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        def originalItems = requisition?.originalRequisitionItems?.sort()
        def lastIndex = originalItems?.size()-1
        def currentIndex = originalItems.findIndexOf { it == requisitionItem }
        def previousItem = originalItems[currentIndex-1]?:originalItems[lastIndex]

        redirect(action: "editRequisitionItem", id: params.id, params: ['requisitionItem.id':previousItem?.id,'actionType':'show'])


    }



    def saveRequisitionItem = {
        println "Save requisition item" + params
        def location = Location.get(session.warehouse.id)
        def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        def products = getRelatedProducts(requisitionItem)
        def quantityOnHandMap = getQuantityOnHandMap(location, products)
        def product = Product.get(params?.product?.id)
        def otherSubstitute = Product.get(params?.otherSubstitute?.id)
        def quantity = params.quantity as int;
        if (!product && otherSubstitute) {
            product = otherSubstitute
        }
        if (!product && !otherSubstitute) {
            requisitionItem.errors.rejectValue("substitutionItem", "Must choose a substitution")
        }

        if (!requisitionItem.hasErrors()) {
            try {
                // Different product
                if (product && requisitionItem.product != product) {
                    println "Choose substitute " + product + " " + quantity
                    requisitionItem.chooseSubstitute(product, null, quantity, params.reasonCode, params.comments)
                    flash.message = "Substitution was made due to ${params.reasonCode}"
                }
                // Same product, different quantity
                else if (requisitionItem.quantity != quantity) {
                    println "Change quantity " + product + " " + quantity
                    requisitionItem.changeQuantity(quantity, params.reasonCode, params.comments)
                    flash.message = "Quantity was changed to ${quantity} due to ${params.reasonCode}"

                }
                else {
                    println "Approve quantity " + quantity
                    requisitionItem.approveQuantity()
                    flash.message = "Requisition item was approved at ${quantity}"
                }

            } catch (ValidationException e) {
                requisitionItem.errors = e.errors
            }

        }

        if (requisitionItem.hasErrors()) {
            //requisitionItem.errors.reject("quantity", "Invalid quantity")
            render(template: "editRequisitionItem", model: [requisition:requisition,requisitionItem:requisitionItem,actionType:params.actionType,quantityOnHandMap:quantityOnHandMap])
            return
        }

        // Update go to next item
        params.actionType="show"
        //requisitionItem = requisitionItem.next()
        products = getRelatedProducts(requisitionItem)
        quantityOnHandMap = getQuantityOnHandMap(location, products)


        render(template:'editRequisitionItem',  model: [requisition:requisition,requisitionItem:requisitionItem,quantityOnHandMap:quantityOnHandMap])

        //render(template:'requisitionItems2', model: getReviewRequisitionModel(params))
    }


    def approveQuantity = {
        log.info "approve quantity = " + params
        def quantityOnHandMap = [:]
        def requisition = Requisition.get(params.id)
        def location = Location.get(session.warehouse.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        if (requisitionItem) {
            def products = getRelatedProducts(requisitionItem)
            quantityOnHandMap = getQuantityOnHandMap(location, products)
            requisitionItem.approveQuantity()
            //requisitionItem.save()
        }
        //render(template:'requisitionItems2', model: getReviewRequisitionModel(params))
        render(template:'editRequisitionItem',  model: [requisition:requisition,requisitionItem:requisitionItem,actionType:params.actionType,quantityOnHandMap:quantityOnHandMap])
        //redirect(action:"nextRequisitionItem", id: requisition.id, params:['requisitionItem.id':requisitionItem.id])
    }

    def undoChangesFromList = {
        log.info "cancel quantity = " + params
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        if (requisitionItem) {
            requisitionItem.undoChanges()
            requisitionItem.save();
        }
        render(template:'requisitionItems2', model: getReviewRequisitionModel(params))
    }


    def undoChanges = {
        log.info "cancel quantity = " + params
        def quantityOnHandMap = [:]
        def location = Location.get(session.warehouse.id)
        def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params.requisitionItem.id)
        if (requisitionItem) {
            def products = getRelatedProducts(requisitionItem)
            quantityOnHandMap = getQuantityOnHandMap(location, products)
            requisitionItem.undoChanges()
            requisitionItem.save();
        }
        //render(template:'requisitionItems2', model: getReviewRequisitionModel(params))

        render(template:'editRequisitionItem',  model: [requisition:requisition,requisitionItem:requisitionItem,actionType:params.actionType,quantityOnHandMap:quantityOnHandMap])
    }


    def changeQuantity = {
        log.info "change quantity " + params
        //  def requisition = Requisition.get(params.id)
        def requisitionItem = RequisitionItem.get(params?.requisitionItem?.id)
        def productPackage = ProductPackage.get(params?.productPackage?.id)
        try {
            requisitionItem.changeQuantity(params.quantity as int, productPackage, params.reasonCode, params.comments);
        } catch(ValidationException e) {
            requisitionItem.errors = e.errors
            flash.errors = e.errors
        }

        // If there are errors we want to render the review page with those errors
        if (requisitionItem.hasErrors()) {
            log.error("There are errors: " + requisitionItem.errors)
            redirect(controller: "requisition", action: "review", id: requisitionItem?.requisition?.id,
                    params:['requisitionItem.id': requisitionItem.id,actionType:params.actionType])
            //render(view: "../requisition/review", model: [requisition:requisition, selectedRequisitionItem: requisitionItem])
            return;
        }
        redirect(controller: "requisition", action: "review", id: requisitionItem?.requisition?.id, params: ['requisitionItem.id':requisitionItem?.id])
    }

    /**
     *  Allow user to cancel the given requisition item.
     */
    def cancelQuantity = {
        log.info "cancel quantity = " + params
        def requisitionItem = RequisitionItem.get(params.id)
        if (requisitionItem) {

            try {
                requisitionItem.cancelQuantity(params.reasonCode, params.comments)
            } catch(ValidationException e) {
                requisitionItem.errors = e.errors
                flash.errors = e.errors
            }

            // If there are errors we want to render the review page with those errors
            if (requisitionItem.hasErrors()) {
                def redirectAction = params?.redirectAction ?: "review"
                redirect(controller: "requisition", action: redirectAction, id: requisitionItem?.requisition?.id,
                        params:['requisitionItem.id': requisitionItem.id,actionType:params.actionType])
                return;
            }

        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(controller: "requisition", action: "list")
            return
        }
        redirect(controller: "requisition", action: "review", id: requisitionItem?.requisition?.id, params: ['requisitionItem.id':requisitionItem?.id])
    }

    /**
     *  Allow user to approve the given requisition item.
    def approveQuantity = {
        log.info "approve quantity = " + params
        def requisitionItem = RequisitionItem.get(params.id)
        if (requisitionItem) {
            requisitionItem.approveQuantity()
            def redirectAction = params?.redirectAction ?: "review"
            // params:['requisitionItem.id':requisitionItem.id]
            redirect(controller: "requisition", action: redirectAction, id: requisitionItem?.requisition?.id, params: ['requisitionItem.id':requisitionItem.id])
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(controller: "requisition", action: "list")
        }
    }
     */
    /**
     * Allow user to undo changes made during the review process.
    def undoChanges = {
        log.info "cancel quantity = " + params
        def requisitionItem = RequisitionItem.get(params.id)
        if (requisitionItem) {
            requisitionItem.undoChanges()
            //requisitionItem.save();
            def redirectAction = params?.redirectAction ?: "review"
            // For now we don't need to choose the selected requisition item (e.g. params:['requisitionItem.id':requisitionItem.id])
            redirect(controller: "requisition", action: redirectAction,
                    id: requisitionItem?.requisition?.id, params: ['requisitionItem.id':requisitionItem.id])
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(controller: "requisition", action: "list")
        }
    }
     */

    /**
     * Allow user to choose substitute during the review process.
     */
    def chooseSubstitute = {
        log.info "choose substitute " + params
        def redirectAction = params?.redirectAction ?: "review"
        def requisitionItem = RequisitionItem.get(params.id)
        def product = Product.get(params.productId)
        def productPackage = ProductPackage.get(params.productPackageId)
        if (requisitionItem) {

            try {
                requisitionItem.chooseSubstitute(product, productPackage, params.quantity as int, params.reasonCode, params.comments)
            } catch(ValidationException e) {
                requisitionItem.errors = e.errors
                flash.errors = e.errors
            }

            // If there are errors we want to render the review page with those errors
            if (requisitionItem.hasErrors()) {
                flash.message = "errors"


                chain(controller: "requisition", action: redirectAction, id: requisitionItem?.requisition?.id,
                        params:['requisitionItem.id': requisitionItem.id,actionType:params.actionType], model: [selectedRequisitionItem:requisitionItem])
                return;
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem'), params.id])}"
            redirect(controller: "requisition", action: "list")
            return;
        }
        redirect(controller: "requisition", action: redirectAction, id: requisitionItem?.requisition?.id, params: ['requisitionItem.id':requisitionItem.id])

    }


}


class AddToPicklistItemsCommand {
    Requisition requisition
    RequisitionItem requisitionItem
    def picklistItems = LazyList.decorate(new ArrayList(), FactoryUtils.instantiateFactory(PicklistItem.class));

    static constraints = {
        requisition(nullable:false)
        requisitionItem(nullable:false)
        picklistItems(nullable:true)

    }


}
