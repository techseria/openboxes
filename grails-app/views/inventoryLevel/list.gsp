
<%@ page import="org.pih.warehouse.inventory.InventoryLevel" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventoryLevel.label', default: 'InventoryLevel')}" />
        <title><warehouse:message code="default.list.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.list.label" args="[entityName]" /></content>
        <asset:javascript src="application.js"/>
        <asset:javascript src="application.css"/>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
            
				<div class="buttonBar">
                    <g:link action="list" class="button icon log"><warehouse:message code="default.button.list.label" default="List"/></g:link>
                    <g:link action="create" class="button icon add"><warehouse:message code="default.button.add.label" default="Add"/></g:link>
                    <g:link action="export" class="button icon arrowdown"><warehouse:message code="default.button.download.label" default="Download"/></g:link>
            	</div>


                <div class="yui-gf">
                    <div class="yui-u first">

                        <div class="dialog box">
                            <h2><warehouse:message code="filters.label" default="Filters"/></h2>
                            <g:form action="list" method="get">
                                <g:selectLocation name="location.id"/>

                            </g:form>
                        </div>
                    </div>
                    <div class="yui-u">

                        <div class="box">
                            <h2><warehouse:message code="results.label" default="Results"/></h2>

                            <table>
                                <thead>
                                    <tr>

                                        <g:sortableColumn property="id" title="${warehouse.message(code: 'inventoryLevel.id.label', default: 'Id')}" />

                                        <th><warehouse:message code="inventoryLevel.product.label" default="Product" /></th>

                                        <g:sortableColumn property="inventory" title="${warehouse.message(code: 'inventoryLevel.inventory.label', default: 'Inventory')}" />

                                        <g:sortableColumn property="status" title="${warehouse.message(code: 'inventoryLevel.status.label', default: 'Status')}" />

                                        <g:sortableColumn property="minQuantity" title="${warehouse.message(code: 'inventoryLevel.minQuantity.label', default: 'Min Quantity')}" />

                                        <g:sortableColumn property="reorderQuantity" title="${warehouse.message(code: 'inventoryLevel.reorderQuantity.label', default: 'Reorder Quantity')}" />
                                        <g:sortableColumn property="maxQuantity" title="${warehouse.message(code: 'inventoryLevel.maxQuantity.label', default: 'Max Quantity')}" />

                                        <g:sortableColumn property="dateCreated" title="${warehouse.message(code: 'inventoryLevel.dateCreated.label', default: 'Date Created')}" />

                                    </tr>
                                </thead>
                                <tbody>
                                <g:each in="${inventoryLevelInstanceList}" status="i" var="inventoryLevelInstance">
                                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                        <td><g:link action="edit" id="${inventoryLevelInstance.id}">${fieldValue(bean: inventoryLevelInstance, field: "id")}</g:link></td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "product")}</td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "inventory")}</td>

                                        <td>${inventoryLevelInstance.status}</td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "minQuantity")}</td>

                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "reorderQuantity")}</td>
                                        <td>${fieldValue(bean: inventoryLevelInstance, field: "maxQuantity")}</td>

                                        <td><format:date obj="${inventoryLevelInstance.dateCreated}" /></td>

                                    </tr>
                                </g:each>
                                </tbody>
                            </table>
                        </div>
                        <div class="paginateButtons">
                            <g:paginate total="${inventoryLevelInstanceTotal}" />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
