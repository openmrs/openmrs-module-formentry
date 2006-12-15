package org.openmrs.module.formEntry.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;

public class AdminList extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public String getTitle() {
		return "formEntry.title";
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("module/formEntry/xsnUpload.form", "formEntry.xsn.title");
		
		return map;
	}
	
}
