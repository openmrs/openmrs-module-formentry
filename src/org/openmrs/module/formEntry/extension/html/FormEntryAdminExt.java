package org.openmrs.module.formEntry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.Extension;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryAdminExt extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "formEntry.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		map.put("module/formEntry/xsnUpload.form", "formEntry.xsn.title");
		map.put("module/formEntry/formEntryQueue.list", "formEntry.FormEntryQueue.manage");
		map.put("module/formEntry/formEntryInfo.htm", "formEntry.info");
		
		return map;
	}
	
}
