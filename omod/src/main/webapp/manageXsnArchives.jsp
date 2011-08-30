<%@ include file="/WEB-INF/template/include.jsp"%>

<openmrs:require privilege="Manage Forms" otherwise="/login.htm" redirect="/module/formentry/manageXsnArchives.htm" />

<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="localHeader.jsp"%>

 <style>

.form { font-weight: bold; border-top: 1px solid #ccc; border-bottom: 1px dashed #ccc; padding: 0.25em 0; }
#xsnArchives { padding: 1em; }
#xsnArchives table { margin: 1em 0; width: 100%; }
#xsnArchives td { text-align: left; }
.live { font-style: italic; }
.failed { color: red; font-weight: bold; }

</style>

<script>

$j(document).ready(function(){

    $j("#selectAll").click(function(){
        var checked_status = this.checked;
        // clear the other select-all button
        if (this.checked)
       		$j("#selectAllButNewest").attr('checked', false);
        // select all
        $j("input[name=xsnIds]").each(function(){
            if (!this.disabled)
                this.checked = checked_status;
        });
    });

    $j("#selectAllButNewest").click(function(){
        var checked_status = this.checked;
        // clear the other select-all button
        if (this.checked)
        	$j("#selectAll").attr('checked', false);
		// select all but newest :-)
        $j("input[name=xsnIds]").each(function(){
            if (!this.disabled && !$j(this).hasClass("newest"))
                this.checked = checked_status;
            else
            	this.checked = false;
        });
    });

});

</script>

<h2><spring:message code="formentry.xsnarchives.manage"/></h2>

<p><spring:message code="formentry.xsnarchives.help" arguments="${location}"/></p>

<div class="boxHeader">
        <b><spring:message code="formentry.xsnarchives.manage"/></b>
</div>

<div class="box">

<form id="xsnArchives" method="POST">
    <input type="submit" value="<spring:message code="formentry.xsnarchives.submit"/>"/>
    <table>
        <tbody>
            <tr>
                <td><input type="checkbox" id="selectAll" name="selectAll"/></td>
                <td width="100%">
                	<label for="selectAll"><spring:message code="formentry.xsnarchives.selectAll"/></label>
                	&nbsp; &nbsp;
                	<input type="checkbox" id="selectAllButNewest" name="selectAllButNewest"/>
                	<label for="selectAllButNewest"><spring:message code="formentry.xsnarchives.selectAllButNewest"/></label>
                </td>
            </tr>
        <c:forEach items="${xsnmap}" var="entry">
            <tr>
                <td colspan="2" class="form">
                    ${entry.key.name} (${entry.key.version}) &mdash; ${entry.key.description}
                </td>
            </tr>
            <c:forEach items="${entry.value}" var="xsn" varStatus="xsnCounter">
                <tr class="xsn<c:if test="${!xsn.archived}"> live</c:if><c:if test="${openmrs:collectionContains(failedArchives, xsn.formEntryXsnId)}"> failed</c:if>">
                    <td>
                        <input type="checkbox" name="xsnIds" value="${xsn.formEntryXsnId}"
                        <c:if test="${!xsn.archived}">disabled</c:if>
                        <c:if test="${xsnCounter.count == 2}">class="newest"</c:if>
                        />
                    </td>
                    <td>
                        <spring:message code="formentry.xsnarchives.created"/> ${xsn.dateCreated} 
                        <c:if test="${!xsn.archived}">&mdash; <spring:message code="formentry.xsnarchives.live"/></c:if>
                        <c:if test="${xsn.archived}">&mdash; <spring:message code="formentry.xsnarchives.archived"/> ${xsn.dateArchived}</c:if>
                    </td>
                </tr>
            </c:forEach>
        </c:forEach>
        </tbody>
    </table>
    <input type="submit" value="<spring:message code="formentry.xsnarchives.submit"/>"/>
</form>
</div>
