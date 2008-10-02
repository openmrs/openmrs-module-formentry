<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/encounter.htm" />

<script type="text/javascript">
	<c:choose>
		<c:when test="${not empty param.nodePath}">
			<c:set var="nodePath" value="${param.nodePath}"/>
		</c:when>
		<c:otherwise>
			<c:set var="nodePath" value="//encounter/encounter.encounter_id"/>
		</c:otherwise>
	</c:choose>
	
	/**
	 * Insert the given encounter id into the infopath form 
	 * at "nodePath" (from query params)
	 * @encounterId the encounter to insert
	 */
	function selectEncounter(encounterId, datetime) {
		var encNode = oDOM.selectSingleNode("${nodePath}");
		clearNil(encNode);
		encNode.text = encounterId;
		
		<c:if test="${not empty param.originalDatetimeNodePath}">
			var origEncNodeValue = oDOM.selectSingleNode('<c:out escapeXml="true" value="${param.originalDatetimeNodePath}"/>');
			if (origEncNodeValue) {
				clearNil(origEncNodeValue);
				origEncNodeValue.text = datetime;
			}
		</c:if>
		
		closeTaskPane();
	}
	
</script>

<h3><spring:message code="Encounter.title"/></h3>

<spring:message code="formentry.taskpane.encounterHelp" />

<br/></br/>

<c:forEach var="enc" items="${encounters}">
	<a href="#selectEnc:${enc.encounterId}" onclick="selectEncounter(${enc.encounterId}, '<openmrs:formatDate date="${enc.encounterDatetime}"/>')">
		<openmrs:formatDate date="${enc.encounterDatetime}"/> - 
		<c:out escapeXml="true" value="${enc.form.name}"/>
	</a><br/>
</c:forEach>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>