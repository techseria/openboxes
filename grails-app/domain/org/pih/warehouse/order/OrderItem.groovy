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

import grails.validation.Validateable
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.Party
import org.pih.warehouse.core.Person
import org.pih.warehouse.core.User
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Category
import org.pih.warehouse.product.Product
import org.pih.warehouse.shipping.Shipment
import org.pih.warehouse.shipping.ShipmentItem

// import java.util.Date

class OrderItem implements Serializable, Validateable {
	
	String id
	String description
	Category category
	Product product
	InventoryItem inventoryItem
	Integer quantity
	BigDecimal unitPrice
    String currencyCode

	User requestedBy	// the person who actually requested the item
    Person recipient

    OrderItemStatusCode orderItemStatusCode = OrderItemStatusCode.PENDING
    //OrderItem parentOrderItem

	// Transfer order
	Location originBinLocation
	Location destinationBinLocation


	// Audit fields
	Date dateCreated
	Date lastUpdated

	static mapping = {
		id generator: 'uuid'
	}
	
	static transients = [ "orderItemType" ]
	
	static belongsTo = [ order : Order, parentOrderItem: OrderItem ]
	
	static hasMany = [ orderShipments : OrderShipment, orderItems: OrderItem ]

    static constraints = {
    	description(nullable:true)
		category(nullable:true)
		product(nullable:true)
		inventoryItem(nullable:true)
		requestedBy(nullable:true)
		quantity(nullable:false, min:1)
		unitPrice(nullable:true)
        orderItemStatusCode(nullable:true)
        parentOrderItem(nullable:true)
		originBinLocation(nullable:true)
		destinationBinLocation(nullable:true)
		recipient(nullable:true)
		currencyCode(nullable:true)
	}

	
	String getOrderItemType() { 
		return (product)?"Product":(category)?"Category":"Unclassified"
	}

	Integer quantityFulfilled() { 
		def quantity = 0;
		try { 
			def shipmentItems = shipmentItems()
			quantity = shipmentItems?.sum { it?.quantity }
		} 
		catch (Exception e) { log.error "Error calculating quantity fulfilled: " + e.message }
		return quantity ?: 0;
	}
	
	Integer quantityRemaining() { 
		return quantity - quantityFulfilled();
	}
	
	
	
	Boolean isPartiallyFulfilled() { 
		return quantityFulfilled() > 0 && quantityFulfilled() < quantity;
	}	
	
	Boolean isCompletelyFulfilled() { 
		return quantityFulfilled() >= quantity;
	}
	
	Boolean isPending() { 
		return !isCompletelyFulfilled()
	}
	
	/**
	 * Gets all shipment items related to this order item 
	 * (ignoring any orphaned shipment item references)
	 * 
	 * @return
	 */
	def shipmentItems() {
		def shipmentItems = []
		orderShipments?.each { 
			try { 
				def shipmentItem = ShipmentItem.get(it?.shipmentItem?.id) 
				if (shipmentItem) { 
					shipmentItems << shipmentItem;
				}
			} catch (Exception e) {
				log.error "Error getting shipment items: " + e.message
			}	
		} 
		return shipmentItems;
	}
	
	/**
	 * Gets all shipments related to this order item 
	 * (ignoring any orphaned shipment item references)
	 * 
	 * @return
	 */
	def listShipments() {
		def shipments = []
		orderShipments.each { 
			try { 
				def shipment = Shipment.get(it?.shipmentItem?.shipment?.id) 
				if (shipment) { 
					shipments << shipment
				}
			} catch (Exception e) { 
				log.error "Error getting shipment: " + e.message
			} 
		} 
		return shipments;
	}

	def totalPrice() { 
		return ( quantity ? quantity : 0.0 ) * ( unitPrice ? unitPrice : 0.0 );
	}
			
	/*
	List addToShipmentItems(ShipmentItem shipmentItem) {
		OrderShipment.link(this, shipmentItem)
		return shipmentItems()
	}

	List removeFromShipmentItems(ShipmentItem shipmentItem) {
		OrderShipment.unlink(this, shipmentItem)
		return shipmentItems()
	}
	*/
	
}
