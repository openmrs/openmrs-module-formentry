package org.openmrs.module.formEntry;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;

public class FormEntryActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.info("Starting the Form Entry module");
		
		Properties p = Context.getRuntimeProperties();
		String val;
		/*
		
		// Override the FormEntry constants if specified by the user
		
		// These runtime property settings can be removed once
		//  everyone has migrated away and are only using global properties
		
		val = p.getProperty("formentry.infopath.server_url", null);
		if (val != null) {
			FormEntryConstants.FORMENTRY_INFOPATH_SERVER_URL = val;
			log.warn("Deprecated runtime property: formentry.infopath.server_url. Set value in global_property table in database now.");
		}

		val = p.getProperty("formentry.infopath.taskpane_caption", null);
		if (val != null) {
			FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_CAPTION = val;
			log.warn("Deprecated runtime property: formentry.infopath.taskpane_caption.  Set value in global_property table in database now.");
		}
		
		val = p.getProperty("formentry.infopath.output_dir", null);
		if (val != null) {
			FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR = val;
			log.warn("Deprecated runtime property: formentry.infopath.output_dir.  Set value in global_property table in database now.");
		}
		
		val = p.getProperty("formentry.infopath.archive_dir", null);
		if (val != null) {
			FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DIR = val;
			log.warn("Deprecated runtime property: formentry.infopath.archive_dir.  Set value in global_property table in database now.");
		}

		val = p.getProperty("formEntry.infopath_archive_date_format", null);
		if (val != null) {
			FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DATE_FORMAT = val;
			log.warn("Deprecated runtime property: formEntry.infopath_archive_date_format.  Set value in global_property table in database now.");
		}
		*/
		
		String[] deprecated = {"formentry.starter_xsn_folder_path", 
			"formentry.infopath.publish_url",
			"formentry.infopath.publish_path",
			"formentry.infopath.submit_url",
			"formentry.infopath.initial_url" };
		
		for (String s : deprecated) {
			val = p.getProperty(s, null);
			if (val != null)
				log.warn("Deprecated runtime property: " + s + ".  This property is no longer read in at runtime and can be deleted.");
		}
		
		/*
		// TODO should be changed to text defaults and constants should be removed
		props.put("formEntry.infopath_output_dir", FormEntryConstants.FORMENTRY_INFOPATH_OUTPUT_DIR);
		props.put("formEntry.infopath_server_url", FormEntryConstants.FORMENTRY_INFOPATH_SERVER_URL);
		props.put("formEntry.infopath_taskpane_caption", FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_CAPTION);
		props.put("formEntry.infopath_archive_date_format", FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DATE_FORMAT);
		props.put("formEntry.infopath_archive_dir", FormEntryConstants.FORMENTRY_INFOPATH_ARCHIVE_DIR);
		*/
		
		
		AdministrationService as = Context.getAdministrationService();
		
		// set up property requirements
		String gp = as.getGlobalProperty("formEntry.infopath_output_dir", ""); 
		if ("".equals(gp))
			throw new ModuleException("Global property 'formEntry.infopath_output_dir' must be defined");
		
		gp = as.getGlobalProperty("formEntry.infopath_server_url", ""); 
		if ("".equals(gp))
			throw new ModuleException("Global property 'formEntry.infopath_server_url' must be defined");
		
		gp = as.getGlobalProperty("formEntry.infopath_archive_dir", ""); 
		String gp2 = as.getGlobalProperty("formEntry.infopath_archive_date_format", "");
		if (!"".equals(gp) && "".equals(gp2))
			throw new ModuleException("Global property 'formEntry.infopath_archive_date_format' must be defined if 'formEntry.infopath_archive_dir' is defined");
		
		
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down the Form Entry module");
	}
	
}
