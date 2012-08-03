<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage FormEntry Xslt" otherwise="/login.htm" redirect="/module/formentry/uploadXslt.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<spring:message code="formentry.xslt.upload" />
</h2>

<form method="post" enctype="multipart/form-data">
	<table>
		<tr>
			<th><spring:message code="formentry.xslt.file" /></th>
			<td><input type="file" name="resourceValue" /></td>
		</tr>
	</table>
	<input type="hidden" name="formId" value="${formId}" />
	<input type="submit" value='<spring:message code="general.submit" />' />
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>