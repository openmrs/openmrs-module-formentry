<%@ include file="/WEB-INF/template/include.jsp" %>

<%
	// this must be set in order to get the minimal header to be used on any pages
	// that are redirected to (like the login)
	session.setAttribute(org.openmrs.web.WebConstants.OPENMRS_HEADER_USE_MINIMAL, "true");
%>

<html>
	<head>
		<openmrs:htmlInclude file="/openmrs.js" />
		<openmrs:htmlInclude file="/scripts/openmrsmessages.js" /> <%-- appendLocale=true is not on here for backwards compatibility --%>
		<openmrs:htmlInclude file="/openmrs.css" />
		<openmrs:htmlInclude file="/style.css" />
		<c:if test="${empty DO_NOT_INCLUDE_JQUERY}">
			<openmrs:htmlInclude file="/scripts/jquery/jquery.min.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui.custom.min.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/js/jquery-ui-datepicker-i18n.js" />
			<openmrs:htmlInclude file="/scripts/jquery-ui/css/redmond/jquery-ui.custom.css" />
		</c:if>
		
		<title><spring:message code="openmrs.title"/></title>
		
		<script type="text/javascript">
			<c:if test="${empty DO_NOT_INCLUDE_JQUERY}">
			var $j = jQuery.noConflict();
			</c:if>
			/* variable used in js to know the context path */
			var openmrsContextPath = '${pageContext.request.contextPath}';
			var dwrLoadingMessage = '<spring:message code="general.loading" />';
			var jsDateFormat = '<openmrs:datePattern localize="false"/>';
			var jsLocale = '<%= org.openmrs.api.context.Context.getLocale() %>';
			
			/* prevents users getting false dwr errors msgs when leaving pages */
			var pageIsExiting = false;
			$j(window).bind('beforeunload', function () { pageIsExiting = true; } );
			
			var handler = function(msg, ex) {
				if (!pageIsExiting) {
					var div = document.getElementById("openmrs_dwr_error");
					div.style.display = ""; // show the error div
					var msgDiv = document.getElementById("openmrs_dwr_error_msg");
					msgDiv.innerHTML = '<spring:message code="error.dwr"/>' + " <b>" + msg + "</b>";
				}
				
			};
			dwr.engine.setErrorHandler(handler);
			dwr.engine.setWarningHandler(handler);
		</script>

		<openmrs:htmlInclude file="/moduleResources/formentry/taskpane.css" />
		<openmrs:htmlInclude file="/moduleResources/formentry/taskpane.js" />
		
		<%-- These are imported here because IE doesn't like our import js from js and dwr doesn't get loaded properly --%>
		<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
		<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
		<openmrs:htmlInclude file="/dwr/engine.js" />
		<openmrs:htmlInclude file="/dwr/util.js" />
		<openmrs:htmlInclude file="/scripts/jquery/dataTables/css/dataTables_jui.css"/>
		<openmrs:htmlInclude file="/scripts/jquery/dataTables/js/jquery.dataTables.min.js"/>
		<openmrs:htmlInclude file="/scripts/jquery-ui/js/openmrsSearch.js" />
		
		<meta http-equiv="msthemecompatible" content="yes" />
		<meta http-equiv="pragma" content="no-cache" />
		<meta http-equiv="expires" content="-1" />
		<%@ include file="keepalive.jsp" %>

	</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<div id="pageBody">
		<div id="contentMinimal">
