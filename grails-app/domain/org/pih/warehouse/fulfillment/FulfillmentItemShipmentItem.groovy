package org.pih.warehouse.fulfillment

class FulfillmentItemShipmentItem {

    String id
    Date last_inventory_date
    Date date_created
    Date last_updated

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
    }
}
