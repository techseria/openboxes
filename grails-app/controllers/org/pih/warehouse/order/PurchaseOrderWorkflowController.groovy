/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.order

import org.pih.warehouse.core.Person
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.springframework.dao.DataIntegrityViolationException

class PurchaseOrderWorkflowController {

	def orderService

	def index() { redirect(action:"purchaseOrder") }
	def purchaseOrder() {
		println("Starting order workflow " + params)
		def suppliers = orderService.getSuppliers()
		// create a new shipment instance if we don't have one already
		def order
		if (params.id) {
			 order = Order.get(params.id)
		} else {
			order = new Order()
			order.orderedBy = Person.get(session.user.id)
			/*flow.order = order;*/
		}

		if(params.addItem == 'Save item') return addItem()
		if(params._eventId == 'deleteItem') return deleteItem()
		if (params.skipTo) {
			if (params.skipTo == 'details')
                return render(view: "enterOrderDetails",model: [order:order])
			else if (params.skipTo == 'items')
				return render(view: "showOrderItems",model: [order:order])

			//else if (params.skipTo == 'confirm') return confirmOrder()

		}

		render(view: "enterOrderDetails",model: [order:order])
		/*on("success").to("enterOrderDetails")
		on("showOrderItems").to("showOrderItems")
		on
		//on("confirmOrder").to("confirmOrder")
*/
	}

	def deleteItem = {
		def orderItem = OrderItem.get(params.id)
		Order order = OrderItem.get(params.id).order
		if (orderItem) {
			order.removeFromOrderItems(orderItem)
			orderItem.delete()
		}
		if (!orderService.saveOrder(order)) {
			return error()
		}
		render(view: "showOrderItems", model: [order:order])
	}
	def	enterOrderDetails = {

			/*on("next") {
				log.info "Enter order details " + params

				flow.order.properties = params
				log.info "Order " + flow.order.properties
				try {
					if (!orderService.saveOrder(flow.order)) {
						return error()
					}
				} catch (Exception e) {
					return error()
				}
			}.to("showOrderItems")
            on("showOrderItems").to("showOrderItems")
			on("enterOrderDetails").to("enterOrderDetails")
			on("cancel").to("cancel")
			on("finish").to("finish")*/
		}
		def showOrderItems = {
			on("back") {
				log.info "saving items " + params
				flow.order.properties = params
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}

			}.to("enterOrderDetails")

			on("deleteItem") {
				log.info "deleting an item " + params
				def orderItem = OrderItem.get(params.id)
				if (orderItem) {
					flow.order.removeFromOrderItems(orderItem);
					orderItem.delete();
				}
			}.to("showOrderItems")

			on("editItem") {
				def orderItem = OrderItem.get(params.id)
				if (orderItem) {
					flow.orderItem = orderItem;
				}
			}.to("showOrderItems")

			on("addItem") {
				log.info "adding an item " + params
				if(!flow.order.orderItems) flow.order.orderItems = [] as HashSet

				def orderItem = OrderItem.get(params?.orderItem?.id)
				if (orderItem) {
					orderItem.properties = params
				}
				else {
					orderItem = new OrderItem(params);
				}

				orderItem.requestedBy = Person.get(session.user.id)

				if (params?.product?.id && params?.category?.id) {
					println("error with product and category")
					orderItem.errors.rejectValue("product.id", "Please choose a product OR a category OR enter a description")
					flow.orderItem = orderItem
					return error()
				}
				else if (params?.product?.id) {
					def product = Product.get(params?.product?.id)
					if (product) {
						orderItem.description = product.name
						orderItem.category = product.category
					}
				}
				else if (params?.category?.id) {
					def category = Category.get(params?.category?.id)
					if (category) {
						orderItem.description = category.name
						//orderItem.category = category
					}
				}

				if (!orderItem.validate() || orderItem.hasErrors()) {
					flow.orderItem = orderItem
					return error();
				}
				flow.order.addToOrderItems(orderItem);
				if (!orderService.saveOrder(flow.order)) {
					return error()
				}
				flow.orderItem = null

			}.to("showOrderItems")



			on("next") {
				log.info "confirm order " + params
				flow.order.properties = params

				println("order " + flow.order)



			}.to("finish")
            on("enterOrderDetails").to("enterOrderDetails")
			on("showOrderItems").to("showOrderItems")
			on("cancel").to("cancel")
			on("finish").to("finish")
			on("error").to("showOrderItems")
		}

	def editItem ={
		def orderItem = OrderItem.get(params.id)
		if (orderItem) {
			orderItem = orderItem
		}

	}

	def addItem = {

			log.info "adding an item " + params
			def order = Order.findById(params.order.id)
			def orderItem = OrderItem.get(params?.orderItem?.id)
			if (orderItem) {
				orderItem.properties = params
			}
			else {
				orderItem = new OrderItem(params);
			}

			orderItem.requestedBy = Person.get(session.user.id)

			if (params?.product?.id && params?.category?.id) {
				println("error with product and category")
				orderItem.errors.rejectValue("product.id", "Please choose a product OR a category OR enter a description")
				return error()
			}
			else if (params?.product?.id) {
				def product = Product.get(params?.product?.id)
				if (product) {
					orderItem.description = product.name
					orderItem.category = product.category
				}
			}
			else if (params?.category?.id) {
				def category = Category.get(params?.category?.id)
				if (category) {
					orderItem.description = category.name
					//orderItem.category = category
				}
			}

			if (!orderItem.validate() || orderItem.hasErrors()) {
				return error();
			}

			order.addToOrderItems(orderItem);
			if (!orderService.saveOrder(order)) {
				return error()
			}

		/*.to("showOrderItems")*/
		render(view: "showOrderItems", model: [order:order])

	}
    def svaeOrder ={ Order order ->

        try {
			if(params.orderID){
				Order oldOrder= Order.findById(params.orderID)
				oldOrder.properties = params
				if (!oldOrder.merge()) {
					render(view: "enterOrderDetails")
				}
				else {
					flash.message = "You have successfully Update a new purchase order.  Please select Issue PO "
					render(view: "enterOrderDetails")
				}
			}
			else{
            if (!orderService.saveOrder(order)) {
                render(view: "enterOrderDetails")
            }
            else {
                flash.message = "You have successfully created a new purchase order.  Please select Issue PO "
				render(view: "enterOrderDetails")
            }
			}
        } catch (DataIntegrityViolationException e) {
            println("data integrity exception")
			render(view: "enterOrderDetails")
        }

    }

		def finish = {

				println("Finishing workflow, save order object " + flow.order)
				// def order = flow.order;

				try {

					if (!orderService.saveOrder(flow.order)) {
						return error()
					}
					else {
						flash.message = "You have successfully created a new purchase order.  Please select Issue PO "
						return success()
					}

				} catch (DataIntegrityViolationException e) {
					println("data integrity exception")
					return error();
				}
			on("success").to("showOrder")
		}
		def cancel = {
			//redirect(controller:"order", action: "list")
            redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}
		def showOrder = {

			redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}

	//	handleError()
	}
