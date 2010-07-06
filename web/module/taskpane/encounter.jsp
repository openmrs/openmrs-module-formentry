<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/encounter.htm" />

<script type="text/javascript">

    // encs object holds all found encounters by encounter id
    var encList = {
    <c:set var="firstEnc" value="true"/>
    <c:forEach var="enc" items="${encounters}">
        <c:if test="${!firstEnc}">, </c:if><c:if test="${firstEnc}"><c:set var="firstEnc" value="false"/></c:if>'${enc.encounterId}': {
            'datetime': '<openmrs:formatDate date="${enc.encounterDatetime}" format="yyyy-MM-dd" />'
            , 'providerId': ${enc.provider.personId}
            , 'providerName': '${enc.provider.givenName} ${enc.provider.familyName} (${enc.provider.personId})'
            , 'locationId': ${enc.location.locationId}
            , 'locationName': '${enc.location.name}'
        }
    </c:forEach>
    }
    
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
	function selectEncounter(encounterId) {
		var encNode = oDOM.selectSingleNode("${nodePath}");
		clearNil(encNode);
		encNode.text = encounterId;
		
		<c:if test="${not empty param.originalDatetimeNodePath}">
			var origEncNodeValue = oDOM.selectSingleNode('<c:out escapeXml="true" value="${param.originalDatetimeNodePath}"/>');
			if (origEncNodeValue) {
				clearNil(origEncNodeValue);
				origEncNodeValue.text = encList[encounterId].datetime;
			}
		</c:if>

		<c:if test="${not empty param.originalProviderNodePath}">
			var origProviderNodeValue = oDOM.selectSingleNode('<c:out escapeXml="true" value="${param.originalProviderNodePath}"/>');
			if (origProviderNodeValue) {
				clearNil(origProviderNodeValue);
				origProviderNodeValue.text = encList[encounterId].providerId + "^" + encList[encounterId].providerName;
			}
		</c:if>

		<c:if test="${not empty param.originalLocationNodePath}">
			var origLocationNodeValue = oDOM.selectSingleNode('<c:out escapeXml="true" value="${param.originalLocationNodePath}"/>');
			if (origLocationNodeValue) {
				clearNil(origLocationNodeValue);
				origLocationNodeValue.text = encList[encounterId].locationId + "^" + encList[encounterId].locationName;
			}
		</c:if>
		
		closeTaskPane();
	}
	
</script>

<h3><spring:message code="Encounter.title"/></h3>

<spring:message code="formentry.taskpane.encounterHelp" />

<br/></br/>

<c:forEach var="enc" items="${encounters}">
	<a href="#selectEnc:${enc.encounterId}" onclick="selectEncounter(${enc.encounterId})">
		<openmrs:formatDate date="${enc.encounterDatetime}"/> - 
		<c:out escapeXml="true" value="${enc.form.name}"/>
	</a><br/>
</c:forEach>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>