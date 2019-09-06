/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.api

import grails.converters.JSON
import grails.util.Holders
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.LocationTypeCode
import org.pih.warehouse.core.User

class LocationApiController extends BaseDomainApiController {

    def locationService
    def userService

    def list = {

        def minLength = Holders.config.openboxes.typeahead.minLength
        if (params.name && params.name.size()<minLength) {
            render([data:[]])
            return
        }

        Location currentLocation = Location.get(session?.warehouse?.id)
        boolean isSuperuser = userService.isSuperuser(session?.user)
        String direction = params?.direction
        def fields = params.fields ? params.fields.split(",") : null
        def locations = locationService.getLocations(fields, params, isSuperuser, direction, currentLocation)
        render ([data:locations] as JSON)
     }


}
