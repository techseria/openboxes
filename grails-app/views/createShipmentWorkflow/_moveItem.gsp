<script type="text/javascript">
	$(document).ready(function(){					
		$("#dlgMoveItem").dialog({ autoOpen: true, modal: true, width: 800});

        $(".updateQuantity").blur( function() {
            var total = parseInt($(totalItemQuantity).text());
            $(".updateQuantity").each(function() {
                total -= parseInt($(this).val());
            });
            $(currentQuantity).val(total);
        });
	});
</script>

<div id="dlgMoveItem" title="${warehouse.message(code:'shipping.moveItem.label')}" style="padding: 10px; display: none;" >
	<g:if test="${flash.itemToMove}">
		<g:form name="moveItem" action="moveItemToContainer">
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="item.label" /></label></td>                            
						<td valign="top" class="value">
							<g:hiddenField name="item.id" value="${flash.itemToMove.id }"/>
							<b>${flash.itemToMove?.quantity }</b> x <format:product product="${flash.itemToMove?.product}"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="default.from.label" /></label></td>                            
						<td valign="top" class="value">
							<div>
								<g:set var="count" value="${1 }"/>
								<table>
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<th><warehouse:message code="container.label" /></th>
										<th><warehouse:message code="default.quantity.label" /></th>
									</tr>
									<tr>
										<td>
											<g:if test="${!flash.itemToMove.container}">
												<warehouse:message code="shipping.unpackedItems.label"/>
											</g:if>
											<g:if test="${flash.itemToMove?.container?.parentContainer }">
												${flash.itemToMove?.container?.parentContainer?.name } &rsaquo;
											</g:if> 
											${flash.itemToMove?.container?.name }
										</td>
										<td>
                                            <div id="totalItemQuantity">${flash.itemToMove?.quantity}</div>
										</td>									
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label><warehouse:message code="default.to.label"/></label></td>                            
						<td valign="top" class="value">
							<div style="height: 200px; overflow: auto;">
								<g:set var="count" value="${1 }"/>
								<table>
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<th><warehouse:message code="container.label" /></th>
										<th><warehouse:message code="default.quantity.label" /></th>
									</tr>
								
									
									<tr class="${count++ % 2 ? 'odd':'even' }">
										<td>
											<warehouse:message code="shipping.unpackedItems.label" default="Unpacked Items" />
										</td>
										<td>
											<g:if test="${flash.itemToMove.container}">
												<g:textField id="newQuantity-0" class="updateQuantity text" name="quantity-0" size="8" value="0"></g:textField>
											</g:if>
											<g:else>
												<g:textField id="currentQuantity" class="currentQuantity readonly text" name="quantity-0" size="8" readonly="readonly" value="${flash.itemToMove?.quantity}"></g:textField>
											</g:else>
										</td>
									</tr>
									
									
									<g:each var="containerTo" in="${shipmentInstance?.containers?.sort{it.sortOrder}}">
										<tr class="${count++ % 2 ? 'odd':'even' }">
											<td>
												<g:set var="selected" test="${flash.itemToMove?.container?.id == containerTo?.id}"/>
												<g:if test="${containerTo?.parentContainer }">${containerTo?.parentContainer?.name } &rsaquo;</g:if> ${containerTo?.name }
											</td>
											<td>
												<g:if test="${containerTo != flash.itemToMove.container}">
													<g:textField id="newQuantity-${containerTo?.id}" class="updateQuantity text" name="quantity-${containerTo?.id}" size="8" value="0"></g:textField>
												</g:if>
												<g:else>
													<g:textField id="currentQuantity" class="currentQuantity readonly text" name="quantity-${containerTo?.id}" size="8" readonly="readonly" value="${flash.itemToMove?.quantity}"></g:textField>
												</g:else>
											</td>
										</tr>									
									</g:each>
								</table>
								
							</div>
							<g:hiddenField id="totalQuantity" class="totalQuantity" name="totalQuantity" disabled="disabled" value="${flash.itemToMove?.quantity}"/>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<div class="buttons center">
								<g:submitButton name="moveItemToContainer" value="${warehouse.message(code:'default.button.move.label')}" class="button"></g:submitButton>
								<button name="cancelDialog" type="reset" onclick="$('#dlgMoveItem').dialog('close');" class="button"><warehouse:message code="default.button.cancel.label" /></button>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>														
	</g:if>
</div>		
		
