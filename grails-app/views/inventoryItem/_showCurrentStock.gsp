<div class="box">
    <h2>
        <g:message code="inventory.currentStock.label" default="Current Stock"/>
        <small>${session.warehouse.name}</small>
    </h2>
    <table >
        <thead>
            <tr class="odd">
                <th class="left" style="">
                    <warehouse:message code="default.actions.label"/>
                </th>
                <th>
                    <warehouse:message code="inventory.binLocation.label" default="Bin Location"/>
                </th>
                <th>
                    <warehouse:message code="default.lotSerialNo.label"/>
                </th>
                <th>
                    <warehouse:message code="default.expires.label"/>
                </th>
                <th class="center middle" >
                    <warehouse:message code="default.qty.label"/>
                </th>
            </tr>
        </thead>
        <tbody>

            <g:each var="entry" in="${commandInstance.quantityByBinLocation.sort { it?.inventoryItem?.expirationDate }}" status="status">
                <g:set var="styleClass" value="${(status%2==0)?'even':'odd' }"/>
                <tr class="prop ${styleClass}">
                    <td class="middle" style="text-align: left; width: 10%" nowrap="nowrap">
                        <g:render template="actionsCurrentStock" model="[commandInstance:commandInstance,binLocation:entry.binLocation,itemInstance:entry.inventoryItem,itemQuantity:entry.quantity]" />
                    </td>
                    <td>
                        <g:if test="${entry?.binLocation}">
                            <g:link controller="location" action="edit" id="${entry.binLocation?.id}">${entry?.binLocation?.name}</g:link>
                        </g:if>
                        <g:else>
                            <warehouse:message code="default.label" default="Default"/>
                        </g:else>
                    </td>
                    <td>
                        ${entry?.inventoryItem?.lotNumber?:"Default"}
                    </td>
                    <td>
                        <g:expirationDate date="${entry?.inventoryItem?.expirationDate}"/>
                    </td>
                    <td class="middle center">
                        ${entry?.quantity} ${entry?.product?.unitOfMeasure}
                    </td>
                </tr>
            </g:each>
            <g:unless test="${commandInstance.quantityByBinLocation}">
                <tr>
                    <td colspan="5">
                        <div class="fade empty center">
                            <warehouse:message code="inventory.noItemsCurrentlyInStock.message" args="[format.product(product:commandInstance?.product)]"/>
                        </div>
                    </td>
                </tr>
            </g:unless>
        </tbody>
        <tfoot>
            <tr class="odd" style="border-top: 1px solid lightgrey; border-bottom: 0px solid lightgrey">
                <td colspan="4" class="right">
                    <!-- This space intentially left blank -->
                </td>
                <td class="center">
                    <div class="large">
                        <g:set var="styleClass" value="color: black;"/>
                        <g:if test="${commandInstance.totalQuantity < 0}">
                            <g:set var="styleClass" value="color: red;"/>
                        </g:if>
                        <span style="${styleClass }" id="totalQuantity">${g.formatNumber(number: commandInstance.totalQuantity, format: '###,###,###') }</span>
                        <span class="">
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>
                        </span>
                    </div>
                </td>
                <g:hasErrors bean="${flash.itemInstance}">
                    <td style="border: 0px;">
                        &nbsp;
                    </td>
                </g:hasErrors>
            </tr>
        </tfoot>
    </table>
</div>
<g:javascript>
    $(document).ready(function() {
        $(".trigger-change").live('change', function(event) {
            var url = $(this).data("url");
            var target = $(this).data("target");
            $.ajax({
                url: url,
                data: { "id":  $(this).val(), "name": "otherBinLocation.id", value: $(this).val()},
                cache: false,
                success: function(html) {
                    $(target).html(html)
                },
                error: function(error) {
                    $(target).html(error)
                }
            });
        });

    });
</g:javascript>

