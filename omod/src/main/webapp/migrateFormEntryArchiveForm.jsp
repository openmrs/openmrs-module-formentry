<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:require privilege="View FormEntry Archive" otherwise="/login.htm" redirect="/module/formentry/migrateFormEntryArchive.form"/>

<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="localHeader.jsp" %>

<h2><spring:message code="formentry.queueArchiveMigration.title"/></h2>
<br />

<form method="post" action="">
	
	<spring:message code="formentry.queueArchiveMigration.1"/> <br/><br/>
	
	<spring:message code="formentry.queueArchiveMigration.2"/> <br/><br/>
	
	<spring:message code="formentry.queueArchiveMigration.archiveDir"/>.
	<spring:message code="formentry.queueArchiveMigration.archiveDir.change"/><br/>
	<br/>
	<b>${archiveDir}</b>
	<br/>
	<br/>
	<br/>
	<c:choose>
		<c:when test="${active}">
			<spring:message code="formentry.stopQueueArchiveMigration.help"/><br/>
			<input type="submit" name="action" value='<spring:message code="formentry.stopQueueArchiveMigration"/>'/>
		</c:when>
		<c:otherwise>
			<spring:message code="formentry.startQueueArchiveMigration.help"/><br/>
			<input type="submit" name="action" value='<spring:message code="formentry.startQueueArchiveMigration"/>'/>
			
			<br/><br/><br/>
			<spring:message code="formentry.queueArchiveMigration.complete"/>
		</c:otherwise>
	</c:choose>
	
</form>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>