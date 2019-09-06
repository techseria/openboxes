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

import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.shipping.ReceiptException
import org.pih.warehouse.shipping.ShipmentException

class ReceiveOrderWorkflowController {


	def orderService;
	
	def index = { redirect(action:"receiveOrder") }

	def receiveOrder = {
		println("Starting order workflow " + params)

		// create a new shipment instance if we don't have one already
		def orderCommand = new OrderCommand();
		if (params.id) {
			orderCommand = orderService.getOrder(params.id, session.user.id)
		}
		else {
			flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'order.label', default: 'Order'), params.id])}"
			redirect(controller: "order", action: "list")
		}
		if(session.orderCommand)
			session.removeAttribute("orderCommand")
		if(session.order)
			session.removeAttribute("order")
		if(session.orderItems)
			session.removeAttribute("orderItems")

		session['orderCommand'] = orderCommand
		session['order'] = orderCommand.order
		session['orderItems'] = orderCommand.orderItems

		if (params.skipTo) {
			if (params.skipTo == 'enterShipmentDetails')
				return render(view: "enterShipmentDetails", model: [orderCommand: orderCommand,order:orderCommand.order])
			else if (params.skipTo == 'processOrderItems')
				return processOrderItems()
			else if (params.skipTo == 'confirmOrderReceipt')
				return confirmOrderReceipt()
		}
		render(view: "enterShipmentDetails", model: [orderCommand: orderCommand,order:orderCommand.order])
	}

	def success = {

	}

	def enterShipmentDetails = {  OrderCommand cmd ->
		if (cmd.hasErrors()) {
			return error()
		}
		cmd.orderItems = session.orderItems
		OrderCommand orderCommand = session.orderCommand
        orderCommand.shipmentType = cmd.shipmentType
        orderCommand.recipient = cmd.recipient
        orderCommand.shippedOn = cmd.shippedOn
        orderCommand.deliveredOn = cmd.deliveredOn
        session["orderCommand"] = orderCommand
		session["orderItems"] = cmd.orderItems
		render(view: "processOrderItems", model: [orderCommand : orderCommand,orderItems:cmd.orderItems])
	}

	def processOrderItems = { OrderItemListCommand command ->
		session["orderListCommand"] = command
		OrderCommand orderCommand = session.orderCommand
		orderCommand.orderItems = session.orderItems
		for(int i =0 ;i< session.orderItems.size() ; i++){
			String temp = "orderItems$i"
			session.orderItems[0].expirationDate = params[temp].expirationDate
			session.orderItems[0].lotNumber = params[temp].lotNumber
			session.orderItems[0].quantityReceived = Integer.parseInt(params[temp].quantityReceived)
		}

		if (command.hasErrors()) {
			return error()
		}
		render(view: "confirmOrderReceipt", model: [orderCommand:session.orderCommand,orderItems:session.orderItems])

	}

	def confirmOrderReceipt = {
		def orderCommand = session.orderCommand;
		orderCommand.orderItems = session.orderItems;
		orderCommand.currentUser = User.get(session.user.id)
		orderCommand.currentLocation = Location.get(session.warehouse.id)
		try {
			orderService.saveOrderShipment(orderCommand)
		}
		catch (ShipmentException se) {
			session.shipment = se?.shipment
			session.receipt = se?.shipment?.receipt;
			return error();
		}
		catch (ReceiptException re) {
			session.receipt = re.receipt;
			return error();
		}
		catch (OrderException oe) {
			session.order = oe.order
			return error()
		}
		// RuntimeExceptions should propagate to the UI
		//catch (RuntimeException e) {
		//	flow.orderCommand = orderCommand
		//	return error();
		//}
		println(">>>>>>>>>>>>> Success!!!")
		success()

	}
	def receiveOrderFlow1 = {
		start {
			action {

				
				flow.orderCommand = orderCommand;
				flow.order = orderCommand.order;
				flow.orderItems = orderCommand.orderItems 

				if (params.skipTo) {
					if (params.skipTo == 'enterShipmentDetails') return enterShipmentDetails()
					else if (params.skipTo == 'processOrderItems') return processOrderItems()
					else if (params.skipTo == 'confirmOrderReceipt') return confirmOrderReceipt()
				}				
				return success()
			}
			on("success").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		enterShipmentDetails {
			on("next") { OrderCommand cmd ->
				flow.orderCommand = cmd
				if (flow.orderCommand.hasErrors()) {
					return error() 	
				}
				println("setting order command for process order items " + flow.orderItems)
				cmd.orderItems = flow.orderItems
				[orderCommand : cmd]
							
			}.to("processOrderItems")
			on("cancel").to("finish")
			on("error").to("enterShipmentDetails")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		processOrderItems {			
			on("next") { OrderItemListCommand command ->
				flow.orderListCommand = command
				flow.orderItems = command.orderItems				
				if (command.hasErrors()) {					
					return error()
				}
			}.to("confirmOrderReceipt")
			
			on("back") { OrderItemListCommand command ->
				flow.orderListCommand = command
				flow.orderItems = command.orderItems				
				if (command.hasErrors()) {					
					return error()
				}
			}.to("enterShipmentDetails")
			
			on("cancel").to("finish")
			//on("error").to("processOrderItems")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}
		confirmOrderReceipt  {
			on("submit") { 
				def orderCommand = flow.orderCommand;
				orderCommand.orderItems = flow.orderItems;
				orderCommand.currentUser = User.get(session.user.id)
				orderCommand.currentLocation = Location.get(session.warehouse.id)
				try {
					orderService.saveOrderShipment(orderCommand)
				}
				catch (ShipmentException se) {
					flow.shipment = se?.shipment;
					flow.receipt = se?.shipment?.receipt;
					return error();
				}
				catch (ReceiptException re) {
					flow.receipt = re.receipt;
					return error();
				}
				catch (OrderException oe) {
					flow.order = oe.order;
					return error();
				}
				// RuntimeExceptions should propagate to the UI
				//catch (RuntimeException e) {
				//	flow.orderCommand = orderCommand
				//	return error();
				//}
				println(">>>>>>>>>>>>> Success!!!")
				success()
				
			}.to("finish")
			on("cancel").to("finish")
			on("back").to("processOrderItems")
			on("error").to("confirmOrderReceipt")
			//on(Exception).to("handleError")
			//on("success").to("finish")
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
		}		
		handleError() { 
			on("enterShipmentDetails").to("enterShipmentDetails")
			on("processOrderItems").to("processOrderItems")
			on("confirmOrderReceipt").to("confirmOrderReceipt")
			
		}
		finish {
			redirect(controller:"order", action : "show", params : [ "id" : flow.order.id ?: '' ])
		}
	}

	

	
}
