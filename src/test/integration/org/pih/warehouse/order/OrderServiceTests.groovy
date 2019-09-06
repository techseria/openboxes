package org.pih.warehouse.order

import org.junit.Test
import org.pih.warehouse.core.Location;
import org.pih.warehouse.core.LocationType;
import org.pih.warehouse.core.User;

import testutils.DbHelper;



class OrderServiceTests extends GroovyTestCase {
	
	
	def orderService
	protected void setUp() {
		def locationType = new LocationType(name: "Depot").save(flush:true)
		new Location(name: "Origin", locationType: locationType).save(flush:true)
		new Location(name: "Destination", locationType: locationType).save(flush:true)
		DbHelper.createAdmin("Justin", "Miranda", "justin.miranda@gmail.com", "justin.miranda", "password", true)
	}

	@Test
	void saveOrder_shouldThrowOrderException() {
		shouldFail(OrderException) {
			def newOrder = new Order();
			orderService.saveOrder(newOrder);
		} 
	}

	@Test
	void saveOrder_shouldSaveSuccessfully() {
		def newOrder = new Order();
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")			
		orderService.saveOrder(newOrder);
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldGenerateOrderNumber() { 		
		def newOrder = new Order();
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder);
		assertNotNull newOrder.orderNumber
	}

	@Test
	void saveOrder_shouldNotGenerateOrderNumber() {
		def newOrder = new Order();
		newOrder.orderNumber = "PO-12345"
		newOrder.description = "Order 1234"
		newOrder.origin = Location.findByName("Origin")
		newOrder.destination = Location.findByName("Destination")
		newOrder.orderedBy = User.findByUsername("justin.miranda")
		orderService.saveOrder(newOrder);
		assertEquals "PO-12345", newOrder.orderNumber
	}

	
		
}
