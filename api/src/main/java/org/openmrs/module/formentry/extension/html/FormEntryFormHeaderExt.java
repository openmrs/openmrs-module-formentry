package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryFormHeaderExt extends Extension {
	
	private String formId;
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public void initialize(Map<String, String> parameters) {
		formId = parameters.get("formId");
	}
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		if (formId != null) {
			map.put("moduleServlet/formentry/formDownload?target=schema&formId=" + formId, "formentry.xsn.downloadSchema");
			map.put("moduleServlet/formentry/formDownload?target=template&formId=" + formId,
			    "formentry.xsn.downloadTemplate");
			map.put("moduleServlet/formentry/formDownload?target=xsn&formId=" + formId, "formentry.xsn.download");
			map.put("moduleServlet/formentry/formDownload?target=rebuild&formId=" + formId, "formentry.xsn.rebuild");
			map.put("module/formentry/uploadXslt.form?formId=" + formId, "formentry.xslt.upload");
			
			Form form = Context.getFormService().getForm(Integer.valueOf(formId));
			if (form != null) {
				FormResource xsltResource = Context.getFormService().getFormResource(form,
				    FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME);
				if (xsltResource != null) {
					CustomDatatype<?> datatype = CustomDatatypeUtil.getDatatype(xsltResource.getDescriptor());
					CustomDatatypeHandler handler = CustomDatatypeUtil.getHandler(xsltResource.getDescriptor());
					if (datatype != null && handler != null) {
						map.put("admin/forms/downloadCustomValue.form?datatype=" + datatype.getClass().getName()
						        + "&handler=" + handler.getClass().getName() + "&value=" + xsltResource.getValueReference(),
						    "formentry.xslt.download");
					}
				}
			}
		}
		
		map.put("module/formentry/xsnUpload.form", "formentry.xsn.title");
		
		return map;
	}
}
