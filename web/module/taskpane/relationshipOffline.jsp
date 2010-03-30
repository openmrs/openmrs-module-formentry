<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/relationshipOffline.htm" />

<c:choose>
	<c:when test="${empty param.title}">
		<h3><spring:message code="Relationship.title"/></h3>
	</c:when>
	<c:otherwise>
		<h3><spring:message code="${param.title}"/></h3>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	<c:choose>
		<c:when test="${not empty param.nodePath}">
			<c:set var="nodePath" value="${param.nodePath}"/>
		</c:when>
		<c:otherwise>
			//pass
		</c:otherwise>
	</c:choose>
	<c:choose>
		<c:when test="${empty param.patientId}">
			alert("You must provide the patientId query parameter to match to");
		</c:when>
	</c:choose>
</script>

<openmrs:portlet url="patientRelationship" moduleId="formentry" size="normal" patientId="${param.patientId}" parameters="useOnKeyDown=true" />

<%@ include file="/WEB-INF/template/footer.jsp" %>