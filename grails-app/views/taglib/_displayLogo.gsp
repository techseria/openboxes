<span class="logo">
    <g:if test="${attrs.includeLink}">
        <a href="${createLink(uri: '/')}">
            <img  !important align="left" src="${attrs?.logo?.url}" height="30" width="190" />
        </a>
    </g:if>
    <g:else>
        <img  !important align="left" src="${attrs?.logo?.url}" />
    </g:else>
    <g:if test="${attrs?.logo?.label && attrs.showLabel}">
        ${attrs?.logo.label}
    </g:if>
</span>

