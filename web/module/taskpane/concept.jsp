<%@ include file="taskpaneHeader.jsp" %>

<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/concept.htm" />

<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.ConceptSearch");
	
	function miniObject(c) {
		this.key = c.conceptId;
		this.value = c.name;
		this.nameKey = c.conceptNameId;
		this.nameValue = c.name;
	}
	
	function miniMapEntry(key) {
		this.key = key;
		this.value = document.getElementById(key).value;
	}
	
	function miniConcept(n) {
		this.conceptId = "<%= org.openmrs.util.OpenmrsConstants.PROPOSED_CONCEPT_IDENTIFIER %>";
		if (n == null)
			this.name = $('proposedText').innerHTML;
		else
			this.name = n;
	}
	
	function showProposeConceptForm() {
		$('searchForm').style.display = "none";
		$('proposeConceptForm').style.display = "block";
		txt = $('proposedText');
		txt.value = "";
		insertContent(txt, searchWidget.lastPhraseSearched);
		return false;
	}
	
	function proposeConcept() {
		var box = $('proposedText');
		if (box.text == '')  {
			alert("Proposed Concept text must be entered");
			box.focus();
		}
		else {
			$('proposeConceptForm').style.display = "none";
			$('searchForm').style.display = "";
			DWRConceptService.findProposedConcepts(box.value, preProposedConcepts);
		}
	}
	
	function preProposedConcepts(concepts) {
		if (concepts.length == 0) {
			searchWidget.select({"objs":[new miniConcept()]});
		}
		else {
			//display a box telling them to pick a preposed concept:
			$("preProposedAlert").style.display = "block";
			$('searchForm').style.display = "";
			searchWidget.doObjectsFound(concepts);
		}
	}
	
	/**
	* Inserts text into textarea and places cursor at end of string
	* More steps than needed right now
	* Borrowed from http://www.alexking.org/blog/2004/06/03/js-quicktags-under-lgpl/
	*/
	function insertContent(myField, myValue) {
		//IE support
		if (document.selection) {
			myField.focus();
			sel = document.selection.createRange();
			sel.text = myValue;
			myField.focus();
		}
		//MOZILLA/NETSCAPE support
		else if (myField.selectionStart || myField.selectionStart == '0') {
			var startPos = myField.selectionStart;
			var endPos = myField.selectionEnd;
			var scrollTop = myField.scrollTop;
			myField.value = myField.value.substring(0, startPos)
						+ myValue 
						+ myField.value.substring(endPos, myField.value.length);
			myField.focus();
			myField.selectionStart = startPos + myValue.length;
			myField.selectionEnd = startPos + myValue.length;
			myField.scrollTop = scrollTop;
		} else {
			myField.value += myValue;
			myField.focus();
		}
	}
	
	/* 
		This method will display the extra inputs defined by 
		params.extraLabel and params.extraNodePath
	*/
	var extraMiniObject;
	var extraForm;
	function showExtraInput(msg) {
		extraMiniObject = new miniObject(msg.objs[0]);

		var extraInputConcept = document.getElementById('extraInputConcept');
		var extraForm = document.getElementById('extraInputForm');
		var searchForm = document.getElementById('searchForm');
		
		extraInputConcept.innerHTML = extraMiniObject.value;
		searchWidget.clearSearch();
		searchForm.style.display = "none";
		extraInputForm.style.display = "block";
		
		extraForm.elements[0].focus();
	}
	
	/* 
		Submits the concept and extra information.
		Only used if extra information is present, otherwise, submission is done at onSelect
	*/
	function submitExtraInformation() {
		// create the extra info map
		var extraMap = new Array();
		<c:forEach var="node" items="${paramValues.extraNodePath}" varStatus="nodeStatus">
			extraMap.push(new miniMapEntry("${node}"));
		</c:forEach>
		pickConcept('${param.nodePath}', extraMiniObject, '${param.createConceptList}', extraMap, '${param.nodeHasSiblings}');
		
		return false;
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("cSearch");			
		
		dojo.event.topic.subscribe("cSearch/select", 
			function(msg) {
				<c:choose>
					<c:when test="${not empty param.extraNodePath}">
						showExtraInput(msg);
					</c:when>
					<c:otherwise>
						for (i=0; i<msg.objs.length; i++) {
							<c:choose>
								<c:when test="${not empty param.nodePath}">
									pickConcept('${param.nodePath}', new miniObject(msg.objs[i]), '${param.createConceptList}', null, '${param.nodeHasSiblings}');
								</c:when>
								<c:otherwise>
									pickProblem('<%= request.getParameter("mode") %>', '//problem_list', new miniObject(msg.objs[i]));
								</c:otherwise>
							</c:choose>
						}
					</c:otherwise>
				</c:choose>
			}
		);
		
		dojo.event.topic.subscribe("cSearch/objectsFound", 
			function(msg) {
				<%-- Do not allow proposed concepts if there is 'extra' information --%>
				<c:if test="${empty param.extraNodePath}">
					if ($("preProposedAlert").style.display != "block")
						msg.objs.push("<a href='#proposeConcept' onclick='javascript:return showProposeConceptForm();'><spring:message code="ConceptProposal.propose.new"/></a>");
				</c:if>
			}
		);
		
		var label = searchWidget.verboseListing.previousSibling;
		label.parentNode.insertBefore(document.createElement('br'), label);
		
		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();
		
		<c:if test="${empty param.nodePath}">
			<c:if test="${empty param.mode}">
				alert('Error #35233: <spring:message code="formentry.nodePathEmpty"/>');
			</c:if>
		</c:if>
				
	});

