package org.openmrs.module.formentry.extension.html;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.web.extension.TableRowExt;
import org.openmrs.util.InsertedOrderComparator;

public class FormEntryFormRowsExt extends TableRowExt {

	private Log log = LogFactory.getLog(this.getClass());
	
	private String formId;
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	@Override
	public void initialize(Map<String, String> parameters) {
		formId = parameters.get("formId");
	}
	
	public Map<String, String> getRows() {
		
		Map<String, String> map = new TreeMap<String, String>(new InsertedOrderComparator());
		
		try {
			File file = FormEntryUtil.getXSNFile(formId + ".xsn");
			Long lastModified = file.lastModified();
			
			if (formId != "1") {
				Date date = new Date(lastModified);
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Context.getLocale());
				
				if (lastModified == 0L)
					map.put("formentry.xsn.lastModified", " (No XSN) ");
				else
					map.put("formentry.xsn.lastModified", dateFormat.format(date).toString());
					
			}
		}
		catch (Exception e) {
			log.warn("Unable to get xsn last modified date for form: " + formId, e);
			// pass
		}
		
		return map;
	}
	
}
