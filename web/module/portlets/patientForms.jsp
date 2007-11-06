<%@ include file="/WEB-INF/template/include.jsp" %>

<openmrs:hasPrivilege privilege="View Encounters">
	<openmrs:portlet url="patientEncounters" id="patientDashboardEncounters" patientId="${patient.patientId}" parameters="num=3|hideHeader=true|title=Encounter.last.encounters" />
	<br/>
</openmrs:hasPrivilege>

<openmrs:hasPrivilege privilege="Form Entry">
	<openmrs:htmlInclude file="/scripts/dojoConfig.js"/>
	<openmrs:htmlInclude file="/scripts/dojo/dojo.js"/>
	<openmrs:htmlInclude file="/dwr/interface/DWRFormService.js"/>

	<%--
		showUnpublishedForms == 'true' means allow users to enter forms that haven't been published yet
	--%>
	<c:set var="showUnpublishedForms" value="false" />
	<openmrs:hasPrivilege privilege="View Unpublished Forms"><c:set var="showUnpublishedForms" value="true" /></openmrs:hasPrivilege>
	
	<%--
		goBackOnEntry == 'true' means have the browser go back to the find patient page after starting to enter a form
	--%>
	<c:set var="goBackOnEntry" value="true" />
	<openmrs:globalProperty key="formentry.patientForms.goBackOnEntry" var="goBackOnEntry" defaultValue="false"/>
	
	<script type="text/javascript">
	
		dojo.require("dojo.widget.openmrs.OpenmrsSearch");
		
		var searchWidget;
		
		
		dojo.addOnLoad( function() {
			
			searchWidget = dojo.widget.manager.getWidgetById("fSearch");			
			
			searchWidget.doFindObjects = function() {
				this.doObjectsFound(this.allObjectsFound);
			};
			
			searchWidget.getCellContent = function(form) {
				var s = '<span onMouseOver="window.status=\'formId=' + form.formId + '\'">';
				s += form.name + " (v." + form.version + ")";
				if (form.published == false)
					s += ' <i>(<spring:message code="formentry.unpublished"/>)</i>';
					
				s += "</span>";
				return s;
			};
			
			searchWidget.doFindObjects = function(searchPhrase) {
				DWRFormService.findForms(this.simpleClosure(this, "doObjectsFound"), searchPhrase, '${showUnpublishedForms}');
			}
			
			dojo.event.topic.subscribe("fSearch/select", 
				function(msg) {
					document.location = "${pageContext.request.contextPath}/moduleServlet/formentry/formDownload?target=formentry&patientId=${patient.patientId}&formId=" + msg.objs[0].formId;
					startDownloading();
				}
			);
			
			searchWidget.resetSearch();
			
			
			searchWidget.allowAutoJump = function() { return false; };
			
			searchWidget.showAll = function() {
				DWRFormService.getForms(this.simpleClosure(this, "doObjectsFound"), '${showUnpublishedForms}');
			};
			
			// avoid unnecessary dwr call the first time by emulating the following line:
			//     searchWidget.showAll();
			var initialForms = new Array();
			var frm;
			<openmrs:forEachRecord name="form">
				<c:if test="${showUnpublishedForms == true || record.published}">
					frm = new Object();
					frm.formId = ${record.formId};
					frm.name = '${fn:replace(record.name, "'", "\\'")}';
					frm.version = '${fn:replace(record.version, "'", "\\'")}';
					frm.published = ${record.published};
					/*
						frm.encounterType = '${fn:replace(record.encounterType, "'", "\\'")}';
						frm.description = '${fn:replace(record.description, "'", "\\'")}';
						frm.build = ${record.build};
					*/
					initialForms.push(frm);
				</c:if>
			</openmrs:forEachRecord>
			searchWidget.doObjectsFound(initialForms);
			
			searchWidget.searchCleared = function() {
				searchWidget.showAll();
			};
			
		});
		
		
		//set up delayed post back
		var timeOut = null;
	
		function startDownloading() {
			<c:if test="${goBackOnEntry == 'true'}">
				timeOut = setTimeout("goBack()", 30000);
			</c:if>
		}
		
		function goBack() {
			document.location='findPatient.htm';
		}
		
		function switchPatient() {
			document.location='findPatient.htm?phrase=${param.phrase}&autoJump=false';
		}
		
		function cancelTimeout() {
			if (timeOut != null)
				clearTimeout(timeOut);
		}
	</script>
	
	<div id="selectFormHeader" class="boxHeader${model.patientVariation}"><spring:message code="formentry.patientDashboard.forms"/></div>
	<div id="selectForm" class="box${model.patientVariation}">
		<div dojoType="OpenmrsSearch" widgetId="fSearch" ></div>
	</div>
	
	<script type="text/javascript">	
		addEvent(window, 'load', function() {
			var widget = dojo.widget.manager.getWidgetById("fSearch");
			try {
				// IE throws an error if the forms table isn't on top
				widget.inputNode.focus();
			} catch(err) {
				//
			}
		});
	</script>
	
</openmrs:hasPrivilege>