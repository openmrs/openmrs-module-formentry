package org.openmrs.module.formentry.extension.html;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.web.extension.TableRowExt;
import org.openmrs.util.InsertedOrderComparator;

/**
 * Adds a row to the editForm.form screen showing the last time the xsn was modified
 */
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
			// if we're not the basic form
			if (formId != "1") {
				FormEntryXsn xsn = ((FormEntryService)Context.getService(FormEntryService.class)).getFormEntryXsn(Integer.valueOf(formId));
				
				if (xsn == null)
					map.put("formentry.xsn.lastModified", " (No XSN) ");
				else {
					StringBuilder output = new StringBuilder();
					output.append(xsn.getCreator().getPersonName());
					output.append(" - ");
					
					Date date = xsn.getDateCreated();
					DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Context.getLocale());
					output.append(dateFormat.format(date));
					
					map.put("formentry.xsn.lastModified", output.toString());
				}
					
			}
		}
		catch (Exception e) {
			log.warn("Unable to get xsn last modified date for form: " + formId, e);
			// pass
		}
		
		return map;
	}
	
}
