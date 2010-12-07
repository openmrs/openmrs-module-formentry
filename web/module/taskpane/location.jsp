<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/location.htm" />

<h3><spring:message code="Location.title"/></h3>

<c:choose>
	<c:when test="${not empty param.nodePath}">
		<c:set var="nodePath" value="${param.nodePath}"/>
	</c:when>
	<c:otherwise>
		<c:set var="nodePath" value="//encounter.location_id"/>
	</c:otherwise>
</c:choose>

<openmrs:htmlInclude file="/dwr/interface/DWREncounterService.js"/>

<script type="text/javascript">
	var lastSearch;
	
	$j(document).ready(function() {
		new OpenmrsSearch("findLocation", false, doLocationSearch, doSelectionHandler, 
				[{fieldName:"name", header:" "}],
				{searchLabel: ' '});
	});
	
	function miniObject(o) {
		this.key = o.locationId;
		this.value = o.name;
	}
	
	function doSelectionHandler(index, data) {
		setObj('${nodePath}', new miniObject(data));
	}
	
	//searchHandler for the Search widget
	function doLocationSearch(text, resultHandler, getMatchCount, opts) {
		lastSearch = text;
		DWREncounterService.findCountAndLocations(text, opts.start, opts.length, getMatchCount, resultHandler);
	}
</script>

<div>
	<div class="searchWidgetContainer">
		<div id="findLocation"></div>
	</div>
</div>

<br />
<small><em><spring:message code="general.search.hint"/></em></small>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>