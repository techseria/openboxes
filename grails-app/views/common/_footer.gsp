<%@ page import="grails.util.Holders; org.pih.warehouse.core.Constants; org.pih.warehouse.core.RoleType" %>
<div id="footer">
	<div style="line-height: 2em;" class="center middle">
		&copy; <g:copyrightYear/> <a href="https://openboxes.com">Powered by Teachseria</a> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.grailsVersion.label"/>: &nbsp; <b><g:meta name="app.grails.version"></g:meta></b> &nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.version.label"/>: &nbsp;<b><a href="https://github.com/openboxes/openboxes/releases/tag/v${g.meta(name:'app.version')}"><g:meta name="app.version"/></a></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<g:message code="application.branchName.label"/>: <b><g:meta name="app.branchName"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
        <g:message code="application.buildNumber.label"/>: <b><g:meta name="app.revisionNumber"/></b>&nbsp;&nbsp; | &nbsp;&nbsp;
		<g:message code="application.environment.label"/>: <b>${grails.util.GrailsUtil.environment}</b> &nbsp;&nbsp; | &nbsp;&nbsp;
		<g:message code="application.buildDate.label"/>: <b><g:meta name="app.buildDate"/></b>&nbsp;&nbsp;
    </div>
    <div class="center" style="line-height: 2em;">
		<%-- <warehouse:message code="default.date.label"/>: <b>${new Date() }</b>&nbsp;&nbsp; | &nbsp;&nbsp;--%>
		<g:message code="default.locale.label"/>: &nbsp;
		<!-- show all supported locales -->
		<g:set var="targetUri" value="${(request.forwardURI - request.contextPath) + '?' + (request.queryString?:'') }"/>
		<g:each in="${grails.util.Holders.config.openboxes.locale.supportedLocales}" var="l">
			<g:set var="locale" value="${new Locale(l)}"/>
			<g:set var="selected" value="${locale == session?.user?.locale || locale == session?.locale }"/>
			<a class="${selected?'selected':''}" href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':locale,'targetUri':targetUri,'lang':locale?.language])}">
				<!-- fetch the display for locale based on the current locale -->
				${locale?.getDisplayName(session?.user?.locale ?: new Locale(Holders.config.openboxes.locale.defaultLocale))}
			</a>
		</g:each>
		<g:isUserInRole roles="[RoleType.ROLE_ADMIN]">
			&nbsp;&nbsp; | &nbsp;&nbsp;
			<a href="${createLink(controller: 'user', action: 'updateAuthUserLocale',
				params: ['locale':'debug','targetUri':targetUri])}">
				<g:message code="admin.debug.label"/>:
			</a>
			<b>${session.useDebugLocale?"on":"off" }</b>
		</g:isUserInRole>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.ipAddress.label" default="IP Address"/>: &nbsp;
            <b>${request.getRemoteAddr()}</b>
		</span>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.hostname.label" default="Hostname"/>: &nbsp;
            <b>${session.hostname?:"Unknown"}</b>
        </span>
        &nbsp;&nbsp; | &nbsp;&nbsp;
        <span>
            <g:message code="default.timezone.label" default="Timezone"/>: &nbsp;
            <b>${session?.timezone?.ID}</b>
        </span>
        <g:if test="${session.warehouse && session.user && session._showTime}">
            &nbsp;&nbsp; | &nbsp;&nbsp;
            <span>
                Data load:
                <b>${(request?.actionDuration?:0)/1000}s</b>
            </span>
            &nbsp;&nbsp; | &nbsp;&nbsp;
            <span>
                Page load:
                <b>${(request?.viewDuration?:0)/1000}s</b>
            </span>
        </g:if>

    <%--
    &nbsp;&nbsp; | &nbsp;&nbsp;
    <warehouse:message code="default.layout.label"/>: &nbsp;
    <g:link controller="dashboard" action="chooseLayout" params="['layout':'custom']">custom</g:link>&nbsp;
    <g:link controller="dashboard" action="chooseLayout" params="['layout':'mega']">mobile</g:link>
    --%>
	</div>

    <%--
    <div class="center">
        <a href="https://twitter.com/justinmiranda" class="twitter-follow-button" data-show-count="false">Follow @justinmiranda</a>
        <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src=p+'://platform.twitter.com/widgets.js';fjs.parentNode.insertBefore(js,fjs);}}(document, 'script', 'twitter-wjs');</script>
    </div>
    --%>
</div>
