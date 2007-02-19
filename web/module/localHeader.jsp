<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
	
	<openmrs:hasPrivilege privilege="Upload XSN">
		<li <c:if test="<%= request.getRequestURI().contains("formentry/xsnUpload") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/formentry/xsnUpload.form">
				<spring:message code="formentry.xsn.title"/>
			</a>
		</li>
	</openmrs:hasPrivilege>
	
	<openmrs:hasPrivilege privilege="View FormEntry Queue">
		<li <c:if test="<%= request.getRequestURI().contains("formentry/formEntryQueue") %>">class="active"</c:if>>
			<a href="${pageContext.request.contextPath}/module/formentry/formEntryQueue.list">
				<spring:message code="formentry.FormEntryQueue.manage"/>
			</a>
		</li>
	</openmrs:hasPrivilege>

	<li <c:if test="<%= request.getRequestURI().contains("formentry/formEntryInfo") %>">class="active"</c:if>>
		<a href="${pageContext.request.contextPath}/module/formentry/formEntryInfo.htm">
			<spring:message code="formentry.info"/>
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