</script>

<style>
	#proposeConceptForm { display: none; }
	#preProposedAlert { display: none; }
	#extraInputForm { display: none; }
	.alert { color: red; }
</style>

<c:choose>
	<c:when test="${empty param.title}">
		<h3><spring:message code="diagnosis.title"/></h3>
	</c:when>
	<c:otherwise>
		<h3><spring:message code="${param.title}"/></h3>
	</c:otherwise>
</c:choose>

<div id="preProposedAlert" class="alert">
	<br>
	<spring:message code="ConceptProposal.proposeDuplicate" />
	<br>
</div>

<div id="searchForm">
	<input name="mode" type="hidden" value='${request.mode}'>
	<div dojoType="ConceptSearch" widgetId="cSearch" inputWidth="9em" showVerboseListing="true" includeClasses='<request:existsParameter name="className"><request:parameters id="c" name="className"><request:parameterValues id="names"><jsp:getProperty name="names" property="value"/>;</request:parameterValues></request:parameters></request:existsParameter>' useOnKeyDown="true" allowConceptEdit="false" <c:if test="${not empty param.includeDrugConcepts}">includeDrugConcepts="true"</c:if> ></div>
	<br />
	<small>
		<em>
			<spring:message code="general.search.hint" />
		</em>
	</small>
</div>

<div id="proposeConceptForm">
	<br />
	<spring:message code="ConceptProposal.proposeInfo" />
	<br /><br />
	<b><spring:message code="ConceptProposal.originalText" /></b><br />
	<textarea name="originalText" id="proposedText" rows="4" cols="20" /></textarea><br />
	<input type="button" onclick="proposeConcept()" value="<spring:message code="ConceptProposal.propose" />" /><br />
	
	<br />
	<span class="alert">
		<spring:message code="ConceptProposal.proposeWarning" />
	</span>
</div>

<form id="extraInputForm" onsubmit="return submitExtraInformation()">
	<br />
	
	<div style="font-weight: bold" id="extraInputConcept"> </div>
	
	<br />
	
	<spring:message code="formentry.taskpane.extraInformationFillIn"/>
	
	<%-- Check to make sure extraLabel and extraNodePath are the same size --%>
	<script type="text/javascript">
		if (${fn:length(paramValues.extraLabel)} != ${fn:length(paramValues.extraNodePath)}) {
			alert("Error 17384A! Query parameters 'extraLabel' and 'extraNodePath' must contain the same number of elements!");
		}
	</script>

	<table>
		<c:forEach var="label" items="${paramValues.extraLabel}" varStatus="labelStatus">
			<tr>
				<td>${label}</td>
				<td><input type="text" id="${paramValues.extraNodePath[labelStatus.index]}" /></td>
			</tr>
		</c:forEach>
	</table>
	<br/>
	<input type="button" value='<spring:message code="formentry.taskpane.extraInformationSubmit"/>' onclick="submitExtraInformation()" />
</form>

<br />

<%@ include file="/WEB-INF/template/footer.jsp"%>
