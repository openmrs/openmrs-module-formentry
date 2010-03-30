<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:htmlInclude file="/scripts/easyAjax.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRRelationshipService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRPatientService.js" />
<openmrs:htmlInclude file="/dwr/util.js" />
<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />

<openmrs:htmlInclude file="/scripts/jquery/jquery-1.3.2.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-1.7.2.custom.min.js" />
<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui-1.7.2.custom.css" />

<script type="text/javascript">
	var $j = jQuery.noConflict(); 
</script>

<style>
	#patientRelationshipPortlet { padding: 0.25em; /* width: 450px; */ }
	#patientRelationshipPortlet h5 { font-size: 0.85em; padding: 0 0.5em; }
	#specifyRelative, #specifyRelationship, #verifyRelationship, #submitButtons { margin: 1em 0; }
	#specifyRelative .content { border: 1px solid #999; }	
	#targetHeader { padding: 0.5em; }
	#targetHeader input.smallButton { margin-left: 1em; }
	#targetExtra { background: #D5E0ED; border-top: 1px solid #999; font-size: 0.85em; padding: 0.5em; line-height: 1.5em; }
	#relationshipSides { background: #ddd; border: 1px solid #999; padding: 0.25em 0.5em; }
	#relationshipSides a.selected { border-color: #3D628B; background-color: #3D628B !important; color: #fff !important; }
	#relationshipSides a, #relationshipSides a:visited { background: #fff; color: #000; display: block; margin: 0.25em 0; padding: 0.5em; line-height: 1em; text-decoration: none; }
	#relationshipSides a:hover { border-color: #3D628B; color: #000; }
	#verifyRelationship .content { background: #cec; border: 1px solid #999; padding: 0.5em; }
	#relativeRelation, #personRelation a.selected, #personRelation span.selected { color: #000 !important; font-weight: bold; text-decoration: none; }
	#personRelation a:hover { text-decoration: none; }
	#submitButtons { text-align: center; }
	#submitButtons button { padding: 0.25em; margin: 0 0.5em; }
