<%@ include file="/WEB-INF/template/include.jsp" %>

<%
	// this must be set in order to get the minimal header to be used on any pages
	// that are redirected to (like the login)
	session.setAttribute(org.openmrs.web.WebConstants.OPENMRS_HEADER_USE_MINIMAL, "true");
%>

<html>
	<head>
		<openmrs:htmlInclude file="/openmrs.js" />
		<openmrs:htmlInclude file="/openmrs.css" />
		<openmrs:htmlInclude file="/style.css" />

		<openmrs:htmlInclude file="/moduleResources/formentry/taskpane.css" />
		<openmrs:htmlInclude file="/moduleResources/formentry/taskpane.js" />
		
		<%-- These are imported here because IE doesn't like our import js from js and dwr doesn't get loaded properly --%>
		<openmrs:htmlInclude file="/scripts/dojoConfig.js" />
		<openmrs:htmlInclude file="/scripts/dojo/dojo.js" />
		<openmrs:htmlInclude file="/dwr/engine.js" />
		<openmrs:htmlInclude file="/dwr/util.js" />

		<meta http-equiv="msthemecompatible" content="yes" />
		<meta http-equiv="pragma" content="no-cache" />
		<meta http-equiv="expires" content="-1" />
		<%@ include file="keepalive.jsp" %>

		<title><spring:message code="openmrs.title"/></title>
	</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
	<div id="pageBody">
		<div id="contentMinimal">
