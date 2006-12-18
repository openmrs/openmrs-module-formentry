package org.openmrs.module.formEntry.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;

public class FormEntryAdminList extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "formEntry.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("module/formEntry/xsnUpload.form", "formEntry.xsn.title");
		map.put("module/formEntry/formEntryQueue.list", "formEntry.FormEntryQueue.manage");
		map.put("module/formEntry/formEntryInfo.htm", "formEntry.info");
		
		return map;
	}
	
}
