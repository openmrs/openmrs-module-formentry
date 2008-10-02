<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/relationship.htm" />

<c:choose>
	<c:when test="${empty param.title}">
		<h3><spring:message code="Relationship.title"/></h3>
	</c:when>
	<c:otherwise>
		<h3><spring:message code="${param.title}"/></h3>
	</c:otherwise>
</c:choose>

<i><spring:message code="formentry.taskpane.submitWarning" /></i>
	
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
	
	/**
	 * Hides the taskpane and optionally adds a message to 
	 * the form if they said they were finished
	 * @showCompletedMessage true/false whether to say 'complete' in the form
	 */
	function hideTaskpane(showCompletedMessage) {
		var node = oDOM.selectSingleNode('${param.nodePath}');
		var valueNode = node.selectSingleNode("value");
		clearNil(valueNode);
		if (showCompletedMessage)
			valueNode.text = '<spring:message code="formentry.taskpane.completed" />';
		else
			valueNode.text = '';
			
		closeTaskPane();
	}
</script>

<openmrs:portlet url="personRelationships" size="normal" patientId="${param.patientId}" parameters="useOnKeyDown=true" />

<script type="text/javascript">
	// show the add new relationship box, but leave the link.
	// doing it here because infopath seems to have a problem 
	// with href="javascript:" tags.
	showDiv('addRelationship'); 
	//hideDiv('addRelationshipLink');
	
	// fix the a href="javascript:" links to be onclicks
	
	dojo.addOnLoad( function() {
		var inputNode = document.getElementByName("add_rel_target");
		var personSearch = dojo.widget.manager.getWidgetById("add_rel_target_search");
		//dojo.event.connect(inputNode, "onkeydown", personSearch, "onInputChange");
	});
</script>

<br/>

<!--

<hr>

<br/>

<input type="button" value="<spring:message code="formentry.relationships.finished"/>" onclick="hideTaskpane(true)"/>

<br/>
<br/>

<input type="button" value="<spring:message code="formentry.relationships.notfinished"/>" onclick="hideTaskpane(false)"/>
-->

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>