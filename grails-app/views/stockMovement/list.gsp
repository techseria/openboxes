<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'stockMovements.label', default: 'Stock Movements')}" />
    <title>
        ${entityName} &rsaquo; <warehouse:message code="enum.StockMovementType.${params.direction}"/>
    </title>
    <content tag="pageTitle">${entityName}</content>
    <link rel="stylesheet" type="text/css" href="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/daterangepicker.min.css" />
    <asset:javascript src="application.js"/>
</head>
<body>

<g:set var="pageParams"
       value="['origin.id':params?.origin?.id, 'destination.id':params?.destination?.id, q:params.q,
               commodityClass:params.commodityClass, status:params.status, direction: params?.direction,
               requestedDateRange:params.requestedDateRange, issuedDateRange:params.issuedDateRange, type:params.type,
               'createdBy.id':params?.createdBy?.id, sort:params?.sort, order:params?.order, relatedToMe:params.relatedToMe,
               'requestedBy.id': params?.requestedBy?.id, receiptStatusCode: params.receiptStatusCode]"/>

<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>

    <div class="summary">
        <div class="title">
            ${entityName} &rsaquo; <warehouse:message code="enum.StockMovementType.${params.direction}"/>
        </div>
    </div>

    <div class="buttonBar">

        <div class="right">

            <g:set var="relatedToMe" value="${params?.createdBy?.id==session?.user?.id}"/>
            <g:if test="${incoming}">
                <div class="button-group">
                    <g:link controller="stockMovement" action="exportItems" class="button">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'page_excel.png')}" />
                        ${warehouse.message(code: 'stockMovements.exportIncomingItems.label', default: 'Export all incoming items')}
                    </g:link>
                </div>
            </g:if>
            <div class="button-group">
                <g:link controller="stockMovement" action="list" params="['createdBy.id':session?.user?.id]" class="button ${relatedToMe?'primary':''}">
                    ${warehouse.message(code:'stockMovements.relatedToMe.label', default: 'My stock movements')}
                    (${statistics["MINE"]?:0 })
                </g:link>
            </div>
            <div class="button-group">
                <g:link controller="stockMovement" action="list" class="button ${(!params.status && !relatedToMe)?'primary':''}">
                    <warehouse:message code="default.all.label"/>
                    (${statistics["ALL"]})
                </g:link>
                <g:each var="status" in="${RequisitionStatus.list()}">
                    <g:if test="${statistics[status]>0}">
                        <g:set var="isPrimary" value="${params.status==status.name()?true:false}"/>
                        <g:link controller="stockMovement" action="list" params="[status:status]" class="button ${isPrimary?'primary':''}">
                            <format:metadata obj="${status}"/>
                            (${statistics[status]?:0 })
                        </g:link>
                    </g:if>
                </g:each>
            </div>
        </div>

        <div class="button-container">
            <g:link controller="stockMovement" action="list" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'application_side_list.png')}" />&nbsp;
                <warehouse:message code="default.list.label" args="[warehouse.message(code: 'stockMovement.label')]"/>
            </g:link>
            <g:link controller="stockMovement" action="index" class="button">
                <img src="${resource(dir: 'images/icons/silk', file: 'add.png')}" />&nbsp;
                <warehouse:message code="default.create.label" args="[warehouse.message(code: 'stockMovement.label')]" />
            </g:link>
        </div>


    </div>

    <div class="yui-gf">
        <div class="yui-u first">
            <div class="box">
                <h2><warehouse:message code="default.filters.label"/></h2>
                <g:form action="list" method="GET">
                    <g:hiddenField name="max" value="${params.max}"/>
                    <g:hiddenField name="offset" value="0"/>
                    <div class="filter-list">
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.search.label"/></label>
                            <p>
                                <g:textField name="q" style="width:100%" class="text" value="${params.q}" placeholder="Search by requisition number, name, etc"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.status.label"/></label>
                            <p>
                                <g:selectRequisitionStatus name="status" value="${params?.status}"
                                                           noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.receiptStatus.label" default="Receipt Status"/></label>
                            <p>
                                <g:select name="receiptStatusCode" value="${params?.receiptStatusCode}" from="${ShipmentStatusCode.list()}"
                                                           noSelection="['':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.origin.label"/></label>
                            <p>
                                <g:selectLocation name="origin.id" value="${params?.origin?.id}"
                                                        noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.destination.label"/></label>
                            <p>
                                <g:selectLocation name="destination.id" value="${params?.destination?.id}"
                                                  noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <%--

                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.dateRequested.label"/></label>
                            <p>
                                <g:textField id="requestedDateRange" name="requestedDateRange" style="width:100%" class="daterange text" value="${params.requestedDateRange}"/>
                            </p>
                        </div>

                        <div class="filter-list-item">
                            <label><warehouse:message code="stockMovement.dateIssued.label"/></label>
                            <p>
                                <g:textField id="issuedDateRange" name="issuedDateRange" style="width:100%" class="daterange text" value="${params.issuedDateRange}"/>
                            </p>
                        </div>

                        --%>
                        <div class="filter-list-item">
                            <label><warehouse:message code="requisition.requestedBy.label"/></label>
                            <p>
                                <g:selectUser name="requestedBy.id" value="${params?.requestedBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.createdBy.label"/></label>
                            <p>
                                <g:selectUser name="createdBy.id" value="${params?.createdBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <div class="filter-list-item">
                            <label><warehouse:message code="default.updatedBy.label"/></label>
                            <p>
                                <g:selectUser name="updatedBy.id" value="${params?.updatedBy?.id}"
                                              noSelection="['null':'']" class="chzn-select-deselect"/>
                            </p>
                        </div>
                        <hr/>
                        <div class="filter-list-item">
                            <button class="button icon search" name="search" class="button">
                                ${warehouse.message(code:'default.search.label')}
                            </button>
                        </div>

                        <div class="clear"></div>
                    </div>
                </g:form>

            </div>
        </div>
        <div class="yui-u">
            <g:if test="${stockMovements}">
                <g:render template="list" model="[stockMovements:stockMovements,
                                                  entityName:entityName,
                                                  totalCount:stockMovements.size(),
                                                  pageParams:pageParams]"/>
            </g:if>
            <g:else>
                <div class="box">
                    <h2>${warehouse.message(code:'stockMovements.label')}</h2>
                    <div class="center empty">
                        <warehouse:message code="default.noResultsFound.label" default="No results found" />
                    </div>
                </div>
            </g:else>

        </div>
    </div>
</div>
<script type="text/javascript" src="//cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>
<script type="text/javascript" src="//cdnjs.cloudflare.com/ajax/libs/jquery-date-range-picker/0.16.1/jquery.daterangepicker.min.js"></script>


<script type="text/javascript">
			$(function() {
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				);

                $(".dialog-box").hide();
                $(".dialog-box").dialog({ autoOpen:false, height: 600, width:800 });

                $(".dialog-trigger").click(function(event){
                    $($(this).attr("data-id")).dialog('open');
                });


                %{--$('#requestedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});--}%
                %{--$('#issuedDateRange').dateRangePicker({format: 'D/MMM/YYYY', separator: '-', autoClose: true});--}%


            });
        </script>

</body>
</html>
