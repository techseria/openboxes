<%@ page import="grails.util.Holders" %>
<g:form controller="productSupplier" method="post">

    <g:hiddenField name="id" value="${productSupplier?.id}"></g:hiddenField>
    <g:hiddenField name="product.id" value="${productSupplier?.product?.id}"></g:hiddenField>
    <g:hiddenField name="dialog" value="true"></g:hiddenField>

    <table>
        <tbody>
            <tr class="prop">
                <td class="name">
                    <label for="product"><warehouse:message code="product.label"/></label>
                </td>
                <td class="value ">
                    <div id="product">
                        ${productSupplier?.product?.productCode}
                        <format:product product="${productSupplier?.product}"/>
                    </div>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="code"><warehouse:message code="default.code.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="code" size="80" class="medium text" value="${productSupplier?.code}"
                                 placeholder="${g.message(code:'productSupplier.code.placeholder')}"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="productCode"><warehouse:message code="productSupplier.productCode.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="productCode" size="80" class="medium text" value="${productSupplier?.productCode}" />
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label for="name"><warehouse:message code="default.name.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="name" size="80" class="medium text" value="${productSupplier?.name}" />
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="name"><warehouse:message code="default.description.label"/></label>
                </td>
                <td class="value ">
                    <g:textArea name="description" class="medium text" value="${productSupplier?.description}" />
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="unitOfMeasure.id"><warehouse:message code="productSupplier.unitOfMeasure.label" default="Unit of Measure" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: productSupplier, field: 'unitOfMeasure', 'errors')}">
                    <g:selectUnitOfMeasure name="unitOfMeasure.id"
                                           noSelection="['null':'']"
                                           value="${productSupplier?.unitOfMeasure?.id}"
                                           class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="ndc"><warehouse:message code="product.ndc.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="ndc" size="80" class="medium text" value="${productSupplier?.ndc}" />
                </td>
            </tr>


            <tr class="prop">
                <td class="name">
                    <label for="manufacturer"><warehouse:message code="productSupplier.manufacturer.label"/></label>
                </td>
                <td class="value ">
                    <g:selectOrganization name="manufacturer"
                                          noSelection="['':'']"
                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_MANUFACTURER]"
                                          value="${productSupplier?.manufacturer?.id}"
                                          class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="manufacturerCode"><warehouse:message code="productSupplier.manufacturerCode.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="manufacturerCode" size="80" class="medium text" value="${productSupplier?.manufacturerCode}" />
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label for="brandName"><warehouse:message code="product.brandName.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="brandName" size="80" class="medium text" value="${productSupplier?.brandName}" />
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="supplier"><warehouse:message code="productSupplier.supplier.label"/></label>
                </td>
                <td class="value ">
                    <g:selectOrganization name="supplier"
                                          noSelection="['':'']"
                                          roleTypes="[org.pih.warehouse.core.RoleType.ROLE_SUPPLIER]"
                                          value="${productSupplier?.supplier?.id}"
                                          class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="supplierCode"><warehouse:message code="productSupplier.supplierCode.label"/></label>
                </td>
                <td class="value ">
                    <g:textField name="supplierCode" size="80" class="medium text" value="${productSupplier?.supplierCode}" />
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="unitPrice"><warehouse:message code="productSupplier.unitPrice.label"/></label>
                </td>
                <td class="value ">
                    <g:hasRoleFinance onAccessDenied="${g.message(code:'errors.userNotGrantedPermission.message', args: [session.user.username])}">
                        <g:textField name="unitPrice" size="80" class="medium text" value="${productSupplier?.unitPrice}" />
                        ${grails.util.Holders.config.openboxes.locale.defaultCurrencyCode}
                    </g:hasRoleFinance>
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label for="preferenceTypeCode"><warehouse:message code="productSupplier.preferenceTypeCode.label"/></label>
                </td>
                <td class="value ">
                    <g:selectPreferenceType name="preferenceTypeCode"
                                            noSelection="['':'']"
                                            value="${productSupplier?.preferenceTypeCode}"
                                            class="chzn-select-deselect"/>
                </td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="ratingTypeCode"><warehouse:message code="productSupplier.ratingTypeCode.label"/></label>
                </td>
                <td class="value ">
                    <g:selectRatingType name="ratingTypeCode"
                                        noSelection="['':'']"
                                        value="${productSupplier?.ratingTypeCode}"
                                        class="chzn-select-deselect"/>
                </td>
            </tr>
        </tbody>
    </table>


    <div class="buttons">
        <g:if test="${productSupplier.id}">
            <g:actionSubmit action="update" class="button icon accept" value="Save" id="update">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
            <g:actionSubmit action="delete" class="button icon trash" value="Delete" id="delete">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
        </g:if>
        <g:else>
            <g:actionSubmit action="save" class="button icon approve" value="Save" id="save">${warehouse.message(code: 'default.button.save.label', default: 'Save')}</g:actionSubmit>
        </g:else>
        &nbsp;
        <a href="#" class="close-dialog" dialog-id="product-supplier-dialog">${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}</a>


    </div>


</g:form>