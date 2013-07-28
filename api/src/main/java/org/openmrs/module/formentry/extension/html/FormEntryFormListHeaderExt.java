package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryFormListHeaderExt extends Extension {

	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	public Map<String, String> getLinks() {
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		if (Context.hasPrivilege(FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN)) {
			map.put("moduleServlet/formentry/formDownload?target=rebuildAll", "formentry.xsn.rebuildAll");
			map.put("module/formentry/xsnUpload.form", "formentry.xsn.title");
		}
		
		return map;
	}
	
}
