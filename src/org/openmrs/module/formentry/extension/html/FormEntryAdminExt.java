package org.openmrs.module.formentry.extension.html;

import java.util.Map;
import java.util.TreeMap;

import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.web.extension.AdministrationSectionExt;
import org.openmrs.util.InsertedOrderComparator;

/**
 * Admin section links for the form entry module on the administration page
 */
public class FormEntryAdminExt extends AdministrationSectionExt {
	
	private static boolean isQueueMigrated = false;
	
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
		map.put("module/formentry/setupRelationshipSchemas.form", "formentry.relationships.manage");
		map.put("module/formentry/manageXsnArchives.htm", "formentry.xsnarchives.manage");
		
		if (isQueueMigrated == false) {
			FormEntryService fs = (FormEntryService)Context.getService(FormEntryService.class);
			if (fs.migrateFormEntryArchiveNeeded())
				map.put("module/formentry/migrateFormEntryArchive.form", "formentry.queueArchiveMigration.migrate");
			else
				isQueueMigrated = true;
		}
		
		return map;
	}
	
}
