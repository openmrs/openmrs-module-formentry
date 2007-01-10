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
			map.put("moduleServlet/formEntry/formDownload?target=schema&formId=" + formId, "Form.downloadSchema");
			map.put("moduleServlet/formEntry/formDownload?target=template&formId=" + formId, "Form.downloadTemplate");
			map.put("moduleServlet/formEntry/formDownload?target=xsn&formId=" + formId, "Form.downloadXSN");
			map.put("moduleServlet/formEntry/formDownload?target=rebuild&formId=" + formId, "Form.rebuildXSN");
		}
		
		map.put("module/formEntry/xsnUpload.form", "formEntry.xsn.title");
		
		return map;
	}
	
	
}
