/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/
package org.pih.warehouse.product

import org.apache.commons.io.FilenameUtils
import org.pih.warehouse.importer.ImportDataCommand

class ProductCatalogController {

    def productService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productCatalogInstanceList: ProductCatalog.list(params), productCatalogInstanceTotal: ProductCatalog.count()]
    }

    def create = {
        def productCatalogInstance = new ProductCatalog()
        productCatalogInstance.properties = params
        return [productCatalogInstance: productCatalogInstance]
    }

    def save = {
        def productCatalogInstance = new ProductCatalog(params)
        if (productCatalogInstance.save(flush: true)) {
            flash.message = "${warehouse.message(code: 'default.created.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), productCatalogInstance.id])}"
            redirect(action: "list", id: productCatalogInstance.id)
        }
        else {
            render(view: "create", model: [productCatalogInstance: productCatalogInstance])
        }
    }

    def show = {
        def productCatalogInstance = ProductCatalog.get(params.id)
        if (!productCatalogInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
            redirect(action: "list")
        }
        else {
            [productCatalogInstance: productCatalogInstance]
        }
    }

    def edit = {
        def productCatalogInstance = ProductCatalog.get(params.id)
        if (!productCatalogInstance) {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [productCatalogInstance: productCatalogInstance]
        }
    }

    def update = {
        def productCatalogInstance = ProductCatalog.get(params.id)
        if (productCatalogInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (productCatalogInstance.version > version) {

                    productCatalogInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog')] as Object[], "Another user has updated this ProductCatalog while you were editing")
                    render(view: "edit", model: [productCatalogInstance: productCatalogInstance])
                    return
                }
            }
            productCatalogInstance.properties = params
            if (!productCatalogInstance.hasErrors() && productCatalogInstance.save(flush: true)) {
                flash.message = "${warehouse.message(code: 'default.updated.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), productCatalogInstance.id])}"
                redirect(action: "edit", id: productCatalogInstance.id)
            }
            else {
                render(view: "edit", model: [productCatalogInstance: productCatalogInstance])
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def productCatalogInstance = ProductCatalog.get(params.id)
        if (productCatalogInstance) {
            try {
                productCatalogInstance.delete(flush: true)
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
                redirect(action: "list", id: params.id)
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productCatalog.label', default: 'ProductCatalog'), params.id])}"
            redirect(action: "list")
        }
    }

    def productCatalogItems = {
        def productCatalogInstance = ProductCatalog.get(params.id)

        render (template: "productCatalogItems", model: [productCatalogInstance:productCatalogInstance])
    }

    def addProductCatalogItem = { ProductCatalogCommand command ->
        println("Command: " + command)
        println("Params: " + params)
        def product = command.product
        def productCatalog = command.productCatalog
        if (productCatalog && product) {
            productCatalog.addToProductCatalogItems([product:product])
            productCatalog.save()
        }
        flash.message = "${warehouse.message(code: 'default.added.message', args: [warehouse.message(code: 'productCatalogItem.label', default: 'Product Catalog Item'), params.id])}"
        redirect(action: "productCatalogItems", id: command.productCatalog.id)
    }

    def removeProductCatalogItem = {
        String productCatalogId
        def productCatalogItem = ProductCatalogItem.get(params.id)
        if (productCatalogItem) {
            try {
                def productCatalog = productCatalogItem.productCatalog
                if (productCatalog) {
                    productCatalogId = productCatalog.id
                    productCatalog.removeFromProductCatalogItems(productCatalogItem)
                    productCatalog.save()
                }
                productCatalogItem.delete()
                flash.message = "${warehouse.message(code: 'default.deleted.message', args: [warehouse.message(code: 'productCatalogItem.label', default: 'Product Catalog Item'), params.id])}"
                //redirect(action: "edit", id: productCatalog.id, fragment: "edit-product-catalog-items")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${warehouse.message(code: 'default.not.deleted.message', args: [warehouse.message(code: 'productCatalogItem.label', default: 'Product Catalog Item'), params.id])}"
                //redirect(action: "edit", id: params.id, fragment: "edit-product-catalog-items")
            }
        }
        else {
            flash.message = "${warehouse.message(code: 'default.not.found.message', args: [warehouse.message(code: 'productCatalogItem.label', default: 'Product Catalog Item'), params.id])}"
            //redirect(action: "list")
        }
        redirect(action: "productCatalogItems", id: productCatalogId)
    }



    def importProductCatalog = { ImportDataCommand command ->

        log.info "uploadCsv " + params

        def columns
        def localFile
        def uploadFile = command?.importFile

        if (request.method == "POST") {

            // Step 1: Upload file
            if (uploadFile && !uploadFile?.empty) {

                def contentTypes = ['application/vnd.ms-excel','text/plain','text/csv','text/tsv']
                println "Content type: " + uploadFile.contentType
                println "Validate: " + contentTypes.contains(uploadFile.contentType)

                try {

                    // Upload file
                    localFile = new File("uploads/" + uploadFile?.originalFilename);
                    localFile.mkdirs()
                    uploadFile?.transferTo(localFile);

                    // Get CSV content
                    def csv = localFile.getText()

                    // Import products
                    def rows = productService.parseProductCatalogItems(csv)

                    rows.each {
                        if (it.productCatalog && it.product) {
                            if (!it.productCatalog.contains(it.product)) {
                                it.productCatalog.addToProductCatalogItems(new ProductCatalogItem(product:it.product))
                            }
                        }
                    }
                    flash.message = "Imported ${rows.size()} from uploaded file ${uploadFile?.originalFilename}"


                } catch (Exception e) {
                    log.error("Exception occurred while uploading product import CSV " + e.message, e)
                    flash.message = "An error occurred while importing product catalog items: ${e.message}"
                }
            }
            else {
                log.warn("Cannot import product catalog items as file was empty")
                flash.message = "An error occurred while importing product catalog items: ${warehouse.message(code: 'import.emptyFile.message', default: 'File is empty')}"
            }
        }
        redirect(action: "edit", id: params.id)
    }

    def exportProductCatalog = {

        def productCatalog = ProductCatalog.get(params.id)
        if (productCatalog) {
            def date = new Date();
            response.setHeader("Content-disposition",
                    "attachment; filename=\"ProductCatalog-${date.format("yyyyMMdd-hhmmss")}.csv\"")
            response.contentType = "text/csv"
            String csv = "Catalog Code,Product Code,Product Name\n"
            productCatalog.productCatalogItems.each {
                csv += "${it.productCatalog.code},${it.product?.productCode},\"${it.product?.name}\"\n"
            }

            render csv
        }
        else {
            //render(text: 'No products found', status: 404)
            response.sendError(404)

        }

    }

}
