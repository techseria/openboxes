/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.core


import org.pih.warehouse.shipping.Shipment

class ItextPdfController {

    def itextPdfService

    def exportPackingListPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id)
      //  Location currentLocation = Location.get(session.warehouse.id)

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }
        def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".pdf"
        itextPdfService.exportPackingListPdf(shipmentInstance, response.outputStream)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")
       // response.outputStream = outputStream;
        return;
    }

    def downloadPackingListPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id);

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        // For some reason, this needs to be here or we get a File Not Found error (ERR_FILE_NOT_FOUND)

        def filename = "Packing List - " + shipmentInstance?.name?.trim() + ".pdf"
        itextPdfService.generatePackingList(response.outputStream, shipmentInstance)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")
        return;
    }

    def certificateOfDonationPdf = {
        log.info params
        def shipmentInstance = Shipment.get(params.id);

        if (!shipmentInstance) {
            throw new Exception("Unable to locate shipment with ID ${params.id}")
        }

        def filename = "Certificate of Donation - " + shipmentInstance?.shipmentNumber + ".pdf"
        itextPdfService.generateCertificateOfDonation(response.outputStream, shipmentInstance)
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}\"");
        response.setContentType("application/pdf")

        return;
    }
}
