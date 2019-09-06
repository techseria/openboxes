package org.pih.warehouse.core

class OrderItemComment {

    String id
    Comment comment

    static mapping = {
        id generator: 'uuid'
    }

    static constraints = {
    }
}
