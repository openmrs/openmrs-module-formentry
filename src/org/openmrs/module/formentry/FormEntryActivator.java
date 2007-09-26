package org.openmrs.module.formentry;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.formentry.migration.MigrateFormEntryXsnsThread;

/**
 * This activator class is called whenever the formentry module is started.
 * The class checks for property deprecation, loads in runtime properties,
 * and starts up the xsn migration if it hasn't happened already
 */
public class FormEntryActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());
	
	private String[] deprecatedRuntimeProperties = {"formentry.starter_xsn_folder_path", 
			"formentry.infopath.publish_url",
			"formentry.infopath.publish_path",
			"formentry.infopath.submit_url",
			"formentry.infopath.initial_url" };
	
	@SuppressWarnings("deprecation")
    private String[] deprecatedGlobalProperties = {FormEntryConstants.FORMENTRY_GP_INFOPATH_OUTPUT_DIR,
			FormEntryConstants.FORMENTRY_GP_INFOPATH_ARCHIVE_DIR,
			FormEntryConstants.FORMENTRY_GP_ARCHIVE_DATE_FORMAT };

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
    public void startup() {
		log.info("Starting the Form Entry module");
		
		Properties runtimeProperties = Context.getRuntimeProperties();
		
		// loop over and log these runtime properties as deprecated 
		for (String s : deprecatedRuntimeProperties) {
			if (runtimeProperties.getProperty(s, null) != null)
				log.warn("Deprecated runtime property: " + s + ".  This property is no longer read in at runtime and can be deleted.");
		}
		
		// log warnings for the user for global property deprecation
		for (String s : deprecatedGlobalProperties) {
			log.warn("Deprecated global property: " + s + ".  This property is no longer used by the formentry and can be deleted.");
		}
		
		// save makecab and lcab locations to the constants
		FormEntryConstants.FORMENTRY_CABEXTRACT_LOCATION = runtimeProperties.getProperty(FormEntryConstants.FORMENTRY_RP_CABEXTRACT_LOCATION);
		FormEntryConstants.FORMENTRY_LCAB_LOCATION = runtimeProperties.getProperty(FormEntryConstants.FORMENTRY_RP_LCAB_LOCATION);
		
		// set up property requirements
		//List<String> errorMessages = new Vector<String>();
		//if (errorMessages.size() > 0)
		//	throw new ModuleException(OpenmrsUtil.join(errorMessages, " \n"));
		
		
		// migrate the xsns
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
