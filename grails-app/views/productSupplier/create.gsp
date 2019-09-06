
<%@ page import="grails.util.Holders; org.pih.warehouse.product.ProductSupplier" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'productSupplier.label', default: 'ProductSupplier')}" />
        <title><warehouse:message code="default.create.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productSupplierInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productSupplierInstance}" as="list" />
	            </div>
            </g:hasErrors>

			<div class="button-bar">
				<g:link class="button" action="list"><warehouse:message code="default.list.label" args="['productSupplier']"/></g:link>
				<g:link class="button" action="create"><warehouse:message code="default.add.label" args="['productSupplier']"/></g:link>
			</div>


			<g:form action="save" method="post" >
				<div class="box">
					<h2><warehouse:message code="default.create.label" args="[entityName]" /></h2>
					<table>
						<tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="product.id"><warehouse:message code="product.label" default="Product" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'product', 'errors')}">
                                    <g:autoSuggest name="product" class="medium text"
                                                   placeholder="${g.message(code:'product.search.label', default: 'Choose product')}"
                                                   jsonUrl="${request.contextPath }/json/findProductByName"
                                                   valueId="${productSupplierInstance?.product?.id}"
                                                   valueName="${productSupplierInstance?.product?.name}"/>

                                </td>
                            </tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="code"><warehouse:message code="productSupplier.code.label" default="Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'code', 'errors')}">
									<g:textField class="text" size="80" name="code" value="${productSupplierInstance?.code}" />
								</td>
							</tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><g:message code="default.name.label"/></label>
                                </td>
                                <td class="value ">
                                    <g:textField name="name" size="80" class="medium text" value="${productSupplier?.name}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="name">
                                    <label for="name"><g:message code="default.description.label"/></label>
                                </td>
                                <td class="value ">
                                    <g:textArea name="description" class="medium text" value="${productSupplier?.description}" />
                                </td>
                            </tr>


							<tr class="prop">
								<td valign="top" class="name">
									<label for="ratingTypeCode"><warehouse:message code="productSupplier.ratingTypeCode.label" default="Rating Type Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'ratingTypeCode', 'errors')}">
									<g:select class="chzn-select-deselect"
                                              name="ratingTypeCode"
                                              from="${org.pih.warehouse.core.RatingTypeCode?.values()}"
                                              value="${productSupplierInstance?.ratingTypeCode}"
                                              noSelection="['': '']" />
								</td>
							</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="preferenceTypeCode"><warehouse:message code="productSupplier.preferenceTypeCode.label" default="Preference Type Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'preferenceTypeCode', 'errors')}">
									<g:select class="chzn-select-deselect"
                                              name="preferenceTypeCode"
                                              from="${org.pih.warehouse.core.PreferenceTypeCode?.values()}"
                                              value="${productSupplierInstance?.preferenceTypeCode}"
                                              noSelection="['': '']" />
								</td>
							</tr>



							<tr class="prop">
								<td valign="top" class="name">
									<label for="upc"><warehouse:message code="productSupplier.upc.label" default="Upc" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'upc', 'errors')}">
									<g:textField class="text" size="80" name="upc" maxlength="255" value="${productSupplierInstance?.upc}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="ndc"><warehouse:message code="productSupplier.ndc.label" default="Ndc" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'ndc', 'errors')}">
									<g:textField class="text" size="80" name="ndc" maxlength="255" value="${productSupplierInstance?.ndc}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="supplier"><warehouse:message code="productSupplier.supplier.label" default="Supplier" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplier', 'errors')}">
                                    <g:selectOrganization name="supplier"
                                                          value="${productSupplierInstance?.supplier?.id}"
                                                          noSelection="['':'']"
                                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_SUPPLIER]"
                                                          class="chzn-select-deselect"/>
                                </td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="supplierCode"><warehouse:message code="productSupplier.supplierCode.label" default="Supplier Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplierCode', 'errors')}">
									<g:textField class="text" size="80" name="supplierCode" maxlength="255" value="${productSupplierInstance?.supplierCode}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="supplierName"><warehouse:message code="productSupplier.supplierName.label" default="Supplier Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'supplierName', 'errors')}">
									<g:textField class="text" size="80" name="supplierName" maxlength="255" value="${productSupplierInstance?.supplierName}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="modelNumber"><warehouse:message code="productSupplier.modelNumber.label" default="Model Number" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'modelNumber', 'errors')}">
									<g:textField class="text" size="80" name="modelNumber" maxlength="255" value="${productSupplierInstance?.modelNumber}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="brandName"><warehouse:message code="productSupplier.brandName.label" default="Brand Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'brandName', 'errors')}">
									<g:textField class="text" size="80" name="brandName" maxlength="255" value="${productSupplierInstance?.brandName}" />
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="manufacturer"><warehouse:message code="productSupplier.manufacturer.label" default="Manufacturer" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturer', 'errors')}">
                                    <g:selectOrganization name="manufacturer"
                                                          value="${productSupplierInstance?.manufacturer?.id}"
                                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                                          noSelection="['':'']"
                                                          class="chzn-select-deselect"/>
								</td>
							</tr>
							<tr class="prop">
								<td valign="top" class="name">
									<label for="manufacturerCode"><warehouse:message code="productSupplier.manufacturerCode.label" default="Manufacturer Code" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturerCode', 'errors')}">
									<g:textField class="text" size="80" name="manufacturerCode" maxlength="255" value="${productSupplierInstance?.manufacturerCode}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="manufacturerName"><warehouse:message code="productSupplier.manufacturerName.label" default="Manufacturer Name" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'manufacturerName', 'errors')}">
									<g:textField class="text" size="80" name="manufacturerName" maxlength="255" value="${productSupplierInstance?.manufacturerName}" />
								</td>
							</tr>
						
							<tr class="prop">
								<td valign="top" class="name">
									<label for="standardLeadTimeDays"><warehouse:message code="productSupplier.standardLeadTimeDays.label" default="Standard Lead Time Days" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'standardLeadTimeDays', 'errors')}">
									<g:textField class="text" name="standardLeadTimeDays" value="${fieldValue(bean: productSupplierInstance, field: 'standardLeadTimeDays')}" />
								</td>
							</tr>

								<tr class="prop">
									<td valign="top" class="name">
										<label for="unitPrice"><warehouse:message code="productSupplier.unitPrice.label" default="Price Per Unit" /></label>
									</td>
									<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'unitPrice', 'errors')}">
										<g:hasRoleFinance>
											<g:textField class="text" name="unitPrice" value="${fieldValue(bean: productSupplierInstance, field: 'unitPrice')}" />
											${grails.util.Holders.config.openboxes.locale.defaultCurrencyCode}
										</g:hasRoleFinance>
									</td>
								</tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="minOrderQuantity"><warehouse:message code="productSupplier.minOrderQuantity.label" default="Min Order Quantity" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'minOrderQuantity', 'errors')}">
									<g:textField class="text" name="minOrderQuantity" value="${fieldValue(bean: productSupplierInstance, field: 'minOrderQuantity')}" />
								</td>
							</tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="unitOfMeasure.id"><warehouse:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'unitOfMeasure', 'errors')}">
                                    <g:selectUnitOfMeasure name="unitOfMeasure.id"
                                                           noSelection="['null':'']"
                                                           value="${productSupplierInstance?.unitOfMeasure?.id}"
                                                           class="chzn-select-deselect"/>
                                </td>
                            </tr>

							<tr class="prop">
								<td valign="top" class="name">
									<label for="comments"><warehouse:message code="productSupplier.comments.label" default="Comments" /></label>
								</td>
								<td valign="top" class="value ${hasErrors(bean: productSupplierInstance, field: 'comments', 'errors')}">
									<g:textArea class="text" name="comments" value="${productSupplierInstance?.comments}" />
								</td>
							</tr>
						

						</tbody>
                        <tfoot>

                        <tr class="prop">
                            <td valign="top"></td>
                            <td valign="top" class="left">
                                <div class="buttons">
                                    <div class="right">
                                        <g:link action="list">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</g:link>
                                    </div>
                                    <div class="left">
                                        <g:submitButton name="create" class="button" value="${warehouse.message(code: 'default.button.create.label', default: 'Create')}" />
                                    </div>
                                </div>
                            </td>
                        </tr>
                        </tfoot>
					</table>
				</div>
            </g:form>
        </div>
    </body>
</html>
