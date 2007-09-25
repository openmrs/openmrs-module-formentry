package org.openmrs.module.formentry;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.formentry.migration.MigrateFormEntryXsnsThread;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	@SuppressWarnings("deprecation")
    public void startup() {
		log.info("Starting the Form Entry module");
		
		Properties p = Context.getRuntimeProperties();
		String val;
		
		String[] deprecatedRuntimeProperties = {"formentry.starter_xsn_folder_path", 
												"formentry.infopath.publish_url",
												"formentry.infopath.publish_path",
												"formentry.infopath.submit_url",
												"formentry.infopath.initial_url" };
		
		for (String s : deprecatedRuntimeProperties) {
			val = p.getProperty(s, null);
			if (val != null)
				log.warn("Deprecated runtime property: " + s + ".  This property is no longer read in at runtime and can be deleted.");
		}
		
		String[] deprecatedGlobalProperties = {FormEntryConstants.FORMENTRY_GP_INFOPATH_OUTPUT_DIR,
												FormEntryConstants.FORMENTRY_GP_INFOPATH_ARCHIVE_DIR,
												FormEntryConstants.FORMENTRY_GP_ARCHIVE_DATE_FORMAT };
		
		//AdministrationService as = Context.getAdministrationService();
		
		// warn the user and mark them as deprecated in the db
		for (String s : deprecatedGlobalProperties) {
			log.warn("Deprecated global property: " + s + ".  This property is no longer used by the formentry and can be deleted.");
			//as.setGlobalProperty(s, value, "DEPRECATED - Can be removed from global properties.");
		}
		
		List<String> errorMessages = new Vector<String>();
		
		// set up property requirements
		if (errorMessages.size() > 0)
			throw new ModuleException(OpenmrsUtil.join(errorMessages, " \n"));
		
		if (MigrateFormEntryXsnsThread.isActive() == false) {
			// Spawn a thread to do the xsn migration from filesystem to db
			// A thread is needed instead of a direct call because the formentry 
			// service isn't set up until after the webmoduleutil.startup is complete
			MigrateFormEntryXsnsThread migrateXsnThread = new MigrateFormEntryXsnsThread(Context.getUserContext(), null);
			migrateXsnThread.setName("Migrate Form Entry XSNs Thread");
			migrateXsnThread.start();
		}
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	@SuppressWarnings("deprecation")
    public void shutdown() {
		log.info("Shutting down the Form Entry module");
		
		// stop the migration xsn thread if its running
		MigrateFormEntryXsnsThread.setActive(false);
		
	}
	
}
