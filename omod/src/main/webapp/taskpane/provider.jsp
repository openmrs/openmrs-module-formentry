<%@page import="org.openmrs.util.OpenmrsConstants"%>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%-- Convert version numbers in this form: 1.8 -> 18, 1.9 -> 19, 1.10 -> 110 for comparison --%>
<c:if test='<%=Integer.valueOf(OpenmrsConstants.OPENMRS_VERSION.substring(0,
	OpenmrsConstants.OPENMRS_VERSION.indexOf(".", 2)).replace(".", "")) < 19 %>'>
	
	<c:redirect url="/module/formentry/taskpane/person.htm?${pageContext.request.queryString}&role=Provider&title=provider.title"/>
</c:if>

<%@ include file="taskpaneHeader.jsp" %>
		
<openmrs:require privilege="Form Entry" otherwise="/login.htm" redirect="/module/formentry/taskpane/provider.htm" />
		
<c:choose>
	<c:when test="${not empty param.nodePath}">
		<c:set var="nodePath" value="${param.nodePath}"/>
	</c:when>
	<c:otherwise>
		<c:set var="nodePath" value="//encounter.provider_id"/>
	</c:otherwise>
</c:choose>

<h3><spring:message code="provider.title"/></h3>

<openmrs:htmlInclude file="/dwr/interface/DWRPersonService.js" />
<openmrs:htmlInclude file="/dwr/interface/DWRProviderService.js" />

<script type="text/javascript">
	dojo.require("dojo.widget.openmrs.PersonSearch");
	dojo.require("dojo.widget.openmrs.OpenmrsSearch");
	
	function miniObject(p) {
		str = '';
		str += p.identifier;
		if (p.displayName)
			str += " (" + p.displayName + ")";
				
		this.key = p.providerId;
		this.value = str;
	}
	
	var searchWidget;
	
	dojo.addOnLoad( function() {
		
		searchWidget = dojo.widget.manager.getWidgetById("pSearch");
		
		dojo.event.topic.subscribe("pSearch/select", 
			function(msg) {
				setObj('${nodePath}', new miniObject(msg.objs[0]));
			}
		);
		
		dojo.event.topic.subscribe("pSearch/objectsFound", 
			function(msg) {
				if (msg.objs.length == 1 && typeof msg.objs[0] == 'string')
					msg.objs.push('<p class="no_hit"><spring:message code="provider.missing" /></p>');
			}
		);
		
		searchWidget.postCreate = function() {
			if (this.personId != "")
				DWRProviderService.getProvider(personId, this.simpleClosure(this, "select"));
		};
		
		searchWidget.doFindObjects = function(text) {
			var tmpIncludedVoided = (this.showIncludeVoided && this.includeVoided.checked);
			DWRProviderService.findProvider(text, tmpIncludedVoided, 0, null, this.simpleClosure(this, "doObjectsFound"));
            
			return false;
		};
		
		searchWidget.getDisplayName = function(p) {
			if (typeof p == 'string') return p;
			str = '';
			str += p.identifier;
			if (p.displayName)
				str += " (" + p.displayName + ")";
					
			return str;
		};
		
		searchWidget.getCellFunctions = function() {
			var arr = new Array();
			arr.push(this.simpleClosure(this, "getNumber"));
			arr.push(this.simpleClosure(this, "getDisplayName"));
			return arr;
		};
		
		searchWidget.showHeaderRow = false;
		
		searchWidget.allowAutoJump = function() {
			return this.text && this.text.length > 1;
		};

		searchWidget.inputNode.focus();
		searchWidget.inputNode.select();

	});

</script>

<div dojoType="PersonSearch" widgetId="pSearch" inputWidth="10em" useOnKeyDown="true" canAddNewPerson="false" roles='<request:existsParameter name="role"><request:parameters id="r" name="role"><request:parameterValues id="names"><jsp:getProperty name="names" property="value"/></request:parameterValues></request:parameters></request:existsParameter>'></div>
<br />
<small><em><spring:message code="general.search.hint"/></em></small>

<br/><br/>

<%@ include file="/WEB-INF/template/footer.jsp" %>