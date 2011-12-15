<%@page import="org.openmrs.util.OpenmrsConstants"%>
<%@ include file="/WEB-INF/template/include.jsp" %>

<%-- Convert version numbers in this form: 1.8 -> 18, 1.9 -> 19, 1.10 -> 110 for comparison --%>
<c:if test="<%=Integer.valueOf(OpenmrsConstants.OPENMRS_VERSION.substring(0, 
	OpenmrsConstants.OPENMRS_VERSION.indexOf(".", 2)).replace(".", "")) < 19 %>">
	
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
		
<script type="text/javascript">
	function processSelection(formFieldId, providerObj){
		if (typeof providerObj == 'string')
			return;
		setObj('${nodePath}', new miniObject(providerObj));
	}
			
	function miniObject(p) {
		str = '';
		str += p.identifier;
		if (p.displayName)
			str += " (" + p.displayName + ")";
				
		this.key = p.providerId;
		this.value = str;
	}
</script>

<h3><spring:message code="provider.title"/></h3>
<openmrs_tag:providerField formFieldName="providerId" callback="processSelection" />