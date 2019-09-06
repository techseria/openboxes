<%@ page import="grails.util.Holders" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<title><warehouse:message code="order.addOrderItems.label"/></title>
<style>
    .dlg { display: none; }
</style>
    <asset:javascript src="application.js"/>
    <asset:javascript src="application.css"/>
</head>
<body>
	<div class="body">
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${order}">
			<div class="errors">
				<g:renderErrors bean="${order}" as="list" />
			</div>
		</g:hasErrors>
        <g:hasErrors bean="${orderItem}">
            <div class="errors">
                <g:renderErrors bean="${orderItem}" as="list" />
            </div>
        </g:hasErrors>

        <div class="dialog">
            <g:render template="/order/summary" model="[orderInstance:order,currentState:'addItems']"/>

            <div class="box">
                <h2><warehouse:message code="order.wizard.addItems.label"/></h2>
                <table style="">
                    <thead>
                        <tr class="odd">
                            <th><warehouse:message code="default.actions.label"/></th>
                            <th><warehouse:message code="order.lineItemNumber.label" default="#"/></th>
                            <g:sortableColumn property="product.productCode" title="${warehouse.message(code:'product.productCode.label')}" />
                            <g:sortableColumn property="product.name" title="${warehouse.message(code:'product.name.label')}" />
                            <g:sortableColumn property="product.vendor" title="${warehouse.message(code:'vendor.name.label', default: 'Vendor')}" />
                            <g:sortableColumn property="product.manufacturer" title="${warehouse.message(code:'manufacturer.name.label', default: 'Manufacturer')}" />

                            <g:sortableColumn property="quantity" title="${warehouse.message(code:'default.quantity.label')}" />
                            <g:sortableColumn property="unitOfMeasure" title="${warehouse.message(code:'default.uom.label', default: 'UOM')}" class="center"/>
                            <g:sortableColumn class="right" property="unitPrice" title="${warehouse.message(code:'order.unitPrice.label')}" />
                            <g:sortableColumn class="right" property="totalPrice" title="${warehouse.message(code:'order.totalPrice.label')}" />
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="i" value="${0 }"/>
                        <g:each var="orderItem" in="${order?.listOrderItems()}">
                            <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                                <g:hiddenField name="orderItems[${i }].order.id" value="${orderItem?.order?.id }" size="5"/>

                                <td class="actionButtons">
                                    <g:if test="${orderItem?.id }">
                                        <a href="javascript:void(-1);" id="edit-item-dialog-${orderItem?.id}" class="button icon edit edit-item-button" data-order-item-id="${orderItem?.id}">
                                            <warehouse:message code="default.button.edit.label"/>
                                        </a>
                                        <g:link action="purchaseOrder" id="${orderItem.id}" event="deleteItem" class="button icon trash" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                            <warehouse:message code="default.button.delete.label"/>
                                        </g:link>
                                    </g:if>
                                </td>

                                <td class="middle">
                                    ${i}
                                </td>
                                <td class="middle">
                                    <g:link controller="inventoryItem" action="showStockCard" id="${orderItem?.product?.id}">
                                        ${orderItem?.product?.productCode}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    <g:link controller="inventoryItem" action="showStockCard" id="${orderItem?.product?.id}">
                                        ${orderItem?.product?.name?:orderItem?.description?.encodeAsHTML()}
                                    </g:link>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.vendor?:"N/A"}
                                    <g:if test="${orderItem?.product?.vendorCode}">
                                        #${orderItem?.product?.vendorCode}
                                    </g:if>
                                </td>
                                <td class="middle">
                                    ${orderItem?.product?.manufacturer?:"N/A"}
                                    <g:if test="${orderItem?.product?.manufacturerCode}">
                                       #${orderItem?.product?.manufacturerCode}
                                    </g:if>
                                </td>
                                <td class="middle">
                                    ${orderItem?.quantity }
                                    <g:hiddenField name="orderItems[${i }].quantity" value="${orderItem?.quantity }" size="5"/>
                                </td>
                                <td class="middle center">
                                    ${orderItem?.product?.unitOfMeasure?:"EA"}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.unitPrice ?: 0.00 }" format="###,###,##0.00##"/>
                                    ${grails.util.Holders.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                                <td class="right middle">
                                    <g:formatNumber number="${orderItem?.totalPrice() }" />
                                    ${Holders.config.openboxes.locale.defaultCurrencyCode}
                                </td>
                            </tr>
                        </g:each>
                        <g:unless test="${order?.orderItems}">
                            <tr>
                                <td colspan="10">
                                    <div class="fade center empty">
                                        <p name="numItemInOrder">
                                            <warehouse:message code="order.itemsInOrder.message" args="[(order?.orderItems)?order?.orderItems?.size():0]"/>
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        </g:unless>
                        <tr class="prop">
                            <td colspan="10" class="center">
                                <button class="button icon add add-item-button">${warehouse.message(code:'order.button.addItem.label', default: 'Add line item')}</button>
                                <button id="btnImportItems" class="button icon add">${warehouse.message(code:'order.button.importItems.label', default: 'Import line items')}</button>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="${(i++ % 2) == 0 ? 'even' : 'odd'}">
                            <th>
                            </th>
                            <th colspan="9" class="right">
                                <warehouse:message code="default.total.label"/>
                                <g:formatNumber number="${order?.totalPrice()?:0.0 }"/>
                                ${Holders.config.openboxes.locale.defaultCurrencyCode}
                            </th>
                        </tr>
                    </tfoot>

                </table>
            </div>

            <div id="add-item-dialog" class="dlg box">
                <g:form action="purchaseOrder" method="post">
                    <g:hiddenField name="order.id" value="${order?.id }"></g:hiddenField>
                    <g:hiddenField name="orderItem.id" value="${orderItem?.id }"></g:hiddenField>
                    <table>
                        <tbody>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='product'><warehouse:message code="product.label"/>:</label></td>
                                <td valign='top' class='value' nowrap="nowrap">
                                    <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                        width="400" valueId="" valueName="" styleClass="text"/>
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='quantity'><warehouse:message code="default.quantity.label"/>:</label></td>
                                <td valign='top' class='value'>
                                    <input type="text" id="quantity" name='quantity' value="" size="10" class="text" />
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'>
                                    <label for='unitOfMeasure'><warehouse:message code="product.unitOfMeasure.label"/>:</label>
                                </td>
                                <td valign='top' class='value'>
                                    <g:hiddenField name="unitOfMeasure" value="each"/>
                                    each
                                </td>
                            </tr>
                            <tr class='prop'>
                                <td valign='top' class='name'><label for='unitPrice'><warehouse:message code="order.unitPrice.label"/>:</label></td>
                                <td valign='top' class='value'>
                                    <input type="text" id="unitPrice" name='unitPrice' value="" size="10" class="text" />
                                    <div class="fade"><warehouse:message code="order.unitPrice.hint"/></div>
                                </td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td valign="top" class="value" colspan="2">

                                    <div class="buttons">
                                        <span class="formButton">
                                            <g:submitButton name="addItem" value="${warehouse.message(code:'order.saveItem.label', default: 'Save item')}" class="button icon approve"></g:submitButton>
                                        </span>
                                    </div>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </g:form>
            </div>

		</div>
        <div id="edit-item-dialog" class="dlg box">
            <g:form action="purchaseOrder" method="post">
                <g:hiddenField id="edit-orderId" name="order.id" value="" />
                <g:hiddenField id ="edit-orderItemId" name="orderItem.id" value=""/>
                <table>
                    <tbody>
                    <tr class='prop'>
                        <td valign='top' class='name'>
                            <label for='product'><warehouse:message code="product.label"/>:</label>
                        </td>
                        <td valign='top' class='value' nowrap="nowrap">
                            <div id="edit-product-name"></div>
                            <input type="hidden" id="edit-product-id"/>
                        </td>
                    </tr>
                    <tr class='prop'>
                        <td valign='top' class='name'>
                            <label for='quantity'><warehouse:message code="default.quantity.label"/>:</label>
                        </td>
                        <td valign='top' class='value'>
                            <input type="text" id="edit-quantity" name='quantity' value="" size="10" class="text" />
                        </td>
                    </tr>
                    <tr class='prop'>
                        <td valign='top' class='name'>
                            <label for='unitOfMeasure'><warehouse:message code="product.unitOfMeasure.label"/>:</label>
                        </td>
                        <td valign='top' class='value'>
                            <div id="edit-uom"></div>
                        </td>
                    </tr>
                    <tr class='prop'>
                        <td valign='top' class='name'>
                            <label for='unitPrice'><warehouse:message code="order.unitPrice.label"/>:</label>
                        </td>
                        <td valign='top' class='value'>
                            <input type="text" id="edit-unitPrice" name='unitPrice' value="" size="10" class="text" />
                            <div class="fade"><warehouse:message code="order.unitPrice.hint"/></div>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td valign="top" class="value" colspan="2">
                            <div class="buttons">
                                <span class="formButton">
                                    <g:submitButton name="addItem" value="${warehouse.message(code:'order.saveItem.label', default: 'Save item')}" class="button"></g:submitButton>
                                </span>
                            </div>
                        </td>
                    </tr>
                    </tbody>

                </table>
            </g:form>
        </div>

        <div id="dlgImportItems" class="dlg box" title="Import Order Items">
            <div>
            <!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->
                <g:uploadForm controller="order" action="importOrderItems">

                    <g:hiddenField name="id" value="${order?.id}" />



                    <h3><warehouse:message code="importOrderItems.chooseOrderItemFile.label" default="Choose import file"/></h3>

                    <input name="fileContents" type="file" />


                    <div class="buttons">
                        <g:submitButton name="importOrderItems" value="Import Order Items" class="button icon add"></g:submitButton>
                    </div>
                </g:uploadForm>
            </div>
        </div>

    </div>
	<g:comboBox />
    <script type="text/javascript">
        $( function() {
            var cookieName, $tabs, stickyTab;

            cookieName = 'stickyTab';
            $tabs = $( '.tabs' );

            $tabs.tabs( {
                select: function( e, ui )
                {
                    $.cookies.set( cookieName, ui.index );
                }
            } );

            stickyTab = $.cookies.get( cookieName );
            if( ! isNaN( stickyTab )  )
            {
                $tabs.tabs( 'select', stickyTab );
            }
        } );


        $(document).ready(function(){
            $("#add-item-dialog").dialog({autoOpen:false, modal: true, width: 800, title: "Add line item"});
            $("#edit-item-dialog").dialog({autoOpen:false, modal: true, width: 800, title: "Edit line item"});
            $(".add-item-button").click(function(event){
                event.preventDefault();
                $("#add-item-dialog").dialog("open");
            });
            $(".edit-item-button").click(function(event){
                var id = $(this).attr("data-order-item-id");
                $.ajax({
                    dataType: "json",
                    timeout: 2000,
                    url: "${request.contextPath}/json/getOrderItem",
                    data: { id: id },
                    success: function (data) {
                        console.log(data);
                        $("#edit-product-id").val(data.product.id);
                        $("#edit-product-name").html(data.product.productCode + " " + data.product.name);
                        $("#edit-orderId").val(data.order.id);
                        $("#edit-uom").text(data.product.unitOfMeasure);
                        $("#edit-orderItemId").val(data.id);
                        $("#edit-quantity").val(data.quantity);
                        $("#edit-unitPrice").val(data.unitPrice);

                    },
                    error: function(xhr, status, error) {
                        alert(error);
                    }
                });

                $("#edit-item-dialog").dialog("open");
            });

            $("#btnImportItems").click(function(event){
                $("#dlgImportItems").dialog('open');
            });
            $("#dlgImportItems").dialog({
                autoOpen: false,
                modal: true,
                width: 600
            });



        });


    </script>

</body>
</html>