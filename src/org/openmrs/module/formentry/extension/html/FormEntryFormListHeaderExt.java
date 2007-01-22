package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.module.Extension;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryFormListHeaderExt extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public Map<String, String> getLinks() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		map.put("moduleServlet/formEntry/formDownload?target=rebuildAll", "formEntry.xsn.rebuildAll");
		map.put("module/formEntry/xsnUpload.form", "formEntry.xsn.title");
		
		return map;
	}
	
}
