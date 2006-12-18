<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="Audit" otherwise="/login.htm" redirect="/module/formEntry/formEntryInfo.htm"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>


<!-- TODO - OpenmrsClassLoader not being used to load in classes here?? -->


<%@ page import="org.openmrs.api.context.Context" %>
<%
	pageContext.setAttribute("formEntryVars", ((org.openmrs.module.formEntry.FormEntryService)Context.getService(org.openmrs.module.formEntry.FormEntryService.class)).getSystemVariables());
%>
	
<br />
<h2><spring:message code="SystemInfo.title"/></h2>
<br />

<br/><br/>
<h3><spring:message code="formEntry.header"/></h3>
<table cellpadding="4" cellspacing="0">
	<tr>
		<th><spring:message code="SystemInfo.name"/></th>
		<th><spring:message code="SystemInfo.value"/></th>
	</tr>
	<c:forEach items="${formEntryVars}" var="var" varStatus="status">
		<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
			<td>${var.key}</td>
			<td>${var.value}</td>
		</tr>
	</c:forEach>
</table>

<br/><br/>
<%@ include file="/WEB-INF/template/footer.jsp" %>