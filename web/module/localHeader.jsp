<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	<openmrs:hasPrivilege privilege="View FormEntry Queue">
		<li <c:if test="<%= request.getRequestURI().contains("formEntry/formEntryQueue") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/formEntry/formEntryQueue.list">
				<spring:message code="formEntry.FormEntryQueue.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>

	<li <c:if test="<%= request.getRequestURI().contains("formEntry/formEntryInfo") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/formEntry/formEntryInfo.htm">
			<spring:message code="formEntry.info"/>
		</a>
	</li>

	<openmrs:extensionPoint pointId="org.openmrs.admin.formentry.localHeader" type="html">
			<c:forEach items="${extension.links}" var="link">
				<li <c:if test="${fn:endsWith(pageContext.request.requestURI, link.key)}">class="active"</c:if> >
					<a href="${pageContext.request.contextPath}/${link.key}"><spring:message code="${link.value}"/></a>
				</li>
			</c:forEach>
	</openmrs:extensionPoint>
</ul>