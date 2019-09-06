<%@ page import="org.pih.warehouse.core.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="custom" />
<g:set var="entityName" value="${warehouse.message(code: 'user.label', default: 'User')}" />
<title><warehouse:message code="default.show.label" args="[entityName]" /></title>
<content tag="pageTitle"><warehouse:message code="default.show.label" args="[entityName]" /></content>
	<asset:javascript src="application.js"/>
	<asset:javascript src="application.css"/>
</head>
<body>
	<div class="body">


        <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
        </g:if>

        <div class="dialog">

            <g:render template="summary" />


            <div class="box">
				<h2><g:message code="user.label"/></h2>
				<table>
					<tbody>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="user.username.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "username")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="default.name.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "name")}
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="user.email.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "email")}								
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name"><label><warehouse:message
									code="default.locale.label" /></label></td>
							<td valign="top" class="value">
								${fieldValue(bean: userInstance, field: "locale.displayName")}
							</td>
						</tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message
                                    code="default.timezone.label" default="Timezone" /></label></td>
                            <td valign="top" class="value">
                                ${userInstance?.timezone}
                            </td>
                        </tr>
						<tr class="prop">
							<td valign="top" class="name">
                                <label><warehouse:message
									code="user.roles.label" default="Roles" /></label></td>
							<td valign="top" class="value">
								<g:if test="${userInstance?.roles}">
									${fieldValue(bean: userInstance, field: "roles")}
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="no.access.label" />
									</span>
								</g:else>
							</td>
						</tr>
                        <tr class="prop" id="locationRoles">
                             <td valign="top" class="name">
                                <label><warehouse:message code="user.locationRoles.label" /></label>
                             </td>
                             <td valign="top" class="value">
                                 <g:if test="${userInstance?.locationRoles}">
                                    ${userInstance?.locationRolesDescription()}
                                 </g:if>
                                 <g:else>
                                     <span class="fade">${warehouse.message(code:'default.none.label')}</span>
                                 </g:else>
                             </td>
                         </tr>
                        <tr class="prop">
                            <td valign="top" class="name"><label><warehouse:message
                                    code="user.defaultLocation.label" /></label></td>
                            <td valign="top" class="value">
                                <g:if test="${userInstance?.warehouse}">
                                    ${fieldValue(bean: userInstance, field: "warehouse")}
                                </g:if>
                                <g:else>
                                    <span class="fade">
                                        <warehouse:message code="default.none.label" />
                                    </span>
                                </g:else>
                            </td>
                        </tr>

						<tr class="prop">
							<td valign="top" class="name">
                                <label><warehouse:message code="user.rememberLastLocation.label" /></label>
							</td>
							<td valign="top" class="value">
                                <g:if test="${userInstance.rememberLastLocation }">
									${userInstance.rememberLastLocation}
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.none.label" />
									</span>
								</g:else>
							</td>
						</tr>
						<tr class="prop">
							<td valign="top" class="name">
                                <label><warehouse:message code="user.lastLoginDate.label" /></label>
							</td>
							<td valign="top" class="value">
								<g:if test="${userInstance.lastLoginDate }">
									<format:datetime obj="${userInstance.lastLoginDate}"></format:datetime>
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.never.label" />
									</span>
								</g:else>
							</td>
						</tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><warehouse:message code="default.lastUpdated.label" /></label>
                            </td>
                            <td valign="top" class="value">
                                <g:if test="${userInstance.lastUpdated }">
                                    <format:datetime obj="${userInstance.lastUpdated}"></format:datetime>
                                </g:if>
                                <g:else>
                                    <span class="fade">
                                        <warehouse:message code="default.never.label" />
                                    </span>
                                </g:else>
                            </td>
                        </tr>
						<tr class="prop">
							<td valign="top" class="name">
								<label><warehouse:message code="default.dateCreated.label" /></label>
							</td>
							<td valign="top" class="value">
								<g:if test="${userInstance.dateCreated }">
									<format:datetime obj="${userInstance.dateCreated}"></format:datetime>
								</g:if>
								<g:else>
									<span class="fade">
										<warehouse:message code="default.never.label" />
									</span>
								</g:else>
							</td>
						</tr>
						<%--
						<tr class="prop">
							<td valign="top" class="name"><warehouse:message
									code="user.photo.label" /></td>
							<td valign="top" class="value">

								<table>
									<tr>
										<td><g:if test="${userInstance.photo}">
												<img class="photo"
													src="${createLink(controller:'user', action:'viewPhoto', id:userInstance.id)}" />
											</g:if></td>
										<td><g:form controller="user" method="post"
												action="uploadPhoto" enctype="multipart/form-data">
												<input type="hidden" name="id" value="${userInstance.id}" />
												<input type="file" name="photo" />
												<span class="buttons"><input class="positive"
													type="submit"
													value="${warehouse.message(code:'default.button.upload.label')}" /></span>
											</g:form></td>
									</tr>
								</table>
							</td>
						</tr>
						--%>
						<!-- 
							<tr class="prop">
	                            <td valign="top" class="prop name"></td>
	                            <td valign="top" class="prop value">
									<g:form>
										<g:hiddenField name="id" value="${userInstance?.id}" />
										<div class="buttons">
											<g:if test="${userInstance?.active}">
												<g:actionSubmit class="positive" action="toggleActivation" value="${warehouse.message(code: 'default.button.deactivate.label', default: 'De-activate')}" />
											</g:if>
											<g:else>
												<g:actionSubmit class="negative" action="toggleActivation" value="${warehouse.message(code: 'default.button.activate.label', default: 'Activate')}" />
											</g:else>
											<g:actionSubmit class="positive" action="edit" value="${warehouse.message(code: 'default.button.edit.label', default: 'Edit')}" />
											<g:actionSubmit class="negative" action="delete" value="${warehouse.message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
										</div>						
									</g:form>
								</td>
	                        </tr>
	                        -->
					</tbody>
				</table>
			</div>
		</div>

	</div>
</body>
</html>