</style>

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PersonSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsPopup");

	// this variable will be populated and sent to the add handler (pickRelationship)
	var relationship = {
		  type: null
		, aOrB: null
		, description: null
		, reverseDescription: null
		, relative: null
	};

	<%--
	The following code should render something along these lines:

	relationshipTypes = {
		'Patient': {
			'Doctor': {id:5, aOrB':'A'},
			'Nurse': {id:3, aOrB:'A'}
		},
		'Doctor': {
			'Patient': {id:5, aOrB:'B'}
		},
		'Nurse': {
			'Patient': {id:3, aOrB:'B'}
		},
		'Sibling': {
			'Sibling': {id:4, aOrB:'A'}
	};
	--%>
	
	// relationshipTypes holds all possible types and matching sides	
	var relationshipTypes = {
	<c:set var="firstRel" value="true"/>
	<c:forEach var="rel" items="${model.relationshipMap}">
		<c:if test="${!firstRel}">, </c:if><c:if test="${firstRel}"><c:set var="firstRel" value="false"/></c:if>'${rel.key}': {
		<c:set var="firstSubRel" value="true"/>
		<c:forEach var="subRel" items="${rel.value}">
			<c:if test="${!firstSubRel}">, </c:if><c:if test="${firstSubRel}"><c:set var="firstSubRel" value="false"/></c:if>'${subRel.key}': {
				id: ${subRel.value.id},
				aOrB: <c:if test="${subRel.value.aIsToB == subRel.key}">"B"</c:if
				><c:if test="${(subRel.value.bIsToA == subRel.key) && !(subRel.value.aIsToB == subRel.key)}">"A"</c:if>
			}
		</c:forEach>			
		}
	</c:forEach>
	}
	
	// helper method to pull just a list of keys out of a JSON object
	var mapKeys = function(map) {
		var keys = [];
		for (var key in map) { keys.push(key); }
		return keys;
	}

	// helper method to disable a button
	var disableButton = function(button) {
		// $j(button).addClass("ui-disabled");
		$j(button).attr("disabled", true);
	}

	// helper method to enable a button
	var enableButton = function(button) {
		// $j(button).removeClass("ui-disabled");
		$j(button).attr("disabled", false);		
	}

	/**
	 * results of the person picker portlet
	 */
	function handlePickPerson(relType, person) {
		// see if there's a patient that matches
		DWRPatientService.getPatient(person.personId, function(patient){
			// if there is, switch to patient instead of person
			var relative = (patient.patientId) ? patient : person;
			relationship.relative = relative;
			// fill out target box
			$j("#targetName").hide();
			$j("#targetExtra").empty();
			if ("identifier" in relative) { $j("#targetExtra").append("<div>Identifier: " + relative.identifier + "</div>"); };
			if (relative.address1) { $j("#targetExtra").append("<div>" + relative.address1 + "</div>"); };
			if (relative.address2) { $j("#targetExtra").append("<div>" + relative.address2 + "</div>"); };
			if (relative.birthdate) {
					$j("#targetExtra").append('<div><spring:message code="formentry.relationships.birthdate" javaScriptEscape="true" /> ' 
							+ relative.birthdate.toDateString());
				$j("#targetExtra").append(relative.birthdateEstimated ? " (est)" : "" + "</div>");
			}
			if ($j("#targetExtra").html() == "") {
				$j("#targetExtra").html("<i>No additional information available</i>");
			}
			$j("#targetExtra").fadeIn("slow");
			// fill spot in verification box
			$j("#relativeName").html(relative.personName);
			// turn on the relationship specification box
			$j("#specifyRelationship").fadeIn("slow");
		});
	}
	
	/**
	 * handle selecting the type of relationship
	 */
	function handlePickRelType(side) {
		// find all the possibilities
		var possibilities = mapKeys(relationshipTypes[side]);
		
		// clear previously stored relationship values
		relationship.aOrB = null;
		relationship.type = null;
		relationship.description = null;
		relationship.reverseDescription = null;
		
		// set the relative's relationship side
		$j("#relativeRelation").html(side);
		disableButton("#addRelationshipButton");
		$j("#personRelation").empty();

		if ($j(possibilities).size() == 1) {
			// there is only one possibilty; set it
			$j("#personRelation").append('<span class="selected">' + possibilities[0] + '</span>');
			specifyRelationship(side, possibilities[0]);
		} else {
			// list the possibilities
			var first = true;
			for (var index in possibilities) {
				if (first) { first = false;	} else { $j("#personRelation").append(" | "); }
				$j("#personRelation").append('<a href="#">' + possibilities[index] + '</a>');
			} 
			// add click functionality to links if there are links ...
			$j("#personRelation a").click(function(){
				$j(this).blur();
				$j(this).toggleClass("selected").siblings().removeClass("selected");
				specifyRelationship(side, $j(this).html());
			});
		}

		// show the verify box
		$j('#verifyRelationship').fadeIn("slow");
	}

	/**
	 * fill in details on the relationship object based on chosen side and other
	 */
	function specifyRelationship(side, other) {
		if(side in relationshipTypes) {
			if (other in relationshipTypes) {
				relationship.description = side;
				relationship.reverseDescription = other;
				relationship.type = relationshipTypes[side][other].id;
				relationship.aOrB = relationshipTypes[side][other].aOrB;
				enableButton("#addRelationshipButton");
			} else {
				alert("second side of the relationship (" + other + ") does not really exist.");
			}
		} else {
			alert("first side of the relationship (" + side + ") does not really exist.");
		}
	}
	
	function handleAddRelationship() {
		/*
		if (relType == null || relType == '' || relativeId == null || relativeId == '') {
			window.alert('<spring:message code="Relationship.error.everything" javaScriptEscape="true"/>');
			return;
		}
		if (relativeId == ${model.patient_id}) {
			window.alert('<spring:message code="Relationship.error.same" javaScriptEscape="true"/>');
		}
		*/

		pickRelationship(relationship);
	}
	
	function handleCancel() {
		closeTaskPane();
	}

	$j(document).ready(function(){
		// hide unwanteds
		$j("#targetExtra").hide();
		$j("#specifyRelationship").hide();
		$j("#verifyRelationship").hide();
		
		// populate the relationships select field
		for(var side in relationshipTypes) {
			$j("#relationshipSides").append('<a href="#" class="ui-widget-content ui-corner-all">' + side + '</a>');
			}; 
		
		// make the relationships select awesome
		$j("#relationshipSides").children().click(function(){
			$j(this).blur();
			$j(this).toggleClass("selected").siblings().removeClass("selected");
			handlePickRelType($j(this).html());
		});
		
		// make buttons into ui buttons
		disableButton("#addRelationshipButton");
	});
	
</script>

<div id="patientRelationshipPortlet">

	<div id="specifyRelative">
		<h5><spring:message code="formentry.relationships.specifyRelative"/></h5>
		<div class="content">
			<div id="targetHeader">
				<span id="targetName"><spring:message code="formentry.relationships.selectPerson"/></span>
				<openmrs_tag:personField formFieldName="add_rel_target"	useOnKeyDown="${model.useOnKeyDown}" callback="handlePickPerson" canAddNewPerson="true"/>
			</div>
			<div id="targetExtra"></div>
		</div>
	</div>

	<div id="specifyRelationship">
		<h5><spring:message code="formentry.relationships.specifyRelationship"/></h5>
		<div class="content">
			<div id="relationshipSides"></div>
		</div>
	</div>
	
	<div id="verifyRelationship">
		<h5><spring:message code="formentry.relationships.verifyRelationship"/></h5>
		<div class="content">
			<div class="personDescription">
				<span id="relativeName">Test</span>: 
				<span id="relativeRelation">Tester</span>
			</div>
			<div class="personDescription">
				<span id="personName">${model.person.personName}</span>: 
				<span id="personRelation">Testee</span>
			</div>
		</div>
	</div>
	
	<div id="submitButtons">
		<button id="addRelationshipButton" type="button"
			onClick="handleAddRelationship()">
			<span class="ui-button-text">
				<spring:message code="formentry.relationships.addRelationship" javaScriptEscape="true"/> 
			</span>
		</button>
		<button id="cancelButton" type="button" 
			onClick="handleCancel()">
			<span class="ui-button-text">
				<spring:message code="general.cancel" javaScriptEscape="true"/> 
			</span>
		</button>
	</div>
	
</div>
