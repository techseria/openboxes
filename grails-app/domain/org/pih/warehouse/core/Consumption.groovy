package org.pih.warehouse.core

import org.grails.datastore.mapping.query.Query
import org.pih.warehouse.inventory.InventoryItem
import org.pih.warehouse.product.Product

class Consumption {

    String id
    Integer day
    InventoryItem inventoryItem
    Integer month
    Product product
    Integer year
    Location location
    Date transactionDate

    // Audit fields
    Date dateCreated;
    Date lastUpdated;
    Integer quantity

    //static belongsTo = [ location : Location, locationGroup : LocationGroup ]

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
    }
}
