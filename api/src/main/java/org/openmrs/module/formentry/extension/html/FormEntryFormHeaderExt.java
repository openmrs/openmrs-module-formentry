package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.Extension;
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

	public Map<String, String> getLinks() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		if (formId != null) {
			map.put("moduleServlet/formentry/formDownload?target=schema&formId=" + formId, "formentry.xsn.downloadSchema");
			map.put("moduleServlet/formentry/formDownload?target=template&formId=" + formId, "formentry.xsn.downloadTemplate");
			map.put("moduleServlet/formentry/formDownload?target=xsn&formId=" + formId, "formentry.xsn.download");
			map.put("moduleServlet/formentry/formDownload?target=rebuild&formId=" + formId, "formentry.xsn.rebuild");
		}
		
		map.put("module/formentry/xsnUpload.form", "formentry.xsn.title");
		
		return map;
	}
	
	
}
