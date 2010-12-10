<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/person.htm" />

<c:choose>
	<c:when test="${not empty param.nodePath}">
		<c:set var="nodePath" value="${param.nodePath}"/>
	</c:when>
	<c:otherwise>
		<c:set var="nodePath" value="//encounter.provider_id"/>
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${empty param.title}">
		<h3><spring:message code="Person.find"/></h3>
	</c:when>
	<c:otherwise>
		<h3><spring:message code="${param.title}"/></h3>
	</c:otherwise>
</c:choose>

<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />

<script type="text/javascript">

	$j(document).ready(function() {
		new OpenmrsSearch("findPerson", false, doPersonSearch, doSelectionHandler, 
				[{fieldName:"givenName", header:" "}, {fieldName:"familyName", header:" "}, {fieldName:"systemId", header:" "}],
				{columnRenderers: [nameColumnRenderer, null, null], 
				columnVisibility: [true, false, false]
				});
	});

	function miniObject(o) {
		this.key = o.personId;
		this.value = getName(o);
	}
	
	function doSelectionHandler(index, data) {
		setObj('${nodePath}', new miniObject(data));
	}
	
	//searchHandler for the Search widget
	function doPersonSearch(text, resultHandler, getMatchCount, opts) {
		DWRPersonService.findCountAndPeople(text, opts.includeVoided, "Provider", opts.start, opts.length, getMatchCount, resultHandler);
	}
	//custom render, appends an arrow and preferredName it exists
	function nameColumnRenderer(oObj){
		return oObj.aData[0]+" "+oObj.aData[1]+" ("+oObj.aData[2]+")";
	}
	
	function getName(o) {
		if (typeof o == 'string') return o;
		str = ''
		str += o.givenName + " ";
		str += o.familyName;
		if (o.systemId)
			str += " (" + o.systemId + ")";
		return str;
	};
</script>

<div>
	<div class="searchWidgetContainer">
		<div id="findPerson"></div>
	</div>
</div>

<br />
<small><em><spring:message code="general.search.hint"/></em></small>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>