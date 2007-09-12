package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.Extension;
import org.openmrs.module.web.extension.AdministrationSectionExt;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryAdminExt extends AdministrationSectionExt {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "formentry.title";
	}
	
	public String getRequiredPrivilege() {
		return "Upload XSN,Manage Form Entry";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		map.put("module/formentry/xsnUpload.form", "formentry.xsn.title");
		map.put("module/formentry/formEntryError.list", "formentry.FormEntryError.manage");
		map.put("module/formentry/formEntryInfo.htm", "formentry.info");
		
		return map;
	}
	
}
