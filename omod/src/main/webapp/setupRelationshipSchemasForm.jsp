<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Edit Forms" otherwise="/login.htm" redirect="/admin/forms/formEdit.form" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<style>
	.nowrap { white-space: nowrap; }
	.centered { text-align: center; }
</style>

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
	<b><spring:message code="formentry.relationships.help" /></b>
</div>

<div class="box">
	<ul>
		<li><spring:message code="formentry.relationships.addingSchemas"/></li>
		<li><spring:message code="formentry.relationships.addingWidgets"/></li>
		<li><spring:message code="formentry.relationships.findMore"/></li>
	</ul>
	<blockquote><i><spring:message code="formentry.relationships.notice"/></i></blockquote>
</div>

<br/>

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
					<th class="smallHeader centered"> <spring:message code="formentry.relationships.addSchema" /> </th>
					<th class="smallHeader centered"> <spring:message code="formentry.relationships.addWidget" /> </th>
					<th class="nowrap" width="100%"> <spring:message code="general.name" /> </th>
					<th class="nowrap"> <spring:message code="Form.version" /> </th>
					<th class="nowrap"> <spring:message code="Form.published" /> </th>
				</tr>
				<c:forEach var="form" items="${forms}" varStatus="status">
					<tr class="<c:choose><c:when test="${status.index % 2 == 0}">evenRow</c:when><c:otherwise>oddRow</c:otherwise></c:choose>">
						<td class="centered"><input type="checkbox" name="addSchema" value="${form.formId}" /></td>
						<td class="centered"><input type="checkbox" name="addWidget" value="${form.formId}" /></td>
						<td><a href="<openmrs:contextPath/>/admin/forms/formEdit.form?formId=${form.formId}">${form.name}</a></td>
						<td class="centered">${form.version}</td>
						<td class="centered"><c:if test="${form.published == true}"><spring:message code="general.yes"/></c:if></td>
					</tr>
				</c:forEach>
			</table>
			<br/>
			<input type="submit" value="<spring:message code="formentry.relationships.setFormSchemas"/>"/>
		</c:otherwise>
	</c:choose>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
