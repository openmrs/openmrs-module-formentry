<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formEdit.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2><spring:message code="formentry.relationships.title"/></h2>

<spring:hasBindErrors name="form">
	<spring:message code="fix.error"/>
	<div class="error">
		<c:forEach items="${errors.allErrors}" var="error">
			<spring:message code="${error.code}" text="${error.code}"/><br/><!-- ${error} -->
		</c:forEach>
	</div>
</spring:hasBindErrors>

<div class="boxHeader">
	<b><spring:message code="formentry.relationships.boxHeaderTitle" /></b>
</div>

<form method="post" class="box">
	<c:choose>
		<c:when test="${fn:length(forms) < 1}">
			<spring:message code="formentry.relationships.noForms"/>
		</c:when>
		<c:otherwise>
			<table cellpadding="2" cellspacing="0" id="formTable" width="98%">
				<tr>
					<th> </th>
					<th> <spring:message code="general.name" /> </th>
					<th> <spring:message code="Form.version" /> </th>
					<th> <spring:message code="Form.published" /> </th>
				</tr>
				<c:forEach var="form" items="${forms}" varStatus="status">
					<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
						<td><input type="checkbox" name="formId" value="${form.formId}" /></td>
						<td><a href="formEdit.form?formId=${form.formId}">${form.name}</a></td>
						<td>${form.version}</td>
						<td><c:if test="${form.published == true}"><spring:message code="general.yes"/></c:if></td>
					</tr>
				</c:forEach>
			</table>
			<br/>
			<input type="submit" value="<spring:message code="formentry.relationships.setFormSchemas"/>"/>
		</c:otherwise>
	</c:choose>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
