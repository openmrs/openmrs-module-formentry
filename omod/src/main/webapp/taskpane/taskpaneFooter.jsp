		<br/>
		</div>
	</div>

</body>
</html>

<%
	// this was set in order to get the minimal header to be used on any pages
	// that are redirected to (like the login)
	// 
	// We remove it here so that other pages by this user aren't minimal
	session.removeAttribute(org.openmrs.web.WebConstants.OPENMRS_HEADER_USE_MINIMAL);
%>