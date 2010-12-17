<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="View FormEntry Error" otherwise="/login.htm" redirect="/module/formentry/formEntryError.list" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

<h2>
	<spring:message code="formentry.FormEntryError.title" />
</h2>

<script type="text/javascript">

	var lists = new Array();
	lists[""] = 0;
	lists["error"] = ${errorSize};

	function changeSize(list) {
		var queueType = "error";
		var start = document.getElementById("start");
		var end   = document.getElementById("end");
		var startOpts = start.options;
		var endOpts = end.options;
		while (startOpts.length) {
			startOpts[0] = null;
			endOpts[0] = null;
		}
		for (var i=1; i <= lists[queueType]; i++) {
			start.appendChild(new Option(i, i));
			end.appendChild(new Option(i, i));
		}
	}
</script>

<form method="post" action="${pageContext.request.contextPath}/moduleServlet/formentry/formEntryErrorDownload">
	<b class="boxHeader"><spring:message code="formentry.FormEntryError.multiple" />:</b>
	<div class="box">
		<table>
			<tr>
				<td colspan="2"><spring:message code="formentry.FormEntryError.select"/></td>
			</tr>
			<tr>
				<td><spring:message code="general.start" /></td>
				<td>
					<select name="startId" id="start"> </select>
				</td>
			</tr>
			<tr>
				<td><spring:message code="general.end" /></td>
				<td>
					<select name="endId" id="end"> </select>
				</td>
			</tr>
		</table>
		<input type="submit" value='<spring:message code="general.download" />' />
	</div>
</form>

<script type="text/javascript">
	changeSize("error");
</script>

<br/>

<%@ include file="/WEB-INF/template/footer.jsp"%>
