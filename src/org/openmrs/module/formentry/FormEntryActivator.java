package org.openmrs.module.formentry;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.formentry.migration.MigrateFormEntryQueueThread;
import org.openmrs.module.formentry.migration.MigrateFormEntryXsnsThread;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Thread holding the migration logic for queues to the filesystem
	 */
	private static MigrateFormEntryQueueThread migrateQueueThread = null;
	
	/**
	 * Thread holding the migration logic for the xsn to the database
	 */
	private static MigrateFormEntryXsnsThread migrateXsnThread = null;
	
	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	@SuppressWarnings("deprecation")
    public void startup() {
		log.info("Starting the Form Entry module");
		
		Properties p = Context.getRuntimeProperties();
		String val;
		
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
		
		
		AdministrationService as = Context.getAdministrationService();
		
		List<String> errorMessages = new Vector<String>();
		
		// set up property requirements
		String gp = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_INFOPATH_OUTPUT_DIR, ""); 
		if ("".equals(gp)) 
			errorMessages.add("_Global_ property '" + FormEntryConstants.FORMENTRY_GP_INFOPATH_OUTPUT_DIR + "' must be defined. (NOT a _runtime_ property)");
		
		gp = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL, ""); 
		if ("".equals(gp))
			errorMessages.add("_Global_ property '" +FormEntryConstants.FORMENTRY_GP_SERVER_URL + "' must be defined. (NOT a _runtime_ property)");
		
		gp = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_INFOPATH_ARCHIVE_DIR, ""); 
		String gp2 = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_ARCHIVE_DATE_FORMAT, "");
		if (!"".equals(gp) && "".equals(gp2))
			errorMessages.add("Global property '" + FormEntryConstants.FORMENTRY_GP_ARCHIVE_DATE_FORMAT + "' must be defined if '" + FormEntryConstants.FORMENTRY_GP_INFOPATH_ARCHIVE_DIR + "' is defined.");
		
		if (errorMessages.size() > 0) {
			throw new ModuleException(OpenmrsUtil.join(errorMessages, " \n"));
		}
		
		if (!Context.isAuthenticated()) {
			try {
				String username = Context.getRuntimeProperties().getProperty(FormEntryConstants.STARTUP_USERNAME, "");
				String password = Context.getRuntimeProperties().getProperty(FormEntryConstants.STARTUP_PASSWORD, "");
				
				Context.authenticate(username, password);
			}
			catch (ContextAuthenticationException e) {
				throw new ModuleException("This module version requires an authenticated user in order to start.  Either start the module manually here or define the " + FormEntryConstants.STARTUP_USERNAME + " and " + FormEntryConstants.STARTUP_PASSWORD + " runtime properties");
			}
		}
		
		// Spawn a thread to do the queue/archive migration from db to filesystem
		// A thread is needed instead of a direct call because the formentry 
		// service isn't set up until after the webmoduleutil.startup is complete
		migrateQueueThread = new MigrateFormEntryQueueThread(Context.getUserContext());
		migrateQueueThread.setName("Migrate Form Entry Queue Thread");
		migrateQueueThread.start();
		
		// Spawn a thread to do the xsn migration from filesystem to db
		// A thread is needed instead of a direct call because the formentry 
		// service isn't set up until after the webmoduleutil.startup is complete
		migrateXsnThread = new MigrateFormEntryXsnsThread(Context.getUserContext());
		migrateXsnThread.setName("Migrate Form Entry XSNs Thread");
		migrateXsnThread.start();
		
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	@SuppressWarnings("deprecation")
    public void shutdown() {
		log.info("Shutting down the Form Entry module");
		
		// stop the migration threads if they're running
		if (migrateQueueThread != null && migrateQueueThread.isAlive()) {
			migrateQueueThread.setActive(false);
		}
		
		if (migrateXsnThread != null && migrateXsnThread.isAlive()) {
			migrateXsnThread.setActive(false);
		}
		
	}
	
}
